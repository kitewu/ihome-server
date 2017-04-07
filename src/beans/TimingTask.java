package beans;

import java.io.OutputStream;
import java.util.Timer;

/**
 * 定时任务类
 * 
 * @author W_SL
 */

public class TimingTask {

	private int hour;
	private int minute;
	private int frequency;
	private String msg;
	private Timer timer = null;
	private int hashcode = 0;
	private OutputStream out = null;
	private String homeid = null;

	public String getHomeid() {
		return homeid;
	}

	public void setHomeid(String homeid) {
		this.homeid = homeid;
	}

	public OutputStream getOut() {
		return out;
	}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public int getHashcode() {
		return hashcode;
	}

	public void setHashcode(int hashcode) {
		this.hashcode = hashcode;
	}

	public Timer getTimer() {
		return timer;
	}

	public void setTimer(Timer timer) {
		this.timer = timer;
	}

	public TimingTask(String homeid, OutputStream out, String msg, int frequency, int hour, int minute) {
		this.homeid = homeid;
		this.out = out;
		this.hour = hour;
		this.frequency = frequency;
		this.minute = minute;
		this.msg = msg;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
