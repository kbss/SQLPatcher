package com.juke.sql.formater.sqlite;

import java.util.ArrayList;
import java.util.List;

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
public class SQLTable {

    private List<SQLiteColumn> columnList;
    private List<SQLRow> rowList;
    private String tableName;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<SQLRow> getRowList() {
        return rowList;
    }

    public void setRowList(List<SQLRow> rowList) {
        this.rowList = rowList;
    }

    public void addRow(SQLRow row) {
        if (rowList == null) {
            rowList = new ArrayList<SQLRow>();
        }
        rowList.add(row);
    }

    public List<SQLiteColumn> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<SQLiteColumn> columnList) {
        this.columnList = columnList;
    }

    public SQLRow getChangedRowByNum(int rowNum) {
        if (rowNum == -1) {
            return null;
        }
        for (SQLRow row : rowList) {
            if (row.getRowNum() == rowNum) {
                return row;
            }
        }
        return null;
    }
}