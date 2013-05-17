package com.juke.sql.formater.sqlite;

/*******************************************************************************
 * Row Data POJO class.
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class RowData {

	private String rowDataStirngValue;
	private boolean changed;
	private SQLiteColumn column;

	public SQLiteColumn getColumn() {
		return column;
	}

	public void setColumn(SQLiteColumn column) {
		this.column = column;
	}

	public RowData(String data, boolean isChanged) {
		rowDataStirngValue = data;
		changed = isChanged;
	}

	public String getRowDataStirngValue() {
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
}