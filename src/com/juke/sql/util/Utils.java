package com.juke.sql.util;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Utils
{

    public static void close(Object... dbResources)
    {
        for (Object resource : dbResources)
        {
            if (resource != null)
            {
                try
                {
                    if (resource instanceof Connection)
                    {
                        ((Connection) resource).close();
                    }
                    else if (resource instanceof Statement)
                    {
                        ((Statement) resource).close();
                    }
                    else if (resource instanceof ResultSet)
                    {
                        ((ResultSet) resource).close();
                    }
                }
                catch (SQLException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static void close(Closeable... dbResources)
    {
        for (Closeable resource : dbResources)
        {
            if (resource != null)
            {
                try
                {
                    resource.close();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public static <T> String join(Collection<T> collections, String separator)
    {
        String result = "";
        if (!collections.isEmpty())
        {
            StringBuilder stringBuilder = new StringBuilder();
            for (T object : collections)
            {
                stringBuilder.append(object.toString()).append(separator);
            }
            int resultLength = stringBuilder.length();
            stringBuilder.delete(resultLength - separator.length(),
                    resultLength);
            result = stringBuilder.toString();
        }
        return result;
    }

    public static <T> String join(Collection<T> collection)
    {
        return join(collection, ", ");
    }

    public static void writeStringToFile(File file, String text)
    {
        PrintWriter out = null;
        FileWriter outFile = null;
        try
        {
            outFile = new FileWriter(file);
            out = new PrintWriter(outFile);
            out.write(text);
            outFile.close();
            out.flush();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            close(out, outFile);
        }
    }

    public static void writeStringToFile(String file, String text)
    {
        writeStringToFile(new File(file), text);
    }

    public static byte[] objectToByteArray(Object obj)
    {
        ByteArrayOutputStream bos = null;
        ObjectOutputStream oos = null;
        byte[] byteArray;
        try
        {
            bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.flush();
            byteArray = bos.toByteArray();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            close(bos, oos);
        }
        return byteArray;
    }

    public static String byteToString(byte[] byteArray)
    {
        String result;
        try
        {
            result = new String(byteArray, "UTF-8");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException();
        }
        return result;
    }
    
    public static String bytArrayToHexString(Object obj)
    {
        StringBuilder stringBuilder = new StringBuilder();
        byte[] byteArray = Utils.objectToByteArray(obj);
        for (byte b : byteArray)
        {
            stringBuilder.append(String.format("%02x", b & 0xff));
        }
        return stringBuilder.toString();
    }
}
