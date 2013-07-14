package com.juke.sql.formater;

import java.sql.ResultSet;
import java.sql.SQLException;

/*******************************************************************************
 * SQL Column interface.
 * 
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface SQLColumn {
    public int OBJECT_TYPE_UN_QOTED = 0;

    public int OBJECT_TYPE_QOTED = 1;

    public int OBJECT_TYPE_BINARY = 2;

    public int getType();

    /**************************************************************************
     * Returns column name
     * 
     * @return
     */
    public String getColumnName();

    public String getObjectStringValue(ResultSet resultSet) throws SQLException;

    public boolean isNotNull();

    public boolean isPrimaryKey();

    public boolean isUnique();

    public void setNotNull(boolean isNotNull);

    public void setPrimaryKey(boolean isPrimaryKey);

    public void setUnique(boolean isUnique);
}
