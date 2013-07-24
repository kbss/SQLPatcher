package com.juke.sql.formater.sqlite;

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
public class RowData {

    private String rowDataStirngValue;
    private boolean changed;
    private SQLiteColumn column;
    private int rowNum;

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public SQLiteColumn getColumn() {
        return column;
    }

    public void setColumn(SQLiteColumn column) {
        this.column = column;
    }

    public RowData(String data) {
        rowDataStirngValue = data;
    }

    public String getRowDataStringValue() {
        return rowDataStirngValue;
    }

    public void setRowDataStirngValue(String rowDataStirngValue) {
        this.rowDataStirngValue = rowDataStirngValue;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public String toString() {
        return rowDataStirngValue;
    }
}