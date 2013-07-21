package com.juke.sql.diff;

import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.SQLException;

import com.juke.sql.util.Utils;

/*******************************************************************************
 * 
 * @author Serhii Krivtsov
 ******************************************************************************/
public class Test {
	public static void main(String arp[]) throws URISyntaxException {

		try {
			Connection toDb = Utils.getConnection("d:\\1.db");
			Connection fromDb = Utils.getConnection("d:\\2.db");
			Diff dff = new Diff(toDb, fromDb);
			dff.compare();
		} catch (SQLException e) {
			System.out.println("Connection Failed! Check output console");
			e.printStackTrace();
		}
	}


}