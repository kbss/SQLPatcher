package com.juke.sql.formater.sqlite;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.juke.sql.formater.SQLFormater;
import com.juke.sql.util.Utils;
import com.juke.sql.writer.SqlWriter;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SQLiteFormater implements SQLFormater {
	private static final String AND_CLAUSE = " AND ";

	private static final String SELECT_ALL = "SELECT * FROM %s";

	private static final String SQL_COLUMN_SEPARATOR = ",";
	private static final String SQL_SEPARATOR = ";";
	private SqlWriter writeListner;
	private QuietCloser closer;

	public SQLiteFormater() {
		closer = new QuietCloser();
	}

	private void addCreateTableStatement(String sqlQuery) {
		writeSQLQuery(sqlQuery, SqlWriter.OTHER);
	}

	private void addDropTableStatement(String sqlQuery) {
		writeSQLQuery(sqlQuery, SqlWriter.DROP);
	}

	private void addInsertStatement(String sqlQuery) {
		writeSQLQuery(sqlQuery, SqlWriter.INSERT);
	}

	@Override
	public void close() {
		if (writeListner != null) {
			writeListner.close();
		}
		closer.close();
	}

	@Override
	public String createDropTableSQLQery(String tableName) {
		addDropTableStatement(String.format(
				SQLFormater.DROP_TABLE_SQL_TEMPLATE, tableName));
		return String.format(SQLFormater.DROP_TABLE_SQL_TEMPLATE, tableName);
	}

	@Override
	public void createFullSQLTableDump(Connection connection, String tableName) {
		getNewTableSQLQuery(connection, tableName);
		List<SQLiteColumn> columnList = getColumnList(connection, tableName);
		ResultSet resultSet = null;
		Statement statement = null;
		String joinedColumns = joinColumns(columnList);

		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(String.format(SELECT_ALL,
					tableName));
			while (resultSet.next()) {
				String result = "";
				for (SQLiteColumn column : columnList) {
					result += column.getObjectStringValue(resultSet)
							+ SQL_COLUMN_SEPARATOR;
				}
				result = result.substring(0, result.length()
						- SQL_COLUMN_SEPARATOR.length());
				addInsertStatement(String.format(SQLFormater.INSERT_QUERY,
						tableName, joinedColumns, result));
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			closeQuietly(resultSet, statement);
		}
	}

	private void createSQLDiff(SQLRow control, SQLRow revised,
			List<SQLiteColumn> columnList, List<SQLiteColumn> keyColumn,
			String tableName, boolean revisedCheck) {

		if (revisedCheck) {
			if (revised.getDataList() == null
					|| revised.getDataList().isEmpty()) {
				String dataSQL = joinStrings(control.getDataList(),
						SQL_COLUMN_SEPARATOR);
				String joinedColumns = joinColumns(columnList);
				addInsertStatement(String.format(SQLFormater.INSERT_QUERY,
						tableName, joinedColumns, dataSQL));
			} else {
				String updateQuery = getUpdateQuery(tableName, control,
						revised, keyColumn);
				if (!updateQuery.isEmpty()) {
					writeSQLQuery(updateQuery);
				}
			}
		} else {
			if (revised.getDataList() == null
					|| revised.getDataList().isEmpty()) {
				addInsertStatement(String.format(SQLFormater.DELETE_QUERY,
						tableName,
						formateDataByColum(keyColumn, control.getDataList())));
			}
		}

	}

	private void createSQlPatch(String tableName,
			List<SQLiteColumn> columnList, Statement etalonStatement,
			Statement revisedStatement, boolean isValidate,
			Connection revisedConnection) throws SQLException {

		String sqlPart = "SELECT * FROM \"" + tableName + "\" ";
		String sql = sqlPart + getOrderByQuery(columnList);
		ResultSet etalonResultSet = etalonStatement.executeQuery(sql);
		List<SQLiteColumn> keyColumns = getKeyColumns(columnList);
		if (keyColumns.isEmpty()) {
			keyColumns = columnList;
		}

		String andClause = SQLiteFormater.AND_CLAUSE;

		StringBuilder sbp = new StringBuilder(sqlPart);
		sbp.append("WHERE ");

		for (SQLiteColumn column : keyColumns) {
			sbp.append(column.getColumnName()).append("=").append("?")
					.append(andClause);
		}
		sbp.delete(sbp.length() - andClause.length(), sbp.length());

		PreparedStatement pstm = revisedConnection.prepareStatement(sbp
				.toString());
		while (etalonResultSet.next()) {
			ResultSet rs = null;
			try {
				int i = 1;
				for (SQLiteColumn column : keyColumns) {
					pstm.setObject(i, column.getObjectValue(etalonResultSet));
					i++;
				}
				SQLRow controlSqlRow = new SQLRow();
				for (SQLiteColumn column : columnList) {
					RowData data = new RowData(
							column.getObjectStringValue(etalonResultSet));
					data.setColumn(column);
					controlSqlRow.setData(data);

				}
				controlSqlRow.setColumnList(columnList);
				rs = pstm.executeQuery();
				SQLRow revisedSqlRow = new SQLRow();
				if (rs.next()) {
					for (SQLiteColumn column : columnList) {
						RowData data = new RowData(
								column.getObjectStringValue(rs));
						revisedSqlRow.setData(data);
					}
				}
				createSQLDiff(controlSqlRow, revisedSqlRow, columnList,
						keyColumns, tableName, isValidate);
			} finally {
				closeQuietly(rs);
			}
		}
		closeQuietly(etalonResultSet, pstm);
	}

	// TODO Try to find SQL query for selecting unique columns
	private List<SQLiteColumn> findAndUpdateUniqueColumns(
			Connection connection, String tableName,
			List<SQLiteColumn> columnList) {
		ResultSet resultSet = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			try {
				resultSet = statement.executeQuery("PRAGMA INDEX_LIST('"
						+ tableName + "')");
			} catch (SQLException e) {
				return columnList;
			}
			while (resultSet.next()) {
				String indexColumnId = getIndexColumnName(connection,
						resultSet.getString("name"));
				if (indexColumnId != null && !indexColumnId.isEmpty()) {
					for (SQLiteColumn colum : columnList) {
						if (colum.getColumnName().equalsIgnoreCase(
								indexColumnId)) {
							colum.setUnique(true);
							break;
						}
					}
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			Utils.close(resultSet, statement);
		}
		return columnList;
	}

	private String formateDataByColum(List<SQLiteColumn> columnList,
			List<RowData> dataList) {
		StringBuilder stringBuilder = new StringBuilder();
		String andClause = SQLiteFormater.AND_CLAUSE;
		for (RowData data : dataList) {
			for (SQLiteColumn column : columnList) {
				if (column.getColumnName().equalsIgnoreCase(
						data.getColumn().getColumnName())) {
					stringBuilder.append(column.getColumnName()).append("=")
							.append(data.getRowDataStringValue())
							.append(andClause);
					break;
				}
			}
		}
		String result = stringBuilder.toString();
		result = result.substring(0, result.length() - andClause.length());
		return result;
	}

	public void generateTableDiff(Connection etalonConnection,
			Connection revisedConnection, String tableName) throws SQLException {
		List<SQLiteColumn> columnList = getColumnList(etalonConnection,
				tableName);
		Statement etalonStatement = etalonConnection.createStatement();
		Statement revisedStatement = revisedConnection.createStatement();

		createSQlPatch(tableName, columnList, etalonStatement,
				revisedStatement, true, revisedConnection);

		createSQlPatch(tableName, columnList, revisedStatement,
				etalonStatement, false, etalonConnection);
		closeQuietly(etalonStatement, revisedStatement);
	}

	private List<SQLiteColumn> getColumnList(Connection connection,
			String tableName) {
		List<SQLiteColumn> columnList = new ArrayList<SQLiteColumn>();
		ResultSet resultSet = null;
		Statement statement = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery("pragma table_info('"
					+ tableName + "')");

			while (resultSet.next()) {
				SQLiteColumn column = new SQLiteColumn(
						resultSet.getString("name"),
						resultSet.getString("type"));
				if (resultSet.getInt("pk") == 1) {
					column.setPrimaryKey(true);
				}
				if (resultSet.getInt("notnull") == 1) {
					column.setNotNull(true);
				}
				columnList.add(column);
			}
			findAndUpdateUniqueColumns(connection, tableName, columnList);
		} catch (SQLException e) {
			throw new RuntimeException(e);

		} finally {
			Utils.close(resultSet, statement);
		}
		return columnList;
	}

	private String getIndexColumnName(Connection connection, String indexName) {
		ResultSet resultSet = null;
		Statement statement = null;
		String columnName = null;
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery("pragma index_info('"
					+ indexName + "')");

			while (resultSet.next()) {
				columnName = resultSet.getString("name");
				break;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);

		} finally {
			Utils.close(resultSet, statement);
		}
		return columnName;
	}

	private List<SQLiteColumn> getKeyColumns(List<SQLiteColumn> columnList) {
		List<SQLiteColumn> keyColumnList = new ArrayList<SQLiteColumn>();
		for (SQLiteColumn column : columnList) {
			if (column.isPrimaryKey() || column.isUnique()) {
				keyColumnList.add(column);
			}
		}
		return keyColumnList;
	}

	public String getNewTableSQLQuery(Connection connection, String tableName) {
		String sql = "                                                  "
				+ "SELECT sql                                           "
				+ "FROM sqlite_master                                   "
				+ "WHERE type = 'table'  AND name = '" + tableName + "' ";
		Statement statement = null;
		ResultSet resultSet = null;
		String result = "";
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				result = resultSet.getString(1) + SQL_SEPARATOR;
				addCreateTableStatement(result);
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			Utils.close(resultSet, statement);
		}
		return result;
	}

	private String getOrderByQuery(List<SQLiteColumn> list) {
		String result = getOrderByQuery(list, true);
		if (result.isEmpty()) {
			result = getOrderByQuery(list, false);
		}
		return result;
	}

	private String getOrderByQuery(List<SQLiteColumn> list, boolean orderByPk) {
		StringBuilder builder = new StringBuilder();
		String separator = SQL_COLUMN_SEPARATOR;
		for (SQLiteColumn column : list) {
			if (!(orderByPk ^ column.isPrimaryKey())) {
				builder.append(column.getColumnName()).append(separator);
			}
		}
		String result = "";
		if (builder.length() > 1) {
			builder.delete(builder.length() - separator.length(),
					builder.length());
			result = " ORDER BY " + builder.toString();
		}
		return result;
	}

	@Override
	public List<String> getTableList(Connection connection) {
		String sql = "                                          "
				+ "SELECT name                                  "
				+ "FROM sqlite_master                           "
				+ "WHERE type = 'table'                         ";
		Statement statement = null;
		ResultSet resultSet = null;
		List<String> tableList = new ArrayList<String>();
		String ignorePattern = "sqlite_.*";
		try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sql);
			while (resultSet.next()) {
				String tableName = resultSet.getString(1);
				if (!tableName.matches(ignorePattern)) {
					tableList.add(tableName);
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			Utils.close(resultSet, statement);
		}
		return tableList;
	}

	private String getUpdateCondition(SQLRow revisedRow,
			List<SQLiteColumn> columnList) {
		List<SQLiteColumn> keyColumns = getKeyColumns(columnList);
		String andClause = SQLiteFormater.AND_CLAUSE;
		if (keyColumns.isEmpty()) {
			keyColumns = columnList;
		}
		StringBuilder condition = new StringBuilder();
		for (SQLiteColumn column : keyColumns) {
			condition.append(column.getColumnName()).append("=")
					.append(revisedRow.getValueByColumn(column))
					.append(andClause);
		}
		String result = condition.toString();
		result = result.substring(0, result.length() - andClause.length());
		return result;
	}

	private String getUpdateQuery(String tableName, SQLRow revisedRow,
			SQLRow etalonRow, List<SQLiteColumn> columnList) {

		StringBuilder changed = new StringBuilder();
		String separator = SQLiteFormater.SQL_COLUMN_SEPARATOR;
		int i = 0;
		for (RowData data : revisedRow.getDataList()) {
			RowData controlData = etalonRow.getDataList().get(i);
			String revisedValue = data.getRowDataStringValue();

			if (revisedValue != null) {
				if (!data.getRowDataStringValue().equals(
						controlData.getRowDataStringValue())) {
					changed.append(data.getColumn().getColumnName())
							.append("=").append(revisedValue).append(separator);

				}
			}
			i++;
		}
		if (changed.length() == 0) {
			return "";
		}
		changed.delete(changed.length() - separator.length(), changed.length());
		return String.format(SQLiteFormater.UPDATE_QUERY, tableName,
				changed.toString(), getUpdateCondition(revisedRow, columnList));
	}

	private String joinColumns(List<SQLiteColumn> columnList) {

		StringBuilder stringBuilder = new StringBuilder();
		for (SQLiteColumn column : columnList) {
			stringBuilder.append(column.getColumnName()).append(
					SQL_COLUMN_SEPARATOR);
		}
		int length = stringBuilder.length();
		stringBuilder.delete(length - SQL_COLUMN_SEPARATOR.length(), length);
		return stringBuilder.toString();
	}

	private <E> String joinStrings(Collection<E> collection, String separator) {

		StringBuilder stringBuilder = new StringBuilder();
		for (E object : collection) {
			stringBuilder.append(object).append(separator);
		}
		int length = stringBuilder.length();
		stringBuilder.delete(length - separator.length(), length);
		return stringBuilder.toString();
	}

	@Override
	public void registreWriter(SqlWriter listner) {
		writeListner = listner;
	}

	public void writeSQLQuery(String sqlQuery) {
		writeSQLQuery(sqlQuery, SqlWriter.OTHER);
	}

	private void writeSQLQuery(String sqlQuery, int type) {
		if (writeListner != null) {
			writeListner.onWrite(sqlQuery, type);
		}
	}

	private void closeQuietly(Object... dbResources) {
		closer.close(dbResources);
	}
}

/**
 * Separate thread that are uses for DB resource closing.
 * 
 * @author SKrivtsov
 * */
class QuietCloser extends Thread {

	private List<Object> resourcesList;
	private boolean stop = false;

	public QuietCloser() {
		resourcesList = Collections.synchronizedList(new ArrayList<Object>());
		this.start();
	}

	public void close(Object... dbResources) {
		resourcesList.add(dbResources);
	}

	public void close() {
		stop = true;
	}

	@Override
	public void run() {
		while (!stop) {
			if (!resourcesList.isEmpty()) {
				synchronized (resourcesList) {
					for (Iterator<Object> it = resourcesList.iterator(); it
							.hasNext();) {
						Object resources = it.next();
						it.remove();
						closeDbResources(resources);
					}
				}
			}
			try {
				sleep(200);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void closeDbResources(Object... dbResources) {
		for (Object resource : dbResources) {
			if (resource != null) {
				try {
					if (resource instanceof Connection) {
						((Connection) resource).close();
					} else if (resource instanceof Statement) {
						((Statement) resource).close();
					} else if (resource instanceof ResultSet) {
						((ResultSet) resource).close();
					}
				} catch (SQLException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}