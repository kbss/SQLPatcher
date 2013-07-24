package com.juke.sql.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.juke.sql.util.Utils;

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
public class SimpleWriteListner implements SqlWriter {

    private static final String FILE_TEMPLATE = "%s_%s.sql";

    private int seq = 1;

    private String folder = "sql\\";

    // private long fileLimit = 1000000000;

    PrintWriter out = null;
    FileWriter outFile = null;
    private File sqlFile;

    public SimpleWriteListner() {
        createFolderIfNotExists();
        sqlFile = new File(folder + String.format(FILE_TEMPLATE, "sql", seq));
        try {
            outFile = new FileWriter(sqlFile, true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        out = new PrintWriter(outFile);
        writeToFile("BEGIN;\n");
    }

    private void createFolderIfNotExists() {
        File destinationFolder = new File(folder);
        if (!destinationFolder.exists() || !destinationFolder.isDirectory()) {
            destinationFolder.mkdir();
        }
    }

    @Override
    public void onWrite(String sqlQery, int sqlType) {
        writeToFile(sqlQery.trim() + ";\n");
    }

    private void writeToFile(String text) {
        out.write(text);
    }

    public void close() {
        out.flush();
        Utils.close(out, outFile);
    }

    // public static void writeStringToFile(File file, String text, boolean
    // append) {
    // PrintWriter out = null;
    // FileWriter outFile = null;
    // try {
    // outFile = new FileWriter(file, append);
    // out = new PrintWriter(outFile);
    // out.write(text);
    // out.flush();
    // } catch (IOException e) {
    // throw new RuntimeException(e);
    // } finally {
    // Utils.close(out, outFile);
    // }
    // }
}