package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * ��¼��֤
 * 
 * @author W_SL
 */
public class CheckLogin {

	/**
	 * ��֤���ص�¼��Ϣ
	 * 
	 * @param dbname
	 * @param passwd
	 * @return
	 */
	public static boolean checkGatewayLogin(String[] msgs) {
		try {
			if (msgs.length == 4) {
				Connection con = ConnectDatabase.connection("ihome_global");
				if (null != con) {
					String sql = "select * from t_homeid where homeid = ? and password = md5(?)";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setString(1, msgs[2]);
					ps.setString(2, msgs[3]);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						con.close();
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * ��֤�ͻ��˵�¼��Ϣ
	 * 
	 * @param uname
	 * @param passwd
	 * @return
	 */
	public static boolean checkClientLogin(String[] msgs) {
		return true;
	/*	try {
			if (msgs.length == 5) {
				Connection con = ConnectDatabase.connection("global");
				if (null != con) {
					String sql = "select * from t_user where email = ? and password = md5(?)";
					PreparedStatement ps = con.prepareStatement(sql);
					ps.setString(1, msgs[3]);
					ps.setString(2, msgs[4]);
					ResultSet rs = ps.executeQuery();
					if (rs.next()) {
						con.close();
						return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;*/
	}
}
