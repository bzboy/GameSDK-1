package com.hr.sdk.ac;

import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import com.hr.sdk.HrSDK;
import com.hr.sdk.dialog.IlongBasicDialog;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.http.Constant;
import com.hr.util.DeviceUtil;
import com.hr.util.Logd;

public class ActivityWeb extends BaseActivity implements WebCall{
	private String TAG ="ActivityWeb";
	private ImageView loadingIv;
	private Animation loadingAnim;
	private TextView titletext ;
	private View backView ;
//	private View shareView ;
	private View title;
	
	private WebView web;
	private final String CompanyUrl = "http://www.ilongyuan.com.cn/";
	
	/**传递过来的mUserInfo的id*/
	private String id  = "";
	/**微信APPID*/
	public static  String ILONGYUAN_WX_APPID = ""; 
	/**IWAPI 是第三方app和微信通信的openapi接口*/
//	private IWXAPI api;
	/**微信支付 朋友圈的最低版本*/
	private static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;
//	/** 判断 js 方法，网页分享页面的 弹出层是否有无，有-->true,不响应返回键 .没有 -->false，响应安卓返回键*/
//	private boolean isJsCanCloseShareMenue  = false;
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		if(web == null){
//			return super.onKeyDown(keyCode, event);
//		}
//		web.loadUrl("javascript:js_share_close()");
//		if(!isJsCanCloseShareMenue){
//			if(keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()){
//				web.goBack();
//				return true;// 返回前一个页面
//			}else{
//				return super.onKeyDown(keyCode, event);
//			}
//		}else{
//			return true;
//		}
////		return super.onKeyDown(keyCode, event);
//	}
	
	public final static int FILECHOOSER_RESULTCODE = 1;  
	public final static int FILECHOOSER_RESULTCODE_FOR_ANDROID_5 = 2;  
	public ValueCallback<Uri> mUploadMessage;  
	public ValueCallback<Uri[]> mUploadMessageForAndroid5; 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(web == null){
			return super.onKeyDown(keyCode, event);
		}
		if (keyCode == KeyEvent.KEYCODE_BACK && web.canGoBack()) {
			web.goBack();// 返回前一个页面
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
//	@Override 这是提供给js 用的方法，暂时 不用
//		public void onBackPressed() {
//			if(web == null){
//				super.onBackPressed();
//				return ;
//			}
//			web.loadUrl("javascript:js_share_close()");
//			Log.d("gst", "onBackPressed()");
//		}

	
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
		setContentView(ResUtil.getLayoutId(this, "ilong_activity_web"));
//		InitWeiXinShare(ActivityWeb.this);
		InitWebTitle();
		loadingIv=(ImageView) findViewById(ResUtil.getId(this, "IlongActivityWeb_Loading"));
		loadingAnim = AnimationUtils.loadAnimation(this, ResUtil.getAnimationID(this, "loading"));  
	    loadingAnim.setInterpolator(new LinearInterpolator());
		try{
			String url = getIntent().getStringExtra("url");
			id = getIntent().getStringExtra("id");
			if(TextUtils.isEmpty(id)){
				Logd.d("SDK", "ActivityWeb中未取到用户支付密码标识");
			}
			initWebView(url);
		}catch(Exception e){
			e.printStackTrace();
		}
		
		
	}

//	/**初始化 微信APPID,以及注册应用到微信*/
//	private void InitWeiXinShare(Activity a){
//		try {
//			ApplicationInfo info = a.getPackageManager().getApplicationInfo(a.getPackageName(), PackageManager.GET_META_DATA);
//			ILONGYUAN_WX_APPID = info.metaData.getString("ILONGYUAN_WX_APPID");
//			if(!TextUtils.isEmpty(ILONGYUAN_WX_APPID)){
//				api = WXAPIFactory.createWXAPI(a, ILONGYUAN_WX_APPID, true);
//				api.registerApp(ILONGYUAN_WX_APPID);
//			}else{
//				Logd.e("SDK", "SDK检索到微信APPID是空");
//			}
//		} catch (Throwable e) {
//			Logd.e("SDK", "SDK检索到微信APPID发生异常");
//			LogUtils.error(e);
//		}
//	}
//	/**初始化 微信APPID,以及注册应用到微信*/
//	private void InitWeiXinShare(Activity a){
//		try {
//			ApplicationInfo info = a.getPackageManager().getApplicationInfo(a.getPackageName(), PackageManager.GET_META_DATA);
//			ILONGYUAN_WX_APPID = info.metaData.getString("ILONGYUAN_WX_APPID");
//			if(!TextUtils.isEmpty(ILONGYUAN_WX_APPID)){
//				api = WXAPIFactory.createWXAPI(a, ILONGYUAN_WX_APPID, true);
//				api.registerApp(ILONGYUAN_WX_APPID);
//			}else{
//				Logd.e("SDK", "SDK检索到微信APPID是空");
//			}
//		} catch (Throwable e) {
//			Logd.e("SDK", "SDK检索到微信APPID发生异常");
//			LogUtils.error(e);
//		}
//	}
	

	/**微信中的方法*/
	private String buildTransaction(final String type) {
		return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
	}
	
	/**悬浮窗中 个人中心，礼包等等web页面 title的初始化*/
	private void  InitWebTitle(){
		titletext = (TextView)findViewById(ResUtil.getId(this, "IlongActivity_web_title"));
		backView = (View) findViewById(ResUtil.getId(this, "IlongActivity_web_back"));
		title = findViewById(ResUtil.getId(this, "IlongActivity_web_title_ll"));
//		shareView = (View) findViewById(ResUtil.getId(this, "IlongActivityWeb_ShareBtn"));
		String text = getIntent().getStringExtra("title");
//		boolean isShowshareBtn = getIntent().getBooleanExtra("isShowShare", true);
//		if(isShowshareBtn){
//			shareView.setVisibility(View.VISIBLE);
//		}else{
//			shareView.setVisibility(View.INVISIBLE);
//		}
		titletext.setText(text);
		if(text!=null&&text.equals("论坛")){
			title.setVisibility(View.VISIBLE);
		}
		backView.setOnClickListener(new View.OnClickListener(){
			public void onClick(View v) {
				ActivityWeb.this.finish();
			}
			
		});

		
	}
	
	/**WebView初始化*/
	private void initWebView(final String webUrl){
		web = (WebView) findViewById(ResUtil.getId(this, "ilong_user_web"));

		web.setWebViewClient(new WebViewClient(){
	         @Override
	         public boolean shouldOverrideUrlLoading(WebView view, String url) {
	          view.loadUrl(url);   //在当前的webview中跳转到新的url
	          return true;
	         }

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				findViewById(ResUtil.getId(ActivityWeb.this, "IlongActivityWeb_loading_parent")).setVisibility(View.VISIBLE);
				loadingIv.startAnimation(loadingAnim);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				findViewById(ResUtil.getId(ActivityWeb.this, "IlongActivityWeb_loading_parent")).setVisibility(View.GONE);
				loadingIv.clearAnimation();
			}
	         
	        });
		WebSettings  webSetting = web.getSettings();
		webSetting.setJavaScriptEnabled(true);
		webSetting.setAllowFileAccess(true);
		web.setWebChromeClient(new MyWebChromeClient(this){

			@Override
			public boolean onJsAlert(WebView view, String url, String message,
					final JsResult result) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setTitle("温馨提示").
				setMessage(message).setCancelable(false).
				setPositiveButton("确认", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
						
					}
				}).
				create().show();
				return true;
			}

			@Override
			public boolean onJsConfirm(WebView view, String url,
					String message, final JsResult result) {
				final AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
				builder.setTitle("温馨提示").setMessage(message).setCancelable(false).setPositiveButton
				("确认", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						result.confirm();
						
					}
				} ).create().show();
				return super.onJsConfirm(view, url, message, result);
			}
			
			
			
		});
		web.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);//允许JS弹出对话框
		web.addJavascriptInterface(new Object(){
			/**关闭页面*/
			@JavascriptInterface
			public void doFinish(){
				finish();
			}
			/**修改密码成功*/
			@JavascriptInterface
			public void onResetPwdSuccess(){
				Logd.e("tag", "onResetPwdSuccess call");
				DeviceUtil.setLogout(ActivityWeb.this, true);
			}
			/**修改密码成功*/
			@JavascriptInterface
			public void onDeleteUserInfo(){
				Logd.e("tag", "onDeleteUserInfo call");
				DeviceUtil.setLogout(ActivityWeb.this, true);
			}
			/**切换账号*/
			@JavascriptInterface
			public void onSwitchAccount(){
				Logd.e("tag", "onDeleteUserInfo call");
				DeviceUtil.setLogout(ActivityWeb.this, true);
			}
			/**复制*/
			@JavascriptInterface
			public void copy(String msg){
				Logd.e("tag", "onDeleteUserInfo call");
				DeviceUtil.setLogout(ActivityWeb.this, true);
			}
			
			@JavascriptInterface
			public void sethasPayPwd(){
				Log.d("gst", "Activityweb存密码的id--"+id+"haspay_pwd");
				DeviceUtil.saveData(ActivityWeb.this, id+"haspay_pwd", "1");
			}
			/**网页点击礼包时候，相应的方法*/
			@JavascriptInterface
			public boolean getGift(String packageName, String apkDownloadUrl){
				//获取的手机中是否安装了应用信息数据,如果packageName判断手机已经安装了该应用，则返回true，如果手机对应packageName的应用，则返回false
				if(!TextUtils.isEmpty(packageName)&&!TextUtils.isEmpty(apkDownloadUrl)){
					List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);
					for(int i=0 ;i< packages.size();i++){
						PackageInfo pack = packages.get(i);
						String currentPackName = "";
						currentPackName = pack.packageName;
						if(!packageName.equals(currentPackName)){
							continue;
						}else{
							return true;
						}
					}
					return false;
				}else{
//					Toast.makeText(ActivityWeb.this, "领取礼包失败,后台配置包名,下载地址有空值", Toast.LENGTH_SHORT).show();
					Logd.d("SDK", "--packageName为空--"+TextUtils.isEmpty(packageName));
					Logd.d("SDK", "--apkDownloadUrl为空--"+TextUtils.isEmpty(apkDownloadUrl));
					return false;
				}
			}
			/**网页调用下载游戏选择浏览器的dialog*/
			@JavascriptInterface
			public void showDownGameDialog(String apkDownloadUrl){
				showIsDownGameDialog(ActivityWeb.this , apkDownloadUrl);
			}
			/**是否切换账号*/
			@JavascriptInterface
			public void switchAccount(){
				isShowSwitchAccountDialog(ActivityWeb.this);
			}
			/**网页上的修改账号密码*/
			@JavascriptInterface
			public void doModifyUserPWD(String name ,String password){
				HashMap<String,String> map = DeviceUtil.readUserFromFiles(ActivityWeb.this);
				String Localname = map.get(Constant.KEY_DATA_USERNAME);
				map.clear();
				map.put(Constant.KEY_DATA_TYPE,Constant.TYPE_USER_NORMAL );
				map.put(Constant.KEY_DATA_USERNAME, Localname);
				String userinfo ="";
				JSONObject json = new JSONObject();
				try {
					json.put(Constant.KEY_LOGIN_USERNAME, Localname);
					json.put(Constant.KEY_LOGIN_PWD, password);
					userinfo = DeviceUtil.encodeData(json.toString());
					map.put(Constant.KEY_DATA_CONTENT, userinfo);
					DeviceUtil.writeUserToFile(map, ActivityWeb.this);
					DeviceUtil.saveData(ActivityWeb.this, DeviceUtil.KEY_UPWD, password);
				} catch (JSONException e) {
					Logd.d("IlongSDK", "网页修改密码发生异常");
					e.printStackTrace();
				}	
			}
			/**这个用在 登录界面，点击 修改密码成功后，做的操作，后续修改吧*/
			@JavascriptInterface
			public void LoginActivityDoModifyPwd(String name, String password){
				doModifyUserPWD(name,password);
				ActivityWeb.this.finish();		
			}
			
			
			/**用户网页上复制 激活码到系统粘贴栏
			 * 这里已经改为 适应 旧版本的方法了，所以，都返回true
			 * @return ture-->复制成功
			 */
			@JavascriptInterface
			public boolean  copyActivactionCode(String activiaction_code){
					ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
					cmb.setText(activiaction_code);
					return true ;
			}
			/**构建一个 发送网页请求对象
			 * boolean  b true 分享到 好友，false 分享到 朋友圈
			 * webUrl --> 分享网址 
			 * webtitle --> 分享网页的title
			 * webdescription --> 分享的
			 * */
			@JavascriptInterface
			public void sendWebpage2WeiXin(boolean b,String webUrl,String webtitle
				,String webdescription){
//				WXWebpageObject webpage = new WXWebpageObject();
//				webpage.webpageUrl = webUrl;
//				WXMediaMessage msg = new WXMediaMessage(webpage);
//				msg.title = webtitle ;
//				msg.description = webdescription ;
//				Bitmap thumb = BitmapFactory.decodeResource(getResources(), ResUtil.getDimenId(ActivityWeb.this, "app_icon"));
//				msg.thumbData = WEIXINUtil.bmpToByteArray(thumb, true);
//				SendMessageToWX.Req req = new SendMessageToWX.Req();
//				req.transaction = buildTransaction("webpage");
//				req.message = msg;
////				req.scene = isTimelineCb.isChecked() ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
//				if(true == b){
//					req.scene =  SendMessageToWX.Req.WXSceneSession;
//				}else{
//					if(api.getWXAppSupportAPI() >TIMELINE_SUPPORTED_VERSION){
//					req.scene = SendMessageToWX.Req.WXSceneTimeline;
//					}else{
//						ActivityWeb.this.runOnUiThread(new Runnable() {							
//							@Override
//							public void run() {
//								Toast.makeText(ActivityWeb.this, "您的微信版本过低，无法分享到朋友圈，请升级到最新版",
//										Toast.LENGTH_SHORT).show();
//								
//							}
//						});
//						Logd.e("SDK", "用户微信版本过低，无法分享到朋友圈，请升级微信到最新版");
//					}
//				}
//				api.sendReq(req);
				Log.d("ActivityWeb", "sendWebpage2WeiXin");
			}
			@JavascriptInterface
			public void goShareSinaWeibo(String url){
				if(!TextUtils.isEmpty(url)){
					Log.d("gst", "去新浪分享的地址-->"+ url);
					Intent it = new Intent(Intent.ACTION_VIEW,Uri.parse(url));
					startActivity(it);
				}
			}
			/**IsJSCloseShareMeue 不要删除，js调用的方法，暂时不用而已*/
//			@JavascriptInterface
//			public void isWebHasShareMeue(boolean isViewVisible){
//				Log.d("gst", "isWebHasShareMeue(b)-->"+isViewVisible);
//				if(!isViewVisible){
//					if( web.canGoBack()){
//						Log.d("gst", "web.canGoBack()--"+web.canGoBack());
//						web.goBack();
//					}else{
//						Log.d("gst", "web.canGoBack()---"+web.canGoBack()+"--ActivityWeb 结束");
//						ActivityWeb.this.finish();
//					}
//				}
//			}
			
			@JavascriptInterface
			public void WebTitleBackKeyPressed(){
				ActivityWeb.this.finish();
			}
			
		}, "bind");
		
		new Thread(new Runnable() {
			
			public void run() {
				ActivityWeb.this.runOnUiThread(new Runnable() {
					public void run() {
						web.loadUrl(webUrl);
					}
				});
			}
		}).start();
		
	}
	
	
	/**弹出 是否前去领取 礼包的dialog样式(其实是Activity)*/
//	public void goGiftActivity(String apkDownloadUrl){
//		Intent it = new Intent(ActivityWeb.this , ActivityGetGift.class);
//		it.putExtra("downGameUrl", apkDownloadUrl);
//		startActivity(it);
//	}
//	@SuppressLint("NewApi")
//	public void copyActivactionCodeA(String activiaction_code){
//		ClipboardManager cmb = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
//		cmb.setText(activiaction_code);
//	}
	
	/**是否去下载游戏的dialog*/
	public void showIsDownGameDialog(final Context context,   final String apkDownLoadUrl){
		 
		final IlongBasicDialog ilongbasicDialog = 
				new IlongBasicDialog(context, ResUtil.getStyleId(context, "IlongBasicDialogStyle"));
		ilongbasicDialog.setCancelable(false);
		ilongbasicDialog.setCanceledOnTouchOutside(false);
		ilongbasicDialog.show();
		ilongbasicDialog.getDialogcontent().setText("还未安装这个游戏，下载安装游戏后即可使用这个礼包");
		ilongbasicDialog.getDialogtitletext().setText("前去下载游戏");
		ilongbasicDialog.getDialogCloseBtn().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ilongbasicDialog.dismiss();
			}
		});
		ilongbasicDialog.getDialogleftBtn().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ilongbasicDialog.dismiss();
			}
		});

		ilongbasicDialog.getDialogrightBtn().setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				Uri downGameUri = Uri.parse(apkDownLoadUrl);
				if(TextUtils.isEmpty(apkDownLoadUrl)){
					downGameUri = Uri.parse(apkDownLoadUrl);
//					Toast.makeText(ActivityWeb.this, "没检测到礼包对应游戏下载地址", Toast.LENGTH_SHORT).show();
					Logd.e("SDK", "后台没有配置该礼包的包名");
					return ;
				}
				if("http".equals(apkDownLoadUrl.substring(0,4))){
					Intent It = new Intent(Intent.ACTION_VIEW, downGameUri);
					startActivity(It);
					ilongbasicDialog.dismiss();
				}
			}
		});	
	}
	
	/**网页点击  切换账号 调用dialog，增加确认动作*/
	public void isShowSwitchAccountDialog(final Context context){
		final IlongBasicDialog switchAccountDialog = new IlongBasicDialog(context, ResUtil.getStyleId(context, "IlongBasicDialogStyle"));
		switchAccountDialog.show();
		switchAccountDialog.getDialogcontent().setText("是否切换账号");
		switchAccountDialog.getDialogtitletext().setText("切换账号");
		switchAccountDialog.getDialogCloseBtn().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				switchAccountDialog.dismiss();
			}
		});
		switchAccountDialog.getDialogleftBtn().setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
			switchAccountDialog.dismiss();
			}
		});
		switchAccountDialog.getDialogrightBtn().setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				ActivityWeb.this.runOnUiThread(new Runnable() {			
					@Override
					public void run() {
						HrSDK.getInstance().logout();
						switchAccountDialog.dismiss();
						ActivityWeb.this.finish();
						
					}
				});
				
			}
		});
	}
	
	@Override
	public void fileChose(ValueCallback<Uri> uploadMsg) {  
		Log.e(TAG, "fileChose");
        openFileChooserImpl(uploadMsg);  
    }

	@Override
	public void fileChose5(ValueCallback<Uri[]> uploadMsg) {  
    	Log.e(TAG, "fileChose5");
        openFileChooserImplForAndroid5(uploadMsg);  
	}
	
    private void openFileChooserImpl(ValueCallback<Uri> uploadMsg) {  
    	Log.e(TAG, "openFileChooserImpl");
        mUploadMessage = uploadMsg;  
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);  
        i.addCategory(Intent.CATEGORY_OPENABLE);  
        i.setType("image/*");  
        startActivityForResult(Intent.createChooser(i, "File Chooser"),  
                FILECHOOSER_RESULTCODE);  
    }  
    
    private void openFileChooserImplForAndroid5(ValueCallback<Uri[]> uploadMsg) { 
    	Log.e(TAG, "openFileChooserImplForAndroid5");
        mUploadMessageForAndroid5 = uploadMsg;  
        Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);  
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);  
        contentSelectionIntent.setType("image/*");  
      
        Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);  
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);  
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");  
      
        startActivityForResult(chooserIntent,  
                FILECHOOSER_RESULTCODE_FOR_ANDROID_5);  
    }   

	
	@Override  
	protected void onActivityResult(int requestCode, int resultCode,  
	        Intent intent) {  
	    if (requestCode == FILECHOOSER_RESULTCODE) {  
	        if (null == mUploadMessage)  
	            return;  
	        Uri result = intent == null || resultCode != RESULT_OK ? null  
	                : intent.getData();  
	        mUploadMessage.onReceiveValue(result);  
	        mUploadMessage = null;  
	  
	    } else if (requestCode == FILECHOOSER_RESULTCODE_FOR_ANDROID_5) {  
	        if (null == mUploadMessageForAndroid5)  
	            return;  
	        Uri result = (intent == null || resultCode != RESULT_OK) ? null  
	                : intent.getData();  
	        if (result != null) {  
	            mUploadMessageForAndroid5.onReceiveValue(new Uri[] { result });  
	        } else {  
	            mUploadMessageForAndroid5.onReceiveValue(new Uri[] {});  
	        }  
	        mUploadMessageForAndroid5 = null;  
	    }  
	}
	private String ActivityName = "com.hr.sdk.ac.ActivityWeb";
	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return ActivityName;
	}  
}
