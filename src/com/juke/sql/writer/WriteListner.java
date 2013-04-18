package com.juke.sql.writer;

import java.util.Observer;

/*******************************************************************************
 * TODO: add class / interface description
 *
 * @author Serhii Krivtsov
 ******************************************************************************/
public interface WriteListner
{
    public void onCreateTableWrite(String sqlQery);
    public void onInsertStatementWrite(String sqlQery);
    public void onUpdateStatementWrite(String sqlQery);
    public void onDropTableWrite(String sqlQery);
}
