package javaWechat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.client.methods.HttpPost;
import org.apache.log4j.Logger;
//dom4j解析xml
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.alibaba.fastjson.JSONObject;
import com.cn.domain.AccessToken;
import com.cn.exception.wechatPayException;

import HttpClient.HttpClient;
import javaWechat.init;

public class wechatPay {
	private static Logger logger = Logger.getLogger(wechatPay.class);

	public init i = null;

	/**
	 * 统一下单
	 * 
	 * @throws DocumentException
	 * @params tradeOrder data 订单实体类 ,HttpServletRequest request
	 * @return Json 调用微信支付api所需要的支付参数
	 **/
	public Map<String, String> Order(tradeOrder data, HttpServletRequest request) throws DocumentException {

		HashMap order = new HashMap(); // 用来封装统一下单后微信返回的参数
		HashMap orderPackage = new HashMap(); // 用来封装最终返回给前端的参数
		// 初始化公众号配置
		i = new init();
		// 生成随机字符串
		String nonceStr = "abcdiVcKdpRxkhJA" + (int) (Math.random() * 100) + (int) (Math.random() * 100)
				+ (int) (Math.random() * 100); // 随机字符串
		// 获取签名信息对象
		HashMap signInfo = i.getPaySign(data, nonceStr, request);
		// 获取签名
		String sign = (String) signInfo.get("sign");
		String url = "https://api.mch.weixin.qq.com/pay/unifiedorder"; // 腾讯第三方接口地址

		// 设置xml内容;
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		sb.append("<appid>");
		sb.append(i.getAppId());
		sb.append("</appid>");
		sb.append("<body>");
		sb.append(data.getbody());
		sb.append("</body>");
		sb.append("<mch_id>");
		sb.append(i.getMchId());
		sb.append("</mch_id>");
		sb.append("<nonce_str>");
		sb.append(nonceStr);
		sb.append("</nonce_str>");
		sb.append("<notify_url>");
		sb.append(data.getnotifyUrl());
		sb.append("</notify_url>");
		sb.append("<out_trade_no>");
		sb.append(data.getoutTradeNo());
		sb.append("</out_trade_no>");
		sb.append("<spbill_create_ip>");
		sb.append(data.getspbillCreateIp());
		sb.append("</spbill_create_ip>");
		sb.append("<total_fee>");
		sb.append(data.gettotalFee());
		sb.append("</total_fee>");
		sb.append("<trade_type>");
		sb.append(data.getTradeType());
		sb.append("</trade_type>");
		sb.append("<sign>");
		sb.append(sign);
		sb.append("</sign>");
		sb.append("<scene_info>");
		sb.append(data.getsceneInfo());
		sb.append("</scene_info>");
		sb.append("<openid>");
		sb.append(data.getUserOpenid());
		sb.append("</openid>");
		sb.append("</xml>");
		// 将字符流转换成xml字符串
		String xml = sb.toString();
		logger.info(xml);

		HttpClient client = new HttpClient();
		// 接收微信响应的xml
		String response = client.doPost("https://api.mch.weixin.qq.com/pay/unifiedorder", null, xml);
		logger.info("微信支付返回的xml:" + response);
		// 解析微信返回的xml
		Document dom = DocumentHelper.parseText(response); // 将字符串转为XML
		// 获得根节点
		Element rootEle = dom.getRootElement();

		String code = rootEle.element("return_code").getText(); // 获取统一下单状态
		logger.info("code:" + code);

		if (!code.equals("SUCCESS")) {
			String msg = rootEle.element("return_msg").getText(); // 获取统一下单失败原因
			logger.info("统一下单失败,下面是官方信息:" + msg);
			throw new wechatPayException(msg); // 抛出异常
		}

		if (rootEle.element("trade_type").getText().equals("MWEB")) {// 如果是H5支付
			orderPackage.put("mweb_url", rootEle.element("mweb_url").getText());
			return orderPackage;// 直接返回给前端跳转链接完成支付
		}

		try {// 若返回prepay_id为空 则统一下单失败 捕获空指针异常
			String packages = "prepay_id=" + rootEle.element("prepay_id").getText();
			String appid = rootEle.element("appid").getText();
			String PaynonceStr = rootEle.element("nonce_str").getText();

			// 封装 微信返回的参数进行二次签名
			order.put("package", packages);
			order.put("nonceStr", nonceStr);
			order.put("appId", appid);
			order.put("signType", "MD5");
			order.put("timeStamp", String.valueOf(signInfo.get("timeTamp")));
			logger.info("二次签名参数:" + JSONObject.toJSONString(order));

			// 二次签名
			String sort = i.sortParameters(order) + "&key=" + i.getKey();
			logger.info(sort);
			String secondSign = MD5.MD5Encode(sort).toUpperCase();
			logger.info("二次签名:" + secondSign);
			// 返回最终前端所需数据

			orderPackage.put("nonceStr", nonceStr);
			orderPackage.put("package", packages);
			orderPackage.put("paySign", secondSign);
			orderPackage.put("timestamp", String.valueOf(signInfo.get("timeTamp")));

		} catch (NullPointerException e) {
			e.printStackTrace();
			logger.info("二次签名出现空指针异常");
		} catch (Exception e) {
			logger.info(e.getMessage());
		}

		logger.info("统一下单后最终返回的jssdk前端所需要的配置:" + JSONObject.toJSONString(orderPackage));
		return orderPackage;
	}

	/**
	 * 订单查询
	 * 
	 * @param map
	 *            微信返回的数据集合
	 */

	public Map<String, String> checkOrder(Map<String, String> orderInfo) {

		HashMap payResult = new HashMap(); // 用来响应支付结果
		init i = new init();
		String sort = i.sortParameters(orderInfo);

		logger.info("the sort:" + sort);
		try {
			logger.info("the key:" + i.getKey());
		} catch (Exception e) {
			logger.info("key错误:" + e.getMessage());
		}

		sort = sort + "&key=" + i.getKey();
		logger.info("查询sort:" + sort);
		String sign = MD5.MD5Encode(sort).toUpperCase();
		logger.info("查询订单时候生成的签名:" + sign);

		// 设置xml内容;
		StringBuffer sb = new StringBuffer();
		sb.append("<xml>");
		sb.append("<appid>");
		sb.append(orderInfo.get("appid"));
		sb.append("</appid>");
		sb.append("<mch_id>");
		sb.append(orderInfo.get("mch_id"));
		sb.append("</mch_id>");
		sb.append("<transaction_id>");
		sb.append(orderInfo.get("transaction_id"));
		sb.append("</transaction_id>");
		sb.append("<nonce_str>");
		sb.append(orderInfo.get("nonce_str"));
		sb.append("</nonce_str>");
		sb.append("<sign>");
		sb.append(sign);
		sb.append("</sign>");
		sb.append("</xml>");
		String xml = sb.toString();

		HttpClient client = new HttpClient();
		// 接收查询订单API微信响应的xml
		String response = client.doPost("https://api.mch.weixin.qq.com/pay/orderquery", null, xml);
		logger.info("微信查询订单返回的xml:" + response);

		try {
			// 解析微信返回的xml
			Document dom;
			dom = DocumentHelper.parseText(response);
			// 获得根节点
			Element rootEle = dom.getRootElement();
			String returnMsg = rootEle.element("return_msg").getText();

			if (!returnMsg.equals("OK")) { // 查询失败，可能是签名错误
				payResult.put("msg", returnMsg);
				return payResult;
			}

			if (rootEle.element("result_code").getText().equals("SUCCESS")) {
				payResult.put("msg", "SUCCESS");
			}

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return payResult;
	}

	/**
	 * 接收微信支付通知
	 * 
	 * @param HttpServletRequest
	 *            request
	 **/
	public Map<String, String> getNotify(HttpServletRequest request) {

		Map<String, String> notify = new HashMap(); // 响应
		HashMap notifyInfo;// 封装微信通知返回的xml

		// 已HTTP请求输入流建立一个BufferedReader对象
		BufferedReader br;
		Document dom = null;

		try {
			br = new BufferedReader(new InputStreamReader(request.getInputStream(), "UTF-8"));
			String buffer = null;
			String xml = ""; // 存放xml请求内容
			while ((buffer = br.readLine()) != null) { // 拼接xml字符串
				// 在页面中显示读取到的请求参数
				xml = xml + buffer;
			}
			logger.info("xml:" + xml);
			// 解析返回xml
			dom = DocumentHelper.parseText(xml);
			// 获得根节点
			Element rootEle = dom.getRootElement();

			// 封装微信返回的数据
			notifyInfo = new HashMap();
			notifyInfo.put("appid", rootEle.element("appid").getText());
			notifyInfo.put("mch_id", rootEle.element("mch_id").getText());
			notifyInfo.put("transaction_id", rootEle.element("transaction_id").getText());
			notifyInfo.put("nonce_str", rootEle.element("nonce_str").getText());
			// 查询订单
			logger.info("用户支付成功微信通知的数据" + JSONObject.toJSONString(notifyInfo));

			if (checkOrder(notifyInfo).get("msg").equals("SUCCESS")) {
				notify.put("msg", "SUCCESS");
				logger.info("支付生效");
				return notify;
			}

		} catch (IOException e) {
			e.printStackTrace();
			logger.info("接收支付通知:" + e.getMessage());
		} catch (DocumentException e) {
			e.printStackTrace();
			logger.info("接收支付通知:" + e.getMessage());
		}

		notify.put("msg", "FAIL"); // 无效报文
		notify.put("info", "无效的xml报文");
		return notify;
	}

	/**
	 * 告诉微信已经完成支付
	 * 
	 * @param HttpServletResponse
	 *            response
	 **/
	public void sendNotify(HttpServletResponse response) {
		String resXml = "<xml>" + "<return_code><![CDATA[SUCCESS]]></return_code>"
				+ "<return_msg><![CDATA ]></return_msg>" + "</xml> ";
		try {
			BufferedOutputStream out = new BufferedOutputStream(response.getOutputStream());
			out.write(resXml.getBytes());
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
