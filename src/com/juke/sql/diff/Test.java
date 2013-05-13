package com.juke.sql.diff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Test
{
    public static void main(String arp[])
    {
        try
        {
            // Class.forName("com.mysql.jdbc.Driver");
            Class.forName("org.sqlite.JDBC");
            // connection =
            // DriverManager.getConnection("jdbc:sqlite:D:\\testdb.db");
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        try
        {
            // Connection connection = DriverManager
            // .getConnection("jdbc:mysql://localhost:3306/test",
            // "root", "");
            // Connection expected = DriverManager
            // .getConnection("jdbc:mysql://localhost:3306/test2",
            // "root", "");
            Connection old = DriverManager
                    .getConnection("jdbc:sqlite:D:\\old.db3");
            Connection newDb = DriverManager
                    .getConnection("jdbc:sqlite:D:\\new.db3");
            Diff dff = new Diff(old, newDb);
            dff.compare();
            PreparedStatement ps = newDb.prepareStatement("INSERT INTO clob_test (id, value) VALUES (4, ?)");
        }
        catch (SQLException e)
        {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
    }
}