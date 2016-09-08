package com.hr.sdk.ac;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;

import com.hr.sdk.HrSDK;
import com.hr.sdk.ac.SdkLoginActivity.LoginSilentListener;
import com.hr.sdk.ac.SdkLoginActivity.UpdateListener;
import com.hr.sdk.modle.LoginCodeModel;
import com.hr.sdk.modle.Notice;
import com.hr.sdk.modle.PackInfoModel;
import com.hr.sdk.modle.RespModel;
import com.hr.sdk.modle.ResponseData;
import com.hr.sdk.tools.IlongCode;
import com.hr.sdk.tools.Json;
import com.hr.sdk.tools.ToastUtils;
import com.hr.sdk.tools.UpdateUtil;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdk.tools.http.HttpUtil;
import com.hr.sdk.tools.http.NetException;
import com.hr.sdk.tools.http.SdkJsonReqHandler;
import com.hr.util.DeviceUtil;

public class LoginHelper{
	
	private static Activity mActivity;
	private boolean isSilent = false;
	private static LoginHelper mHelper;
	private static LoginSilentListener mLoginListener;
	
	public static LoginHelper getInstance(){
		if(mHelper == null){
			mHelper = new LoginHelper();
		}
		return mHelper;
	}
	
	public void doLogin() {
	HashMap<String, String> map = DeviceUtil.readUserFromFiles(mActivity);
		
		//如果有用户信息，则自动登录
		if (map != null && map.size() > 0) {
			getUpdate(map);
		} else{
			//用户名不存在，直接以游客登录
			//用户名存在，游客用游客方式，正式账号显示登录界面
			getUserFromNet();	
		}
	}
	
	/**游客获取*/
	public void getUserFromNet(){
		String url = Constant.httpHost + Constant.USER_QUICK_REG;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		params.put("pid", DeviceUtil.getUniqueCode(mActivity));
		params.put("version", "201512");
		if(HrSDK.getInstance().getDebugMode()){
			DeviceUtil.appendToDebug("getUserFromNet params: " + params.toString());
		}
		HttpUtil.newIntance().httpGet(mActivity, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, String content) {
				try {
					JSONObject json = new JSONObject(content);
					int errno = json.getInt("errno");
					if(errno == 200){
						String userName = json.getJSONObject("data").getString("username");
						String pid = DeviceUtil.getUniqueCode(mActivity);
						String userinfo = makeUserInfo(Constant.TYPE_USER_NOT_REGISTER, userName, pid);
						//开始登录
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(Constant.KEY_DATA_TYPE, Constant.TYPE_USER_NOT_REGISTER);
				        map.put(Constant.KEY_DATA_USERNAME, userName);
				        map.put(Constant.KEY_DATA_CONTENT, userinfo);
				        if(HrSDK.getInstance().getDebugMode()){
							DeviceUtil.appendToDebug("getUpdate  map: " + map.toString());
						}
						getUpdate(map);
					}else{
						callFailed();				
					}
				} catch (Exception e) {
					e.printStackTrace();
					callFailed();	
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				callFailed();		
			}
		});
	}
	
	
	
	public String makeUserInfo(String type, String userName, String pwd) throws Exception{
		JSONObject json = new JSONObject();
		json.put(Constant.KEY_LOGIN_USERNAME, userName);
		if(type.equals(Constant.TYPE_USER_NORMAL)){
			json.put(Constant.KEY_LOGIN_PWD, pwd);
		}else if(type.equals(Constant.TYPE_USER_NOT_REGISTER)){
			json.put(Constant.KEY_LOGIN_PID, pwd);
		}
		return DeviceUtil.getencodeData(json.toString());
	}
	
	

	public void getUpdate(final HashMap<String, String> map) {
		//执行请求包信息
		String url = Constant.httpHost + Constant.USER_PACK_INFO;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("version", UpdateUtil.getVersion(HrSDK.getActivity()));
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		HttpUtil.newHttpsIntance(mActivity).httpsPost(mActivity, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, String content) {
				try {
					RespModel respModel = Json.StringToObj(content, RespModel.class);
					if (null != respModel && respModel.getErrno() == 200) {
						
						PackInfoModel packInfoModel = Json.StringToObj(respModel.getData(), PackInfoModel.class);
						//论坛地址
						HrSDK.URL_BBS = packInfoModel.getBbs();
						
						if (packInfoModel.getKf() == 1) {
							HrSDK.getInstance().setHasChat(true);
						}
						//设置公告
						setNotice(packInfoModel.getNotice());
//						判断强制更新不？ uptadta>0有更新，force>0 必须更新
						int update = packInfoModel.getUpdate();
						String uri = packInfoModel.getUri();
						if (update > 0 && uri != null && uri.startsWith("http:")) {
							
							HrSDK.showUpdateCancle(mActivity, packInfoModel, new UpdateListener() {
								
								@Override
								public void doLogin() {
									//执行登陆
									login(map);
								}
							});
						} 
						else {
							//执行登陆
							login(map);
						}
					} else {
						callFailed();	
					}
				} catch (Exception e) {
					e.printStackTrace();
					callFailed();
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				DeviceUtil.showToast(mActivity, slException.getMessage());
				callFailed();		
			}
		});
	}
	
	public void callFailed(){
		DeviceUtil.showToast(mActivity, "登录失败");
		HrSDK.getInstance().callbackLogin.onFailed("登录失败");		
	}
	
	public void setNotice(String notice){
		try {
			JSONObject json = new JSONObject(notice);
			HrSDK.mNotice = new Notice(json.getString("id"), json.getString("url"), json.getString("title"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public void login(final HashMap<String, String> map) {
		final String url = Constant.httpHost + Constant.USER_QUICK_LOGIN;
		final Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("sign", map.get(Constant.KEY_DATA_CONTENT));
		params.put("pack_key", HrSDK.getInstance().getSid());
		if(HrSDK.getInstance().getDebugMode()){
			DeviceUtil.getUniqueCode(mActivity);
			DeviceUtil.appendToDebug("login params: " + map.toString() + "\n\n  " + params.toString());
		}
		HttpUtil.newIntance().httpPost(mActivity, url, params, new SdkJsonReqHandler(params) {
			@Override
			public void ReqYes(Object reqObject, final String content) {
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				if (null != respModel) {
					if (respModel.getErrno() == IlongCode.S2C_SUCCESS_CODE) {
						LoginCodeModel codeModel = Json.StringToObj(respModel.getData(), LoginCodeModel.class);
						DeviceUtil.setLogout(mActivity, false);
						//记住用户类型
						if(map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL)){
							HrSDK.TYPE_USER = Constant.TYPE_USER_NORMAL;
							//save
							DeviceUtil.writeUserToFile(map, mActivity);
						}else{
							HrSDK.TYPE_USER = Constant.TYPE_USER_NOT_REGISTER;
						}
						HrSDK.getInstance().callbackLogin.onSuccess(Json.StringToObj(respModel.getData(),ResponseData.class).getCode());
							
//							//公告暂时不放出来
//							String noticeId = DeviceUtil.getData(HrSDK.getActivity(), DeviceUtil.KEY_NOTICE_ID);
//							Logd.d("gst", "手机中获取到的id---"+noticeId);
//							if(noticeId.equals(HrSDK.mNotice.id)){
//								HrSDK.getInstance().callbackLogin.onSuccess(Json.StringToObj(respModel.getData(),ResponseData.class).getCode());
//							}else{
//								goNoticePage(codeModel.getCode());
//							}
																														
//						Gamer.Login("一区");
					} else {
						DeviceUtil.setLogout(mActivity, true);
						Constant.paseError(respModel.getErrno());
						HrSDK.getInstance().callbackLogin.onFailed("登录失败");
						DeviceUtil.showToast(mActivity, "登录失败");
					}
				} else {
					DeviceUtil.showToast(mActivity, "登录失败");
					HrSDK.getInstance().callbackLogin.onFailed("登录失败");
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				
				ToastUtils.show(mActivity, "登录失败," + slException.getMessage());
			}
		});
	}
	
	
	public void goNoticePage(String codeModel){
		Intent it = new Intent(mActivity, ActivityGameNoticePage.class);
		it.putExtra("url", HrSDK.mNotice.url);
		if (HrSDK.mNotice != null && !HrSDK.mNotice.url.isEmpty()) {
			it.putExtra("title", HrSDK.mNotice.title);
			it.putExtra("id", HrSDK.mNotice.id);
			it.putExtra("logincode", codeModel);
			mActivity.startActivity(it);
		} else {
			HrSDK.getInstance().callbackLogin.onSuccess(codeModel);
		}
	}
	
	
	public void init(Activity activity, boolean isSilent) {
		mActivity = activity;
		this.isSilent = isSilent;
	}

}
