package lzwebsoft.app.weibo.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encryptor {
	/**
	 * 对明文密码进行加密，加密码过程：
	 * <code>
	 * I = hexchar2bin(md5(plain));
	 * H = md5(I + uin);
	 * G = md5(H + verify.toUpperCase());
	 * </code>
	 * 得到的G即为加密后的密码
	 * @param uin 请求得到的加密码
	 * @param plain 明文密码
	 * @param verify 验证码
	 * @return 加密后的密码
	 */
	public static String encrypt(long uin, String plain, String verify)
	{
		byte [] data = concat(md5(plain.getBytes()), long2bytes(uin)); 
		String code = byte2HexString(md5(data));
		data = md5((code+verify.toUpperCase()).getBytes());
		return byte2HexString(data);
	}
	
	
	public static byte[] concat(byte[] bytes1, byte[] bytes2){
		byte [] big = new byte[bytes1.length+bytes2.length];
		System.arraycopy(bytes1, 0, big, 0, bytes1.length);
		System.arraycopy(bytes2, 0, big, bytes1.length, bytes2.length);
		return big;
	}
	
	/**
	 * 计算一个字节数组的Md5值
	 * @param bytes
	 * @return
	 */
	private static byte[] md5(byte[] bytes)
	{
		 MessageDigest dist = null;
		 byte[] result  	= null; 
	        try {
		        dist = MessageDigest.getInstance("MD5");
		        result = dist.digest(bytes);
	        } catch (NoSuchAlgorithmException e) {
	        	throw new IllegalArgumentException(e);
	        }
	    	  return result;
	}
	
	/**
	 * 把字节数组转换为16进制表示的字符串
	 * @param b
	 * @return
	 */
  private static String byte2HexString(byte[] b) 
  {   
	   StringBuffer sb = new StringBuffer();
	   char[] hex = new char[] { 
	            '0', '1', '2', '3', '4', '5', '6', '7',
	            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
	    };
   	if(b == null)
   		return "null";
   	
   	int offset = 0;
   	int len    = b.length; 
   	
       // 检查索引范围
       int end = offset + len;
       if(end > b.length)
           end = b.length;
       
       sb.delete(0, sb.length());
       for(int i = offset; i < end; i++) {
             sb.append(hex[(b[i] & 0xF0) >>> 4])
           	 .append(hex[b[i] & 0xF]);
       }
       return sb.toString();
   }
   
   /**
    * 计算GTK(gtk啥东东？)
    * @param skey
    * @return
    */
   public static String gtk(String skey){
	   int hash = 5381;
	   for(int i=0; i<skey.length(); i++){
		   hash += (hash<<5) + skey.charAt(i);
	   }
	   return Integer.toString(hash & 0x7fffffff) ;
   }
   
   /**
    * 把整形数转换为字节数组
    * @param i
    * @return
    */
   public static byte[] long2bytes(long i)
   {
         byte [] b = new byte[8];
         for(int m=0; m<8; m++, i>>=8) {
       	  b[7-m] = (byte) (i & 0x000000FF);	//奇怪, 在C# 整型数是低字节在前  byte[] bytes = BitConverter.GetBytes(i);
       	  									//而在JAVA里，是高字节在前
         }
         return b;
   }
   
   /**
    * 把一个16进制字符串转换为字节数组，字符串没有空格，所以每两个字符
    * 一个字节
    * 
    * @param s
    * @return
    */
   public static byte[] hexString2Byte(String s) {
       int len = s.length();
       byte[] ret = new byte[len >>> 1];
       for(int i = 0; i <= len - 2; i += 2) {
           ret[i >>> 1] = (byte)(Integer.parseInt(s.substring(i, i + 2).trim(), 16) & 0xFF);
       }
       return ret;
   }
}
