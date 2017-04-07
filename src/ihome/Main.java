package ihome;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import beans.Home;
import beans.TimingTask;
import utils.CheckLogin;
import utils.DatabaseUtil;
import utils.DatabaseUtilForTimer;
import utils.Participle;
import utils.SendMsg;
import utils.Util;

/**
 * 主程序
 * 
 * @author W_SL
 */
public class Main {

	private int port = 5678; // 端口
	byte B[] = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 };
	private ExecutorService es = Executors.newFixedThreadPool(50); // 线程池
	private Map<String, MyThread> gateway_threads = new HashMap<String, MyThread>();
	private static Map<Integer, MyThread> client_threads = new HashMap<Integer, MyThread>();

	public static Map<Integer, MyThread> getClient_threads() {
		return client_threads;
	}

	public static void setClient_threads(Map<Integer, MyThread> client_threads) {
		Main.client_threads = client_threads;
	}

	private Map<String, MyThread> openwrt_threads = new HashMap<String, MyThread>();
	private static List<TimingTask> timingtask = new LinkedList<TimingTask>();

	/**
	 * 线程类
	 * 
	 * @author WSL
	 *
	 */
	public class MyThread extends Thread {
		private boolean isrun = true;
		private Home myhome = null;
		private String homeid = null;
		private int hashcode = 0;
		private Socket socket = null;
		private BufferedReader in = null;
		private DataOutputStream out = null;
		private int flag = 0; // 标志，网关线程为1，客户端线程为2，openwrt为3

		public MyThread(Socket socket) {
			this.hashcode = this.hashCode();
			this.socket = socket;
			try {
				in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
				out = new DataOutputStream(socket.getOutputStream());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * 处理网关线程发来的消息
		 * 
		 * @param msgs
		 */
		public void processMsgFromGateway(String[] msgs) {
			try {
				if (msgs[1].equals("1")) { // 电影操作
					BufferedWriter temp = new BufferedWriter(
							new OutputStreamWriter(client_threads.get(new Integer(msgs[2])).out, "UTF-8"));
					temp.write(msgs[3]);
					temp.flush();
					temp.close();
				} else if (msgs[1].equals("2")) { // 温湿度信息
					String[] arr = msgs[2].split(";");
					int temp = Integer.parseInt(arr[0]);
					int humi = Integer.parseInt(arr[1]);
					float light = Float.parseFloat(arr[2]);
					myhome.setTemp(temp);
					myhome.setHumi(humi);
					myhome.setLight(light);
					// 初次接收温湿度信息，开启存储温湿度信息的时钟
					if (myhome.getTimer_save_thl() == null) {
						myhome.setTimer_save_thl(DatabaseUtilForTimer.Start(homeid, temp, humi, light));
					}
					// 存储临时数据到数据库
					DatabaseUtil.saveTempData(homeid, temp, humi, light);
				} else if (msgs[1].equals("3")) { // 家庭影院
					// 更新电影下载状态到数据库
					String msg = msgs[0];
					for (int i = 1; i < msgs.length; i++) {
						msg += "/" + msgs[i];
					}
					DatabaseUtil.updateFilmStatInDB(homeid, msg.substring(4));
				} else if (msgs[1].equals("4")) { // 通知
					String msg = msgs[0] + "/" + msgs[1] + "/" + msgs[2];
					if (msgs[2].equals("0")) {// 火警取消
						DatabaseUtil.saveTempWarnningMsg(homeid, "0");// 存到临时表以在网页显示
					} else if (msgs[2].equals("1")) {// 火警
						DatabaseUtil.saveTempWarnningMsg(homeid, "安全警报，发现火情!");// 存到临时表以在网页显示
						DatabaseUtil.saveOperationToDb(homeid, "0007", "安全警报，发现火情!");// 存到历史记录
					} else if (msgs[2].equals("3")) {// 离家模式报警
						DatabaseUtil.saveTempWarnningMsg(homeid, "安全警报，家中有人闯入!");// 存到临时表以在网页显示
						DatabaseUtil.saveOperationToDb(homeid, "0007", "安全警报，家中有人闯入!");// 存到历史记录
						Timer temp = new Timer();
						temp.schedule(new TimerTask() {
							@Override
							public void run() {
								// 1分钟后自动取消警报
								DatabaseUtil.saveTempWarnningMsg(homeid, "0");
								this.cancel();
							}
						}, 60000, 60000);
					} else if (msgs[2].equals("4")) {// 操作失败
						byte[] b = msgs[3].getBytes();
						byte[] bb = new byte[3];
						bb[0] = b[0];
						bb[1] = b[1];
						bb[2] = b[2];
						msg = msgs[0] + "/" + msgs[1] + "/" + myhome.getProtocol_map().get(Util.parseByteToString(bb))
								+ " 操作失败，未检测到设备";
					} else if (msgs[2].equals("5")) {// 有触摸，发送给开发板
						// 发送给开发板
						MyThread open = openwrt_threads.get(homeid);
						if (null != open) {
							OutputStream out = open.getOut();
							if (null != out) {
								open.getOut().write("2/1".getBytes());
								open.getOut().flush();
							}
						}
						if (myhome.getSmart_mode()) { // 先判断智能模式是否开启
							Timer timer = new Timer();
							timer.schedule(new TimerTask() {
								@Override
								public void run() {
									SendMsg.sendMsg(homeid, client_threads, new String("1/3/吴绍岭到访"));
									System.out.println("1/3/吴绍岭到访");
									this.cancel();
								}
							}, 3000, 3000000);
						}
					} else if (msgs[2].equals("6")) {// 触摸取消
						// do nothing
					}
					SendMsg.sendMsg(homeid, client_threads, msg);
				} else if (msgs[1].equals("5")) {// 下载完成
					String url = msgs[2] + "//";
					for (int i = 4; i < msgs.length - 1; i++) {
						url += msgs[i] + "/";
					}
					url += msgs[msgs.length - 1];
					System.out.println(url);
					DatabaseUtil.deleteFilmStatFromDB(homeid, url);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 处理客户端线程发的消息
		 * 
		 * @param msgs
		 */
		public void processMsgFromClient(String[] msgs) {
			try {
				if (msgs[1].equals("2")) { // 留言板更新
					SendMsg.sendMsg(homeid, client_threads, new String(msgs[0] + "/" + msgs[1] + "/" + msgs[3]));
				} else if (msgs[1].equals("6")) { // 删除定时任务
					ManageTimer.deleteTimer(homeid, msgs[3].split("&"));
				} else if (msgs[1].equals("10")) { // 获取电影列表
					MyThread gateway_thread = gateway_threads.get(homeid);
					if (gateway_thread != null) {
						client_threads.put(hashcode, this);
						flag = 2;
						String from = String.valueOf(MyThread.this.hashcode) + "||/"
								+ msgs[3].replaceAll(";", "/").substring(1);
						byte[] t1 = from.getBytes();
						byte[] t3 = new byte[t1.length + 1];
						if (msgs[3].charAt(0) == '0') {// 获取列表
							t3[0] = 0x20;
						} else if (msgs[3].charAt(0) == '1') {// 播放
							t3[0] = 0x21;
						} else if (msgs[3].charAt(0) == '2') {// 暂停
							t3[0] = 0x22;
						}
						System.arraycopy(t1, 0, t3, 1, t1.length);
						SendMsg.sendMsg(gateway_thread, t3);
						while (true)
							;
					} else {
						BufferedWriter temp = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
						temp.write("3");
						temp.flush();
						temp.close();
					}
				} else if (msgs[1].equals("12")) { // 语音消息，分词
					if (msgs.length < 4) {
						System.out.println("unrecognition!!!");
						out.writeUTF("1/1/指令未识别");
						out.flush();
						return;
					}
					byte[] result = Participle.GetResultAndChangedProtocol(msgs[3], "null");
					if (null == result) {
						System.out.println("unrecognition!!!");
						out.writeUTF("1/1/指令未识别");
						out.flush();
					} else {
						MyThread gateway_thread = gateway_threads.get(homeid);
						if (null != gateway_thread) {
							out.writeUTF("1/1/操作成功");
							out.flush();
							System.out.println("operate succeed");
							// 发送消息到网关
							SendMsg.sendMsg(gateway_thread.getOut(), result);
							// 后续处理
							String str = Util.parseByteToString(result);
							judge(str, gateway_thread);
							// 更改状态及存入历史纪录
							changeDeviceState(str);
						} else {
							out.writeUTF("1/1/网关未连接");
							out.flush();
							System.out.println("gateway disconnect");
						}
					}
				} else if (msgs[1].equals("13")) { // 语音消息，分词
					BufferedWriter temp_out = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
					if (msgs.length < 4) {
						System.out.println("unrecognition!!!");
						temp_out.write("指令未识别");
						temp_out.flush();
						return;
					}
					byte[] result = Participle.GetResultAndChangedProtocol(Participle.getResult(msgs[3]));
					if (null == result) {
						System.out.println("unrecognition!!!");
						temp_out.write("指令未识别");
						temp_out.flush();
					} else {
						MyThread gateway_thread = gateway_threads.get(homeid);
						if (null != gateway_thread) {
							temp_out.write("操作成功");
							temp_out.flush();
							// 发送消息到网关
							SendMsg.sendMsg(gateway_thread.getOut(), result);
							// 后续处理
							String str = Util.parseByteToString(result);
							judge(str, gateway_thread);
							// 更改状态及存入历史纪录
							changeDeviceState(str);
						} else {
							temp_out.write("网关未连接");
							temp_out.flush();
							System.out.println("gateway disconnect");
						}
					}
				} else {// 需要网关
					MyThread gateway = gateway_threads.get(homeid);
					if (null == gateway) {// 网关未连接，不执行
						System.out.println("failed, gateway disconnect!");
						return;
					} else {// 网关连接，执行
						myhome = gateway.getMyhome();
						if (msgs[1].equals("1")) { // 操作消息
							SendMsg.sendMsg(gateway.getOut(), Util.parseStringToByteArray(msgs[3]));
							judge(msgs[3], gateway);
						} else if (msgs[1].equals("3")) { // 人脸识别
							if (myhome.getSmart_mode()) { // 先判断模式是否开启
								Timer timer = new Timer();
								timer.schedule(new TimerTask() {
									@Override
									public void run() {
										SendMsg.sendMsg(msgs[2], client_threads,
												new String(msgs[0] + "/" + msgs[1] + "/" + "吴绍岭到访"));
										System.out.println(msgs[0] + "/" + msgs[1] + "/" + "吴绍岭到访");
										this.cancel();
										disConnected();
									}
								}, 3000, 3000);
							}
						} else if (msgs[1].equals("5")) { // 添加定时任务
							String[] arr = msgs[3].split("&");
							TimingTask newtimer = new TimingTask(homeid, gateway.getOut(), arr[0], new Integer(arr[1]),
									new Integer(arr[2]), new Integer(arr[3]));
							timingtask.add(newtimer);
							ManageTimer.addTimer(newtimer);
							System.out.println("start timer success");
						} else if (msgs[1].equals("8")) { // 空调自动控制阈值
							String[] ss = msgs[3].split(";");
							myhome.setTemp_max(new Integer(ss[1]));
							myhome.setTemp_min(new Integer(ss[0]));
							System.out.println("set temp success");
						} else if (msgs[1].equals("9")) { // 窗帘自动控制阈值
							String[] ss = msgs[3].split(";");
							myhome.setLight_max(new Integer(ss[1]));
							myhome.setLight_min(new Integer(ss[0]));
						} else if (msgs[1].equals("11")) {// 电影下载相关操作
							String url = "";
							for (int i = 0; i < msgs.length; i++) {
								url += msgs[i] + "/";
							}
							int pos = msgs[0].length() + msgs[1].length() + msgs[2].length() + 3;
							url = url.substring(pos, url.length() - 1);
							System.out.println(url);
							byte[] b = url.getBytes();
							if (msgs[3].charAt(0) == '1') {// 新建
								DatabaseUtil.insertFilmStatToDB(homeid, url.substring(1));
								b[0] = 0x30;
							} else if (msgs[3].charAt(0) == '2') {// 取消
								DatabaseUtil.deleteFilmStatFromDB(homeid, url.substring(1));
								b[0] = 0x31;
							} else if (msgs[3].charAt(0) == '3') {// 暂停
								b[0] = 0x32;
							}
							SendMsg.sendMsg(gateway.getOut(), b);
						} 
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/**
		 * 对操作消息附加处理
		 * 
		 * @param msg
		 */
		public void judge(String msg, MyThread mythread) {
			myhome = mythread.getMyhome();
			if (msg.equals("16;00;00")) {// 关闭窗帘
				myhome.setIs_window_open(false);
			} else if (msg.equals("16;00;01")) {// 打开窗帘
				myhome.setIs_window_open(true);
			} else if (msg.equals("05;02;00")) {// 关闭智能窗帘
				myhome.getTimer_smart_window().cancel();
				myhome.setTimer_smart_window(null);
			} else if (msg.equals("05;02;01")) {// 打开智能窗帘
				if (!ManageTimer.startTimerForWindow(mythread)) // 智能窗帘开启失败
					System.out.println("smart window statr failed");
			} else if (msg.equals("05;01;00")) {// 关闭智能模式
				myhome.setSmart_mode(false);
			} else if (msg.equals("05;01;01")) {// 开启智能模式
				myhome.setSmart_mode(true);
			} else if (msg.equals("15;04;00")) {// 关闭空调
				myhome.setIs_condition_open(false);
			} else if (msg.equals("15;04;01")) {// 开启空调
				myhome.setIs_condition_open(true);
			}
		}

		/**
		 * 更改设备状态并存入历史纪录
		 * 
		 * @param result
		 */
		public void changeDeviceState(String result) {
			// 更改设备状态&保存到历史纪录
			String[] temp = DatabaseUtil.getTypeAndOperation(homeid, result);
			if (null != temp) {
				int state = 0;
				if (result.charAt(7) == '1')
					state = 1;
				DatabaseUtil.updateDeviceState(homeid, temp[1], state);
				DatabaseUtil.saveOperationToDb(homeid, temp[1], temp[0]);
			} else {
				System.out.println("update device state failed");
			}
		}

		@Override
		public void run() {
			while (isrun) {
				try {
					char[] b = new char[1024];
					int len = in.read(b);
					String str = new String(b, 0, len);
					System.out.println(str);
					String[] msgs = str.split("/");
					if (msgs[0].equals("1")) { // 客户端
						homeid = msgs[2];
						if (msgs[1].equals("0")) {// 手机连接
							flag = 2;
							client_threads.put(hashcode, this);
							System.out.println("phone connected!");
						} else if (msgs[1].equals("14")) { // 开发板
							flag = 3;
							openwrt_threads.put(homeid, this);
							System.out.println("openwrt connected!");
						} else {// PC客户端连接或其他
							processMsgFromClient(msgs);
						}
					} else if (msgs[0].equals("0")) {// 网关
						if (msgs[1].equals("0")) {
							if (CheckLogin.checkGatewayLogin(msgs)) { // 验证通过
								out.write(0x41);
								out.flush();
								flag = 1;
								homeid = msgs[2];
								myhome = new Home(homeid);
								myhome.init(out, this);
								gateway_threads.put(homeid, this);
								System.out.println("gateway connected!");
							} else { // 验证失败
								out.write(0x40);
								out.flush();
								disConnected();
							}
						} else {
							processMsgFromGateway(msgs);
						}
					}
				} catch (Exception e) {
					disConnected();
				}
			}
			System.out.println("disconnected!");
		}

		// 断开连接，处理后续工作
		public void disConnected() {
			// 线程
			this.isrun = false;
			// 关闭流及socket
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (flag == 1) { // 网关断开
				// 保存温湿度数据的时钟停止
				if (myhome.getTimer_save_thl() != null)
					myhome.getTimer_save_thl().cancel();
				// 窗帘自动控制时钟停止
				if (myhome.getTimer_smart_window() != null)
					myhome.getTimer_smart_window().cancel();
				// 空调自动控制时钟停止
				if (myhome.getTimer_smart_condition() != null)
					myhome.getTimer_smart_condition().cancel();
				// 定时任务链表删除
				Iterator<TimingTask> it = timingtask.iterator();
				while (it.hasNext()) {
					TimingTask temp = it.next();
					if (temp.getHomeid().equals(homeid)) {
						temp.getTimer().cancel();
						it.remove();
					}
				}
				myhome.getProtocol_map().clear();
				gateway_threads.remove(homeid);
			} else if (flag == 2) {// 客户端线程
				client_threads.remove(hashcode);
			} else if (flag == 3) {// 客户端线程
				openwrt_threads.remove(hashcode);
			}
		}

		public DataOutputStream getOut() {
			return out;
		}

		public void setOut(DataOutputStream out) {
			this.out = out;
		}

		public String getHomeid() {
			return homeid;
		}

		public void setHomeid(String homeid) {
			this.homeid = homeid;
		}

		public int getFlag() {
			return flag;
		}

		public void setFlag(int flag) {
			this.flag = flag;
		}

		public Home getMyhome() {
			return myhome;
		}

		public void setMyhome(Home myhome) {
			this.myhome = myhome;
		}
	}

	public void Start() {
		System.out.println("Server started");
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			while (true) {
				Socket socket = ss.accept();
				es.execute(new MyThread(socket));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static List<TimingTask> getTimingtask() {
		return timingtask;
	}

	public static void main(String[] args) {
		if (InitServer.init())
			new Main().Start();
		else {
			System.out.println("Init failed, exit");
			System.exit(0);
		}
	}
}
