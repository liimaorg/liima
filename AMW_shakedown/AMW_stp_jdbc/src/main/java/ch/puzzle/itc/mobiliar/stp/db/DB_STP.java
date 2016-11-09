package ch.puzzle.itc.mobiliar.stp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB_STP {
	/**
	 * @param args
	 *            takes two arguments: The jdbc connection string, the user name
	 *            and the password, additionally requires the jdbc driver in its
	 *            classpath
	 */
	public static void main(String[] args) {
		if (args.length > 2) {
			String jdbcConnectionString = args[0];
			String userName = args[1];
			String password = args[2];
			Connection conn;
			try {
				conn = DriverManager.getConnection(jdbcConnectionString, userName, password);
				conn.close();
				System.out.println("Successfully connected to database ("+jdbcConnectionString+" with user "+userName+")!");
				System.exit(0);
			} catch (SQLException e) {
				System.err.println("Was not able to connect to database ("+jdbcConnectionString+" with user "+userName+")!" + e.getMessage());
				System.exit(1);
			}
		} else {
			System.out.println("Missing parameters! The jdbc connection string, the username and the password is needed to be passed as arguments!");
			System.exit(1);
		}
	}
}
