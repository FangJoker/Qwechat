package javaWechat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.cn.domain.AccessToken;


import HttpClient.HttpClient;

/*单例模式*/
public class SingleAccessToken {
	
	public  HttpClient hc =null;
	public static SingleAccessToken  singleAccessToken = null;
	private String appid;
	private String secret;
	private Properties prop = null; 
	private static Logger logger = Logger.getLogger(SingleAccessToken.class);
	private AccessToken accessToken = null;
	
	private SingleAccessToken(){
		accessToken = getToken(); 
		initThread();
	}
	
	public AccessToken getAccessToken(){
		return accessToken;
	}
	
	/*
	 * 返回 singleAccessToken 实例
	 * 需要在第一次使用时生成实例，所以为了线程安全，使用synchronized关键字来确保只会生成单例
	 * */
	public static synchronized SingleAccessToken getInstance(){
		
		LocalDate day = LocalDate.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		if(singleAccessToken == null){
			logger.info("实例过期重新实例化中:"+day.format(dateTimeFormatter));
			singleAccessToken = new SingleAccessToken();
		}
		logger.info("返回实例:"+day.format(dateTimeFormatter));
		   return singleAccessToken;
	}
	
    public void setAccessToken(AccessToken accessToken){
    	this.accessToken =accessToken;
    }
    
     public AccessToken getToken() {
		
		LocalDate day = LocalDate.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

		init i = new init();
		//获取公众号相关信息
		this.appid = i.getAppId();
		this.secret= i.getAppSecret();
		//请求acess_token
		hc = new HttpClient();
		String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid="+this.appid+"&secret="+this.secret;
		String jsonObject = hc.doGet(url);
		//解析返回的json
		JSONObject result =  JSONObject.parseObject(jsonObject);
		//实例化 AccessToken 类
		accessToken = new AccessToken();
		accessToken.setToken(result.getString("access_token"));
		accessToken.setExpiresIn(result.getIntValue("expires_in"));
		logger.info("返回响应:"+JSON.toJSONString(result));
		logger.info("请求accessToken:"+accessToken.getToken()+"时间:"+day.format(dateTimeFormatter));
		//返回 accessToken 实体类
		return accessToken;
	}
     
     
     /**
      * 开启线程，定时设置AccessTokenService为空
  	 * @return 
      */
  	private void initThread(){
          new Thread(new Runnable() {

        	  @Override
              public void run() {
            	  LocalDate day = LocalDate.now();
          		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
          		
                  try {
                     //睡眠7000秒
                      logger.info("线程开始:"+day.format(dateTimeFormatter));
                      Thread.sleep(7000*1000);    
                      singleAccessToken =null;

                  } catch (InterruptedException e) {
                      // TODO Auto-generated catch block
                      e.printStackTrace();
                      logger.info("线程异常:"+day.format(dateTimeFormatter));
                  }
                  logger.info("线程结束:"+day.format(dateTimeFormatter));
              }
          }).start();
      }
}
