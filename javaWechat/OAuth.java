package javaWechat;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.http.HttpRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.istack.internal.logging.Logger;

import HttpClient.HttpClient;

public class OAuth {
	private static Logger logger = Logger.getLogger(OAuth.class);
	
	public init i = null ;
	public HttpClient hc=null;
	public  String accessToken = null;  //用户网页授权accessToken
	public  String refreshToken = null; //用户网页授权 refresh_token
	public String openId = null ; //网页授权用户openid
	public OAuth(){
		this.i = new init();
		this.hc = new HttpClient();
	}
	
	 /*微信授权
	 *返回scope参数
	 * @params Sring encodeUrl redirect_uri 是经过urlencode处理后的, String state 重定向后携带的参数,String Type 授权类型，H5为微信内部浏览器授权，WEB为网站应用授权
	 * @return  重定向回微信
	 * */
	
	public ModelAndView getCode(String encodeUrl , String state, String Type){
		if(state == null){ 
			state = "STATE";
		}
		if(Type.equals("H5")){ //微信内部浏览器授权
			String url = "https://open.weixin.qq.com/connect/oauth2/authorize?appid="+i.getAppId()+"&redirect_uri="+encodeUrl+"&response_type=code&scope=snsapi_userinfo&state="+state+"#wechat_redirect";
			return  new ModelAndView(new RedirectView(url));  //重定向到微信	
		}else{
			String url = "https://open.weixin.qq.com/connect/qrconnect?appid="+i.getAppId()+"&redirect_uri="+encodeUrl+"&response_type=code&scope=snsapi_login&state="+state+"#wechat_redirect";
			return  new ModelAndView(new RedirectView(url));  //重定向到扫码页面
		}
		 
	}
	
	
	 /*获取网页认证 accessToken
	 * 过期后刷新
	 * @params Sring code 微信返回的jscode
	 * */
    public void setToken(String code) throws IOException{
    	
    	String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="+i.getAppId()+"&secret="+i.getAppSecret()+"&code="+code+"&grant_type=authorization_code";
    	String response =  hc.doGet(url); 	
    	JSONObject jsonObject =  JSONObject.parseObject(response);
		accessToken= jsonObject.getString("access_token");
		refreshToken = jsonObject.getString("refresh_token");
		openId = jsonObject.getString("openid");
    	
    	//7200秒后通过refresh_token刷新accessToken
    	new Thread(new Runnable(){

			@Override
			public void run() {
				try {
					logger.info("7200秒后刷新网页授权accessToken");
					Thread.sleep(7200*1000);
					String url = "ttps://api.weixin.qq.com/sns/oauth2/refresh_token?appid="+i.getAppId()+"&grant_type=refresh_token&refresh_token="+refreshToken;
					hc.doGet(url);
					accessToken= jsonObject.getString("access_token");
			    	refreshToken = jsonObject.getString("refresh_token");
			    	logger.info("网页授权accessToken完成");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}    
				
			}
    		
    	}).start();
    	
    }
    
    public String getUserOpenid(){
    	return openId;
    }
  
    /*返回用户信息
    * 在这之前必须获得jscode 和accessToken
    * @params String id  openid
    * */
  public String getUserInfo(String id){
	  if(accessToken == null && refreshToken==null ){
		  throw new NullPointerException();
	  }
	  String url = "https://api.weixin.qq.com/sns/userinfo?access_token="+accessToken+"&openid="+id+"&lang=zh_CN";
	  return hc.doGet(url);
  }
	
}
