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
 * ������
 * 
 * @author W_SL
 */
public class Main {

	private int port = 5678; // �˿�
	byte B[] = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f, 0x10 };
	private ExecutorService es = Executors.newFixedThreadPool(50); // �̳߳�
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
	 * �߳���
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
		private int flag = 0; // ��־�������߳�Ϊ1���ͻ����߳�Ϊ2��openwrtΪ3

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
		 * ���������̷߳�������Ϣ
		 * 
		 * @param msgs
		 */
		public void processMsgFromGateway(String[] msgs) {
			try {
				if (msgs[1].equals("1")) { // ��Ӱ����
					BufferedWriter temp = new BufferedWriter(
							new OutputStreamWriter(client_threads.get(new Integer(msgs[2])).out, "UTF-8"));
					temp.write(msgs[3]);
					temp.flush();
					temp.close();
				} else if (msgs[1].equals("2")) { // ��ʪ����Ϣ
					String[] arr = msgs[2].split(";");
					int temp = Integer.parseInt(arr[0]);
					int humi = Integer.parseInt(arr[1]);
					float light = Float.parseFloat(arr[2]);
					myhome.setTemp(temp);
					myhome.setHumi(humi);
					myhome.setLight(light);
					// ���ν�����ʪ����Ϣ�������洢��ʪ����Ϣ��ʱ��
					if (myhome.getTimer_save_thl() == null) {
						myhome.setTimer_save_thl(DatabaseUtilForTimer.Start(homeid, temp, humi, light));
					}
					// �洢��ʱ���ݵ����ݿ�
					DatabaseUtil.saveTempData(homeid, temp, humi, light);
				} else if (msgs[1].equals("3")) { // ��ͥӰԺ
					// ���µ�Ӱ����״̬�����ݿ�
					String msg = msgs[0];
					for (int i = 1; i < msgs.length; i++) {
						msg += "/" + msgs[i];
					}
					DatabaseUtil.updateFilmStatInDB(homeid, msg.substring(4));
				} else if (msgs[1].equals("4")) { // ֪ͨ
					String msg = msgs[0] + "/" + msgs[1] + "/" + msgs[2];
					if (msgs[2].equals("0")) {// ��ȡ��
						DatabaseUtil.saveTempWarnningMsg(homeid, "0");// �浽��ʱ��������ҳ��ʾ
					} else if (msgs[2].equals("1")) {// ��
						DatabaseUtil.saveTempWarnningMsg(homeid, "��ȫ���������ֻ���!");// �浽��ʱ��������ҳ��ʾ
						DatabaseUtil.saveOperationToDb(homeid, "0007", "��ȫ���������ֻ���!");// �浽��ʷ��¼
					} else if (msgs[2].equals("3")) {// ���ģʽ����
						DatabaseUtil.saveTempWarnningMsg(homeid, "��ȫ�������������˴���!");// �浽��ʱ��������ҳ��ʾ
						DatabaseUtil.saveOperationToDb(homeid, "0007", "��ȫ�������������˴���!");// �浽��ʷ��¼
						Timer temp = new Timer();
						temp.schedule(new TimerTask() {
							@Override
							public void run() {
								// 1���Ӻ��Զ�ȡ������
								DatabaseUtil.saveTempWarnningMsg(homeid, "0");
								this.cancel();
							}
						}, 60000, 60000);
					} else if (msgs[2].equals("4")) {// ����ʧ��
						byte[] b = msgs[3].getBytes();
						byte[] bb = new byte[3];
						bb[0] = b[0];
						bb[1] = b[1];
						bb[2] = b[2];
						msg = msgs[0] + "/" + msgs[1] + "/" + myhome.getProtocol_map().get(Util.parseByteToString(bb))
								+ " ����ʧ�ܣ�δ��⵽�豸";
					} else if (msgs[2].equals("5")) {// �д��������͸�������
						// ���͸�������
						MyThread open = openwrt_threads.get(homeid);
						if (null != open) {
							OutputStream out = open.getOut();
							if (null != out) {
								open.getOut().write("2/1".getBytes());
								open.getOut().flush();
							}
						}
						if (myhome.getSmart_mode()) { // ���ж�����ģʽ�Ƿ���
							Timer timer = new Timer();
							timer.schedule(new TimerTask() {
								@Override
								public void run() {
									SendMsg.sendMsg(homeid, client_threads, new String("1/3/�����뵽��"));
									System.out.println("1/3/�����뵽��");
									this.cancel();
								}
							}, 3000, 3000000);
						}
					} else if (msgs[2].equals("6")) {// ����ȡ��
						// do nothing
					}
					SendMsg.sendMsg(homeid, client_threads, msg);
				} else if (msgs[1].equals("5")) {// �������
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
		 * ����ͻ����̷߳�����Ϣ
		 * 
		 * @param msgs
		 */
		public void processMsgFromClient(String[] msgs) {
			try {
				if (msgs[1].equals("2")) { // ���԰����
					SendMsg.sendMsg(homeid, client_threads, new String(msgs[0] + "/" + msgs[1] + "/" + msgs[3]));
				} else if (msgs[1].equals("6")) { // ɾ����ʱ����
					ManageTimer.deleteTimer(homeid, msgs[3].split("&"));
				} else if (msgs[1].equals("10")) { // ��ȡ��Ӱ�б�
					MyThread gateway_thread = gateway_threads.get(homeid);
					if (gateway_thread != null) {
						client_threads.put(hashcode, this);
						flag = 2;
						String from = String.valueOf(MyThread.this.hashcode) + "||/"
								+ msgs[3].replaceAll(";", "/").substring(1);
						byte[] t1 = from.getBytes();
						byte[] t3 = new byte[t1.length + 1];
						if (msgs[3].charAt(0) == '0') {// ��ȡ�б�
							t3[0] = 0x20;
						} else if (msgs[3].charAt(0) == '1') {// ����
							t3[0] = 0x21;
						} else if (msgs[3].charAt(0) == '2') {// ��ͣ
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
				} else if (msgs[1].equals("12")) { // ������Ϣ���ִ�
					if (msgs.length < 4) {
						System.out.println("unrecognition!!!");
						out.writeUTF("1/1/ָ��δʶ��");
						out.flush();
						return;
					}
					byte[] result = Participle.GetResultAndChangedProtocol(msgs[3], "null");
					if (null == result) {
						System.out.println("unrecognition!!!");
						out.writeUTF("1/1/ָ��δʶ��");
						out.flush();
					} else {
						MyThread gateway_thread = gateway_threads.get(homeid);
						if (null != gateway_thread) {
							out.writeUTF("1/1/�����ɹ�");
							out.flush();
							System.out.println("operate succeed");
							// ������Ϣ������
							SendMsg.sendMsg(gateway_thread.getOut(), result);
							// ��������
							String str = Util.parseByteToString(result);
							judge(str, gateway_thread);
							// ����״̬��������ʷ��¼
							changeDeviceState(str);
						} else {
							out.writeUTF("1/1/����δ����");
							out.flush();
							System.out.println("gateway disconnect");
						}
					}
				} else if (msgs[1].equals("13")) { // ������Ϣ���ִ�
					BufferedWriter temp_out = new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
					if (msgs.length < 4) {
						System.out.println("unrecognition!!!");
						temp_out.write("ָ��δʶ��");
						temp_out.flush();
						return;
					}
					byte[] result = Participle.GetResultAndChangedProtocol(Participle.getResult(msgs[3]));
					if (null == result) {
						System.out.println("unrecognition!!!");
						temp_out.write("ָ��δʶ��");
						temp_out.flush();
					} else {
						MyThread gateway_thread = gateway_threads.get(homeid);
						if (null != gateway_thread) {
							temp_out.write("�����ɹ�");
							temp_out.flush();
							// ������Ϣ������
							SendMsg.sendMsg(gateway_thread.getOut(), result);
							// ��������
							String str = Util.parseByteToString(result);
							judge(str, gateway_thread);
							// ����״̬��������ʷ��¼
							changeDeviceState(str);
						} else {
							temp_out.write("����δ����");
							temp_out.flush();
							System.out.println("gateway disconnect");
						}
					}
				} else {// ��Ҫ����
					MyThread gateway = gateway_threads.get(homeid);
					if (null == gateway) {// ����δ���ӣ���ִ��
						System.out.println("failed, gateway disconnect!");
						return;
					} else {// �������ӣ�ִ��
						myhome = gateway.getMyhome();
						if (msgs[1].equals("1")) { // ������Ϣ
							SendMsg.sendMsg(gateway.getOut(), Util.parseStringToByteArray(msgs[3]));
							judge(msgs[3], gateway);
						} else if (msgs[1].equals("3")) { // ����ʶ��
							if (myhome.getSmart_mode()) { // ���ж�ģʽ�Ƿ���
								Timer timer = new Timer();
								timer.schedule(new TimerTask() {
									@Override
									public void run() {
										SendMsg.sendMsg(msgs[2], client_threads,
												new String(msgs[0] + "/" + msgs[1] + "/" + "�����뵽��"));
										System.out.println(msgs[0] + "/" + msgs[1] + "/" + "�����뵽��");
										this.cancel();
										disConnected();
									}
								}, 3000, 3000);
							}
						} else if (msgs[1].equals("5")) { // ��Ӷ�ʱ����
							String[] arr = msgs[3].split("&");
							TimingTask newtimer = new TimingTask(homeid, gateway.getOut(), arr[0], new Integer(arr[1]),
									new Integer(arr[2]), new Integer(arr[3]));
							timingtask.add(newtimer);
							ManageTimer.addTimer(newtimer);
							System.out.println("start timer success");
						} else if (msgs[1].equals("8")) { // �յ��Զ�������ֵ
							String[] ss = msgs[3].split(";");
							myhome.setTemp_max(new Integer(ss[1]));
							myhome.setTemp_min(new Integer(ss[0]));
							System.out.println("set temp success");
						} else if (msgs[1].equals("9")) { // �����Զ�������ֵ
							String[] ss = msgs[3].split(";");
							myhome.setLight_max(new Integer(ss[1]));
							myhome.setLight_min(new Integer(ss[0]));
						} else if (msgs[1].equals("11")) {// ��Ӱ������ز���
							String url = "";
							for (int i = 0; i < msgs.length; i++) {
								url += msgs[i] + "/";
							}
							int pos = msgs[0].length() + msgs[1].length() + msgs[2].length() + 3;
							url = url.substring(pos, url.length() - 1);
							System.out.println(url);
							byte[] b = url.getBytes();
							if (msgs[3].charAt(0) == '1') {// �½�
								DatabaseUtil.insertFilmStatToDB(homeid, url.substring(1));
								b[0] = 0x30;
							} else if (msgs[3].charAt(0) == '2') {// ȡ��
								DatabaseUtil.deleteFilmStatFromDB(homeid, url.substring(1));
								b[0] = 0x31;
							} else if (msgs[3].charAt(0) == '3') {// ��ͣ
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
		 * �Բ�����Ϣ���Ӵ���
		 * 
		 * @param msg
		 */
		public void judge(String msg, MyThread mythread) {
			myhome = mythread.getMyhome();
			if (msg.equals("16;00;00")) {// �رմ���
				myhome.setIs_window_open(false);
			} else if (msg.equals("16;00;01")) {// �򿪴���
				myhome.setIs_window_open(true);
			} else if (msg.equals("05;02;00")) {// �ر����ܴ���
				myhome.getTimer_smart_window().cancel();
				myhome.setTimer_smart_window(null);
			} else if (msg.equals("05;02;01")) {// �����ܴ���
				if (!ManageTimer.startTimerForWindow(mythread)) // ���ܴ�������ʧ��
					System.out.println("smart window statr failed");
			} else if (msg.equals("05;01;00")) {// �ر�����ģʽ
				myhome.setSmart_mode(false);
			} else if (msg.equals("05;01;01")) {// ��������ģʽ
				myhome.setSmart_mode(true);
			} else if (msg.equals("15;04;00")) {// �رտյ�
				myhome.setIs_condition_open(false);
			} else if (msg.equals("15;04;01")) {// �����յ�
				myhome.setIs_condition_open(true);
			}
		}

		/**
		 * �����豸״̬��������ʷ��¼
		 * 
		 * @param result
		 */
		public void changeDeviceState(String result) {
			// �����豸״̬&���浽��ʷ��¼
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
					if (msgs[0].equals("1")) { // �ͻ���
						homeid = msgs[2];
						if (msgs[1].equals("0")) {// �ֻ�����
							flag = 2;
							client_threads.put(hashcode, this);
							System.out.println("phone connected!");
						} else if (msgs[1].equals("14")) { // ������
							flag = 3;
							openwrt_threads.put(homeid, this);
							System.out.println("openwrt connected!");
						} else {// PC�ͻ������ӻ�����
							processMsgFromClient(msgs);
						}
					} else if (msgs[0].equals("0")) {// ����
						if (msgs[1].equals("0")) {
							if (CheckLogin.checkGatewayLogin(msgs)) { // ��֤ͨ��
								out.write(0x41);
								out.flush();
								flag = 1;
								homeid = msgs[2];
								myhome = new Home(homeid);
								myhome.init(out, this);
								gateway_threads.put(homeid, this);
								System.out.println("gateway connected!");
							} else { // ��֤ʧ��
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

		// �Ͽ����ӣ������������
		public void disConnected() {
			// �߳�
			this.isrun = false;
			// �ر�����socket
			try {
				if (in != null)
					in.close();
				if (out != null)
					out.close();
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (flag == 1) { // ���ضϿ�
				// ������ʪ�����ݵ�ʱ��ֹͣ
				if (myhome.getTimer_save_thl() != null)
					myhome.getTimer_save_thl().cancel();
				// �����Զ�����ʱ��ֹͣ
				if (myhome.getTimer_smart_window() != null)
					myhome.getTimer_smart_window().cancel();
				// �յ��Զ�����ʱ��ֹͣ
				if (myhome.getTimer_smart_condition() != null)
					myhome.getTimer_smart_condition().cancel();
				// ��ʱ��������ɾ��
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
			} else if (flag == 2) {// �ͻ����߳�
				client_threads.remove(hashcode);
			} else if (flag == 3) {// �ͻ����߳�
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
