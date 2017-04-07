package ihome;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Date;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import beans.Home;
import beans.TimingTask;
import ihome.Main.MyThread;
import utils.ConnectDatabase;
import utils.DatabaseUtil;
import utils.SendMsg;

/**
 * 管理定时任务
 * 
 * @author WSL
 *
 */
@SuppressWarnings("all")
public class ManageTimer {

	/**
	 * 添加定时任务
	 * 
	 * @param t
	 */
	public static void addTimer(TimingTask t) {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				try {
					// 发送消息
					SendMsg.sendMsg(t.getOut(), t.getMsg());
					// 更改状态
					String[] temp = DatabaseUtil.getTypeAndOperation(t.getHomeid(), t.getMsg());
					if (null != temp) {
						int state = 0;
						if (t.getMsg().split(";")[2].equals("01"))
							state = 1;
						DatabaseUtil.updateDeviceState(t.getHomeid(), temp[1], state);
					} else
						System.out.println("update device state failed");
					// 如果执行一次，则删除定时任务
					if (t.getFrequency() == 0)
						deleteTimer(this.hashCode());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};
		t.setTimer(new Timer());
		t.setHashcode(task.hashCode());
		int hour = new Integer(t.getHour());
		int minute = new Integer(t.getMinute());
		Date date = new Date();
		int chour = date.getHours();
		int cminute = date.getMinutes();
		int csecond = date.getSeconds();
		int cha = hour * 3600 + minute * 60 - chour * 3600 - cminute * 60 - csecond;
		if (cha < 0) {
			cha += 86400000;
		}
		t.getTimer().schedule(task, cha * 1000, 86400000);
		/*
		 * if (t.getFrequency() == 1) t.getTimer().schedule(task, cha * 1000,
		 * 86400000); else t.getTimer().schedule(task, cha * 1000, 86400000);
		 */
	}

	/**
	 * 从链表中删除定时任务
	 * 
	 * @param homeid
	 * @param arr
	 */
	public static void deleteTimer(String homeid, String[] arr) {
		Iterator<TimingTask> it = Main.getTimingtask().iterator();
		while (it.hasNext()) {
			TimingTask temp = it.next();
			if (temp.getMsg().equals(arr[0]) && temp.getFrequency() == new Integer(arr[1])
					&& temp.getHour() == new Integer(arr[2]) && temp.getMinute() == new Integer(arr[3])
					&& temp.getHomeid().equals(homeid)) {
				temp.getTimer().cancel();
				it.remove();
				break;
			}
		}
	}

	/**
	 * 从链表中删除定时任务
	 * 
	 * @param hashcode
	 */
	public static void deleteTimer(int hashcode) {
		try {
			Iterator<TimingTask> it = Main.getTimingtask().iterator();
			while (it.hasNext()) {
				TimingTask temp = it.next();
				if (temp.getHashcode() == hashcode) {
					temp.getTimer().cancel();
					it.remove();
					Connection con = ConnectDatabase.connection("ihome_global");
					if (con != null) {
						String sql = "delete from t_timing where frequency = ? and hour = ? and minute = ? and msg = ? and homeid = ?";
						PreparedStatement ps = con.prepareStatement(sql);
						ps.setInt(1, temp.getFrequency());
						ps.setInt(2, temp.getHour());
						ps.setInt(3, temp.getMinute());
						ps.setString(4, temp.getMsg());
						ps.setString(5, temp.getHomeid());
						ps.executeUpdate();
						System.out.println("delete timer success");
					}
					con.close();
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 开启窗帘控制时钟
	 * 
	 * @param mythread
	 * @return
	 */
	public static boolean startTimerForWindow(MyThread mythread) {
		Home home = mythread.getMyhome();
		if (home.getTimer_smart_window() != null)
			home.getTimer_smart_window().cancel();
		Timer mytimer = new Timer();
		mytimer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (home.getLight() > home.getLight_max() && home.isIs_window_open()) {// 关窗帘
					home.setIs_window_open(false);
					SendMsg.sendMsg(mythread.getOut(), "16;00;00");
					DatabaseUtil.updateDeviceState(mythread.getHomeid(), "0008", 0);
				}
				if (home.getLight() < home.getLight_min() && !home.isIs_window_open()) {// 开窗帘
					home.setIs_window_open(true);
					SendMsg.sendMsg(mythread.getOut(), "16;00;01");
					DatabaseUtil.updateDeviceState(mythread.getHomeid(), "0008", 1);
				}
			}
		}, 0, 1000);
		home.setTimer_smart_window(mytimer);
		return true;
	}

	/**
	 * 开启空调控制时钟
	 * 
	 * @param mythread
	 * @return
	 */
	public static boolean startTimerForCondition(MyThread mythread) {
		Home home = mythread.getMyhome();
		if (home.getTimer_smart_condition() != null)
			home.getTimer_smart_condition().cancel();
		Timer mytimer = new Timer();
		mytimer.schedule(new TimerTask() {
			@Override
			public void run() {
				//温度过高或过低且没开空调
				if (((home.getTemp() > home.getTemp_max()) || (home.getTemp() < home.getTemp_min())) && (!home.isIs_condition_open())) {// 开空调
					home.setIs_condition_open(true);
				//	SendMsg.sendMsg(mythread.getOut(), "15;04;00");
					DatabaseUtil.updateDeviceState(mythread.getHomeid(), "0002", 1);
					SendMsg.sendMsg(home.getHomeid(), Main.getClient_threads(), "1/1/已为您打开空调，设定温度25度");
				}
			}
		}, 0, 5000);
		home.setTimer_smart_condition(mytimer);
		return true;
	}
}
