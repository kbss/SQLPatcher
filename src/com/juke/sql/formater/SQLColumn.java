package com.juke.sql.formater;

import java.sql.ResultSet;
import java.sql.SQLException;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface SQLColumn
{
    public int OBJECT_TYPE_UN_QOTED = 0;

    public int OBJECT_TYPE_QOTED = 1;

    public int OBJECT_TYPE_BINARY = 2;

    public int getType();

    public String getColumnName();

    public String getObjectStringValue(ResultSet resultSet) throws SQLException;
}
