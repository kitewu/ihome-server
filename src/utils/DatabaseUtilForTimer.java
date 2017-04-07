package utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 保存温湿度状态到数据库历史记录表
 * 
 * @author WSL
 */
public class DatabaseUtilForTimer {

	/**
	 * 启动timer
	 * 
	 * @param dbname
	 * @param temp
	 * @param humi
	 * @param light
	 * @return
	 */
	public static Timer Start(String dbname, int temp, int humi, float light) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					Connection con = ConnectDatabase.connection(dbname);
					if (con != null) {
						String sql = "insert into t_record(dates,times,temperature,humidity,light) values(?, now(), ?, ?, ?)";
						PreparedStatement ps = con.prepareStatement(sql);
						Date date = new Date(new java.util.Date().getTime());
						ps.setDate(1, date);
						ps.setInt(2, temp);
						ps.setInt(3, humi);
						ps.setFloat(4, light);
						ps.executeUpdate();
						System.out.println("save message to database success! timer");
					}
					con.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		Timer timer = new Timer();
		timer.schedule(task, 2000, 600000);
		return timer;
	}
}
