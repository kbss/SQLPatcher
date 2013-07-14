package com.juke.sql.writer;

import java.io.File;

import com.juke.sql.util.Utils;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class SimpleWriteListner implements WriteListner {

    private static final String FILE_TEMPLATE = "%s_%s.sql";

    private int createTableSeq = 0;

    private String folder = "sql\\";

    private long fileLimit = 500;

    public SimpleWriteListner() {
        createFolderIfNotExists();
    }

    // private String getSequence(String prefix, int seq) {
    // String result = String.format(FILE_TEMPLATE, prefix, seq);
    // while (new File(folder + result).exists()) {
    // result = String.format(FILE_TEMPLATE, prefix, seq);
    // }
    // return result;
    // }

    private void createFolderIfNotExists() {
        File destinationFolder = new File(folder);
        if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
            destinationFolder.mkdir();
        }
    }

    @Override
    public void onWrite(String sqlQery, int sqlType) {

        File sqlFile = new File(folder
                + String.format(FILE_TEMPLATE, "sql", createTableSeq));
        String query = sqlQery + ";\n";
        if (sqlFile.length() + query.length() > fileLimit) {
            while (sqlFile.exists()) {
                sqlFile = new File(folder
                        + String.format(FILE_TEMPLATE, "sql", createTableSeq++));
            }
        }
        Utils.writeStringToFile(sqlFile, query, true);

    }

}
