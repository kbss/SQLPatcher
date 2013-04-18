package com.juke.sql.formater.sqlite;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.juke.sql.formater.SQLColumn;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SQLiteColumn implements SQLColumn, Comparable
{
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

    public SQLiteColumn(String columnName, String columnType)
    {
        this.columnName = columnName;
        if (quotedTypes.contains(columnType.split("\\(")[0]))
        {
            objectType = OBJECT_TYPE_QOTED;
        }
        else if (binaryTypes.contains(columnType))
        {
            objectType = OBJECT_TYPE_BINARY;
        }
        else
        {
            objectType = OBJECT_TYPE_UN_QOTED;
        }
    }

    static
    {
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
    private String byteToHex(byte[] bytes)
    {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes)
        {
            String result = Integer.toHexString(b);
            if (result.length() > 3)
            {
                result = result.substring(result.length() - 2, result.length());
            }
            else if (result.length() < 2)
            {
                result = "0" + result;
            }
            stringBuilder.append(result);
        }
        return stringBuilder.toString();
    }

    /***************************************************************************
     * TODO: add getter description
     * 
     * @return the columnName
     */
    @Override
    public String getColumnName()
    {
        return columnName;
    }

    @Override
    public String getObjectStringValue(ResultSet resultSet) throws SQLException
    {
        String result = "";
        if (objectType == OBJECT_TYPE_BINARY)
        {
            result = "x'" + byteToHex(resultSet.getBytes(getColumnName()))
                    + "'";
        }
        else if (objectType == OBJECT_TYPE_QOTED)
        {
            result = SLQ_QUOTE + resultSet.getString(getColumnName())
                    + SLQ_QUOTE;
        }
        else
        {
            result = resultSet.getString(getColumnName());
        }
        return result;
    }

    /***************************************************************************
     * TODO: add getter description
     * 
     * @return the type
     */
    @Override
    public int getType()
    {
        return objectType;
    }

    /***************************************************************************
     * TODO: add getter description
     * 
     * @return the isNotNull
     */
    public boolean isNotNull()
    {
        return isNotNull;
    }

    /***************************************************************************
     * TODO: add getter description
     * 
     * @return the isPrimaryKey
     */
    public boolean isPrimaryKey()
    {
        return isPrimaryKey;
    }

    /***************************************************************************
     * TODO: add getter description
     * 
     * @return the isUnique
     */
    public boolean isUnique()
    {
        return isUnique;
    }

    /***************************************************************************
     * TODO: add setter description
     * 
     * @param isNotNull
     *            the isNotNull to set
     */
    public void setNotNull(boolean isNotNull)
    {
        this.isNotNull = isNotNull;
    }

    /***************************************************************************
     * TODO: add setter description
     * 
     * @param isPrimaryKey
     *            the isPrimaryKey to set
     */
    public void setPrimaryKey(boolean isPrimaryKey)
    {
        this.isPrimaryKey = isPrimaryKey;
    }

    /***************************************************************************
     * TODO: add setter description
     * 
     * @param isUnique
     *            the isUnique to set
     */
    public void setUnique(boolean isUnique)
    {
        this.isUnique = isUnique;
    }

    @Override
    public int compareTo(Object o)
    {
        return getColumnName().compareTo(((SQLiteColumn) o).getColumnName());
    }
}