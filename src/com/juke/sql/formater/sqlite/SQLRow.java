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