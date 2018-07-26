package javaWechat;

import java.util.HashMap;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import com.alibaba.fastjson.JSONObject;

/** 订单实体类
 * */
public class tradeOrder {
   public String notifyUrl =null;
   public String body=null;
   public String outTradeNo=null;
   public String spbillCreateIp=null;
   public String sceneInfo=null;
   public String userOpenid=null;
   public String tradeType =  null;
   public int totalFee;
   

   
   public void setUserOpenid(String openid){
	   this.userOpenid = openid;
   }
   public String getUserOpenid(){
	    return this.userOpenid;
   }
   public void setnotifyUrl(String url){
	   this.notifyUrl = url;
   }
   public String getnotifyUrl(){
	   return this.notifyUrl;
   }
   
   public void setbody(String body){
       this.body = body;
   }
   
   public String getbody(){
       return this.body;
   }

   public void setoutTradeNo(String tradeNo){
       this.outTradeNo = tradeNo;
   }

   public String getoutTradeNo(){
       return this.outTradeNo;
   }
   
   public void setspbillCreateIp(String ip){
       this.spbillCreateIp = ip;
   }

   public  String getspbillCreateIp () {
       return this.spbillCreateIp;
   }
   
   public void settotalFee( int fee){
	   this.totalFee = fee;
   }
   
   public int gettotalFee(){
	    return this.totalFee;
   }
   public void setTradeType(String type){
	   this.tradeType = type;
   }
   public String getTradeType(){
	   return this.tradeType;
   }

   /**
    * 获取场景值
    * */
   public String getsceneInfo(){
	   JSONObject json = new JSONObject();
	   HashMap map = new  HashMap();
	   map.put("payInfo", "在线微信支付");
	   json.put("h5_info", map);
	   return json.toString();
   }
   
   /**
    * 从Request对象中获得客户端IP，处理了HTTP代理服务器和Nginx的反向代理截取了ip
    * @param request
    * @return ip
    */
  public static String getLocalIp(HttpServletRequest request) {
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
  
 
  
}
  
