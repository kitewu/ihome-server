package utils;

/**
 * 工具类
 * @author WSL
 *
 */
public class Util {
	
	static byte B[] = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f,
			0x10, 0x11, 0x12, 0x13, 0x14, 0x15};
	static String[] STR = {"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",  "15", "16", 
	"17", "18", "19", "20", "21"};
	
	/**
	 * 字符串转字节数组
	 * @param msg
	 * @return
	 */
	public static byte[] parseStringToByteArray(String msg) {
		byte[] a = new byte[3];
		String[] b = msg.split(";");
		for (int i = 0; i < 3; i++) {
			a[i] = B[Integer.parseInt(b[i])];
		}
		return a;
	}
	
	/**
	 * 字节数组转字符串
	 * @param b
	 * @return
	 */
	public static String parseByteToString(byte[] b){
		String result = "";
		result += STR[b[0]];
		for(int i = 1; i < b.length; i++){
			result += ";" + STR[b[i]];
		}
		return result;
	}
}
