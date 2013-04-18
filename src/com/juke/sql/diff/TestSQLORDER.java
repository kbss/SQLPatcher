package com.juke.sql.diff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class TestSQLORDER
{

    /***************************************************************************
     * TODO: add method description
     * 
     * @param args
     */
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
            e.printStackTrace();
            return;
        }
        try
        {
            // Connection connection = DriverManager
            // .getConnection("jdbc:mysql://localhost:3306/test",
            // "root", "");
            // Connection expected = DriverManager
            // .getConnection("jdbc:mysql://localhost:3306/test2",
            // "root", "");
            Connection connection = DriverManager
                    .getConnection("jdbc:sqlite:D:\\testdbR.db3");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM test");
            while (resultSet.next())
            {
                System.out.println(resultSet.getInt(1) + " =1 "
                        + resultSet.getString(2));
                
            }

        }
        catch (SQLException e)
        {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }

    }

}
