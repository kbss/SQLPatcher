package com.juke.sql.formater.sqlite;

import java.util.ArrayList;
import java.util.List;

/*******************************************************************************
 * SQL Table POJO class.
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