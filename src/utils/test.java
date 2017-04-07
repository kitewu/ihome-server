package utils;

import java.sql.PreparedStatement;

import com.mysql.jdbc.Connection;

public class test {
	public static void main(String[] args) throws Exception {
		Connection con = (Connection) ConnectDatabase.connection1("db_iotweb");
		if (null != con) {
			String sql = "insert into t_tag values(?, ?)";
			PreparedStatement ps = con.prepareStatement(sql);
			ps.setString(1, "100");
			ps.setString(2, "Œ‚…‹¡Î");
			ps.executeUpdate();
			con.close();
		}else{
			System.out.println("hhh");
		}
		System.out.println(true);
	}
}
