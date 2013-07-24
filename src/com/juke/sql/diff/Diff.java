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
 * SQLPatcher - <a
 * href="https://github.com/kbss/SQLPatcher">https://github.com/kbss
 * /SQLPatcher</a><br>
 * 
 * Copyright (C) 2013 Serhii Krivtsov<br>
 * 
 * SQLPatcher is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.<br>
 * <br>
 * SQLPatcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>. <br>
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

    /*******************************************************************************
     * Returns list of tables that was added in revised DB.
     * 
     * @return list of tables
     */
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

    /*******************************************************************************
     * Returns list of tables that no longer exists in revised db
     * 
     * @return list of tables
     */
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

    public List<String> getTableListForCompare() {
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

    /*******************************************************************************
     * Attaches two SQLite connections into new one.
     * 
     * @param main
     *            DB connections with main alias
     * @param second
     *            DB Connections with sec alias
     * @return attached connection.
     */
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

    /*******************************************************************************
     * Returns DB bath from given connections.
     * 
     * @param connection
     *            DB connections
     * @return
     * @throws SQLException
     */
    @SuppressWarnings("resource")
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