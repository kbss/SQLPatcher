package com.juke.sql.formater.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.juke.sql.formater.SQLColumn;

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
public class SQLiteColumn implements SQLColumn, Comparable<SQLiteColumn> {
    private static List<String> binaryTypes = new ArrayList<String>();

    private static List<String> quotedTypes = new ArrayList<String>();

    private static final String SLQ_QUOTE = "\"";

    private static final String TYPE_BINARY = "BINARY";

    private static final String TYPE_BLOB = "BLOB";

    private static final String TYPE_CHAR = "CHAR";

    private static final String TYPE_DATE = "DATE";

    private static final String TYPE_DATETEXT = "DATETEXT";

    private static final String TYPE_DATETIME = "DATETIME";

    private static final String TYPE_GUID = "GUID";

    private static final String TYPE_MEMO = "MEMO";

    private static final String TYPE_NCHAR = "NCHAR";

    private static final String TYPE_NTEXT = "NTEXT";

    private static final String TYPE_NVARCHAR = "NVARCHAR";

    private static final String TYPE_NVARCHAR2 = "NVARCHAR2";

    private static final String TYPE_TEXT = "TEXT";

    private static final String TYPE_VARCHAR = "VARCHAR";

    private static final String TYPE_VARCHAR2 = "VARCHAR2";

    private static final String TYPE_WORD = "WORD";

    private static final String TYPE_CLOB = "CLOB";

    private String columnName;

    private boolean isNotNull = false;

    private boolean isPrimaryKey = false;

    private boolean isUnique = false;

    private int objectType = 0;
    
    private String objectTypeName;

    public String getObjectTypeName() {
        return objectTypeName;
    }

    public SQLiteColumn(String columnName, String columnType) {
        this.columnName = columnName;
        if (quotedTypes.contains(columnType.split("\\(")[0])) {
            objectType = OBJECT_TYPE_QOTED;
        } else if (binaryTypes.contains(columnType)) {
            objectType = OBJECT_TYPE_BINARY;
        } else {
            objectType = OBJECT_TYPE_UN_QOTED;
        }

        this.objectTypeName = columnType;
    }

    static {
        quotedTypes.add(TYPE_CHAR);
        quotedTypes.add(TYPE_DATE);
        quotedTypes.add(TYPE_DATETEXT);
        quotedTypes.add(TYPE_DATETIME);
        quotedTypes.add(TYPE_GUID);
        quotedTypes.add(TYPE_MEMO);
        quotedTypes.add(TYPE_NCHAR);
        quotedTypes.add(TYPE_NTEXT);
        quotedTypes.add(TYPE_NVARCHAR);
        quotedTypes.add(TYPE_NVARCHAR2);
        quotedTypes.add(TYPE_TEXT);
        quotedTypes.add(TYPE_VARCHAR);
        quotedTypes.add(TYPE_VARCHAR2);
        quotedTypes.add(TYPE_WORD);
        quotedTypes.add(TYPE_CLOB);

        binaryTypes.add(TYPE_BLOB);
        binaryTypes.add(TYPE_BINARY);
    }

    // TODO find another way to convert into two digit hex
    private String byteToHex(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            String result = Integer.toHexString(b);
            if (result.length() > 3) {
                result = result.substring(result.length() - 2, result.length());
            } else if (result.length() < 2) {
                result = "0" + result;
            }
            stringBuilder.append(result);
        }
        return stringBuilder.toString();
    }

    @Override
    public String getColumnName() {
        return columnName;
    }

    @Override
    public String getObjectStringValue(ResultSet resultSet) throws SQLException {
        String result = "";
        if (objectType == OBJECT_TYPE_BINARY) {
            result = "x'" + byteToHex(resultSet.getBytes(getColumnName()))
                    + "'";
        } else if (objectType == OBJECT_TYPE_QOTED) {
            result = SLQ_QUOTE + resultSet.getString(getColumnName())
                    + SLQ_QUOTE;
        } else if (resultSet.getString(getColumnName()) == null) {
            result = "null";
        } else if (resultSet.getString(getColumnName()).isEmpty()) {
            result = "\"" + resultSet.getString(getColumnName()) + "\"";

        } else {
            result = resultSet.getString(getColumnName());
        }
        return result;
    }

    public Object getObjectValue(ResultSet resultSet) throws SQLException {
        return resultSet.getObject(getColumnName());
    }

    @Override
    public int getType() {
        return objectType;
    }

    @Override
    public boolean isNotNull() {
        return isNotNull;
    }

    @Override
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    @Override
    public boolean isUnique() {
        return isUnique;
    }

    @Override
    public void setNotNull(boolean isNotNull) {
        this.isNotNull = isNotNull;
    }

    @Override
    public void setPrimaryKey(boolean isPrimaryKey) {
        this.isPrimaryKey = isPrimaryKey;
    }

    @Override
    public void setUnique(boolean isUnique) {
        this.isUnique = isUnique;
    }

    @Override
    public int compareTo(SQLiteColumn column) {
        return getColumnName().compareTo(column.getColumnName());
    }
}