package javaWechat;

public class wechat {
      public String appId;
      public String appSecret;
      public String mchId;
      public String notifyUrl;
      public String apiKey;
      public String key;

      
      public void setAppId(String appid){
    	  this.appId = appid;
      }
      public String getAppId(){
    	   return this.appId;
      }    
      public void setAppSecret(String appsecret){
    	  this.appSecret = appsecret;
      }
      public String getAppSecret(){
    	   return this.appSecret;
      }
      public void setMchId(String mchid){
    	  this.mchId = mchid;
      }
      public String getMchId(){
    	   return this.mchId;
      }
      public void setNotifyUrl(String url){
    	  this.notifyUrl = url;
      }
      public String getNotifyUrl(){
    	   return this.notifyUrl;
      } 
      
      public void setapiKey(String key){
    	  this.apiKey=key;
      }
      public String getapiKey(){
    	  return this.apiKey;
      }
      
      public void setkey(String key){
    	  this.key = key;
      }
      
      public String getkey(){
    	  return this.key;
      }
     
}
