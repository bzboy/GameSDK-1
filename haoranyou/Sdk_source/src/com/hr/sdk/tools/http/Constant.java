package com.hr.sdk.tools.http;

import org.json.JSONObject;

import android.content.pm.PackageInfo;
import android.net.Uri;
import android.util.Log;

import com.hr.sdk.HrSDK;
import com.hr.util.Logd;

public class Constant {
	
	public static boolean isRelease = true;
	
	/** 服务器host */
	public static String httpHost = "http://api.sdk.tjhaoran.cn";
	/** 服务器https */
	public static String httpsHost = "https://api.sdk.tjhaoran.cn";
	/**WEB页面地址*/
	public static String WEB_PAGE_HOST = "http://web.sdk.tjhaoran.cn";
	static {
		if(! isRelease){
			httpHost = "http://139.196.21.34:9902";
			httpsHost = "https://139.196.21.34:9902";
			WEB_PAGE_HOST = "http://139.196.21.34:9901";
		}
	}
	
	
	//忘记密码地址
	public static final String USER_FORGOT_PWD = WEB_PAGE_HOST + "/app/client/reset";	
	/**充值界面*/
	public static final String WEB_PAY = WEB_PAGE_HOST + "/app/user/pay";	
	/**个人中心 页面*/
	public static final String USER_CENTER = WEB_PAGE_HOST + "/app/user";	
	/** 分享页面 */
	public static final String SHARE_GAME =WEB_PAGE_HOST+"/app/public/share";
	/**设置支付密码?access_token=xxx&client_id=xxx*/
	public static final String SET_PAY_PWD = WEB_PAGE_HOST+ "/app/user/setPin";
	/** 消息页面 */
	public static final String USER_NEWS = WEB_PAGE_HOST+"/App/news/index";
	//活动
	public static final String USER_ACTIVE = WEB_PAGE_HOST + "/App/news/active";
	//礼包
	public static final String USER_GIFT = WEB_PAGE_HOST + "/app/gifts";
	//帮助
	public static final String USER_HELP = WEB_PAGE_HOST+"/app/faq";
	/**浩然游币支付失败，411余额不足时候，点击 Dialog确定 按钮，跳转 网页的*/
	public static final String USER_411RECHARGE = WEB_PAGE_HOST+"/app/user/pay";
	
	
	
	
	//用户详情 post 参数 access_token
	public static final String USER_DETAIL = "/Oauth/User/detail";
	/**请求包的接口*/
	public static final String USER_PACK_INFO = "/Oauth/Pack/detail";
	public static final String USER_LOGIN = "/Oauth/Game/auth_code";
	
	/**登录接口*/ 
	public static final String USER_QUICK_LOGIN = "/Api/Oauth/quickLogin";
	/**获取游客账号*/
	public static final String USER_QUICK_REG = "/api/user/quickReg";
	/**绑定手机号*/
	public static final String USER_BIND_PHONE = "/Api/User/bindPhone";
	/**(旧)设置用户名*/
	public static final String USER_RENAME = "/api/user/rename";
	//client_id,  phone, password,  verify_code, pack_key
	public static final String USER_REGISTER_MOBILE = "/V201601/Register/phone";
	public static final String USER_REGISTER_USERNAME = "/V201601/Register/user";
	//phone,client_id
	public static final String USER_REG_SMS = "/V201601/Sms/register";
	public static final String USER_PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDFK2EP+e1cdAwhdCHfsjlRi3jg2CYZxBgccZw0B2Bq/alkPsJZC259G20A4bkX33V19zBe9xKruo13tDi309Z8dNKsfSjjcu1mp1BGHnct9GY+kqjaaVhe7OS04J5wjJEgywsy9+Von8XvynTLawSHghMSg9pUoQPxdOFd6zhp9QIDAQAB";
	
	
	
	
	
	/**普通用户*/
	public static final String TYPE_USER_NORMAL = "USER_NORMAL";
	/**未注册用户，即游客*/
	public static final String TYPE_USER_NOT_REGISTER = "USER_NOTREGISTER";
	/**绑定类型  是否是游客支付*/
	public static final String BIND_PAY_NOT_REGISTER = "pay_type_not_register";
	/**来自支付*/
	public static final String TYPE_FROM_PAY = "type_from_pay";	
	/**类型key*/
	public static final String KEY_DATA_TYPE = "TYPE";
	/**内容key*/
	public static final String KEY_DATA_CONTENT = "CONTENT";
	/**用户名*/
	public static final String KEY_DATA_USERNAME = "USERNAME";
	/**手机号*/
	public static final String KEY_DATA_PHONE = "PHONE";
	/**密码*/
	public static final String KEY_DATA_PWD = "PWD";
	/**登录信息, 放入json之中*/
	public static final String KEY_LOGIN_USERNAME = "username";
	public static final String KEY_LOGIN_PWD = "password";
	public static final String KEY_LOGIN_PID = "pid";
	
	/**启动登录界面的类型*/
	public static final String TYPE_IS_LOGIN_SWITCH_ACCOUNT = "switch_account";
	
	/** 公告信息*/
	
	/** ↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓跳转网页↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓ */
	/**
	 * 获取忘记密码URI
	 * @return
	 */
	public static final Uri getFrogetPasswordUri() {
		return Uri.parse(Constant.USER_FORGOT_PWD);
	}
	
	/**获取设置支付密码 网而言的URI*/
	public static final Uri getSetPayPWDUri(String access_token) {
		Uri uri = null;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.SET_PAY_PWD + "?access_token=" + HrSDK.mToken + "&from=sdk"+
		"&client_id="+HrSDK.getInstance().getAppId());
		} else {
			uri = Uri.parse(Constant.SET_PAY_PWD + "?from=sdk");
		}
		return uri;
	}
	
	/**获取设置论坛网页的URI*/
	public static final Uri getUserBBS(String access_token) {
		Uri uri = null;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(HrSDK.URL_BBS +"?access_token=" + HrSDK.mToken + "&from=sdk"+
					"&client_id="+HrSDK.getInstance().getAppId());
		} else {
			uri = Uri.parse(HrSDK.URL_BBS + "?from=sdk");
		}
		return uri;
	}
	
	/**
	 * 跳转充值界面
	 * @return
	 */
	public static final Uri getWebPay(String url) {
		Uri uri = null;
		String access_token = HrSDK.mToken;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(url + "?access_token=" + HrSDK.mToken + "&from=sdk" + AppendToWebUri());
		} else {
			uri = Uri.parse(url + "?from=sdk");
		}
		return uri;
	}
	
	/**
	 * 获取用户中心URI
	 * @return
	 */
	public static final Uri getUserCenterUri(String access_token) {
		Uri uri = null;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.USER_CENTER + "?access_token=" + HrSDK.mToken + "&from=sdk"
		+AppendToWebUri());
		} else {
			uri = Uri.parse(Constant.USER_CENTER + "?from=sdk");
		}
		return uri;
	}

	/**
	 * 获取礼包页面URI
	 * @return
	 */
	public static final Uri getGiftUri(String access_token) {
		Uri uri = null;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.USER_GIFT + "?access_token=" + HrSDK.mToken + "&from=sdk"
		+AppendToWebUri());
		} else {
			uri = Uri.parse(Constant.USER_GIFT);
		}
		return uri;
	}

	/**
	 * 获取帮助页面URI
	 * @return
	 */
	public static final Uri getHelpUri(String access_token) {
		Uri uri = null;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.USER_HELP + "?access_token=" + HrSDK.mToken  + "&from=sdk"
					+AppendToWebUri());
		} else {
			uri = Uri.parse(Constant.USER_HELP + "?client_id=" + HrSDK.getInstance().getAppId() + "&from=sdk"+AppendToWebUri());
		}
		return uri;
	}

	/**
	 * 获取消息页面URI
	 * @return
	 */
	public static final Uri getUSERNEWS(String access_token) {
		Uri uri = null;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.USER_NEWS + "?access_token=" + HrSDK.mToken + "&from=sdk"
					+AppendToWebUri());
		} else {
			uri = Uri.parse(Constant.USER_NEWS + "?from=sdk");
		}
		return uri;
	}

	/**
	 * 获取活动页面URI
	 * @return
	 */
	public static final Uri getActiveUri() {
		Uri uri = Uri.parse(Constant.USER_ACTIVE + "?from=sdk"
	+"&access_token="+HrSDK.getInstance().mToken+ AppendToWebUri());
		return uri;
	}
	
	/**获取 分享的 URI*/
	public static final Uri getShareGameUri(String access_token){
		Uri  uri = null ;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.SHARE_GAME + "?access_token=" + HrSDK.mToken + "&from=sdk"
					+AppendToWebUri());
		} else {
			uri = Uri.parse(Constant.SHARE_GAME + "?from=sdk");
		}
		return uri;
	}
	
	/**浩然游币 支付失败411，弹出是否前去充值浩然币的对话框的时候提示跳转的网页*/
	public static Uri goWebRechargeLongBi(String access_token){
		Uri  uri = null ;
		if (null != access_token && !access_token.isEmpty()) {
			uri = Uri.parse(Constant.USER_411RECHARGE + "?access_token=" + HrSDK.mToken + "&from=sdk"
					+AppendToWebUri());
		} else {
			uri = Uri.parse(Constant.USER_411RECHARGE + "?from=sdk");
		}
		return uri;
	}
	
	/**ActivityUser 中，个人中心，礼包，等等，提供给网页追加,packkey=key,lysid=sid字段值*/
	public static String AppendToWebUri(){
		String appendURL ="&client_id="+HrSDK.getInstance().getAppId()+"&pack_key="+HrSDK.getInstance().getSid();
		return appendURL;
	}
	
	
	
	////////////////////////////////////////////////////
	//错误代码
	private static final int ERRNO_SUCCESS = 200; // 成功代码
	// 100 - 200 是常规错误
	private static final int ERRNO_PARAM = 100; // 参数错误
	private static final int ERRNO_SIGN = 101; // 签名不匹配
	private static final int ERRNO_UNKOWN = 102; // 未知错误
	private static final int ERRNO_KEY_ERROR = 103; // app_key错误
	private static final int ERRNO_API_ERROR = 104; // 接口错误
	private static final int API_ERR_GAME = 105; // 游戏APPID信息错误
	public static final int API_ERR_USERNAME=106;
	public static final int API_ERR_SMS = 107; // 短信发送错误
	// 300 是登录相关代码
	private static final int ERRNO_NO_USER = 300; // 用户不存在
	private static final int ERRNO_PASSWORD_ERROR = 301; // 密码错误
	private static final int ERRNO_NO_ACTIVATE = 302; // 用户未激活
	private static final int ERRNO_SESSION_ERROR = 303; // SESSION错误
	private static final int ERRNO_SESSION_EXPIRED = 304; // SESSION失效
	private static final int ERRNO_USER_EXIST = 305; // 账号已存在
	private static final int ERRNO_USER_INVALID = 306; // 用户名不合法
	private static final int ERRNO_CODE_ERROR = 307; // auth_code错误或者不存在 不匹配
	private static final int ERRNO_CODE_EXPIRED = 308; // auth_code过期失效
	private static final int ERRNO_TOKEN_ERROR = 309; // access_token错误或者不存在不匹配
	private static final int ERRNO_TOKEN_EXPIRED = 310; // access_token过期失效
	public static final int ERRNO_MOBILE_EXISTS = 311; // 用户已经绑定了手机号
	public static final int ERRNO_SMSCODE_ERROR = 312; // 手机验证码错误
	public static final int ERRNO_Conpon_Error = 314;         //玩家代金券无效
	public static final int ERRNO_SMSCODE_ERROR_315 = 315; // 支付密码错误
	private static final int API_ERR_NO_PAY_PASSWORD = 316; // 未设置支付密码
	private static final int ERRNO_ACCOUNT_AREADY_EXIST = 320; // 账号已存在
	// 400 是支付相关
	private static final int ERRNO_ORDER_ERROR = 401; // 订单生成错误
	private static final int API_ERR_ORDER_VISITOR = 402; // 游客账号无正式订单权限
	private static final int ERRNO_PAY_PWD = 410; // 支付密码错误
	
	private static final int ERRNO_NO_MONEY = 411; // 余额不足
	private static final int API_ERR_PAY_GET_NO = 412; // 获取外部订单号失败
	private static final int API_ERR_PAY_METHOD = 413; // 支付方式无效
	private static final int ERRNO_ORDER_NO = 420; // 订单不存在
	private static final int ERRNO_ORDER_INFO_MATCH = 421; // 订单数据不匹配
	private static final int ERRNO_ORDER_UNPAYED = 422; // 订单未支付
	private static final int API_ERR_ORDER_PAYED = 423; // 订单已支付

	private static final int API_ERR_SERVER  = 500; // 服务器错误
	private static final int ERRNO_RENAME_ACCOUNT_AREADY_EXIST = 501; // 订单未支付
	
	private static final int  ERRNO_User_UnLogin_Error = 100001;      //用户未登录

	public static String paseError(int code) {
		String str = "";
		switch (code) {
			case API_ERR_USERNAME:
			str = "用户名不合法";
				break;
			case API_ERR_NO_PAY_PASSWORD:
				str = "未设置支付密码";
				
				break;
			case API_ERR_SERVER:
				str = "服务器错误";
				break;
			case API_ERR_PAY_GET_NO:
				str = "获取外部订单号失败";
				break;
		   	case API_ERR_PAY_METHOD:
		   		str = "支付方式无效";
		   		break;
		    case API_ERR_ORDER_PAYED:
		    	str = "订单已支付";
		        break;
		    case API_ERR_ORDER_VISITOR:
	    	   str = "游客账号无正式订单权限";
		        break;
		    case API_ERR_GAME:
		    	str = "游戏APPID信息错误";
			    break;
		    case API_ERR_SMS:
		    	str = "短信发送失败，请检查手机号格式（温馨提示：单个号码每天上限5条）";
			    break;
			case ERRNO_SUCCESS:
				Logd.d("parseError", code + "..");
				break;
				// 100 - 200 是常规错误
			case ERRNO_PARAM:
				str = "参数错误";
				break;
			case ERRNO_SIGN:
				str = "签名不匹配";
				break;
			case ERRNO_UNKOWN:
				str = "未知错误";
				break;
			case ERRNO_KEY_ERROR:
				str = "app_key错误";
				break;
			case ERRNO_API_ERROR:
				str = "接口错误";
				break;

			// 300 是登录相关代码
			case ERRNO_NO_USER:
				str = "用户不存在";
				break;
			case ERRNO_PASSWORD_ERROR:
				str = "用户名或密码错误";
				break;
			case ERRNO_NO_ACTIVATE:
				str = "用户未激活";
				break;
			case ERRNO_SESSION_ERROR:
				str = "SESSION错误";
				break;
			case ERRNO_SESSION_EXPIRED:
				str = "SESSION失效";
				break;
			case ERRNO_USER_EXIST:
				str = "账号已存在";
				break;
			case ERRNO_USER_INVALID:
				str = "用户名不合法";
				break;
			case ERRNO_CODE_ERROR:
				str = "登录失效";
				HrSDK.getInstance().logout();
				break;
			case ERRNO_CODE_EXPIRED:
				str = "登录失效";
				HrSDK.getInstance().logout();
				break;
			case ERRNO_TOKEN_ERROR:
				str = "登录失效";
				HrSDK.getInstance().logout();
				break;
			case ERRNO_TOKEN_EXPIRED:
				str = "登录失效";
				HrSDK.getInstance().logout();
				break;
			case ERRNO_MOBILE_EXISTS:
				str = "用户已经绑定了手机号";
				break;
			case ERRNO_SMSCODE_ERROR:
				str = "手机验证码错误";
				break;
			case ERRNO_SMSCODE_ERROR_315:
				str = "支付密码错误";
				break;	
			case ERRNO_ACCOUNT_AREADY_EXIST:
				str = "账号已存在";
				break;
			// 400 是支付相关
			case ERRNO_ORDER_ERROR:
				str = "订单生成错误";
				break;
			// 402 
			case ERRNO_PAY_PWD:
				str = "支付密码错误";
				break;
			case ERRNO_NO_MONEY:
				str = "余额不足";
				break;
			case ERRNO_ORDER_NO:
				str = "订单不存在";
				break;
			case ERRNO_ORDER_INFO_MATCH:
				str = "订单数据不匹配";
				break;
			case ERRNO_ORDER_UNPAYED:
				str = "订单未支付";
				break;
			case ERRNO_RENAME_ACCOUNT_AREADY_EXIST:
				str = "您的游客账号已在其他游戏中升级，请使用升级后的账号进行登录，如有问题，请联系客服。";
				break;
			case ERRNO_Conpon_Error :         //玩家代金券无效
				str = "玩家代金券无效";
				break;
			case  ERRNO_User_UnLogin_Error :      //用户未登录
				str = "用户未登录";
				break;
			default: str = "请求失败";
				break;
		}
		if(str != null && str.length() > 0)
			HrSDK.showToast(str);
		return str;
	}
	
	public static void paseError(String data) {
		try {
			Logd.e("error", data.toString());
			JSONObject json = new JSONObject(data);
			int errno = json.getInt("errno");
			Constant.paseError(errno);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	

}
