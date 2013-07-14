package com.juke.sql.writer;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface WriteListner {
    public static int INSERT = 0;
    public static int UPDATE = 1;
    public static int DROP = 2;
    public static int OTHER = 10;

    public void onWrite(String sqlQery, int sqlType);
}
