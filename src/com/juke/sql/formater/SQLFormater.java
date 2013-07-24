package com.juke.sql.formater;

import java.sql.Connection;
import java.util.List;

import com.juke.sql.writer.SqlWriter;

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
public interface SQLFormater {
    public String DROP_TABLE_SQL_TEMPLATE = "DROP TABLE IF EXISTS %s";
    public String INSERT_QUERY = "INSERT INTO %s (%s) VALUES (%s)";
    public String DELETE_QUERY = "DELETE FROM %s WHERE %s";
    public String UPDATE_QUERY = "UPDATE %s SET %s WHERE %s";

    public void createFullSQLTableDump(Connection connection, String tableName);

    public List<String> getTableList(Connection connection);

    public String createDropTableSQLQery(String tableName);

    public void registreWriter(SqlWriter listner);

    public void close();
}