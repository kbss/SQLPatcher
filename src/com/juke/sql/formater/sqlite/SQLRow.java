package com.juke.sql.formater.sqlite;

import java.util.ArrayList;
import java.util.List;
/*******************************************************************************
 * SQL Row POJO class.
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SQLRow {

	private List<SQLiteColumn> columnList;

	private List<RowData> dataList;

	private boolean isRowDeleted;

	public boolean isRowDeleted() {
		return isRowDeleted;
	}

	public void setRowDeleted(boolean isRowDeleted) {
		this.isRowDeleted = isRowDeleted;
	}

	public List<SQLiteColumn> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<SQLiteColumn> columnList) {
		this.columnList = columnList;
	}

	public List<RowData> getDataList() {
		return dataList;
	}

	public void setDataList(List<RowData> dataList) {
		this.dataList = dataList;
	}

	public void setData(RowData data) {
		if (dataList == null) {
			dataList = new ArrayList<RowData>();
		}
		dataList.add(data);
	}
}