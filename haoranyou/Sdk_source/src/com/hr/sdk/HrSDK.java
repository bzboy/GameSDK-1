package com.hr.sdk;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.hr.sdk.ac.ActivityGameNoticePage;
import com.hr.sdk.ac.ActivityUser;
import com.hr.sdk.ac.ExitSDKActivity;
import com.hr.sdk.ac.LoginHelper;
import com.hr.sdk.ac.SdkLoginActivity;
import com.hr.sdk.ac.SdkLoginActivity.UpdateListener;
import com.hr.sdk.gamecenter.GameService;
import com.hr.sdk.i.ILongExitCallback;
import com.hr.sdk.i.ILongInitCallback;
import com.hr.sdk.i.ILongPayCallback;
import com.hr.sdk.i.IToken2UserInfo;
import com.hr.sdk.i.IlongGame;
import com.hr.sdk.i.IlongLoginCallBack;
import com.hr.sdk.modle.Notice;
import com.hr.sdk.modle.PackInfoModel;
import com.hr.sdk.modle.RespModel;
import com.hr.sdk.modle.UserInfo;
import com.hr.sdk.pay.LyPayActivity;
import com.hr.sdk.tools.IlongCode;
import com.hr.sdk.tools.Json;
import com.hr.sdk.tools.LogUtils;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.SDKMark;
import com.hr.sdk.tools.ToastTipString;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdk.tools.http.HttpUtil;
import com.hr.sdk.tools.http.NetException;
import com.hr.sdk.tools.http.SdkJsonReqHandler;
import com.hr.util.DeviceUtil;
import com.hr.util.Logd;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;

/**
 * SDK
 * @author niexiaoqiang
 */
public class HrSDK implements IlongGame {
	public static String appId = "";
	private static String lySid = "0";
	private Activity mActivity = null;

	public static UserInfo mUserInfo = null;
	public static String mToken = null;
	private boolean hasChat = false;
	
	public ILongInitCallback callbackInit = null;
	public IlongLoginCallBack callbackLogin = null;
	public ILongPayCallback callbackPay = null;
	public ILongExitCallback callbackExit = null;
	
	public static DisplayMetrics screenInfo = new DisplayMetrics();
	//论坛地址
	public static String URL_BBS = "";
	private boolean isDebug = false;
	private boolean isBackEable = true;
	
	private boolean isInited = false;
	
	public static final String TAG = "HrSDK";
	
	public static Notice mNotice;
	
	public static String debugInfo = "";
	/**用户类型*/
	public static String TYPE_USER = Constant.TYPE_USER_NORMAL;
	
	/**是否显示登录界面*/
	private boolean isShowLoginView = true;
	public IToken2UserInfo iToken2UserInfo;
	
	/**accountId 游戏用户id.默认值设为 unKnown */
	public static String AccountId = "unknown";
	
	public void setUserToken(Context context, final String token, final IToken2UserInfo iToken2UserInfo) {
		this.iToken2UserInfo = iToken2UserInfo;
		if(token == null || token.length() == 0 || iToken2UserInfo == null){
			Log.e(TAG, "setUserToken 参数错误");
			showToast("setUserToken 参数错误");
			return;
		}
		mToken = token;
		updateUserInfo(context, token, iToken2UserInfo);
	}

	public void updateUserInfo(Context context, final String token, final IToken2UserInfo iToken2UserInfo) {
		String url = Constant.httpHost + Constant.USER_DETAIL;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("access_token", token);
		HttpUtil.newHttpsIntance(context).httpPost(context, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, final String content) {
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				if (null != respModel && respModel.getErrno() == IlongCode.S2C_SUCCESS_CODE) {
					mUserInfo = Json.StringToObj(respModel.getData(), UserInfo.class);
					AccountId =mUserInfo.getUid();
					Gamer.sdkCenter.Login(AccountId);
					DeviceUtil.saveData(mActivity, mUserInfo.getId()+"haspay_pwd", mUserInfo.getPay_password());
				}
				showNotice();
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				iToken2UserInfo.onFailed();
			}
		});
	}
	
	public void showNotice(){
		if(HrSDK.mNotice == null) {
			iToken2UserInfo.onSuccess(mUserInfo);
			return;
		}
		String noticeId = DeviceUtil.getData(mActivity,DeviceUtil.KEY_NOTICE_ID);

		if(!noticeId.equals(HrSDK.mNotice.id)){
			Intent it = new Intent(mActivity, ActivityGameNoticePage.class);
			it.putExtra("url", HrSDK.mNotice.url);
			it.putExtra("title", HrSDK.mNotice.title);
			it.putExtra("id", HrSDK.mNotice.id);
			
			mActivity.startActivity(it);
		}
	}

	public boolean isHasChat() {
		return hasChat;
	}

	public void setHasChat(boolean hasChat) {
		this.hasChat = hasChat;
	}

	public static void initAppId(Activity a) {
		try {
			ApplicationInfo info = a.getPackageManager().getApplicationInfo(a.getPackageName(), PackageManager.GET_META_DATA);
			appId = info.metaData.getString("GAME_APPID");
			if(TextUtils.isEmpty(appId)){
				showToast("检测到GAME_APPID为空值，请配置GAME_APPID");
			}
		} catch (Throwable e) {
			showToast(ToastTipString.NotSetAppId);
			LogUtils.error(e);
		}
	}

	public void setSid(String sid) {
		lySid = sid;
	}

	public String initSid(Activity a) {
		lySid = SDKMark.getMark(a);
		if(lySid == null || lySid.length() == 0){
			lySid = "0";
			Logd.d("LYSDK", "sid is default");
		}
		return lySid;
	}
	
	public HrSDK setDebugModel(boolean flag){
		isDebug = flag;
		return getInstance();
	}
	
	public boolean getDebugMode(){
		return isDebug;
	}
	
	public HrSDK setBackEable(boolean flag){
		isBackEable = flag;
		return getInstance();
	}
	
	public boolean getBackEable(){
		return isBackEable;
	}
	
	

	public boolean isShowLoginView() {
		return isShowLoginView;
	}

	public HrSDK setShowLoginView(boolean isShowLoginView) {
		this.isShowLoginView = isShowLoginView;
		return getInstance();
	}

	public String getSid() {
		if (lySid.length() <= 0) {
			LogUtils.warn("SID为空,请先初始化!!!");
		}
		return lySid;
	}
	
	private static void initScreen(final Context context){
		WindowManager mWindowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
		mWindowManager.getDefaultDisplay().getMetrics(screenInfo);
	}

	@Override
	public void init(Activity a, ILongInitCallback callbackInit, IlongLoginCallBack callbackLogin, 
			ILongPayCallback callbackPay, ILongExitCallback callbackExit) {
		if (a == null || callbackInit == null || callbackLogin == null || callbackPay == null
				|| callbackExit == null) {
			Log.e(TAG, "初始化参数错误，有空指针");
			callbackInit.onFailed();
			return;
		}
		this.mActivity = a;
		this.callbackInit = callbackInit;
		this.callbackLogin = callbackLogin;
		this.callbackPay = callbackPay;
		this.callbackExit = callbackExit;
		initAppId(mActivity);
	
		initSid(mActivity);
		initScreen(mActivity);
		//初始化debug信息
//		Gamer.isDebug=isDebug;
        //初始化数据收集，包括异常收集
		mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
//				Gamer.init((Context)mActivity);
				Gamer.init(mActivity, "hr", false);
			}
		});
		
		DeviceUtil.isMoved(mActivity);
		
		DeviceUtil.initDebug();
		if(isDebug){
			showToast("当前是debug模式，正式发布请将debug设置为false或不调用setDebugModel");
		}
		if (appId.length() > 0 && lySid.length() > 0){
			isInited = true;
			callbackInit.onSuccess();
		}
		else {
			callbackInit.onFailed();
		}
		Logd.e(TAG, "init call .....");
	}

	@Override
	public void login() {	
		if(! isInited){
			Logd.e(TAG, "请先初始化");
			return;
		}
		
		HashMap<String, String> map = DeviceUtil.readUserFromFiles(mActivity);
		boolean isGuest = (map == null || map.size() == 0);
		
		if(isShowLoginView || DeviceUtil.isLogout(mActivity)){
			//游客不受注销限制
			if(isGuest && !isShowLoginView){
				LoginHelper.getInstance().init(mActivity, true);
				LoginHelper.getInstance().doLogin();
				return;
			}
			Intent loginIntent = new Intent(mActivity, SdkLoginActivity.class);
			mActivity.startActivity(loginIntent);
		}else{
			LoginHelper.getInstance().init(mActivity, true);
			mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					LoginHelper.getInstance().doLogin();
				}
			});
			
		}
	}

	@Override
	public void logout() {
		DeviceUtil.setLogout(mActivity, true);
		hideFloatView();
		mToken = null;
		//数据收集
		Gamer.sdkCenter.logout(AccountId);
		callbackLogin.onLogout();

	}
	
	public void onPause() {
		hideFloatView();
//		if(Gamer.isSaveQueue())
//			DeviceUtil.saveData(mActivity, Gamer.LoclQueueName, JSON.toJSONString(Gamer.Waiting_Queue_Signal));
		Gamer.onPause();
	}

	public void onResume() {
		if (mToken != null && mToken.length() > 0)
			showFloatView();
		Gamer.onResume();
	}

	@Override
	public void showFloatView() {
		Intent floatIntent = new Intent(mActivity, GameService.class);
		mActivity.startService(floatIntent);
	}

	@Override
	public void hideFloatView() {
		Intent floatIntent = new Intent(mActivity, GameService.class);
		mActivity.stopService(floatIntent);
	}

	@Override
	public void exitSDK() {
		Intent exitIntent = new Intent(mActivity, ExitSDKActivity.class);
		mActivity.startActivity(exitIntent);
	}
	
	private boolean verifyParam(Bundle b){
		try {
			if (mToken == null || mToken.equals("")) {
				Toast.makeText(mActivity, "请先登录", Toast.LENGTH_LONG).show();
				callbackPay.onFailed();
				callbackLogin.onLogout();
				return false;
			}
			String amount = b.getString("amount");
			String orderId = b.getString("app_order_id");
			String uid = b.getString("app_uid");
			String notify_uri = b.getString("notify_uri");
			String product_name = b.getString("product_name");
			String product_id = b.getString("product_id");
			String userName = b.getString("app_username");
			
			if(amount == null || amount.length() < 4){
				Logd.e(TAG, "amount 错误，请保留两位小数");
			}
			if(orderId == null || orderId.length() == 0){
				Logd.e(TAG, "订单号不能为空");
			}
			if(notify_uri == null || ! notify_uri.startsWith("http")){
				Logd.e(TAG, "回调地址错误");
			}
			if(product_name == null || product_name.length() < 1){
				Logd.e(TAG, "product_name 不能为空");
			}
			if(product_id == null || product_id.length() < 1){
				Logd.e(TAG, "product_id 不能为空");
			}
			if(userName == null || userName.length() == 0){
				Log.e(TAG, "app_username 不能为空");
			}
			
			if(uid == null || uid.length() == 0){
				Logd.e(TAG, "uid 不能为空");
			}
			
			b.putString("access_token", mToken);//用户登陆成功之后用authcode 获取的accessToken 值
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return false;
	}

	@Override
	public void pay(Bundle bundle) {
		if(!verifyParam(bundle)){
			showToast("支付参数错误");
			callbackPay.onFailed();
			return;
		}
		if(mUserInfo == null){
			showToast("登录失效，请重新登录");
			callbackPay.onFailed();
			callbackLogin.onLogout();
			Logd.e(TAG, "userinfo is null");
			return ;
		}
		Intent payIntent = new Intent(mActivity, LyPayActivity.class);
		bundle.putString("uid", mUserInfo.getId());//用户登陆成功之后获取到的uid
		payIntent.putExtras(bundle);
		mActivity.startActivity(payIntent);
	}
	
	public static void showToast(final String msg){
		if(getInstance().mActivity == null){
			Log.e(TAG, "activity is null, 请初始化！");
			return;
		}
		getInstance().mActivity.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(getInstance().mActivity.getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		});
	}

	public String getAppId() {
		if(TextUtils.isEmpty(appId)){
			initAppId(mActivity);
		}
		return appId;
	}

	private static HrSDK ilongSDK;

	private HrSDK() {
	}

	public static HrSDK getInstance() {
		if (ilongSDK == null) {
			ilongSDK = new HrSDK();
		}
		return ilongSDK;
	}

	public static Activity getActivity() {
		return getInstance().mActivity;
	}

//	public static void showUpdate(final Activity activity, final String uri) {
//		Dialog dialog = new Dialog(activity, ResUtil.getStyleId(activity, "Dialog_update"));
//		View view = LayoutInflater.from(activity).inflate(ResUtil.getLayoutId(activity, "ilong_activity_update_sdk"), null);
//		dialog.setContentView(view);
//		Button btnConfirm = (Button) view.findViewById(ResUtil.getId(activity, "btn_confirm"));
//		dialog.setCancelable(false);
//		dialog.setCanceledOnTouchOutside(false);
//		btnConfirm.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
//				it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				activity.startActivity(it);
//				activity.finish();
//			}
//		});
//		dialog.show();
//	}
	
	//是否 展示强制更新  Dialog
	public static Dialog dialogUpdate;
		public static void showUpdateCancle(final Activity activity, final PackInfoModel pInfo, final UpdateListener mListener) {

			dialogUpdate = new Dialog(activity, ResUtil.getStyleId(activity,"ilongyuanAppUpdataCanCancle"));
			View appUpdataPage = activity.getLayoutInflater().inflate(
					ResUtil.getLayoutId(activity,"ilong_app_updata_can_cancle_dialog"), null);
			Button CancleBtn = (Button) appUpdataPage.findViewById(ResUtil.getId(activity, "appUpdataCancle"));

			Button OKUpdataBtn = (Button) appUpdataPage.findViewById(ResUtil.getId(activity, "appUpdataOk"));
			
			View view = appUpdataPage.findViewById(ResUtil.getId(activity, "CancleBtnfatherReleLayout"));
			TextView content = (TextView)appUpdataPage.findViewById(ResUtil.getId(activity, "ilong_update_content"));
			content.setText(pInfo.getUpdate_msg());
			if (pInfo.getForce() == 1) {
				CancleBtn.setVisibility(View.GONE);
				view.setVisibility(View.GONE);
			}

			CancleBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					Gamer.sdkCenter.ButtonClick(AccountId,ButtonTypeConstant.TYPE_BUTTON_APPUPDATA_CANCLE );
					mListener.doLogin();
					hideDialog();
				}
			});

			OKUpdataBtn.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					hideDialog();
					Gamer.sdkCenter.ButtonClick(AccountId,ButtonTypeConstant.TYPE_BUTTON_APPUPDATA_OK );
					Intent it = new Intent(Intent.ACTION_VIEW, Uri.parse(pInfo.getUri()));
					it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					activity.startActivity(it);
					activity.finish();

				}
			});

			dialogUpdate.setCancelable(false);
			dialogUpdate.setCanceledOnTouchOutside(false);
			dialogUpdate.setContentView(appUpdataPage);
			dialogUpdate.show();

		}
	public static void hideDialog(){
		if(dialogUpdate != null && dialogUpdate.isShowing()){
			dialogUpdate.cancel();
		}
	}
	
	@Override
	public void showUserCenter(){
		if(mActivity == null){
			Logd.e(TAG, "mactivity is null");
			return;
		}
		if(mUserInfo == null || mToken == null || mToken.length() == 0){
			DeviceUtil.showToast(mActivity, "请先登录");
			return;
		}
		Intent intent = new Intent(mActivity, ActivityUser.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mActivity.startActivity(intent);
	}
	
	/**
	 * 清除缓存
	 */
	public void deleteCache(){
		DeviceUtil.deleteFolderFile(DeviceUtil.getBasePath(mActivity), true);
		DeviceUtil.clearApplicationData(mActivity);
	}
}



