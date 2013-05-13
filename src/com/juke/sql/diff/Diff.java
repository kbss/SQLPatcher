package com.juke.sql.diff;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import com.juke.sql.formater.SQLFormater;
import com.juke.sql.formater.sqlite.SQLiteFormater;
import com.juke.sql.writer.SimpleWriteListner;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Diff
{
    private Connection actual;

    private Connection expected;

    private SQLFormater sqlFormater;

    private List<String> oldTables;

    private List<String> newTables;

    private List<String> missingTables;

    private List<String> dropedTables;

    private List<String> tableListForCompare;

    public Diff(Connection actual, Connection expected)
    {
        this.actual = actual;
        this.expected = expected;
        sqlFormater = new SQLiteFormater();
        sqlFormater.registreWriter(new SimpleWriteListner());
    }

    private List<String> getNewTables()
    {
        if (missingTables == null)
        {
            missingTables = new ArrayList<String>();
            for (String table : getNewTableList())
            {
                if (!getOldTableList().contains(table))
                {
                    missingTables.add(table);
                }
            }
        }
        return missingTables;
    }

    private List<String> getNewTableList()
    {
        if (newTables == null)
        {
            newTables = sqlFormater.getTableList(expected);
        }
        return newTables;
    }

    private List<String> getOldTableList()
    {
        if (oldTables == null)
        {
            oldTables = sqlFormater.getTableList(actual);
        }
        return oldTables;
    }

    private List<String> getDropedTables()
    {
        if (dropedTables == null)
        {
        	dropedTables = new ArrayList<String>();
            for (String table : getOldTableList())
            {
                if (!getNewTableList().contains(table))
                {
                    dropedTables.add(table);
                }
            }
        }
        return dropedTables;
    }

    private List<String> getTableListForCompare()
    {
        if (tableListForCompare == null)
        {
            tableListForCompare = new ArrayList<String>();
            tableListForCompare.addAll(oldTables);
            tableListForCompare.removeAll(dropedTables);
            tableListForCompare.removeAll(missingTables);
        }
        return tableListForCompare;
    }

    public void compare()
    {
        for (String tableName : getNewTables())
        {
            sqlFormater.createFullSQLTableDump(expected,
                    tableName);
        }
        for (String tableName : getDropedTables())
        {
            System.out.println(sqlFormater.createDropTableSQLQery(tableName));
        }

    }
}
