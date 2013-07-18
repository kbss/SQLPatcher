package com.juke.sql.diff;

import java.io.File;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.sqlite.SQLiteConfig;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Test {
    public static void main(String arp[]) throws URISyntaxException {

        try {
            Connection toDb = getConnection("d:\\1.db");
            Connection fromDb = getConnection("d:\\2.db");
            Diff dff = new Diff(toDb, fromDb);
            dff.compare();
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
        }
    }

    public static Connection getConnection(String dbFile) throws SQLException,
            URISyntaxException {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Properties connectionProps = new Properties();
        connectionProps.setProperty("flags", "READONLY");
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"
                + new File(dbFile).toURI().getPath(), connectionProps);
        return connection;
    }
}