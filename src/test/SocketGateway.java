package test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * 测试用，模拟PC客户端
 * 
 * @author WSL
 *
 */
public class SocketGateway {

	private static Socket socket;
	private static BufferedReader in;
	private static BufferedWriter out;

	public static void main(String[] args) {
		try {
			socket = new Socket("115.159.127.79", 5678);

			// 发送消息
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"));
						out.write("0/0/ihome_001/123");
						out.flush();
						Scanner sc = new Scanner(System.in);
						while (sc.hasNext()) {
							String str = sc.next();
							out.write(str);
							out.flush();
							System.out.println(true);
						}
						sc.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}).start();

			// 接收消息
			in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
			while (true) {
				char[] b = new char[1024];
				int len = in.read(b);
				System.out.println(new String(b, 2, len));
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
