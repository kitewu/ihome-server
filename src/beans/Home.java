package beans;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Timer;

import ihome.Main;
import ihome.ManageTimer;
import ihome.Main.MyThread;
import utils.ConnectDatabase;

/**
 * 家庭类
 * 
 * @author WSL
 */
@SuppressWarnings("all")
public class Home {

	private String homeid = null;
	private int temp = 25; // 温度
	private int humi = 0; // 湿度
	private float light = 0; // 光照
	private Timer timer_save_thl = null;// 自动保存温度湿度亮度数据时钟

	private boolean is_window_open = false;// 窗帘是否开启
	private Timer timer_smart_window = null;// 窗帘自动控制时钟
	private int light_min = 0; // 窗帘自动控制阈值
	private int light_max = 1000;

	private boolean is_condition_open = false;// 空调是否开启
	private Timer timer_smart_condition = null;// 空调自动控制时钟
	private int temp_min = 17; // 空调自动控制阈值
	private int temp_max = 30;

	private boolean smart_mode = true; // 智能模式默认为开启

	private HashMap<String, String> protocol_map = null;// 保存协议信息

	public Home(String homeid) {
		this.homeid = homeid;
	}

	/**
	 * 网关连接时初始化
	 * 
	 * @param out
	 * @param mythread
	 * @return
	 */
	public Timer getTimer_smart_condition() {
		return timer_smart_condition;
	}

	public boolean isIs_condition_open() {
		return is_condition_open;
	}

	public void setIs_condition_open(boolean is_condition_open) {
		this.is_condition_open = is_condition_open;
	}
	
	public void setTimer_smart_condition(Timer timer_smart_condition) {
		this.timer_smart_condition = timer_smart_condition;
	}

	public int getTemp_min() {
		return temp_min;
	}

	public void setTemp_min(int temp_min) {
		this.temp_min = temp_min;
	}

	public int getTemp_max() {
		return temp_max;
	}

	public void setTemp_max(int temp_max) {
		this.temp_max = temp_max;
	}

	public boolean init(OutputStream out, MyThread mythread) {
		Connection con = null;
		try {
			// 删除可能存在的过期的定时任务
			con = ConnectDatabase.connection("ihome_global");
			if (null != con) {
				Date date = new Date();
				int hour = date.getHours();
				int minute = date.getMinutes();
				String sql = "delete from t_timing where (frequency = ?) and ((hour < ?) or (hour = ? and minute < ?)) and homeid = ?";
				PreparedStatement ps = con.prepareStatement(sql);
				ps.setInt(1, 0);
				ps.setInt(2, hour);
				ps.setInt(3, hour);
				ps.setInt(4, minute);
				ps.setString(5, homeid);
				ps.executeUpdate();
				con.close();
			}
			con = ConnectDatabase.connection(homeid);
			if (null != con) {
				// 缓存协议信息
				if (protocol_map == null)
					protocol_map = new HashMap<String, String>();
				String sql = "select * from t_operation_id";
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rss = ps.executeQuery(sql);
				while (rss.next()) {
					protocol_map.put(rss.getString("msg_id"), rss.getString("operation"));
				}

				// 状态表置0
				sql = "update t_device_stat set stat = 0";
				ps = con.prepareStatement(sql);
				ps.executeUpdate();

				// 智能模式和智能窗帘置1
				sql = "update t_device_stat set stat = 1 where id = '0010' or id = '0009'";
				ps = con.prepareStatement(sql);
				ps.executeUpdate();

				// 清空临时数据表
				sql = "update t_temp_data set temperature = 25, humidity = 0, light = 0, warnning = 0, light_min = 0, light_max = 1000, temp_min = 17, temp_max = 30";
				ps = con.prepareStatement(sql);
				ps.executeUpdate();
				con.close();

				// 启动窗帘自动控制时钟
				ManageTimer.startTimerForWindow(mythread);

				// 启动控制空调时钟
				ManageTimer.startTimerForCondition(mythread);

				// 添加定时任务
				if (addTimer(out)) {
					return true;
				}
			}
		} catch (Exception e) {
			try {
				con.close();
			} catch (SQLException e1) {
			}
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 添加定时任务
	 * 
	 * @param out
	 * @return
	 */
	public boolean addTimer(OutputStream out) {
		Connection con = null;
		try {
			con = ConnectDatabase.connection("ihome_global");
			if (con != null) {
				String sql = "select * from t_timing";
				PreparedStatement ps = con.prepareStatement(sql);
				ResultSet rs = ps.executeQuery(sql);
				while (rs.next()) {
					TimingTask t = new TimingTask(homeid, out, rs.getString("msg"), rs.getInt("frequency"),
							rs.getInt("hour"), rs.getInt("minute"));
					Main.getTimingtask().add(t);
					ManageTimer.addTimer(t);
				}
				con.close();
				return true;
			}
		} catch (Exception e) {
			try {
				con.close();
			} catch (SQLException e1) {
			}
			e.printStackTrace();
		}
		return false;
	}

	public String getHomeid() {
		return homeid;
	}

	public void setHomeid(String homeid) {
		this.homeid = homeid;
	}

	public HashMap<String, String> getProtocol_map() {
		return protocol_map;
	}

	public void setProtocol_map(HashMap<String, String> protocol_map) {
		this.protocol_map = protocol_map;
	}

	public int getTemp() {
		return temp;
	}

	public void setTemp(int temp) {
		this.temp = temp;
	}

	public int getHumi() {
		return humi;
	}

	public void setHumi(int humi) {
		this.humi = humi;
	}

	public float getLight() {
		return light;
	}

	public void setLight(float light) {
		this.light = light;
	}

	public boolean isIs_window_open() {
		return is_window_open;
	}

	public void setIs_window_open(boolean is_window_open) {
		this.is_window_open = is_window_open;
	}

	public boolean getSmart_mode() {
		return smart_mode;
	}

	public void setSmart_mode(boolean smart_mode) {
		this.smart_mode = smart_mode;
	}

	public int getLight_min() {
		return light_min;
	}

	public void setLight_min(int light_min) {
		this.light_min = light_min;
	}

	public int getLight_max() {
		return light_max;
	}

	public void setLight_max(int light_max) {
		this.light_max = light_max;
	}

	public Timer getTimer_smart_window() {
		return timer_smart_window;
	}

	public void setTimer_smart_window(Timer timer_smart_window) {
		this.timer_smart_window = timer_smart_window;
	}

	public Timer getTimer_save_thl() {
		return timer_save_thl;
	}

	public void setTimer_save_thl(Timer timer_save_thl) {
		this.timer_save_thl = timer_save_thl;
	}
}
