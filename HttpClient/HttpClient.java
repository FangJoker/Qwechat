package HttpClient;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.ws.rs.ClientErrorException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.logging.Logger;

public class HttpClient {

	private static Logger logger = Logger.getLogger(HttpClient.class);
	 public static CloseableHttpClient httpClient = HttpClientBuilder.create().build();




	/**
	 * get 请求
	 * 
	 * @param String url
	 * @return String
	 */

	public String doGet(String url) {
		// 创建HttpClient 实例
		CloseableHttpClient client = HttpClients.createDefault();
		logger.info("doget");
		try {
			// 2. 创建一个 get 对象
			HttpGet request = new HttpGet(url);
			// 执行GET 获取响应
			HttpResponse response = client.execute(request);
			try {
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					// 读取服务器返回过来的json字符串数据
					String result = EntityUtils.toString(response.getEntity(),HTTP.UTF_8);	
					// 关闭client
					client.close();
					return result;
				}
			} catch (IOException e) {
				e.printStackTrace();
				logger.info("获取响应json失败");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			logger.info("Http Client 创建错误");

		} catch (Exception e) {
			e.printStackTrace();
			logger.info("Http Client 创建错误");
		}
		return null;
	}
	

	/**
	 * post请求
	 * 
	 * @param String url
	 * @param String params ,String xml 
	 * @return String 
	 */

	public String doPost(String url, Map params, String xml) {
		
		CloseableHttpClient client = HttpClients.createDefault();
		
		try {	
			// 创建HttpPost 实例
			HttpPost postRequest = new HttpPost(url);
			// post参数
			ArrayList<NameValuePair> nvp = new ArrayList<NameValuePair>();
			
			if(xml == null && params!=null){ //传入的是map
				logger.info("传入的是map");
				for (Iterator iter = params.keySet().iterator(); iter.hasNext();) {
					String name = iter.next().toString();
					String value = params.get(name).toString();
					nvp.add(new BasicNameValuePair(name, value));
				}
				// 设置请求实体 完成传参 输入数据为UTF-8编码后的带post参数的URL
				postRequest.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));

			}else{//传入的是 xml
				logger.info("传入的是xml");
				//实例化字符串xml
				StringEntity xmlEntity =  new StringEntity(xml,"UTF-8");
				xmlEntity.setContentEncoding("UTF-8");  

				//设置请求头
				postRequest.setHeader("Content-Type","text/xml"); 
				//设置请求实体
				postRequest.setEntity(xmlEntity);
				logger.info("发送的xml"+xml);
		    }
				// 执行 发送请求
				HttpResponse response = client.execute(postRequest);	
				// 获取响应实体
				HttpEntity respEntity = response.getEntity();
				if (respEntity != null) {
					String result = EntityUtils.toString(response.getEntity(),"UTF-8");
					logger.info("请求响应:"+result);
					client.close(); // 关闭资源
					return result;
				}

		}catch (ClientErrorException e){
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		catch(Exception e){
			e.printStackTrace();
		}finally{
			try{
				client.close(); //关闭资源
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		return null;
	}
	


}
