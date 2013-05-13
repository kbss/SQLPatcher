package com.juke.sql.formater;

import java.sql.Connection;
import java.util.List;

import com.juke.sql.writer.WriteListner;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface SQLFormater
{
    public String DROP_TABLE_SQL_TEMPLATE = "DROP TABLE IF EXISTS %s;";
    public String INSERT_QERY = "INSERT INTO %s (%s) VALUES (%s)";

    public void createFullSQLTableDump(Connection connection, String tableName);

    public List<String> getTableList(Connection connection);

    public String createDropTableSQLQery(String tableName);
    
    public void registreWriter(WriteListner listner);
}
