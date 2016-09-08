package com.hr.sdk.ac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.hr.sdk.HrSDK;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.http.Constant;
import com.hr.util.DeviceUtil;
import com.lygame.constant.ActivityLifeTypeConstant;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;

public class ActivityGameNoticePage extends BaseActivity{
	private WebView web;
	private TextView noticePageTitle;

	private CheckBox noticeCheckBox;

	
	private  String gameNoticeid ;
	
//	private String logincallback;
	//是否需要 回调 登录接口

	
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
	
	@Override
	public void onCreate(Bundle b){
	
		super.onCreate(b);
		setContentView(ResUtil.getLayoutId(this, "ilong_game_notice_page"));
		initOtherView();
		try{
			String url = getIntent().getStringExtra("url");
			
			initWebView(url);
		
		}catch(Exception e){
			e.printStackTrace();

		}
		
		
	}
	
	private void initWebView(String weburl){

		web = (WebView) findViewById(ResUtil.getId(this, "game_notice_page_web"));
		
		web.setWebViewClient(new WebViewClient(){
	         @Override
	         public boolean shouldOverrideUrlLoading(WebView view, String url) {
	 
//	          view.loadUrl(url);   //在当前的webview中跳转到新的url
	        view.loadUrl(url);
	
	        return true;
	         }
	        });
		web.getSettings().setJavaScriptEnabled(true);
		web.addJavascriptInterface(new Object(){
			@JavascriptInterface
			public void doFinish(){
				finish();
			}
			@JavascriptInterface
			public void clickNotice(String url){
				
				Intent intent = new Intent(ActivityGameNoticePage.this, ActivityWeb.class);				
				intent.putExtra("url", Constant.getWebPay(url).toString());
				intent.putExtra("title", "充值");
				if(HrSDK.mUserInfo != null)
					intent.putExtra("id", HrSDK.mUserInfo.getUid());
				startActivity(intent);
				finish();
			}
			
		}, "bind");
		
		web.loadUrl(weburl);
//		web.loadUrl("http://www.taobao.com");
	}
	
	
	public void initOtherView(){
		noticePageTitle = (TextView) findViewById(ResUtil.getId(this, "game_notice_page_title"));

		noticeCheckBox = (CheckBox) findViewById(ResUtil.getId(this, "game_notice_page_checkbox"));
		noticePageTitle.setText(getIntent().getStringExtra("title"));
		gameNoticeid = getIntent().getStringExtra("id");
//		logincallback = getIntent().getStringExtra("logincode");
		
		findViewById(ResUtil.getId(this, "game_notice_page_close_Btn")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_GAME_NOTICE_PAGE_CLOSE);
				finish();
				
			}});;
		
	
		noticeCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_GAME_NOTICE_PAGE_CHECKBOX );
				if(isChecked){
					DeviceUtil.saveData(ActivityGameNoticePage.this,DeviceUtil.KEY_NOTICE_ID, gameNoticeid);

				}else{
					DeviceUtil.saveData(ActivityGameNoticePage.this,DeviceUtil.KEY_NOTICE_ID, "");
	
				}
				
			}
		});
		
	}
		

	@Override
	protected void onDestroy() {

		super.onDestroy();
//		HrSDK.getInstance().callbackLogin.onSuccess(logincallback);
		try {
			HrSDK.getInstance().iToken2UserInfo.onSuccess(HrSDK.mUserInfo);
		} catch (Exception e) {
			e.printStackTrace();
			if(HrSDK.getInstance().iToken2UserInfo != null){
				HrSDK.getInstance().iToken2UserInfo.onFailed();
			}
		}		
	}


	private static String ActivityName = "com.hr.sdk.ac.ActivityGameNoticePage";
 
	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return ActivityName;
	}

	
}
