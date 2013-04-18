package com.juke.sql.formater.sqlite;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.juke.sql.formater.SQLColumn;
import com.juke.sql.formater.SQLFormater;
import com.juke.sql.util.Utils;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SQLiteFormater implements SQLFormater
{
    // private static final String CREATE_TABLE_SQL = "CREATE TABLE %s (%s);";

    private static final String SQL_SEPARATOR = ";";

    private static final String SQL_COLUMN_SEPARATOR = ", ";

    private static final String SELECT_ALL = "SELECT * FROM %s";

    private String joinColumns(List<SQLiteColumn> columnList)
    {

        StringBuilder stringBuilder = new StringBuilder();
        for (SQLiteColumn column : columnList)
        {
            stringBuilder.append(column.getColumnName()).append(
                    SQL_COLUMN_SEPARATOR);
        }
        int length = stringBuilder.length();
        stringBuilder.delete(length - SQL_COLUMN_SEPARATOR.length(), length);
        return stringBuilder.toString();
    }

    private void addInsertStatement(String sqlQuery)
    {
        System.err.println(sqlQuery);
    }

    private void addDropTableStatement(String sqlQuery)
    {
        System.err.println(sqlQuery);
    }

    @Override
    public String createFullSQLTableDump(Connection connection, String tableName)
    {
        List<SQLiteColumn> columnList = getColumnList(connection, tableName);
        ResultSet resultSet = null;
        Statement statement = null;
        String joinedColumns = joinColumns(columnList);

        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(String.format(SELECT_ALL,
                    tableName));
            while (resultSet.next())
            {
                String result = "";
                for (SQLiteColumn column : columnList)
                {
                    result += column.getObjectStringValue(resultSet)
                            + SQL_COLUMN_SEPARATOR;
                }
                result = result.substring(0, result.length()
                        - SQL_COLUMN_SEPARATOR.length());
                addInsertStatement(String.format(SQLFormater.INSERT_QERY,
                        tableName, joinedColumns, result));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Utils.close(resultSet, statement);
        }

        return getNewTableSQLQuery(connection, tableName);
    }

    private void getOldTablesDiff(Connection newDatabase, Connection oldDatabase)
    {

    }

    private boolean isTableStructureEquals(Connection newDatabase,
            Connection oldDatabase, String tableName)
    {
        List<SQLiteColumn> oldColumList = getColumnList(oldDatabase, tableName);
        List<SQLiteColumn> newColumList = getColumnList(oldDatabase, tableName);
        Collections.sort(oldColumList);
        Collections.sort(newColumList);
        return oldColumList.equals(newColumList);
    }

    private List<SQLiteColumn> getColumnList(Connection connection,
            String tableName)
    {
        List<SQLiteColumn> columnList = new ArrayList<SQLiteColumn>();
        ResultSet resultSet = null;
        Statement statement = null;
        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("pragma table_info('"
                    + tableName + "')");

            while (resultSet.next())
            {
                SQLiteColumn column = new SQLiteColumn(
                        resultSet.getString("name"),
                        resultSet.getString("type"));
                if (resultSet.getInt("pk") == 1)
                {
                    column.setPrimaryKey(true);
                }
                if (resultSet.getInt("notnull") == 1)
                {
                    column.setNotNull(true);
                }
                columnList.add(column);
            }
            findAndUpdateUniqueColumns(connection, tableName, columnList);
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);

        }
        finally
        {
            Utils.close(resultSet, statement);
        }
        return columnList;
    }

    private String getIndexColumnName(Connection connection, String indexName)
    {
        ResultSet resultSet = null;
        Statement statement = null;
        String columnName = null;
        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery("pragma index_info('"
                    + indexName + "')");

            while (resultSet.next())
            {
                columnName = resultSet.getString("name");
                break;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);

        }
        finally
        {
            Utils.close(resultSet, statement);
        }
        return columnName;
    }

    // TODO Try to find SQL query for selecting unique columns
    private List<SQLiteColumn> findAndUpdateUniqueColumns(
            Connection connection, String tableName,
            List<SQLiteColumn> columnList)
    {
        ResultSet resultSet = null;
        Statement statement = null;
        try
        {
            statement = connection.createStatement();
            try
            {
                resultSet = statement.executeQuery("PRAGMA INDEX_LIST('"
                        + tableName + "')");
            }
            catch (SQLException e)
            {
                return columnList;
            }
            while (resultSet.next())
            {
                String indexColumnId = getIndexColumnName(connection,
                        resultSet.getString("name"));
                if (indexColumnId != null && !indexColumnId.isEmpty())
                {
                    for (SQLiteColumn colum : columnList)
                    {
                        if (colum.getColumnName().equalsIgnoreCase(
                                indexColumnId))
                        {
                            colum.setUnique(true);
                            break;
                        }
                    }
                }
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Utils.close(resultSet, statement);
        }
        return columnList;
    }

    public String getNewTableSQLQuery(Connection connection, String tableName)
    {
        String sql = "                                                  "
                + "SELECT sql                                           "
                + "FROM sqlite_master                                   "
                + "WHERE type = 'table'  AND name = '" + tableName + "'     ";
        Statement statement = null;
        ResultSet resultSet = null;
        String result = "";
        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                result = resultSet.getString(1) + SQL_SEPARATOR;
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Utils.close(resultSet, statement);
        }
        return result;
    }

    @Override
    public List<String> getTableList(Connection connection)
    {
        String sql = "                                          "
                + "SELECT name                                  "
                + "FROM sqlite_master                           "
                + "WHERE type = 'table'                         ";
        Statement statement = null;
        ResultSet resultSet = null;
        List<String> tableList = new ArrayList<String>();
        try
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
            while (resultSet.next())
            {
                tableList.add(resultSet.getString(1));
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            Utils.close(resultSet, statement);
        }
        return tableList;
    }

    @Override
    public String createDropTableSQLQery(String tableName)
    {
        addDropTableStatement(String.format(
                SQLFormater.DROP_TABLE_SQL_TEMPLATE, tableName));
        return String.format(SQLFormater.DROP_TABLE_SQL_TEMPLATE, tableName);
    }
}