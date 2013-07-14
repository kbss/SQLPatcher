package com.juke.sql.diff;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Test {
    public static void main(String arp[]) {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        try {
            Connection old = DriverManager
                    .getConnection("jdbc:sqlite:D:\\old.db3");
            Connection newDb = DriverManager
                    .getConnection("jdbc:sqlite:D:\\new.db3");
            Diff dff = new Diff(old, newDb);
            dff.compare();
        } catch (SQLException e) {
            throw new RuntimeException("Connection Failed!", e);

        }
    }
}