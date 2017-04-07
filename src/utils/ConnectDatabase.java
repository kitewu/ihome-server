package utils;

import java.sql.Connection;
import java.sql.DriverManager;

/**
 * 连接数据库
 * 
 * @author W_SL
 *
 */
public class ConnectDatabase {

	/**
	 * 连接数据库
	 * 
	 * @param dbname
	 * @return
	 */
	public static Connection connection(String dbname) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://your db url:3306/" + dbname, "your username", "your password");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static Connection connection1(String dbname) {
		try {
			Class.forName("com.mysql.jdbc.Driver");
			return DriverManager.getConnection("jdbc:mysql://your db url:3306/" + dbname, "your username", "your password");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
