package ihome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;

import utils.ConnectDatabase;
import utils.Participle;

/**
 * 初始化
 * 
 * @author W_SL
 */
@SuppressWarnings("all")
public class InitServer {

	/**
	 * 初始化，删除过期定时任务
	 * 
	 * @return
	 */
	public static boolean init() {
		Connection con = ConnectDatabase.connection("ihome_global");
		if (con != null) {
			try {
				//删除过期的定时任务
				Date date = new Date();
				int hour = date.getHours();
				int minute = date.getMinutes();
				String sql = "delete from t_timing where (frequency = ?) and ((hour < ?) or (hour = ? and minute < ?))";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setInt(1, 0);
				ps.setInt(2, hour);
				ps.setInt(3, hour);
				ps.setInt(4, minute);
				ps.executeUpdate();
				con.close();
				//初始化分词
				if(Participle.init())
					return true;
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			} finally {
				try {
					con.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
