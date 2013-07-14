package com.juke.sql.formater.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.juke.sql.formater.SQLFormater;
import com.juke.sql.util.Utils;
import com.juke.sql.writer.WriteListner;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SQLiteFormater implements SQLFormater {
    private static final String SQL_SEPARATOR = ";";

    private static final String SQL_COLUMN_SEPARATOR = ", ";

    private static final String SELECT_ALL = "SELECT * FROM %s";
    private WriteListner writeListner;

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

    private void addInsertStatement(String sqlQuery) {
        if (writeListner != null) {
            writeListner.onWrite(sqlQuery, WriteListner.INSERT);
        }
    }

    private void addDropTableStatement(String sqlQuery) {
        if (writeListner != null) {
            writeListner.onWrite(sqlQuery, WriteListner.DROP);
        }
    }

    private void addCreateTableStatement(String sqlQuery) {
        if (writeListner != null) {
            writeListner.onWrite(sqlQuery, WriteListner.OTHER);
        }
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
            Utils.close(resultSet, statement);
        }
    }

    // private boolean isTableStructureEquals(Connection newDatabase,
    // Connection oldDatabase, String tableName) {
    // List<SQLiteColumn> oldColumList = getColumnList(oldDatabase, tableName);
    // List<SQLiteColumn> newColumList = getColumnList(oldDatabase, tableName);
    // Collections.sort(oldColumList);
    // Collections.sort(newColumList);
    // return oldColumList.equals(newColumList);
    // }

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

    @Override
    public List<String> getTableList(Connection connection) {
        String sql = "                                          "
                + "SELECT name                                  "
                + "FROM sqlite_master                           "
                + "WHERE type = 'table'                         ";
        Statement statement = null;
        ResultSet resultSet = null;
        List<String> tableList = new ArrayList<String>();
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                tableList.add(resultSet.getString(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            Utils.close(resultSet, statement);
        }
        return tableList;
    }

    @Override
    public String createDropTableSQLQery(String tableName) {
        addDropTableStatement(String.format(
                SQLFormater.DROP_TABLE_SQL_TEMPLATE, tableName));
        return String.format(SQLFormater.DROP_TABLE_SQL_TEMPLATE, tableName);
    }

    @Override
    public void registreWriter(WriteListner listner) {
        writeListner = listner;
    }

    public void generateTableDiff(Connection etalonConnection,
            Connection revisedConnection, String tableName) throws SQLException {
        List<SQLiteColumn> columnList = getColumnList(etalonConnection,
                tableName);
        Statement etalonStatement = etalonConnection.createStatement();
        Statement revisedStatement = revisedConnection.createStatement();

        SQLTable etlonDiffTable = getTableDiff(tableName, columnList,
                etalonStatement, revisedStatement);

        SQLTable revisedDiffTable = getTableDiff(tableName, columnList,
                revisedStatement, etalonStatement);

        createTableQueryDiff(etlonDiffTable, revisedDiffTable);
    }

    private void createTableQueryDiff(SQLTable etlonDiffTable,
            SQLTable revisedDiffTable) {
        String joinedColumns = joinColumns(etlonDiffTable.getColumnList());
        // ------------------------ INSERT DIFF-----------------------------
        if (revisedDiffTable.getRowList() != null) {
            for (SQLRow row : revisedDiffTable.getRowList()) {
                if (row.isRowDeleted()) {
                    String dataSQL = "";
                    for (RowData data : row.getDataList()) {
                        dataSQL += data.getRowDataStirngValue() + ",";
                    }
                    dataSQL = dataSQL.substring(0, dataSQL.length() - 1);
                    addInsertStatement(String.format(SQLFormater.INSERT_QUERY,
                            etlonDiffTable.getTableName(), joinedColumns,
                            dataSQL));
                } else {

                    System.out.println("Num:" + row.getRowNum());

                    for (RowData data : row.getDataList()) {
                        if (data.isChanged()) {

                            System.out
                                    .print(data.getColumn().getColumnName()
                                            + "="
                                            + data.getRowDataStirngValue()
                                            + ", ");
                        }
                    }
                    System.out.println();
                    SQLRow etlonRow = etlonDiffTable.getChangedRowByNum(row
                            .getRowNum());
                    System.out.println("Num:" + etlonRow.getRowNum());

                    for (RowData data : etlonRow.getDataList()) {
                        if (data.isChanged()) {

                            System.out
                                    .print(data.getColumn().getColumnName()
                                            + "="
                                            + data.getRowDataStirngValue()
                                            + ", ");
                        }
                    }
                    System.out.println();
                }
            }
        }
        // ------------------------ Delete DIFF-----------------------------

        List<SQLiteColumn> keyColumn = getKeyColumns(etlonDiffTable
                .getColumnList());

        if (keyColumn.isEmpty()) {
            keyColumn = etlonDiffTable.getColumnList();
        }

        if (etlonDiffTable.getRowList() != null) {
            for (SQLRow row : etlonDiffTable.getRowList()) {
                if (row.isRowDeleted()) {
                    addInsertStatement(String.format(SQLFormater.DELETE_QUERY,
                            etlonDiffTable.getTableName(),
                            formateDataByColum(keyColumn, row.getDataList())));
                } else {
                    // System.out.println("Num:" + row.getRowNum());
                    //
                    // for (RowData data : row.getDataList()) {
                    // if (data.isChanged()) {
                    // System.out
                    // .print(data.getColumn().getColumnName()
                    // + "="
                    // + data.getRowDataStirngValue()
                    // + ", ");
                    // }
                    // }
                    // System.out.println();
                }
            }

        }
        // ------------------------ Delete DIFF-----------------------------
    }

    private String formateDataByColum(List<SQLiteColumn> columnList,
            List<RowData> dataList) {
        StringBuilder stringBuilder = new StringBuilder();
        for (RowData data : dataList) {
            for (SQLiteColumn column : columnList) {
                if (column.getColumnName().equalsIgnoreCase(
                        data.getColumn().getColumnName())) {
                    stringBuilder.append(column.getColumnName()).append("=")
                            .append(data.getRowDataStirngValue());
                    break;
                }
            }
        }
        return stringBuilder.toString();
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

    private SQLTable getTableDiff(String tableName,
            List<SQLiteColumn> columnList, Statement etalonStatement,
            Statement revisedStatement) throws SQLException {
        String sql = "SELECT * FROM \"" + tableName + "\"";
        ResultSet etalonResultSet = etalonStatement.executeQuery(sql);
        ResultSet revisedResultSet = revisedStatement.executeQuery(sql);
        SQLTable table = new SQLTable();
        table.setColumnList(columnList);
        table.setTableName(tableName);
        boolean isEOF = false;
        int rowNum = 0;
        if (revisedResultSet.next()) {
            while (etalonResultSet.next()) {
                SQLRow row = new SQLRow();
                boolean isChanged = false;
                int correctDataCount = 0;
                for (SQLiteColumn column : columnList) {
                    String etalonValue = column
                            .getObjectStringValue(etalonResultSet);
                    String revisedValue = null;
                    column.getObjectStringValue(revisedResultSet);
                    if (!isEOF) {
                        revisedValue = column
                                .getObjectStringValue(revisedResultSet);
                    } else {
                        isChanged = false;
                        row.setRowDeleted(true);
                    }
                    RowData data = new RowData(etalonValue);
                    if (!etalonValue.equalsIgnoreCase(revisedValue)) {
                        isChanged = true;
                        rowNum++;
                    } else {
                        correctDataCount++;
                    }
                    data.setChanged(isChanged);
                    data.setColumn(column);
                    row.setData(data);
                    row.setRowNum(rowNum);
                    if (correctDataCount == 0) {
                        row.setRowDeleted(true);
                    }
                }
                if (!isChanged) {
                    if (!revisedResultSet.next()) {
                        isEOF = true;
                    }
                } else if (correctDataCount > 0) {
                    table.addRow(row);
                    if (!revisedResultSet.next()) {
                        isEOF = true;
                    }
                } else {
                    table.addRow(row);
                }
            }
        }
        return table;
    }

    // private void printSQL(SQLTable table) {
    // table.getRowList();
    // printlnColumnList(table.getColumnList());
    // for (SQLRow row : table.getRowList()) {
    // List<RowData> dataList = row.getDataList();
    // for (RowData data : dataList) {
    // System.out.print(data.getRowDataStirngValue() + " - "
    // + (data.isChanged()) + "\t");
    // }
    // System.out.println();
    // }
    //
    // }

    // private void printlnColumnList(List<SQLiteColumn> list) {
    // for (SQLiteColumn column : list) {
    // System.out.print(column.getColumnName() + "\t");
    // }
    // System.out.println("========================================");
    // }
}