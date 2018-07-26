
# 开始使用 #
## 准备工作 ##
1. 导入 javaWechat 和 HttpClient 包，如果是基于ssm框架，直接放在 **src/main/java**。
2. 需要的pom支持如下：<br>
	 
		 <!--HTTP CLIETN--> 
	 	    <dependency>  
	 	    
	    	    <groupId>org.apache.httpcomponents</groupId>  
	    	    
	    	     <artifactId>httpcore</artifactId>  
	    	     
	    	      <version>4.4.6</version>  
	    	      
		    </dependency>  
		    
		   <dependency>  
	    	    <groupId>org.apache.httpcomponents</groupId>  
	    	    
	    	     <artifactId>httpclient</artifactId>  
	    	     
	    	     	<version>4.5.3</version>  
	    	     	
			</dependency>  
	
	
	   		<!--sha1加密-->
	  		 <dependency>
	    			<groupId>commons-codec</groupId>
	    				<artifactId>commons-codec</artifactId>
	    					<version>1.11</version>
			</dependency>
			
			
			<!-- analyze xml use dom4j -->
	
	        <dependency>
	
	            <groupId>dom4j</groupId>
	
	            <artifactId>dom4j</artifactId>
	
	            <version>1.6.1</version>
	            
	       </dependency>

## 配置信息 ##
1. 在 wechat.properties中填入对应信息

2. 在javaWechat 包中找到init类，在设置配置信息这一处代码中修改
 
     
		BufferedReader in = new BufferedReader(new FileReader("wechat.properties所在的绝对路径/wechat.properties"));

当然配置文件名称可以随意，但是里面的Key名称就不要更改了。

# OAuth #
**目前只写了 微信内部浏览器的授权**

### 微信网页授权 ###
微信授权主要是三个步骤<br>

1. 使用appid等信息获取code

2. 通过code换取 网页授权accessToken

3. 拉取用户信息

<br>**封装到了 OAuth这个类当中。**<br>
<br> 简单的授权示例：

第一步：获得jscode

	OAuth oauth = new OAuth();
	//return 后页面会重定向到指定的url,state为携带的参数，形式以"&state="，H5指的是微信内部浏览器授权，目前固定填写H5，主要encodeUrl 是值你指定的页面经过Urlencode编码处理后的链接。
	return oauth.getCode(encodeUrl, "state", "H5");  

    
第二步： 使用jscode换取accessToken
     
因为jscode是放在上一步指定跳转的url里面的，所以需要通过  @RequestParam("code") 来获取参数

	oauth.setToken(code);
    //获取openid
	String openId = oauth.getUserOpenid();
    //拉取用户信息 json字符串
	String userInfo = oauth.getUserInfo(openId);

完整示例：<br>

	@RequestMapping("/wechatLogin")
	public ModelAndView getCode() {
		oauth = new OAuth();
		String encodeUrl = "http%3a%2f%2fwww.qiaohserver.cn%2fwechat%2ftest%2findex"; // 重定向url
		return oauth.getCode(encodeUrl, null, "H5");
	}

	/**
	 * 获取网页授权accessToken 重定向回应用首页
	 */
	@RequestMapping("/index")
	public ModelAndView setToken(@RequestParam("code") String code) {
		if (code == null) {
			logger.info("没有获取到jscode");
			return null;
		}
		try { // 通过code 换取accessToken
			oauth.setToken(code);
			openId = oauth.getUserOpenid();
		} catch (IOException e) {
			e.printStackTrace();
			logger.info("获取网页授权accessToken失败");
		}
		return new ModelAndView(new RedirectView("http://www.qiaohserver.cn/?openid=" + openId)); // 重定向到应用首页

	}

# 微信支付 #
**目前只支持 公众号支付和H5支付**

## 订单实体类 ##

**统一下单所需要参数都全部封装好到 tradeOrder 这个类里面**

  	
	tradeOrder tradeOrder = new tradeOrder(); // 实例化订单

    tradeOrder.setnotifyUrl("url"); // 设置支付成功微信通知你的地址
		tradeOrder.setbody("body"); //订单内容
		tradeOrder.setUserOpenid(openid);//如果交易方式是JSAPI 需要传入用户openid
		tradeOrder.settotalFee(1); //传入总价格 (int) 单位为分， 这里传入1分钱为例子
		tradeOrder.setTradeType("JSAPI");//交易类型，H5支付为 MWEB ，公众号为  JSAPI
		tradeOrder.setoutTradeNo("66666");//设置订单号

## 统一下单 ##

**微信支付的实现过程都在 wechatPay这个类（主要是解析xml 和发送xml ,签名算法在 init这个类）;**
 
统一下单用到 wechatPay中的**Order**方法，需要传入的参数有
 **tradeOrder 订单实体类 和HttpServletRequest** 返回前端所需要的配置（数据类型为Map）


	Map<String, String> packages = new HashMap();// 接收统一下单返回前端所需配置的map
    wechatPay wechatPay = new wechatPay();
	// 统一下单
	packages = wechatPay.Order(tradeOrder, request);

### 公众号统一下单成功返回示例 ###


	{
    "package": {
        "package": "prepay_id=wx261406193223284ffe5582db2222097589", 
        "paySign": "20CF15509C83671744858F55DB63DF02", 
        "nonceStr": "abcdiVcKdpRxkhJA536794", 
        "timestamp": "1532585179115"
     }
	}
   
将这些配置直接传给前端调用微信的jsapi就可以唤起微信支付

### H5统一下单成功返回示例 ###

	{
    "package": {
        "mweb_url": "https://wx.tenpay.com/cgi-bin/mmpayweb-bin/checkmweb?prepay_id=**************&package=******* "
    	}
	}
    
然后直接将这个地址扔给前端，让用户访问，就可以进行微信支付。

## 支付通知 ##

支付成功后微信会向之前统一下单填的通知地址POST一段xml，需要做的就是解析xml，然后取出相关参数，再用这些参数计算出签名，然后发送订单查询请求到微信，来判断接收到的xml是否为假报文，完成支付业务逻辑。

接收通知和验证报文已经全部封装到一个方法 getNotify（）中，**返回支付结果的Map,如果是有效的支付，则键msg里面的值是"SUCCESS"**只要在你设置的接收微信通知的方法里调用就行。 示例代码：

		wechatPay wp = new wechatPay();
		Map<String, String> Notify = wp.getNotify(request); //接收支付通知
		if(Notify.get("msg").equals("SUCCESS")){
			// 数据库操作code
			wp.sendNotify(response); //告诉微信支付完成，response 为HttpServletResponse 
		}


# 获取调用jsapi所需要的参数 #
  
  		
		init i = new init();
		JSONObject res = i.getJsConfig(request);  //request 为当前 HttpServletRequest ,返回相关配置的json

## 返回示例 ##

 	
	{
    "config": {
        "debug": true, 
        "appId": "wxcf552d5f505f694c", 
        "timestamp": "1532586265145", 
        "nonceStr": "jssdkVcKdpRxkhJA49513", 
        "signature": "775539ebbb25fbe8b71344ee5db8cbda326bc5af", 
        "jsApiList": "['chooseWXPay']"
    	}
	}