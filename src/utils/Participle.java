package utils;

import com.sun.jna.Library;
import com.sun.jna.Native;

/**
 * ��÷ִʺ�Ľ��
 * 
 * @author WSL
 *
 */
public class Participle {

	private static String libpath = "NLPIR";// ��Ӧ·��Ϊlinux-x86-64/libNLPIR.so
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
	 * ��ʼ��
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
	 * ��ȡ�ִʽ��
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
	 * ��ȡ�ִʽ�������Э��ת��
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
			
			int state = 0;// ��ʶ����
			String device = null;// ��ʶ�豸
			boolean lock = false;
			String mode = null;

			for (int i = 0; i < strs.length; i++) {
				String temp = strs[i].trim();
				if (temp.equals("��") || temp.equals("��") || temp.equals("����") || temp.equals("����")
						|| temp.equals("����")) {
					state = 1;
				} else if (temp.equals("��") || temp.equals("�ر�") || temp.equals("�ص�") || temp.equals("����")
						|| temp.equals("ֹͣ")) {
					state = 2;
				} else if (temp.equals("����")) {
					pos = "����";
				} else if (temp.equals("����")) {
					pos = "����";
				} else if (temp.equals("����")) {
					pos = "����";
				} else if (temp.equals("ϴ�ּ�")) {
					pos = "ϴ�ּ�";
				} else if (temp.equals("����")) {
					device = "����";
				} else if (temp.equals("�����")) {
					device = "�����";
				} else if (temp.equals("�յ�")) {
					device = "�յ�";
				} else if (temp.equals("��")) {
					device = "��";
				} else if (temp.equals("����")) {
					device = "����";
				} else if (temp.equals("����ģʽ")) {
					mode = "����ģʽ";
				} else if (temp.equals("���ģʽ")) {
					mode = "���ģʽ";
				} else if (temp.equals("���ܴ���")) {
					mode = "���ܴ���";
				} else if (temp.equals("����")) {
					lock = true;
				} else if (temp.equals("��")) {
					lock = true;
				}
			}
			byte[] result = new byte[3];
			if (lock) {// ����
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
			if (mode != null) {// ģʽ
				if (state == 0)
					return null;
				result[2] = 0x00;
				if (state == 1)
					result[2] = 0x01;
				if (mode.equals("���ģʽ")) {
					result[0] = 0x05;
					result[1] = 0x00;
				} else if (mode.equals("����ģʽ")) {
					result[0] = 0x05;
					result[1] = 0x01;
				} else if (mode.equals("���ܴ���")) {
					result[0] = 0x05;
					result[1] = 0x02;
				}
				return result;
			}
			// �豸
			if (device == null || state == 0 || pos == "null")
				return null;
			result[2] = 0x00;
			if(state == 1)
				result[2] = 0x01;
			if (pos.equals("����") && device.equals("��")) {
				result[0] = 0x0a;
				result[1] = 0x01;
			} else if (pos.equals("����") && device.equals("�յ�")) {
				result[0] = 0x0f;
				result[1] = 0x04;
			} else if (pos.equals("����") && device.equals("�����")) {
				result[0] = 0x08;
				result[1] = 0x00;
			} else if (pos.equals("����") && device.equals("����")) {
				result[0] = 0x0a;
				result[1] = 0x02;
			} else if (pos.equals("����") && device.equals("����")) {
				result[0] = 0x10;
				result[1] = 0x00;
			} else if (pos.equals("����") && device.equals("��")) {
				result[0] = 0x0a;
				result[1] = 0x04;
			} else if (pos.equals("ϴ�ּ�") && device.equals("��")) {
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
	 * ��ȡ�ִʽ�������Э��ת��
	 * 
	 * @param strs
	 * @return
	 */
	public static byte[] GetResultAndChangedProtocol(String sentence) {
		try {
			String[] strs = CLibrary.Instance.NLPIR_GetKeyWords(sentence, 10, false).split("#");
			boolean flag1 = false;// ��ʶ����
			boolean flag2 = false;// ��ʶ�豸
			byte[] result = new byte[3];
			for (int i = 0; i < strs.length; i++) {
				String temp = strs[i].trim();
				if (temp.equals("��") || temp.equals("��") || temp.equals("����") || temp.equals("����")
						|| temp.equals("����")) {
					flag1 = true;
					result[2] = 0x01;
				} else if (temp.equals("��") || temp.equals("�ر�") || temp.equals("�ص�") || temp.equals("����")
						|| temp.equals("ֹͣ")) {
					flag1 = true;
					result[2] = 0x00;
				} else if (temp.equals("������")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x01;
				} else if (temp.equals("�����յ�")) {
					flag2 = true;
					result[0] = 0x0f;
					result[1] = 0x04;
				} else if (temp.equals("������")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x04;
				} else if (temp.equals("ϴ�ּ��")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x03;
				} else if (temp.equals("���ҷ���")) {
					flag2 = true;
					result[0] = 0x0a;
					result[1] = 0x02;
				} else if (temp.equals("���ҵ����")) {
					flag2 = true;
					result[0] = 0x08;
					result[1] = 0x00;
				} else if (temp.equals("���Ҵ���")) {
					flag2 = true;
					result[0] = 0x10;
					result[1] = 0x00;
				} else if (temp.equals("����ģʽ")) {
					flag2 = true;
					result[0] = 0x05;
					result[1] = 0x01;
				} else if (temp.equals("���ģʽ")) {
					flag2 = true;
					result[0] = 0x05;
					result[1] = 0x00;
				} else if (temp.equals("���ܴ���")) {
					flag2 = true;
					result[0] = 0x05;
					result[1] = 0x02;
				} else if (temp.equals("����")) {
					flag2 = true;
					result[0] = 0x15;
					result[1] = 0x00;
				} else if (temp.equals("��")) {
					flag2 = true;
					result[0] = 0x15;
					result[1] = 0x00;
				}
			}
			if (flag1 && flag2) {
				if (result[0] == 0x15 && result[1] == 0x00 && result[2] == 0x00)// �ر�������������
					return null;
				return result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
