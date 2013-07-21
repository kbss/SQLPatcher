package com.juke.sql.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.juke.sql.util.Utils;

/*******************************************************************************
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
