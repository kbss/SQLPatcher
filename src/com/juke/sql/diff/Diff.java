package com.juke.sql.diff;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.juke.sql.formater.sqlite.SQLiteFormater;
import com.juke.sql.util.Utils;
import com.juke.sql.writer.SimpleWriteListner;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Diff {
	private Connection actual;

	private Connection expected;

	private SQLiteFormater sqlFormater;

	private List<String> oldTables;

	private List<String> newTables;

	private List<String> missingTables;

	private List<String> dropedTables;

	private List<String> tableListForCompare;

	public Diff(Connection actual, Connection expected) {
		this.actual = actual;
		this.expected = expected;
		sqlFormater = new SQLiteFormater();
		sqlFormater.registreWriter(new SimpleWriteListner());
	}

	private List<String> getNewTables() {
		if (missingTables == null) {
			missingTables = new ArrayList<String>();
			for (String table : getNewTableList()) {
				if (!getOldTableList().contains(table)) {
					missingTables.add(table);
				}
			}
		}
		return missingTables;
	}

	private List<String> getNewTableList() {
		if (newTables == null) {
			newTables = sqlFormater.getTableList(expected);
		}
		return newTables;
	}

	private List<String> getOldTableList() {
		if (oldTables == null) {
			oldTables = sqlFormater.getTableList(actual);
		}
		return oldTables;
	}

	private List<String> getDropedTables() {
		if (dropedTables == null) {
			dropedTables = new ArrayList<String>();
			for (String table : getOldTableList()) {
				if (!getNewTableList().contains(table)) {
					dropedTables.add(table);
				}
			}
		}
		return dropedTables;
	}

	private List<String> getTableListForCompare() {
		if (tableListForCompare == null) {
			tableListForCompare = new ArrayList<String>();
			tableListForCompare.addAll(oldTables);
			tableListForCompare.removeAll(dropedTables);
			tableListForCompare.removeAll(missingTables);
		}
		return tableListForCompare;
	}

	public void compare() throws SQLException {
		System.out.println("Processing...");
		for (String tableName : getNewTables()) {
			sqlFormater.createFullSQLTableDump(expected, tableName);
		}
		for (String tableName : getDropedTables()) {
			sqlFormater.createDropTableSQLQery(tableName);
		}

		List<String> tableList = getOldTableList();
		long start = System.currentTimeMillis();
		Connection controlAttached = attachConnections(actual, expected);

		Connection revisedAttached = attachConnections(expected, actual);

		for (String tableName : tableList) {
			sqlFormater.generateTableDiff(controlAttached, revisedAttached,
					tableName);
		}
		Utils.close(controlAttached, revisedAttached);
		System.out
				.println((System.currentTimeMillis() - start) / 1000f + " s.");
		sqlFormater.writeSQLQuery("COMMIT");
		sqlFormater.close();
	}

	public Connection attachConnections(Connection main, Connection second) {
		Connection connection = null;
		Statement st = null;
		try {
			connection = Utils.getConnection(getDBPath(main));
			st = connection.createStatement();
			st.execute("attach \"" + getDBPath(second) + "\" as sec;");
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		} finally {
			Utils.close(st);
		}
		return connection;
	}

	private String getDBPath(Connection connection) throws SQLException {
		Statement st = null;
		ResultSet rs = null;
		try {
			st = connection.createStatement();
			rs = st.executeQuery("PRAGMA database_list;");
			while (rs.next()) {
				if ("main".equals(rs.getString("name"))) {
					return rs.getString("file");
				}
			}
		} finally {
			Utils.close(rs, st);
		}
		return null;
	}
}
