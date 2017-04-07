package utils;

import java.io.DataOutputStream;
import java.io.OutputStream;
import java.util.Map;

import ihome.Main.MyThread;

/**
 * 发送消息
 * 
 * @author W_SL
 *
 */
public class SendMsg {

	/**
	 * 发送消息到网关
	 * 
	 * @param mythread
	 * @param msg
	 */
	public static void sendMsg(MyThread mythread, String msg) {
		try {
			if (null != mythread) {
				OutputStream out = mythread.getOut();
				if (null != out) {
					out.write(Util.parseStringToByteArray(msg));
					out.flush();
					System.out.println("send msg " + msg + " to gateway success");
					return;
				} else {
					System.out.println("send msg to gateway failed, outputstream is null");
				}
			}
			System.out.println("send msg " + msg + " to gateway failed, gateway disconnected");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息到网关
	 * 
	 * @param mythread
	 * @param b
	 */
	public static void sendMsg(MyThread mythread, byte[] b) {
		try {
			if (null != mythread) {
				OutputStream out = mythread.getOut();
				if (null != out) {
					out.write(b);
					out.flush();
					System.out.println("send msg to gateway success");
					return;
				} else {
					System.out.println("send msg to gateway failed, outputstream is null");
				}
			}
			System.out.println("send msg to gateway failed, gateway disconnected");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息到网关
	 * 
	 * @param out
	 * @param msg
	 */
	public static void sendMsg(OutputStream out, String msg) {
		try {
			if (out != null) {
				out.write(Util.parseStringToByteArray(msg));
				out.flush();
				System.out.println("send msg to gateway success");
			} else {
				System.out.println("send msg to gateway failed, outputstream is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息到网关
	 * 
	 * @param out
	 * @param b
	 */
	public static void sendMsg(OutputStream out, byte[] b) {
		try {
			if (out != null) {
				out.write(b);
				out.flush();
				System.out.println("send msg to gateway success");
			} else {
				System.out.println("send msg to gateway failed, outputstream is null");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 发送消息到手机客户端
	 * 
	 * @param homeid
	 * @param client_threads
	 * @param msg
	 */
	public static void sendMsg(String homeid, Map<Integer, MyThread> client_threads, String msg) {
		int i = 0;
		for (Map.Entry<Integer, MyThread> entry : client_threads.entrySet()) {
			try {
				MyThread mythread = entry.getValue();
				DataOutputStream out = mythread.getOut();
				if (null != out) {
					if (mythread.getHomeid().equals(homeid) && mythread.getFlag() == 2) {
						out.writeUTF(msg);
						out.flush();
						i++;
					}
				} else {
					System.out.println("sendmsg to client failed, outputstream is null");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(i + " client send success");
	}
}
