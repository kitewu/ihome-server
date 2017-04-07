package utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 保存温湿亮度通知到临时数据表
 * 
 * @author W_SL
 *
 */
public class DatabaseUtil {

	/**
	 * 更新设备状态
	 * 
	 * @param con
	 * @param id
	 * @param state
	 */
	public static void updateDeviceState(String dbname, String id, int state) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			String sql = "update t_device_stat set stat = ? where id = ?";
			PreparedStatement ps = con.prepareStatement(sql);
			ps = con.prepareStatement(sql);
			ps.setInt(1, state);
			ps.setString(2, id);
			ps.executeUpdate();
			con.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存温湿度信息
	 * 
	 * @param dbname
	 * @param temp
	 * @param humi
	 * @param light
	 */
	public static void saveTempData(String dbname, int temp, int humi, float light) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				String sql = "update t_temp_data set temperature = ?, humidity = ?, light = ?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setInt(1, temp);
				ps.setInt(2, humi);
				ps.setFloat(3, light);
				ps.executeUpdate();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 保存临时通知信息
	 * 
	 * @param dbname
	 * @param content
	 */
	public static void saveTempWarnningMsg(String dbname, String content) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				String sql = "update t_temp_data set warnning = ?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(1, content);
				ps.executeUpdate();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 更新电影下载状态到数据库
	 * 
	 * @param dbname
	 * @param content
	 */
	public static void updateFilmStatInDB(String dbname, String content) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				String[] str = content.split(";");
				String sql = "update t_download_videos set allsize = ?, downloadsize = ?, needtime = ?, speed = ? where url = ?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setString(1, str[1]);
				ps.setString(2, str[2]);
				ps.setString(3, str[3]);
				ps.setString(4, str[4]);
				ps.setString(5, str[0].trim());
				ps.executeUpdate();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 插入电影下载状态到数据库
	 * 
	 * @param dbname
	 * @param url
	 */
	public static void insertFilmStatToDB(String dbname, String url) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				String sql = "select count(*) from t_download_videos where url = '" + url + "'";
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rss = ps.executeQuery(sql);
				rss.next();
				if (rss.getInt(1) == 0) {
					sql = "insert into t_download_videos values(?, ?, ?, ?, ?)";
					ps = con.prepareStatement(sql);
					ps.setString(1, url.trim());
					ps.setString(2, "0");
					ps.setString(3, "0");
					ps.setString(4, "0");
					ps.setString(5, "0");
					ps.executeUpdate();
				}
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 删除电影下载
	 * 
	 * @param dbname
	 * @param content
	 */
	public static void deleteFilmStatFromDB(String dbname, String content) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				String sql = "delete from t_download_videos where url = '" + content.trim() + "'";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.executeUpdate();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 将操作记录存入操作记录数据库
	 * 
	 * @param dbname
	 * @param operation
	 */
	public static void saveOperationToDb(String dbname, String type, String operation) {
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				// 保存
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String[] arr = df.format(new Date()).split(" ");

				String sql = "insert into t_operation values(?, ?, ?, ?)";
				PreparedStatement ps = con.prepareStatement(sql);
				ps = con.prepareStatement(sql);
				ps.setString(1, arr[0]);
				ps.setString(2, arr[1]);
				ps.setString(3, type);
				ps.setString(4, operation);
				ps.executeUpdate();
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 根据msg_id获得id， types, operation属性
	 * 
	 * @param dbname
	 * @param msg_id
	 * @return
	 */
	public static String[] getTypeAndOperation(String dbname, String msg_id) {
		String[] re = null;
		try {
			Connection con = ConnectDatabase.connection(dbname);
			if (con != null) {
				// 获得数据
				String sql = "select * from t_operation_id where msg_id = '" + msg_id + "'";
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rss = ps.executeQuery(sql);
				rss.next();
				re = new String[2];
				re[0] = rss.getString("operation");
				re[1] = rss.getString("types");
				con.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return re;
	}
}
