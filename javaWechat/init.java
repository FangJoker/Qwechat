package javaWechat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.cn.domain.AccessToken;
import HttpClient.HttpClient;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class init {
    
	private static Logger logger = Logger.getLogger(init.class);
	private Properties prop = null; 
    public  wechat wechat =null ;  
	private SingleAccessToken singleAccessToken; //accessToken 的单例
	/**获取JSSDK accessToken
	 * */
	
	 public String getAccessToken(){
			
			singleAccessToken = singleAccessToken.getInstance(); //获取singleAccessToken单例
			AccessToken accessToken = singleAccessToken.getAccessToken(); //通过单例获取 accessToken
			return accessToken.getToken();
	 }
	/**
	 * 获取公众号相关配置
	 * */
	public init(){
		 
		LocalDate day = LocalDate.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		
		 prop = new Properties();
		 try{//设置配置信息目录
			 //BufferedReader in = new BufferedReader(new FileReader("C:/Users/59859/Workspaces/MyEclipse 2016 CI/wechat/src/main/resources/wechat.properties"));
			 BufferedReader in = new BufferedReader(new FileReader("/usr/apache-tomcat-8.0.51/webapps/wechat/WEB-INF/classes/wechat.properties"));
			 prop.load(in);		
		 }catch(IOException e){
			 e.printStackTrace();
			 logger.info("配置文件获取失败:"+day.format(dateTimeFormatter));
		 }
		 wechat = new  wechat();
		 wechat.setAppId(prop.getProperty("appid"));
		 wechat.setAppSecret(prop.getProperty("secret"));
		 wechat.setAppSecret(prop.getProperty("secret"));
		 wechat.setMchId(prop.getProperty("mchId"));
		 wechat.setNotifyUrl(prop.getProperty("notifyurl"));
		 wechat.setapiKey(prop.getProperty("apiKey"));
		 wechat.setkey(prop.getProperty("key"));
		 
		 
	}
	/**返回支付密匙
 	 * **/
	 public String getKey(){
		 return wechat.getkey();
	 }
	
	 /** 
	 * 支付签名生成
	 * @params  tradeOrder data 商品实体类 ，String nonceStr 生成签名时用的随机字符串,HttpServletRequest request
	 * @return HasMap 返回签名 和生成签名时的时间戳
	 **/
	public HashMap getPaySign( tradeOrder data ,String nonceStr, HttpServletRequest request){
		
	  long timeTamp = System.currentTimeMillis(); //获取当前时间戳	
	  
	  try{  
		     //先传入当前客户端IP
		     data.setspbillCreateIp(getIp(request));
		    //获取签名参数
	        Map map = getSignMap(data,nonceStr);
	         //并按照参数名ASCII字典序排序
	        String sort=sortParameters( map); 
	        //拼接API秘钥 
	        sort=sort+"&key="+wechat.getkey();; 
			logger.info("sort:"+sort);
			//MD5加密
			String sign= MD5.MD5Encode(sort).toUpperCase();
			//封装返回
			HashMap info = new HashMap();
			info.put("sign", sign);
			info.put("timeTamp",timeTamp);
			logger.info(JSONObject.toJSONString(info));
			return info ;
	      
	  }catch(Exception e){
		  logger.info("又他妈是sign出错");
		  e.printStackTrace();
	  }
	  
		 return null;
	}
	
	/**
	 * @param HttpServletRequest request
	 * 获取当前客户端Ip
	 * @return String ip
	 */
	 public String getIp(HttpServletRequest request){
		//获取当前客户端Ip
			String remoteAddr = request.getRemoteAddr();
		    String forwarded = request.getHeader("X-Forwarded-For");
		    String realIp = request.getHeader("X-Real-IP");
		  
	        String ip = null;
		      if (realIp == null) {
		          if (forwarded == null) {
		              ip = remoteAddr;
		          } else {
		              ip = remoteAddr + "/" + forwarded.split(",")[0];
		          }
		      } else {
		          if (realIp.equals(forwarded)) {
		              ip = realIp;
		          } else {
		              if(forwarded != null){
		                  forwarded = forwarded.split(",")[0];
		              }
		              ip = realIp + "/" + forwarded;
		          }
		      }
		      return ip;
	 }
	 
	 /**获取当前完整的url
	   * */
	  public  static  String getUrl(HttpServletRequest request){
		  HttpServletRequest httpRequest=(HttpServletRequest)request;  
		  if(httpRequest.getQueryString()==null){//不带参数
			  String nowUrl = "http://" + request.getServerName() //服务器地址  
              //+ ":"   
             // + request.getServerPort()           //端口号  
              + httpRequest.getContextPath()      //项目名称  
              + httpRequest.getServletPath();    //请求页面或其他地址  
            
			  return nowUrl;
		  }else{
			  String nowUrl = "http://" + request.getServerName() //服务器地址  
              //+ ":"   
             // + request.getServerPort()           //端口号  
              + httpRequest.getContextPath()      //项目名称  
              + httpRequest.getServletPath()  //请求页面或其他地址  
              + "?" + (httpRequest.getQueryString()); //参数 
			  return nowUrl;
		  }
	    
	      
	  }
	  
	  /** 
	   * 对参数列表进行排序，并拼接key=value&key=value形式 
	   * @param map 
	   * @return 
	   */ 
	  public static String sortParameters(Map<String, String> map) { 
	    Set<String> keys = map.keySet(); 
	    List<String> paramsBuf = new ArrayList<String>(); 
	    for (String k : keys) { 
	      paramsBuf.add((k + "=" + getParamString(map, k))); 
	    } 
	    // 对参数排序 
	    Collections.sort(paramsBuf); 
	    String result=""; 
	    int count=paramsBuf.size(); 
	    for(int i=0;i<count;i++){ 
	      if(i<(count-1)){ 
	        result+=paramsBuf.get(i)+"&"; 
	      }else { 
	        result+=paramsBuf.get(i); 
	      } 
	    } 
	    return result; 
	  } 
	   /** 
	   * 返回key的值 
	   * @param map 
	   * @param key 
	   * @return 
	   */ 
	  public  static  String getParamString(Map map, String key) { 
	    String buf = ""; 
	    if (map.get(key) instanceof String[]) { 
	      buf = ((String[]) map.get(key))[0]; 
	    } else { 
	      buf = (String) map.get(key); 
	    } 
	    return buf; 
	  } 
	  

	/**
	 * 拼装支付签名生成参数
	 * @param nonceStr 随机字符串
	 * @param tradeOrder data 订单实体类
	 * @return map
	 * */
	public  Map<String, String >getSignMap(tradeOrder data, String nonceStr){
		
		Map map = new HashMap();
		map.put("openid", "oqiHrw_Ta8TGRXN7R2-0VUkjgafc");
		map.put("appid",wechat.getAppId());
		map.put("body", data.getbody());
		map.put("mch_id", wechat.getMchId());
		map.put("nonce_str", nonceStr);
		map.put("out_trade_no",data.getoutTradeNo());
		map.put("spbill_create_ip",data.getspbillCreateIp());
		map.put("total_fee", String.valueOf(data.gettotalFee()));
		map.put("trade_type", "JSAPI");
		map.put("notify_url", data.getnotifyUrl());
		map.put("scene_info", data.getsceneInfo());
		return  map;
	}
    /**获得jssdk权限签名
     * @param HttpServletRequest request 
     * @return json 
     * */
	public JSONObject getSign(HttpServletRequest request){
		
	     String accessToken = getAccessToken();
		 HttpClient client = new HttpClient();
		 String res = client.doGet("https://api.weixin.qq.com/cgi-bin/ticket/getticket?access_token="+accessToken+"&type=jsapi");
		 logger.info("原始ticket信息:"+res);
		 Object ticket = JSONObject.parseObject(res).get("ticket");
		 logger.info("jssdk ticker:"+ticket);
		 long timeTamp = System.currentTimeMillis(); //获取当前时间戳	
		//生成随机字符串
	    String nonceStr = "jssdkVcKdpRxkhJA"+(int)(Math.random()*100)+(int)(Math.random()*100)+(int)(Math.random()*100); //随机字符串
		//获取当前URL
	    String url=getUrl(request);
	    logger.info("当前url:"+url);
	    //封装所需参数
	    HashMap config = new HashMap();
	    config.put("noncestr",nonceStr );
	    config.put("jsapi_ticket", ticket);
	    config.put("timestamp",String.valueOf(timeTamp));
	    config.put("url", url);
	    
	    //排序
	    String sort =sortParameters(config);
	    logger.info("sort:"+sort);
	    JSONObject json = new JSONObject();
	    json.put("signature", DigestUtils.sha1Hex(sort));
	    logger.info("签名:"+SHA1.encode(sort));
	    json.put("noncestr", nonceStr);
	    json.put("timestamp", String.valueOf(timeTamp));
	    logger.info("jsskd签名:"+JSONObject.toJSONString(json));
	    return json;
		 
		 
	}
	
	/**获取jssdk config
	 * @param HttpServletRequest request
	 * 
	 * */
	public JSONObject getJsConfig(HttpServletRequest request){
		JSONObject config = new JSONObject();
		//获取签名json
		JSONObject sign = getSign(request);
		config.put("sign", sign);
		config.put("appid", wechat.getAppId());
		return config;
		
	}
	
	
	public String getAppId() {
		// TODO Auto-generated method stub
		if(this.wechat.appId == null){
			logger.info("初始化众号配置信息失败");
		}
		return this.wechat.appId;
	}

	
	public String getAppSecret() {
		// TODO Auto-generated method stub
		return this.wechat.appSecret;
	}

	
	public String getNotifyUrl() {
		// TODO Auto-generated method stub
		return this.wechat.notifyUrl;
	}

	
	public String getMchId() {
		// TODO Auto-generated method stub
		return this.wechat.mchId;
	}
}


