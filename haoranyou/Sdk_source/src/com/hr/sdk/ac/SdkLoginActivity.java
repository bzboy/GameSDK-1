package com.hr.sdk.ac;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.hr.sdk.HrSDK;
import com.hr.sdk.dialog.LyProgressDialog;
import com.hr.sdk.dialog.WelcomeToast;
import com.hr.sdk.modle.LoginCodeModel;
import com.hr.sdk.modle.Notice;
import com.hr.sdk.modle.PackInfoModel;
import com.hr.sdk.modle.RespModel;
import com.hr.sdk.modle.ResponseData;
import com.hr.sdk.tools.IlongCode;
import com.hr.sdk.tools.Json;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.ToastUtils;
import com.hr.sdk.tools.UpdateUtil;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdk.tools.http.HttpUtil;
import com.hr.sdk.tools.http.NetException;
import com.hr.sdk.tools.http.SdkJsonReqHandler;
import com.hr.util.DeviceUtil;
import com.loopj.android.http.RequestHandle;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class SdkLoginActivity extends BaseActivity {
	private LayoutInflater layoutInflater = null;
	private LyProgressDialog progressDialog;
	private RequestHandle requestHandle;

	private ViewGroup loginDialogContainer;
	private ViewGroup full_contianer;

	private View normalLoginView;
	private EditText usernameEditText;
	private EditText passwordEditText;

	private boolean isInterrupt = false;
	private View autoLoginView;
	private Handler handler = new Handler();
	
	private boolean isCancelNetwork=false;
	
	private static final String GUESTNAME="guest";
	private static final String GUESTCONTRNT="guestcontent";
	/**
	 * 自动登录的逻辑
	 */
	public void doAutoLogin(HashMap<String, String> map) {
		isInterrupt = false;
		loginDialogContainer.setVisibility(View.GONE);
		full_contianer.setVisibility(View.GONE);
		
		setUserName(map.get(Constant.KEY_DATA_USERNAME));
		View interrupuBtn = autoLoginView.findViewById(ResUtil.getId(this, "ilong_auto_login_btn"));
		Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_BILONG_AUTO_LOGIN);
		String type = map.get(Constant.KEY_DATA_TYPE);
		if(type != null && type.equals(Constant.TYPE_USER_NORMAL)){
			interrupuBtn.setVisibility(View.VISIBLE);
		}else{
			interrupuBtn.setVisibility(View.INVISIBLE);
		}
		
		interrupuBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				try {
					onClickSwitchAccount();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		getUpdate(true, map);
	}
	
	/**点击切换账号*/
	private void onClickSwitchAccount(){
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				isInterrupt = true;
				if (null != requestHandle && !requestHandle.isFinished()) {
					isCancelNetwork=true;
				}
				autoLoginView.setVisibility(View.GONE);
				loginDialogContainer.setVisibility(View.VISIBLE);
				full_contianer.setVisibility(View.VISIBLE);
				dissmissProgressDialog();
			}
		});
		
	}

	private void initView() {
		loginDialogContainer = (FrameLayout) findViewById(ResUtil.getId(this, "ilong_dialog_container"));
		full_contianer = (ViewGroup) findViewById(ResUtil.getId(this, "full_contianer"));
		autoLoginView = findViewById(ResUtil.getId(this, "ilong_auto_login_view"));
		normalLoginView = layoutInflater.inflate(ResUtil.getLayoutId(this, "ilong_layout_login_normal"), loginDialogContainer, false);
		loginDialogContainer.addView(normalLoginView);
		HashMap<String, String> map = DeviceUtil.readUserFromFiles(this);
		initNormalLoginView(map);
		//是否是切换账号类型  如果是切换账号类型，则直接显示登录界面
		boolean isSwitch = getIntent().getBooleanExtra(Constant.TYPE_IS_LOGIN_SWITCH_ACCOUNT, false);
		if(isSwitch){
			showNormalView(false, "");
		}else{			
			//如果有用户信息，则是正式用户，自动登录
			if (map != null && map.size() > 0) {
				if(DeviceUtil.isLogout(this)){
					showNormalView(false, "");
				}else{
					showAutoView();
					doAutoLogin(map);
				}
			} else{
				//用户名不存在，直接以游客登录
				//用户名存在，游客用游客方式，正式账号显示登录界面
				
//				String userType = DeviceUtil.getData(this, DeviceUtil.KEY_UTYPE);
//				if(userType == null || userType.equals("")){
//					//没有任何数据
//					showAutoView();
//					getUserFromNet();
//				}else if(userType.equals(Constant.TYPE_USER_NOT_REGISTER)){
//					//用户类型为游客类型
//					showAutoView();
//					doAutoLoginNotRegister();
//				}
//				else{
//					showNormalView();
//				}	
				showAutoView();
				View interrupuBtn = autoLoginView.findViewById(ResUtil.getId(this, "ilong_auto_login_btn"));
				interrupuBtn.setVisibility(View.INVISIBLE);
				getUserFromNet();
			}
		}
		
	}
	
	private void showAutoView(){
		autoLoginView.setVisibility(View.VISIBLE);
		loginDialogContainer.setVisibility(View.GONE);
		full_contianer.setVisibility(View.GONE);
	}
	
	/**是否是游客，是否显示登录失败*/
	private void showNormalView(boolean isGUest, String msg){
		if(msg != null && msg.length() > 0)
			DeviceUtil.showToast(SdkLoginActivity.this, "登录失败");
		if(isGUest){
			HrSDK.getInstance().callbackLogin.onFailed("登录失败");
			finish();
			return;
		}
		autoLoginView.setVisibility(View.GONE);
		loginDialogContainer.setVisibility(View.VISIBLE);
		full_contianer.setVisibility(View.VISIBLE);
		
	}
	
	

	private void initNormalLoginView(HashMap<String, String> map) {
		// 用户名框
		usernameEditText = (EditText) normalLoginView.findViewById(ResUtil.getId(this, "ilong_username_edittext"));
		// 密码框
		passwordEditText = (EditText) normalLoginView.findViewById(ResUtil.getId(this, "ilong_password_edittext"));
		String userName = map.get(Constant.KEY_DATA_USERNAME);
		if(userName == null || map.get(Constant.KEY_DATA_USERNAME).equals("")){
			userName = DeviceUtil.getData(this, DeviceUtil.KEY_UID);
		}
		String userType = DeviceUtil.getData(this, DeviceUtil.KEY_UTYPE);
		//如果不是游客，则填充用户名、密码
		if(!userType.equals(Constant.TYPE_USER_NOT_REGISTER)){
			usernameEditText.setText(userName);
			if(!DeviceUtil.isLogout(this))
				passwordEditText.setText(DeviceUtil.getData(this, DeviceUtil.KEY_UPWD));
		}
		
		//忘记密码
		normalLoginView.findViewById(ResUtil.getId(this, "forget_password")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_FORGET_PWD_BUTTON);
				v.setEnabled(false);
				Intent intent = new Intent(SdkLoginActivity.this, ActivityWeb.class);
				intent.putExtra("url", Constant.getFrogetPasswordUri().toString());
				intent.putExtra("title", "个人中心");
				startActivity(intent);
				v.setEnabled(true);
			}
		});
		// 一键注册按钮
		View fast_reg_text = normalLoginView.findViewById(ResUtil.getId(this, "fast_reg_text"));
		fast_reg_text.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View view) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_FAST_REG);
				addOneKeyRegistView();
			}
		});
		// 登陆
		Button goIntoGameBtn = (Button) normalLoginView.findViewById(ResUtil.getId(this, "ilong_go_into_game_btn"));
		goIntoGameBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_LOGINACTIVITY_LOGIN_BUTTON);
				// 点击登陆按钮
				final String username = usernameEditText.getText().toString();
				final String password = passwordEditText.getText().toString();
				// 判定账号是否为空
				if (TextUtils.isEmpty(username)) {
					Toast.makeText(SdkLoginActivity.this, "请输入账号", Toast.LENGTH_SHORT).show();
					return;
				}
				if (TextUtils.isEmpty(password)) {
					Toast.makeText(SdkLoginActivity.this, "请输入密码", Toast.LENGTH_SHORT).show();
					return;
				}
				showProgressDialog();
				//执行请求包信息
				HashMap<String, String> map = new HashMap<String, String>();
				map.put(Constant.KEY_DATA_TYPE, Constant.TYPE_USER_NORMAL);
				map.put(Constant.KEY_DATA_USERNAME, username);
				map.put(Constant.KEY_DATA_PWD, password);
				String userinfo = "";
				try {
					userinfo = makeUserInfo(Constant.TYPE_USER_NORMAL, username, password);
				} catch (Exception e) {
					e.printStackTrace();
					Toast.makeText(SdkLoginActivity.this, "登录失败", Toast.LENGTH_LONG).show();
					return;
				}
				map.put(Constant.KEY_DATA_CONTENT, userinfo);
				getUpdate(false, map);
			}
		});
		
	}
	
	private void setUserName(String userName, boolean isNotRegister){
		if(autoLoginView == null || userName == null){
			return;
		}
		TextView usernameTextView = (TextView) autoLoginView.findViewById(ResUtil.getId(this, "ilong_auto_login_username_textview"));
		if(isNotRegister){
			usernameTextView.setText("游客模式");
		}else {
			usernameTextView.setText(userName);
		}
		
	}
	
	private void setUserName(String userName){
		if(autoLoginView == null || userName == null){
			return;
		}
		TextView usernameTextView = (TextView) autoLoginView.findViewById(ResUtil.getId(this, "ilong_auto_login_username_textview"));
		String userType = DeviceUtil.getData(this, DeviceUtil.KEY_UTYPE);
		if(userType.equals(Constant.TYPE_USER_NOT_REGISTER)){
			usernameTextView.setText("游客模式");
		}else {
			usernameTextView.setText(userName);
		}
		
	}
	
	/**游客获取*/
	public void getUserFromNet(){
		showProgressDialog();
		final String url = Constant.httpHost + Constant.USER_QUICK_REG;
		final Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		params.put("pid", DeviceUtil.getUniqueCode(this));
		params.put("version", "201512");
		if(HrSDK.getInstance().getDebugMode()){
			DeviceUtil.appendToDebug("getUserFromNet params: " + params.toString());
		}
		requestHandle = HttpUtil.newIntance().httpGet(this, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, String content) {
				try {
					JSONObject json = new JSONObject(content);
					int errno = json.getInt("errno");
					if(errno == 200){
						String userName = json.getJSONObject("data").getString("username");
						setUserName(userName, true);
						String pid = DeviceUtil.getUniqueCode(SdkLoginActivity.this);
						String userinfo = makeUserInfo(Constant.TYPE_USER_NOT_REGISTER, userName, pid);
						//开始登录
						HashMap<String, String> map = new HashMap<String, String>();
						map.put(Constant.KEY_DATA_TYPE, Constant.TYPE_USER_NOT_REGISTER);
				        map.put(Constant.KEY_DATA_USERNAME, userName);
				        map.put(Constant.KEY_DATA_CONTENT, userinfo);
				        if(HrSDK.getInstance().getDebugMode()){
							DeviceUtil.appendToDebug("getUpdate  map: " + map.toString());
						}
						getUpdate(true, map);
					}else{
						dissmissProgressDialog();
						showNormalView(true, "登录失败");
					}
				} catch (Exception e) {
					e.printStackTrace();
					showNormalView(true, "登录失败");
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				dissmissProgressDialog();
				showNormalView(true, "登录失败");
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
	
	

	public void getUpdate(final boolean autoLogin, final HashMap<String, String> map) {
		//执行请求包信息
		String url = Constant.httpHost + Constant.USER_PACK_INFO;
	
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("version", UpdateUtil.getVersion(HrSDK.getInstance().getActivity()));
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		requestHandle = HttpUtil.newHttpsIntance(SdkLoginActivity.this).httpsPost(SdkLoginActivity.this, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, String content) {
				try {
					Log.e(TAG, content);
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
							hideLoginView();
							HrSDK.showUpdateCancle(SdkLoginActivity.this, packInfoModel, new UpdateListener() {
								
								@Override
								public void doLogin() {
									//执行登陆
									login(autoLogin, map);
								}
							});
						} 
						else {
							//执行登陆
							login(autoLogin, map);
						}
					} else {
						dissmissProgressDialog();
						onClickSwitchAccount();
					}
				} catch (Exception e) {
					e.printStackTrace();
					showNormalView(true, "登录失败");
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				dissmissProgressDialog();
				showNormalView(!map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL), "登录失败，网络异常");
			}
		});
	}
	
	public void setNotice(String notice){
		try {
			if(notice == null || notice.length() == 0) return;
			JSONObject json = new JSONObject(notice);
			HrSDK.mNotice = new Notice(json.optString("id"), json.optString("url"), json.optString("title"));
		} catch (Exception e) {
			e.printStackTrace();
			HrSDK.mNotice = null;
		}
	}
	
	public static interface UpdateListener{
		public void doLogin();
	}

	/**
	 * 
	 * 手机注册
	 */
	public void register(final String phone, final String password, final String verify_code) {
		showProgressDialog();
		String url = Constant.httpHost + Constant.USER_REGISTER_MOBILE;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		params.put("mobile", phone);
		params.put("password", password);
		params.put("code", verify_code);
		requestHandle = HttpUtil.newHttpsIntance(SdkLoginActivity.this).httpsPostJSON(SdkLoginActivity.this, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, final String content) {
				dissmissProgressDialog();
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				onkeyRegistFinishedBtn.setEnabled(true);
				if (respModel.getErrno() == 200) {
					Gamer.sdkCenter.Register(phone, "default",Gamer.sdkCenter.DEFAULT_VAULE, Gamer.sdkCenter.DEFAULT_VAULE, phone);
					usernameEditText.setText(phone);
					passwordEditText.setText(password);
					ToastUtils.show(SdkLoginActivity.this, "注册成功");
					//注册成功，就清除掉注册信息
					ilong_reg_usernme.setText("");
					ilong_reg_pwd.setText("");
					v_edittext.setText("");
					backToNormalLoginView();
				} else if(respModel.getErrno() == Constant.ERRNO_SMSCODE_ERROR){
					ToastUtils.show(SdkLoginActivity.this, "验证码错误，请重新输入验证码");
				} else {
					Constant.paseError(respModel.getErrno());
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				System.out.println(reqObject.toString() + ", " + slException.toString());
				dissmissProgressDialog();
				onkeyRegistFinishedBtn.setEnabled(true);
				ToastUtils.show(SdkLoginActivity.this, "注册失败," + slException.getMessage());
			}
		});
		onkeyRegistFinishedBtn.setEnabled(false);
	}

	/**
	 * 发送验证码
	 * @param phone
	 */
	public void sendSms(final String phone) {
		if(!isMobileNum(phone))
		{
			ToastUtils.show(SdkLoginActivity.this, "请输入正确的号码");
			return;
		}
		showProgressDialog();
		get_verif_button.setEnabled(false);
		String url = Constant.httpHost + Constant.USER_REG_SMS;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		params.put("is_demo", 0);
		params.put("mobile", phone);
		if(true){
			Log.e("", url + ", " + params.toString());
		}
		requestHandle = HttpUtil.newHttpsIntance(SdkLoginActivity.this).httpsPostJSON(SdkLoginActivity.this, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, final String content) {
				dissmissProgressDialog();
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				if (respModel.getErrno() == 200) {
					new TimerDown(get_verif_button, 60 * 1000, 1000).start();
					Toast.makeText(SdkLoginActivity.this, "短信验证码已发送，请注意查收", Toast.LENGTH_LONG).show();
				} else if(respModel.getErrno() ==  Constant.ERRNO_MOBILE_EXISTS){
					ToastUtils.show(SdkLoginActivity.this, "该手机号已绑定");
					get_verif_button.setEnabled(true);
				}else if(respModel.getErrno() ==  Constant.API_ERR_SMS){
					ToastUtils.show(SdkLoginActivity.this, Constant.paseError(respModel.getErrno()));
					get_verif_button.setEnabled(true);
				}
				else{
					get_verif_button.setEnabled(true);
					ToastUtils.show(SdkLoginActivity.this, "请不要频繁发送验证码");
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				dissmissProgressDialog();
				ToastUtils.show(SdkLoginActivity.this, "发送失败," + slException.getMessage());
				get_verif_button.setEnabled(true);
			}
		});
	}

	/**
	 * 多少秒后重新获取
	 * @author niexiaoqiang
	 */
	class TimerDown extends CountDownTimer {
		private Button mButton;

		public TimerDown(Button button, long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
			this.mButton = button;
		}

		@Override
		public void onFinish() {
			mButton.setText("获取验证码");
//			mButton.setTextColor(Color.argb(0XFF, 0X2A, 0X6E, 0xD3));
			mButton.setTextColor(Color.argb(0XFF, 0XFF, 0XFF, 0XFF));
			mButton.setEnabled(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			mButton.setEnabled(false);
//			mButton.setTextColor(Color.argb(0XFF, 0X92, 0X92, 0x92));
			mButton.setTextColor(Color.argb(0XFF, 0XFF, 0XFF, 0XFF));
			mButton.setText("重新获取(" + millisUntilFinished / 1000 + ")S");
		}
	}
	
// 	{"data":{"uid":"2451750","id":"0317d64187c519b85e7c365784bbd74c","phone":"","level":"0",
//	"isPhoneBind":"0","name":"rar2016","score":"0","money":"0"},"errinfo":"","errno":200}
	/**
	 * 
	 * @param autoLogin
	 * @param map
	 */
	public void login(final boolean autoLogin, final HashMap<String, String> map) {
		 String url = Constant.httpHost + Constant.USER_QUICK_LOGIN;
			
			final Map<String, Object> params = new HashMap<String, Object>(0);
			params.put("client_id", HrSDK.getInstance().getAppId());
			params.put("sign", map.get(Constant.KEY_DATA_CONTENT));
			params.put("pack_key", HrSDK.getInstance().getSid());
			if(HrSDK.getInstance().getDebugMode()){
				DeviceUtil.getUniqueCode(this);
				DeviceUtil.appendToDebug("login params: " + map.toString() + "\n\n  " + params.toString());
			}
		requestHandle = HttpUtil.newIntance().httpPost(SdkLoginActivity.this, url, params, new SdkJsonReqHandler(params) {
			@Override
			public void ReqYes(Object reqObject, final String content) {
			
				if(isCancelNetwork)
					return;
				isCancelNetwork=false;
				if(HrSDK.getInstance().getDebugMode()){
					DeviceUtil.appendToDebug("login result: " + content.toString());
				}
				int delytime = 0;
				if (autoLogin) {
					delytime = 1500;
				}
				new Handler().postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
						dissmissProgressDialog();
						if (autoLogin && isInterrupt) {
							return;
						}
						RespModel respModel = Json.StringToObj(content, RespModel.class);
						if (null != respModel) {
							if (respModel.getErrno() == IlongCode.S2C_SUCCESS_CODE) {
								LoginCodeModel codeModel = Json.StringToObj(respModel.getData(), LoginCodeModel.class);
//								这是测试数据
//								Gamer.Login("一区");
								String type = map.get(Constant.KEY_DATA_TYPE);
								String name = map.get(Constant.KEY_DATA_USERNAME);
								if(type.equals(Constant.TYPE_USER_NOT_REGISTER)){
									name = "游客" + name;
								}
								DeviceUtil.setLogout(SdkLoginActivity.this, false);
								new WelcomeToast(SdkLoginActivity.this, name).show();
								//记住用户类型
								if(map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL)){
									HrSDK.TYPE_USER = Constant.TYPE_USER_NORMAL;
									//save
									DeviceUtil.writeUserToFile(map, SdkLoginActivity.this);
								}else{
									HrSDK.TYPE_USER = Constant.TYPE_USER_NOT_REGISTER;
//									IlongSDK.mUserInfo=new UserInfo("游客","", "", "0", "0",Json.StringToObj(respModel.getData(),ResponseData.class).getOpenId(),"0","");
									//游客也保存，读取的时候注意，如果是游客，则不用
//									DeviceUtil.writeUserToFile(map, SdkLoginActivity.this);
									DeviceUtil.saveData(SdkLoginActivity.this, HrSDK.getInstance().getAppId(), JSON.toJSONString(map));
								}
								//如果有密码，则保存密码
								if(map.containsKey(Constant.KEY_DATA_PWD)){
									DeviceUtil.saveData(SdkLoginActivity.this, DeviceUtil.KEY_UPWD, map.get(Constant.KEY_DATA_PWD));
								}
								
								//是否是切换账号类型  如果是切换账号类型，回调切换账号
								boolean isSwitch = getIntent().getBooleanExtra(Constant.TYPE_IS_LOGIN_SWITCH_ACCOUNT, false);
								String auth_code = Json.StringToObj(respModel.getData(),ResponseData.class).getCode();
								if(isSwitch){
									HrSDK.getInstance().callbackLogin.onSwitchAccount(auth_code);
								}else{
									HrSDK.getInstance().callbackLogin.onSuccess(auth_code);
//									//公告暂时不放出来
//									String noticeId = DeviceUtil.getData(SdkLoginActivity.this,DeviceUtil.KEY_NOTICE_ID);
////									Logd.d("gst", "手机中获取到的id---"+noticeId);
//									if(noticeId.equals(HrSDK.mNotice.id)){
//							
//										HrSDK.getInstance().callbackLogin.onSuccess(auth_code);
//									}else{
//										goNoticePage(auth_code);
//									}
																										
								}
								finish();
							} else {
								DeviceUtil.setLogout(SdkLoginActivity.this, true);
								Constant.paseError(respModel.getErrno());
								showNormalView(!map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL), "登录失败");
							}
						} else {
							showNormalView(!map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL), "登录失败");
						}
						} catch (Exception e) {
							e.printStackTrace();
							ToastUtils.show(SdkLoginActivity.this, "登录失败");
							showNormalView(!map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL), "登录失败");
						}
					}
				}, delytime);
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				ToastUtils.show(SdkLoginActivity.this, "登录失败," + slException.getMessage());
				dissmissProgressDialog();
				showNormalView(!map.get(Constant.KEY_DATA_TYPE).equals(Constant.TYPE_USER_NORMAL), "登录失败");
			}
		});
	}
	
	
	public void goNoticePage(String codeModel){
		Intent it = new Intent(SdkLoginActivity.this,
				ActivityGameNoticePage.class);
		it.putExtra("url", HrSDK.mNotice.url);
		if (HrSDK.mNotice != null && !HrSDK.mNotice.url.isEmpty()) {
			it.putExtra("title", HrSDK.mNotice.title);
			it.putExtra("id", HrSDK.mNotice.id);
			it.putExtra("logincode", codeModel);
			startActivity(it);

		} else {
			HrSDK.getInstance().callbackLogin.onSuccess(codeModel);
		}
	
		
		
	}
	
	public  boolean verifyRegParam(Context context, String userName, String pwd, String pwd2){
		if(userName.length() < 6 || userName.length() > 16){
			Toast.makeText(context, "用户名请输入6-16位字母或数字", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(! Pattern.compile("^[A-Za-z]+$").matcher(userName.substring(0, 1)).matches()){
			Toast.makeText(context, "请输入以字母开头的用户名，可以包含数字", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(! Pattern.compile("^[0-9a-zA-Z]+$").matcher(userName).matches()){
			Toast.makeText(context, "用户名只能包含字母和数字!", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(pwd.length() < 6 || pwd.length() > 16){
			Toast.makeText(context, "密码请输入6-16位字母或数字", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(! Pattern.compile("^[0-9a-zA-Z]+$").matcher(pwd).matches()){
			Toast.makeText(context, "密码只能包含字母和数字!", Toast.LENGTH_LONG).show();
			return false;
		}
		
		if(! pwd.equals(pwd2)){
			Toast.makeText(context, "两次输入密码不一致", Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
	
	/**以用户名注册*/
	private void regWithUname(final String userName, final String pwd, String pwd2){
		String url = Constant.httpHost + Constant.USER_REGISTER_USERNAME;
		if(! verifyRegParam(this, userName, pwd, pwd2)){
			return;
		}
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("client_id", HrSDK.getInstance().getAppId());
		params.put("pack_key", HrSDK.getInstance().getSid());
		params.put("username", userName);
		params.put("password", pwd);
		requestHandle = HttpUtil.newHttpsIntance(SdkLoginActivity.this).httpsPostJSON(SdkLoginActivity.this, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, final String content) {
				dissmissProgressDialog();
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				onkeyRegistFinishedBtn.setEnabled(true);
				if (respModel.getErrno() == 200) {
					Gamer.sdkCenter.Register(userName, "default",Gamer.sdkCenter.DEFAULT_VAULE, Gamer.sdkCenter.DEFAULT_VAULE, Gamer.sdkCenter.DEFAULT_VAULE);
					usernameEditText.setText(userName);
					passwordEditText.setText(pwd);
					ToastUtils.show(SdkLoginActivity.this, "注册成功");
					//注册成功就清空消息
					ilong_reg_usernme.setText("");
					ilong_reg_pwd.setText("");
					v_edittext.setText("");
					backToNormalLoginView();
				} else {
					Constant.paseError(respModel.getErrno());
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				System.out.println(reqObject.toString() + ", " + slException.toString());
				dissmissProgressDialog();
				onkeyRegistFinishedBtn.setEnabled(true);
				ToastUtils.show(SdkLoginActivity.this, "注册失败," + slException.getMessage());
			}
		});
		onkeyRegistFinishedBtn.setEnabled(false);
	}
	
	
	public void hideLoginView() {
		if (autoLoginView != null)
			autoLoginView.setVisibility(View.GONE);
		if (loginDialogContainer != null) {
			loginDialogContainer.setVisibility(View.GONE);
			full_contianer.setVisibility(View.GONE);
		}
		dissmissProgressDialog();
	}

	private void backToNormalLoginView() {
		//执行动画
		ObjectAnimator inLogin = ObjectAnimator.ofFloat(normalLoginView, "translationX", 0f);
		inLogin.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				usernameEditText.setEnabled(true);
				passwordEditText.setEnabled(true);
				ilong_reg_usernme.setEnabled(false);
				ilong_reg_pwd.setEnabled(false);
				v_edittext.setEnabled(false);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}
		});
		inLogin.setDuration(800);
		inLogin.setInterpolator(new OvershootInterpolator());
		ObjectAnimator outReg = ObjectAnimator.ofFloat(oneKeyRegistView, "translationX", (float) loginDialogContainer.getWidth());
		outReg.setDuration(800);
		outReg.setInterpolator(new OvershootInterpolator());
		inLogin.start();
		outReg.start();
	}

	private View oneKeyRegistView;
	private TextView ilong_back_login_btn;
	private Button onkeyRegistFinishedBtn;
	private Button get_verif_button;
	private EditText ilong_reg_usernme;
	private EditText ilong_reg_pwd;
	private EditText v_edittext;
	
	/**顶部的蓝色条 以及 两个按钮*/
	private Button btnBar1, btnBar2, btnReg1, btnReg2;
	/**验证码签名的勾勾*/
	private View v_icon;
	/**等待*/
	private View ilong_progress_login;
	/**主输入区域*/
	private View ilong_reg_content;
	/**用户名 icon*/
	private ImageView name_icon;
	
	private String[] hints = new String[]{
			"请输入手机号",
			"请输入密码",
			"输入验证码",
			"请输入用户名",
			"请输入密码",
			"请确认密码"
	};
	
	private void showLoginProgress(){
		ilong_progress_login.setVisibility(View.VISIBLE);
		ilong_reg_content.setVisibility(View.INVISIBLE);
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				ilong_progress_login.setVisibility(View.GONE);
				ilong_reg_content.setVisibility(View.VISIBLE);
				btnReg1.setEnabled(true);
				btnReg2.setEnabled(true);
			}
		}, 600);
	}
	
	private void initTopButton(View root){
		btnBar1 = (Button) root.findViewById(ResUtil.getId(this, "ilong_bar_1"));
		btnBar2 = (Button) root.findViewById(ResUtil.getId(this, "ilong_bar_2"));
		btnReg1 = (Button) root.findViewById(ResUtil.getId(this, "ilong_reg_1"));
		btnReg2 = (Button) root.findViewById(ResUtil.getId(this, "ilong_reg_2"));
		v_icon = root.findViewById(ResUtil.getId(this, "v_icon"));
		ilong_progress_login = root.findViewById(ResUtil.getId(this, "ilong_progress_login"));
		ilong_reg_content = root.findViewById(ResUtil.getId(this, "ilong_reg_content"));
		name_icon = (ImageView)(root.findViewById(ResUtil.getId(this, "name_icon")));
		
		btnBar1.setSelected(true);
		btnReg1.setSelected(true);
		btnReg1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_REG_PHONE );
				if(v.isSelected()) return;
				v.setEnabled(false);
				showLoginProgress();
				ilong_reg_usernme.setText("");
				ilong_reg_pwd.setText("");
				v_edittext.setText("");				
				
				btnBar1.setSelected(true);
				btnBar2.setSelected(false);
				btnReg1.setSelected(true);
				btnReg2.setSelected(false);
				
				ilong_reg_usernme.setInputType(InputType.TYPE_CLASS_PHONE);
				v_edittext.setInputType(InputType.TYPE_CLASS_PHONE);
				
				ilong_reg_usernme.setHint(hints[0]);
				InputFilter[] filters = {new InputFilter.LengthFilter(11)};
				ilong_reg_usernme.setFilters(filters);
				ilong_reg_pwd.setHint(hints[1]);
				v_edittext.setHint(hints[2]);
				v_icon.setVisibility(View.VISIBLE);
				get_verif_button.setVisibility(View.VISIBLE);
				name_icon.setImageResource(ResUtil.getDrawableId(SdkLoginActivity.this, "ilong_icon_phone"));
				
			}
		});
		
		btnReg2.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_REG_NAME );
				if(v.isSelected()) return;
				v.setEnabled(false);
				showLoginProgress();
				
				ilong_reg_usernme.setText("");
				ilong_reg_pwd.setText("");
				v_edittext.setText("");	
				
				btnBar2.setSelected(true);
				btnBar1.setSelected(false);
				btnReg1.setSelected(false);
				btnReg2.setSelected(true);
				ilong_reg_usernme.setInputType(InputType.TYPE_CLASS_TEXT);
				v_edittext.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
				
				ilong_reg_usernme.setHint(hints[3]);
				InputFilter[] filters = {new InputFilter.LengthFilter(16)};
				ilong_reg_usernme.setFilters(filters);
				ilong_reg_pwd.setHint(hints[4]);
				v_edittext.setHint(hints[5]);
				v_icon.setVisibility(View.INVISIBLE);
				get_verif_button.setVisibility(View.INVISIBLE);
				name_icon.setImageResource(ResUtil.getDrawableId(SdkLoginActivity.this, "ilong_icon_phone"));
			}
		});
	}

	private void addOneKeyRegistView() {
		if (oneKeyRegistView == null) {
			oneKeyRegistView = layoutInflater.inflate(ResUtil.getLayoutId(this, "ilong_layout_onekey_regist"), loginDialogContainer, false);
			
			ilong_reg_usernme = (EditText) oneKeyRegistView.findViewById(ResUtil.getId(this, "ilong_reg_usernme"));
			ilong_reg_pwd = (EditText) oneKeyRegistView.findViewById(ResUtil.getId(this, "ilong_reg_pwd"));
			v_edittext = (EditText) oneKeyRegistView.findViewById(ResUtil.getId(this, "v_edittext"));
			ilong_back_login_btn = (TextView) oneKeyRegistView.findViewById(ResUtil.getId(this, "ilong_back_login_btn"));
			ilong_back_login_btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ILONG_HAS_ACCOUNT);
					backToNormalLoginView();
				}
			});
			onkeyRegistFinishedBtn = (Button) oneKeyRegistView.findViewById(ResUtil.getId(this, "ilong_onkey_regist_finished"));
			onkeyRegistFinishedBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_REGIST_FINISHED_BUTTON );
					String phone = ilong_reg_usernme.getText().toString().trim();
					String passwprd = ilong_reg_pwd.getText().toString().trim();
					String vcode = v_edittext.getText().toString().trim();
					if(btnReg1.isSelected()){
						//获取验证码
						if (null == phone || phone.isEmpty() || phone.length() != 11 || !phone.startsWith("1")) {
							ToastUtils.show(SdkLoginActivity.this, "请输入11位手机号");
							return;
						}
						
						if (null == passwprd || passwprd.isEmpty()) {
							ToastUtils.show(SdkLoginActivity.this, "请输入密码");
							return;
						}
						if(passwprd.length() < 6){
							ToastUtils.show(SdkLoginActivity.this, "密码长度不能小于6位");
							return;
						}
						if(passwprd.length() > 16){
							ToastUtils.show(SdkLoginActivity.this, "密码长度不能大于16位");
							return;
						}
						if (null == vcode || vcode.isEmpty() || vcode.length() < 4 || vcode.length() > 8) {
							ToastUtils.show(SdkLoginActivity.this, "请输入正确的验证码");
							return;
						}
						register(phone, passwprd, vcode);
					}else{
						regWithUname(phone, passwprd, vcode);
					}
				}
			});
			get_verif_button = (Button) oneKeyRegistView.findViewById(ResUtil.getId(this, "get_verif_button"));
			get_verif_button.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_GET_VERIFY_CODE);
					//获取验证码
					String phone = ilong_reg_usernme.getText().toString();
					if (null == phone || phone.isEmpty()) {
						ToastUtils.show(SdkLoginActivity.this, "请输入手机号");
						return;
					}
					sendSms(phone);
				}
			});
			initTopButton(oneKeyRegistView);
			
			ViewHelper.setTranslationX(oneKeyRegistView, loginDialogContainer.getWidth());
			loginDialogContainer.addView(oneKeyRegistView);
		}
		//执行动画
		ObjectAnimator outLogin = ObjectAnimator.ofFloat(normalLoginView, "translationX", (float) -loginDialogContainer.getWidth());
		outLogin.setDuration(800);
		outLogin.addListener(new AnimatorListener() {

			@Override
			public void onAnimationStart(Animator arg0) {

			}

			@Override
			public void onAnimationRepeat(Animator arg0) {

			}

			@Override
			public void onAnimationEnd(Animator arg0) {
				usernameEditText.setEnabled(false);
				passwordEditText.setEnabled(false);
				v_edittext.setEnabled(true);
				ilong_reg_pwd.setEnabled(true);
				ilong_reg_usernme.setEnabled(true);
			}

			@Override
			public void onAnimationCancel(Animator arg0) {

			}
		});

		outLogin.setInterpolator(new OvershootInterpolator());
		ObjectAnimator inReg = ObjectAnimator.ofFloat(oneKeyRegistView, "translationX", (float) 0);
		inReg.setDuration(800);
		inReg.setInterpolator(new OvershootInterpolator());
		outLogin.start();
		inReg.start();
	}
	@Override
	public void onResume(){
		super.onResume();
		dissmissProgressDialog();

	}

	/**
	 * 显示加载框
	 */
	private void showProgressDialog() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (progressDialog == null) {
					progressDialog = new LyProgressDialog(SdkLoginActivity.this);
				}
				Log.e("TAG", "show()....");
				progressDialog.show();
			}
		});
		
	}

	/**
	 * 隐藏加载框
	 */
	private void dissmissProgressDialog() {
		handler.post(new Runnable() {
			
			@Override
			public void run() {
				if (progressDialog != null && progressDialog.isShowing()) {
					progressDialog.dismiss();
				}
			}
		});
		
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		layoutInflater = LayoutInflater.from(this);
		setContentView(ResUtil.getLayoutId(this, "ilong_activity_sdk"));
		setBackPress();
		initView();

	}
	
	public void setBackPress(){
		if(HrSDK.getInstance().getBackEable()){
			//设置在4秒钟之后，才能取消登录界面
			HrSDK.getInstance().setBackEable(false);
			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				
				@Override
				public void run() {
					HrSDK.getInstance().setBackEable(true);
				}
			}, 1000);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        	if(HrSDK.getInstance().getBackEable()){
        		HrSDK.getInstance().callbackLogin.onCancel();
        		return super.onKeyDown(keyCode, event);
        	}else{
        		return true;
        	}
        }
        return super.onKeyDown(keyCode, event);
    }
	
	public interface LoginSilentListener{
		void onLoginSuccess();
		void onLoginFailed();
	}

	public boolean isMobileNum(String mobiles) {
		if(mobiles.length()!=11||!mobiles.startsWith("1")||!mobiles.matches("[0-9]*"))
			return false;
        return true;
    }
	


	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return ActivityName;
	}
	private String ActivityName = "com.hr.sdk.ac.SdkLoginActivity";
}
