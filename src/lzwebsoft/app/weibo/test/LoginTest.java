package lzwebsoft.app.weibo.test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lzwebsoft.app.weibo.util.Encryptor;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * 测试登录
 * @author zqluo
 *
 */
public class LoginTest {
	public static final String CHECK_VERIFY_URL = "http://check.ptlogin2.qq.com/check?uin={0}&appid=1003903&r={1}";
	public static final String USER_LOGIN_URL = "http://ptlogin2.qq.com/login";
	public static final String CHANNEL_LOGIN_URL = "http://d.web2.qq.com/channel/login2";
	
	public static final String WEIBO_URL = "http://t.qq.com/";
	public static final String MORE_INFO_URL = "http://api1.t.qq.com/asyn/home.php";
	
	public static final String REGXP_CHECK_VERIFY = "ptui_checkVC\\('(.*?)','(.*?)','(.*?)'\\)";
	
	public static void main(String[] args) throws URISyntaxException {
		String accountNumber = "751939573";//"1070772010";
		String accountPassword = "[luo3781110]";//"xu1234";
		
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
				httpClient.execute(httpGet);
				CookieStore cookieStore=((AbstractHttpClient) httpClient).getCookieStore();
				
				httpGet = new HttpGet(WEIBO_URL);
				DefaultHttpClient httpClient2 = new DefaultHttpClient();
				httpClient2.setCookieStore(cookieStore);
				response = httpClient2.execute(httpGet);
				cookieStore = httpClient2.getCookieStore();
				
				context = EntityUtils.toString(response.getEntity());
				
				Document document = Jsoup.parse(context);
				Elements msgBoxs = document.select("#talkList li>div[class=msgBox]");
				for(Element msgBox : msgBoxs) {
					Elements userNames = msgBox.getElementsByClass("userName");
					Elements msgCnts = msgBox.getElementsByClass("msgCnt");
					if(userNames!=null&&userNames.size()>0&&msgCnts!=null&&msgCnts.size()>0) {
						System.out.print(userNames.get(0).getElementsByTag("a").get(0).ownText());
						System.out.print(": ");
						System.out.println(msgCnts.get(0).ownText());
					}
					// 得到转发的内容
					Elements replyBoxs = msgBox.getElementsByClass("replyBox");
					if(replyBoxs!=null&&replyBoxs.size()>0) {
						Elements replyBoxCnts = replyBoxs.get(0).getElementsByClass("msgCnt");
						if(replyBoxCnts!=null&&replyBoxCnts.size()>0) {
							Element replyBoxName = replyBoxCnts.get(0).getElementsByTag("a").get(0);
							System.out.print(replyBoxName.ownText());
							System.out.print(": ");
							System.out.println(replyBoxCnts.get(0).ownText());
						}
					}
					System.out.println("==================================");
				}
				
				// 微博翻页
				Elements lis = document.select("#talkList li:last-child");
				Element last_info = lis.first();
				
				String id = last_info.attr("id");
				String page = last_info.attr("from");
				String time = last_info.attr("rel");
				
				List<NameValuePair> moreParams = new ArrayList<NameValuePair>();
				moreParams.add(new BasicNameValuePair("time", time));
				moreParams.add(new BasicNameValuePair("page", page));
				moreParams.add(new BasicNameValuePair("id", id));
				moreParams.add(new BasicNameValuePair("apiType", "8"));
				moreParams.add(new BasicNameValuePair("apiHost", "http://api.t.qq.com"));
				moreParams.add(new BasicNameValuePair("_r", String.valueOf(new Date().getTime())));
				prams = URLEncodedUtils.format(moreParams, "UTF-8");
				
				//http://api1.t.qq.com/asyn/home.php?&&apiType=8&apiHost=http://api.t.qq.com&_r=1364199050480&_r=1364199050740
				//http://api1.t.qq.com/asyn/home.php?&time=1364171103&page=4&id=234207009838751&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=1364191421224
				//http://api1.t.qq.com/asyn/home.php?&time=1364176024&page=3&id=258252119653735&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=1364186420310
				//http://api1.t.qq.com/asyn/home.php?&time=1364121424&page=9&id=235984052151296&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=1364191855494
				
				//http://api1.t.qq.com/asyn/home.php?&time=1364194623&page=1&id=251950051372155&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=1364198709007
				//http://api1.t.qq.com/asyn/home.php?&time=1364194933&page=2&id=181885090731332&apiType=8&apiHost=http%3A%2F%2Fapi.t.qq.com&_r=1364198749991
				StringBuffer more_info_url = new StringBuffer(MORE_INFO_URL);
				more_info_url.append("?&").append(prams);
				HttpGet moreGet = new HttpGet(more_info_url.toString());
				System.out.println(more_info_url.toString());
				DefaultHttpClient httpClient3 = new DefaultHttpClient();
				moreGet.setHeader("rf", "http://t.qq.com/websoft1");
				httpClient3.setCookieStore(cookieStore);
				response = httpClient3.execute(moreGet);
				
				System.out.println(EntityUtils.toString(response.getEntity()));
			} else {
				// resultCode为其他值，则验证密码
				
			}
			
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
