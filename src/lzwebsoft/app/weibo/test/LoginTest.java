package lzwebsoft.app.weibo.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import lzwebsoft.app.weibo.util.Encryptor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.helper.StringUtil;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 测试抓取微博信息
 * @author zqluo
 *
 */
public class LoginTest {
	public static final String CHECK_VERIFY_URL = "http://check.ptlogin2.qq.com/check?uin={0}&appid=1003903&r={1}";
	public static final String USER_LOGIN_URL = "http://ptlogin2.qq.com/login";
	public static final String VALIDATION_CODE_URL = "http://captcha.qq.com/getimage?aid=1003903&r={0}&uin={1}";
	
	public static final String WEIBO_JSON_URL = "http://api1.t.qq.com/asyn/home.php";
	
	public static final String REGXP_CHECK_VERIFY = "ptui_checkVC\\('(.*?)','(.*?)','(.*?)'\\)";
	public static final String REGXP_CHECK_LOGIN = "ptuiCB\\('(\\d+)','(\\d+)','(.*?)','(\\d+)','(.*?)', '(.*?)'\\)";
	
	// 获取验证码
	public static JFrame validationCode(HttpClient httpClient, HttpGet httpGet, String account) {
		String url = MessageFormat.format(VALIDATION_CODE_URL, Math.random(), account);
		httpGet = new HttpGet(url);
		try {
			// 从远程服务器上得到ImageIcon对象
			HttpResponse reponse = httpClient.execute(httpGet);
			HttpEntity entery = reponse.getEntity();
			InputStream stream = entery.getContent();
			ImageIcon image = new ImageIcon(ImageIO.read(stream));
			
			// 使用窗体显示图片
			JFrame frame = new JFrame();
			frame.setTitle("验证码");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setLocationRelativeTo(null); // 居中对齐
			
			JLabel verifyLabel = new JLabel(image);
			verifyLabel.setSize(image.getIconWidth(), image.getIconHeight());
			verifyLabel.setOpaque(true);
			verifyLabel.setBorder(null);
			frame.add(verifyLabel);
			frame.setSize(200, image.getIconHeight()+50);
			frame.setResizable(false);
			frame.setVisible(true);
			frame.setAlwaysOnTop(true);
			
			return frame;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	// 登录并抓取微博信息
	public static void login(HttpClient httpClient, HttpGet httpGet, long uin, String accountNumber, String accountPassword, String codeNum)
	    throws ClientProtocolException, IOException {
		List<NameValuePair> formparams = new ArrayList<NameValuePair>();
		formparams.add(new BasicNameValuePair("u", accountNumber));
		formparams.add(new BasicNameValuePair("p", Encryptor.encrypt(uin, accountPassword, codeNum)));
		formparams.add(new BasicNameValuePair("verifycode", codeNum));
		formparams.add(new BasicNameValuePair("webqq_type", "10"));
		formparams.add(new BasicNameValuePair("remember_uin","1"));
		formparams.add(new BasicNameValuePair("login2qq", "1"));
		formparams.add(new BasicNameValuePair("aid", "1003903"));
		formparams.add(new BasicNameValuePair("u1", "http://web.qq.com/loginproxy.html?login2qq=1&webqq_type=10"));
		formparams.add(new BasicNameValuePair("h", "1"));
		formparams.add(new BasicNameValuePair("ptredirect", "0"));
		formparams.add(new BasicNameValuePair("ptlang", "2052"));
		formparams.add(new BasicNameValuePair("from_ui", "1"));
		formparams.add(new BasicNameValuePair("pttype", "1"));
		formparams.add(new BasicNameValuePair("dumy", ""));
		formparams.add(new BasicNameValuePair("fp", "loginerroralert"));
		formparams.add(new BasicNameValuePair("action", "2-13-47578"));
		formparams.add(new BasicNameValuePair("mibao_css", "m_webqq"));
		formparams.add(new BasicNameValuePair("t", "1"));
		formparams.add(new BasicNameValuePair("g", "1"));
		
		String prams = URLEncodedUtils.format(formparams, "UTF-8");
		StringBuffer login_url = new StringBuffer(USER_LOGIN_URL);
		login_url.append("?").append(prams);
		httpGet = new HttpGet(login_url.toString());
		httpGet.setHeader("Referer", "http://qzone.qq.com/");
		HttpResponse response = httpClient.execute(httpGet);
		CookieStore cookieStore=((AbstractHttpClient) httpClient).getCookieStore();
		
		String context = EntityUtils.toString(response.getEntity());
		Pattern p = Pattern.compile(REGXP_CHECK_LOGIN);
		Matcher m = p.matcher(context);
		int resultCode = 1;
		String msg = "";
		String userName = "";
		if(m.find()){
			msg = m.group(5);
        	resultCode = Integer.parseInt(m.group(1));
        	userName = m.group(6);
		}
		
		if(resultCode==0) {
			// 登录成功
			System.out.print(userName+"$");
			System.out.println(msg);
			
			// 抓取第一页微博
			//http://api1.t.qq.com/asyn/home.php?&&apiType=8&apiHost=http://api.t.qq.com&_r=1364277155700&_r=1364277161963
			Date now = new Date();
			long _r = now.getTime();
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair("apiType", "8"));
			params.add(new BasicNameValuePair("apiHost", "http://api.t.qq.com"));
			params.add(new BasicNameValuePair("_r", String.valueOf(_r)));
			params.add(new BasicNameValuePair("_r", String.valueOf(_r+6263)));
			
			String paramsStr = URLEncodedUtils.format(params, "UTF-8");
			StringBuffer url = new StringBuffer(WEIBO_JSON_URL);
			url.append("?&&").append(paramsStr);
			
			httpGet = new HttpGet(url.toString());
			httpGet.setHeader("Referer", "http://api1.t.qq.com/proxy.html");
			httpGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:19.0) Gecko/20100101 Firefox/19.0");
			DefaultHttpClient httpClient2 = new DefaultHttpClient();
			httpClient2.setCookieStore(cookieStore);
			response = httpClient2.execute(httpGet);
			cookieStore = httpClient2.getCookieStore();
			
			context = EntityUtils.toString(response.getEntity());
			
			int currentPg = 1;  // 标记当前页数
			String[] nextPgInfo = parserJSON(context);
			
			// 微博翻页
			Scanner in = new Scanner(System.in);
			while(nextPgInfo!=null&&nextPgInfo.length==2) {
				
				System.out.print("继续浏览下一页(Y/N)?: ");
				String order = in.next();
				if(order!=null&&order.trim().equalsIgnoreCase("Y")) {
					currentPg++;
					
					List<NameValuePair> moreParams = new ArrayList<NameValuePair>();
					moreParams.add(new BasicNameValuePair("time", nextPgInfo[0]));
					moreParams.add(new BasicNameValuePair("page", String.valueOf(currentPg)));
					moreParams.add(new BasicNameValuePair("id", nextPgInfo[1]));
					moreParams.add(new BasicNameValuePair("apiType", "8"));
					moreParams.add(new BasicNameValuePair("apiHost", "http://api.t.qq.com"));
					moreParams.add(new BasicNameValuePair("_r", String.valueOf(new Date().getTime())));
					prams = URLEncodedUtils.format(moreParams, "UTF-8");
					
					//http://api1.t.qq.com/asyn/home.php?&time=1364121424&page=9&id=235984052151296&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=1364191855494
					StringBuffer more_info_url = new StringBuffer(WEIBO_JSON_URL);
					more_info_url.append("?&").append(prams);
					HttpGet moreGet = new HttpGet(more_info_url.toString());
					DefaultHttpClient httpClient3 = new DefaultHttpClient();
					moreGet.setHeader("Referer", "http://api1.t.qq.com/proxy.html");
					moreGet.setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; rv:19.0) Gecko/20100101 Firefox/19.0");
					httpClient3.setCookieStore(cookieStore);
					response = httpClient3.execute(moreGet);
					
					context = EntityUtils.toString(response.getEntity());
					
					nextPgInfo = parserJSON(context);
				} else {
					return;
				}
			}
			
		} else {
			System.out.print(userName+"$ ");
			System.out.println(msg);
		}
	}
	
	// 解析微博JSON数据
	public static String[] parserJSON(String context) {
		JsonParser jsonParser = new JsonParser();
		JsonElement jsonElement = jsonParser.parse(context);
		JsonObject jsonObj = jsonElement.getAsJsonObject();
		
		JsonElement resultEmt = jsonObj.get("result");
		int result = resultEmt.getAsInt();
		
		// 请求微博信息成功
		if(result==0) {
			JsonElement infoElm = jsonObj.get("info");
			JsonObject infoObj = infoElm.getAsJsonObject();
			
			// 判断是否还有下一页
			JsonElement hasNextElm = infoObj.get("hasNext");
			int hasNext = hasNextElm.getAsInt();
			
			JsonArray talks = infoObj.getAsJsonArray("talk");
			for(int i=0; i<talks.size(); i++) {
				JsonElement weiboElm = talks.get(i);
				JsonObject weiboObj = weiboElm.getAsJsonObject();
				// 得到昵称
				JsonElement nickElm = weiboObj.get("nick");
				String nick = nickElm.getAsString();
				// 得到备注名称
				JsonElement bknameElm = weiboObj.get("bkname");
				String bkname = bknameElm.getAsString();
				
				// 得到内容
				JsonElement contentElm = weiboObj.get("content");
				String news = contentElm.getAsString().replaceAll("<.*?>", "");
				
				StringBuffer sb = new StringBuffer(nick);
				if(!StringUtil.isBlank(bkname)) {
					sb.append("【").append(bkname).append("】");
				}
				sb.append(": ").append(news);
				
				// 判断是否存在转发内容
				if(weiboObj.has("source")) {
					sb.append("\r\n转自：\r\n");
					
					JsonObject sourceObj = weiboObj.getAsJsonObject("source");
					// 得到昵称
					JsonElement nickElmSrc = sourceObj.get("nick");
					String nickSrc = nickElmSrc.getAsString();
					// 得到备注名称
					JsonElement bknameElmSrc = sourceObj.get("bkname");
					String bknameSrc = bknameElmSrc.getAsString();
					
					// 得到内容
					JsonElement contentElmSrc = sourceObj.get("content");
					String newsSrc = contentElmSrc.getAsString().replaceAll("<.*?>", "");
					
					sb.append(nickSrc);
					if(!StringUtil.isBlank(bknameSrc)) {
						sb.append("【").append(bknameSrc).append("】");
					}
					sb.append(": ").append(newsSrc);
				}
				
				System.out.println(sb.toString());
				System.out.println("=========================================");
				
				// 得到最后一条微博和time和id，用于实现翻页
				if(hasNext==1&&i==talks.size()-1) {
					String[] nextPgInfo = new String[2];
					// 得到time
					JsonElement timeElem = weiboObj.get("timestamp");
					nextPgInfo[0] = timeElem.getAsString();
					// 得到ID
					JsonElement idElem = weiboObj.get("id");
					nextPgInfo[1] = idElem.getAsString();
					
					return nextPgInfo;
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) throws URISyntaxException {
		String accountNumber = "1070772010";
		String accountPassword = "xu1234";
		
		HttpClient httpClient = new DefaultHttpClient();
		String url = MessageFormat.format(CHECK_VERIFY_URL, accountNumber, accountPassword, Math.random());
		HttpGet httpGet = new HttpGet(url);
		
		try {
			HttpResponse response = httpClient.execute(httpGet);
			String context = EntityUtils.toString(response.getEntity());
			
			int resultCode = 1;
			String codeNum = null;
			long uin = 0;
			Pattern p = Pattern.compile(REGXP_CHECK_VERIFY);
			Matcher m = p.matcher(context);
			if(m.find()){
	        	String qqHex = m.group(3);
				qqHex = qqHex.replaceAll("\\\\x", "");
				resultCode = Integer.parseInt(m.group(1));
				codeNum = m.group(2);
				uin = Long.parseLong(qqHex, 16);
			}
			
			if(resultCode==0) {
				// resultCode为0说明不需验证码
				login(httpClient, httpGet, uin, accountNumber, accountPassword, codeNum);
			} else {
				// resultCode为其他值，则验证密码
				System.out.println("需要输入验证码！");
				JFrame frame = validationCode(httpClient, httpGet, accountNumber);
				Scanner in = new Scanner(System.in);
				System.out.print("请输入对话框中的验证码：");
				String verifyCode = in.next();
				if(frame!=null&&frame instanceof JFrame) {
					frame.setVisible(false);
					frame.dispose();
				}
				login(httpClient, httpGet,uin, accountNumber, accountPassword, verifyCode);
			}
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
