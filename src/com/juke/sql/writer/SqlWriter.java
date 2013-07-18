package com.juke.sql.writer;

import java.io.Closeable;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface SqlWriter extends Closeable {
    public static int INSERT = 0;
    public static int UPDATE = 1;
    public static int DROP = 2;
    public static int OTHER = 10;

    public void onWrite(String sqlQery, int sqlType);
    
    public void close();
}
