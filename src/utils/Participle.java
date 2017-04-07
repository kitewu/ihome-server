package utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * 获得分词后的结果
 * 
 * @author WSL
 *
 */
public class Participle {

	private static String libpath = "NLPIR";// 对应路径为linux-x86-64/libNLPIR.so
	private static String rootpath = "NLPIR";

	public interface CLibrary extends Library {
		CLibrary Instance = (CLibrary) Native.loadLibrary(libpath, CLibrary.class);

		public int NLPIR_Init(String sDataPath, int encoding, String sLicenceCode);

		public int NLPIR_ImportUserDict(String path);

		public String NLPIR_GetKeyWords(String sLine, int nMaxKeyLimit, boolean bWeightOut);

		public String NLPIR_GetLastErrorMsg();

		public void NLPIR_Exit();
	}

	/**
	 * 初始化
	 * 
	 * @return
	 */
	public static boolean init() {
		int charset_type = 1;
		if (0 == CLibrary.Instance.NLPIR_Init(rootpath, charset_type, "0")) {
			System.err.println("Participle init failed, reason is " + CLibrary.Instance.NLPIR_GetLastErrorMsg());
			return false;
		}
		if (CLibrary.Instance.NLPIR_ImportUserDict(rootpath + "/mykey.txt") == 0)
			System.out.println("add mykeys failed");
		return true;
	}

	/**
	 * 获取分词结果
	 * 
	 * @param sentence
	 * @return
	 */
	public static String getResult(String sentence) {
		try {
			return CLibrary.Instance.NLPIR_GetKeyWords(sentence, 10, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取分词结果并完成协议转换
	 * 
	 * @param sentence
	 * @param pos
	 * @return
	 */
	public static byte[] GetResultAndChangedProtocol(String sentence, String pos) {
		try {
			System.out.println(sentence + "==" + pos);
			
			String[] strs = CLibrary.Instance.NLPIR_GetKeyWords(sentence, 10, false).split("#");
			
			for (int i = 0; i < strs.length; i++) {
				System.out.println(strs[i].trim());
			}
			System.out.println("===============");
			
			int state = 0;// 标识开关
			String device = null;// 标识设备
			boolean lock = false;
			String mode = null;

			for (int i = 0; i < strs.length; i++) {
				String temp = strs[i].trim();
				if (temp.equals("打开") || temp.equals("开") || temp.equals("开开") || temp.equals("开启")
						|| temp.equals("启动")) {
					state = 1;
				} else if (temp.equals("关") || temp.equals("关闭") || temp.equals("关掉") || temp.equals("关上")
						|| temp.equals("停止")) {
					state = 2;
				} else if (temp.equals("客厅")) {
					pos = "客厅";
				} else if (temp.equals("卧室")) {
					pos = "卧室";
				} else if (temp.equals("厨房")) {
					pos = "厨房";
				} else if (temp.equals("洗手间")) {
					pos = "洗手间";
				} else if (temp.equals("风扇")) {
					device = "风扇";
				} else if (temp.equals("调光灯")) {
					device = "调光灯";
				} else if (temp.equals("空调")) {
					device = "空调";
				} else if (temp.equals("灯")) {
					device = "灯";
				} else if (temp.equals("窗帘")) {
					device = "窗帘";
				} else if (temp.equals("智能模式")) {
					mode = "智能模式";
				} else if (temp.equals("离家模式")) {
					mode = "离家模式";
				} else if (temp.equals("智能窗帘")) {
					mode = "智能窗帘";
				} else if (temp.equals("门锁")) {
					lock = true;
				} else if (temp.equals("锁")) {
					lock = true;
				}
			}
			byte[] result = new byte[3];
			if (lock) {// 门锁
				if (state == 1) {
					result[0] = 0x15;
					result[1] = 0x00;
					result[2] = 0x01;
					return result;
				} else if (state == 2) {
					result[0] = 0x15;
					result[1] = 0x00;
					result[2] = 0x00;
					return result;
				}
			}
			if (mode != null) {// 模式
				if (state == 0)
					return null;
				result[2] = 0x00;
				if (state == 1)
					result[2] = 0x01;
				if (mode.equals("离家模式")) {
					result[0] = 0x05;
					result[1] = 0x00;
				} else if (mode.equals("智能模式")) {
					result[0] = 0x05;
					result[1] = 0x01;
				} else if (mode.equals("智能窗帘")) {
					result[0] = 0x05;
					result[1] = 0x02;
				}
				return result;
			}
			// 设备
			if (device == null || state == 0 || pos == "null")
				return null;
			result[2] = 0x00;
			if(state == 1)
				result[2] = 0x01;
			if (pos.equals("客厅") && device.equals("灯")) {
				result[0] = 0x0a;
				result[1] = 0x01;
			} else if (pos.equals("客厅") && device.equals("空调")) {
				result[0] = 0x0f;
				result[1] = 0x04;
			} else if (pos.equals("卧室") && device.equals("调光灯")) {
				result[0] = 0x08;
				result[1] = 0x00;
			} else if (pos.equals("卧室") && device.equals("风扇")) {
				result[0] = 0x0a;
				result[1] = 0x02;
			} else if (pos.equals("卧室") && device.equals("窗帘")) {
				result[0] = 0x10;
				result[1] = 0x00;
			} else if (pos.equals("厨房") && device.equals("灯")) {
				result[0] = 0x0a;
				result[1] = 0x04;
			} else if (pos.equals("洗手间") && device.equals("灯")) {
				result[0] = 0x0a;
				result[1] = 0x03;
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 获取分词结果并完成协议转换
	 * 
	 * @param strs
	 * @return
	 */
	public static byte[] GetResultAndChangedProtocol(String sentence) {
		try {
			String[] strs = CLibrary.Instance.NLPIR_GetKeyWords(sentence, 10, false).split("#");
			boolean flag1 = false;// 标识开关
			boolean flag2 = false;// 标识设备
			byte[] result = new byte[3];
			for (int i = 0; i < strs.length; i++) {
				String temp = strs[i].trim();
				if (temp.equals("打开") || temp.equals("开") || temp.equals("开开") || temp.equals("开启")
						|| temp.equals("启动")) {
					flag1 = true;
					result[2] = 0x01;
				} else if (temp.equals("关") || temp.equals("关闭") || temp.equals("关掉") || temp.equals("关上")
						|| temp.equals("停止")) {
					flag1 = true;
					result[2] = 0x00;
				} else if (temp.equals("客厅灯")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x01;
				} else if (temp.equals("客厅空调")) {
					flag2 = true;
					result[0] = 0x0f;
					result[1] = 0x04;
				} else if (temp.equals("厨房灯")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x04;
				} else if (temp.equals("洗手间灯")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x03;
				} else if (temp.equals("卧室风扇")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x02;
				} else if (temp.equals("卧室调光灯")) {
					flag2 = true;
					result[0] = 0x08;
					result[1] = 0x00;
				} else if (temp.equals("卧室窗帘")) {
					flag2 = true;
					result[0] = 0x10;
					result[1] = 0x00;
				} else if (temp.equals("智能模式")) {
					flag2 = true;
					result[0] = 0x05;
					result[1] = 0x01;
				} else if (temp.equals("离家模式")) {
					flag2 = true;
					result[0] = 0x05;
					result[1] = 0x00;
				} else if (temp.equals("智能窗帘")) {
					flag2 = true;
					result[0] = 0x05;
					result[1] = 0x02;
				} else if (temp.equals("门锁")) {
					flag2 = true;
					result[0] = 0x15;
					result[1] = 0x00;
				} else if (temp.equals("锁")) {
					flag2 = true;
					result[0] = 0x15;
					result[1] = 0x00;
				}
			}
			if (flag1 && flag2) {
				if (result[0] == 0x15 && result[1] == 0x00 && result[2] == 0x00)// 关闭门锁，不操作
					return null;
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
