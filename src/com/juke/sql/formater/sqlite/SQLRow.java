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
public class SQLRow {

    private List<SQLiteColumn> columnList;

    private List<RowData> dataList;

    private boolean isRowDeleted;

    private boolean isRowAdded;

    int rowNum = -1;

    public List<SQLiteColumn> getColumnList() {
        return columnList;
    }

    public List<RowData> getDataList() {
        return dataList;
    }

    public int getRowNum() {
        return rowNum;
    }

    public boolean isRowAdded() {
        return isRowAdded;
    }

    public boolean isRowDeleted() {
        return isRowDeleted;
    }

    public void setColumnList(List<SQLiteColumn> columnList) {
        this.columnList = columnList;
    }

    public void setData(RowData data) {
        if (dataList == null) {
            dataList = new ArrayList<RowData>();
        }
        dataList.add(data);
    }

    public void setDataList(List<RowData> dataList) {
        this.dataList = dataList;
    }

    public void setRowAdded(boolean isRowAdded) {
        this.isRowAdded = isRowAdded;
    }

    public void setRowDeleted(boolean isRowDeleted) {
        this.isRowDeleted = isRowDeleted;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public String getValueByColumn(SQLiteColumn column) {
        for (RowData data : dataList) {
            if (data.getColumn().getColumnName()
                    .equalsIgnoreCase(column.getColumnName())) {
                return data.getRowDataStringValue();
            }
        }

        throw new IllegalArgumentException("No such column '"
                + column.getColumnName() + "'");
    }
}