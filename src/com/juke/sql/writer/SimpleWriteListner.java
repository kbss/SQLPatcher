package com.juke.sql.writer;

import java.io.File;

/*******************************************************************************
 * TODO: add class / interface description
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SimpleWriteListner implements WriteListner
{

    private static final String FILE_TEMPLATE = "%s_%s.sql";

    private int createTableSeq = 0;

    private int createInsertSeq = 0;

    private int createUpdateSeq = 0;

    private int createDropSeq = 0;

    private String folder = "sql\\";

    public SimpleWriteListner()
    {
        createFolderIfNotExists();
    }

    private int getSequence(String prefix, int seq)
    {

        String result = String.format(FILE_TEMPLATE, prefix, seq);
        if (!new File(folder + result).exists())
        {
            return seq;
        }
        return getSequence(prefix, seq + 1);
    }

    private void createFolderIfNotExists()
    {
        File destinationFolder = new File(folder);
        if (!destinationFolder.exists() || !destinationFolder.isDirectory())
        {
            destinationFolder.mkdir();
        }
    }

    @Override
    public void onCreateTableWrite(String sqlQery)
    {
        createTableSeq = getSequence("insert",createTableSeq);
    }

    @Override
    public void onInsertStatementWrite(String sqlQery)
    {
        createInsertSeq++;
    }

    @Override
    public void onUpdateStatementWrite(String sqlQery)
    {
        createUpdateSeq++;
    }

    @Override
    public void onDropTableWrite(String sqlQery)
    {
        createDropSeq++;
    }
}
