package com.juke.sql.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Properties;

/*******************************************************************************
 * SQLPatcher - <a
 * href="https://github.com/kbss/SQLPatcher">https://github.com/kbss
 * /SQLPatcher</a><br>
 * 
 * Copyright (C) 2013 Serhii Krivtsov<br>
 * 
 * SQLPatcher is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.<br>
 * <br>
 * SQLPatcher is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>. <br>
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Utils {
    public static void close(Object... dbResources) {
        for (Object resource : dbResources) {
            if (resource != null) {
                try {
                    if (resource instanceof Connection) {
                        ((Connection) resource).close();
                    } else if (resource instanceof Statement) {
                        ((Statement) resource).close();
                    } else if (resource instanceof ResultSet) {
                        ((ResultSet) resource).close();
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void close(Closeable... dbResources) {
        for (Closeable resource : dbResources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static <T> String join(Collection<T> collections, String separator) {
        String result = "";
        if (!collections.isEmpty()) {
            StringBuilder stringBuilder = new StringBuilder();
            for (T object : collections) {
                stringBuilder.append(object.toString()).append(separator);
            }
            int resultLength = stringBuilder.length();
            stringBuilder.delete(resultLength - separator.length(),
                    resultLength);
            result = stringBuilder.toString();
        }
        return result;
    }

    public static <T> String join(Collection<T> collection) {
        return join(collection, ", ");
    }

    public static void writeStringToFile(File file, String text, boolean append) {
        PrintWriter out = null;
        FileWriter outFile = null;
        try {
            outFile = new FileWriter(file, append);
            out = new PrintWriter(outFile);
            out.write(text);
            outFile.close();
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(out, outFile);
        }
    }

    public static void writeStringToFile(File file, String text) {
        writeStringToFile(file, text, false);
    }

    public static void writeStringToFile(String file, String text) {
        writeStringToFile(new File(file), text);
    }

    public static byte[] objectToByteArray(Object obj) {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        byte[] byteArray;
        try {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            byteArray = bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            close(bos, oos);
        }
        return byteArray;
    }

    public static String byteToString(byte[] byteArray) {
        String result;
        try {
            result = new String(byteArray, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
        return result;
    }

    public static String bytArrayToHexString(Object obj) {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] byteArray = Utils.objectToByteArray(obj);
        for (byte b : byteArray) {
            stringBuilder.append(String.format("%02x", b & 0xff));
        }
        return stringBuilder.toString();
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