package com.hr.sdk.ac;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.hr.sdk.HrSDK;
import com.hr.sdk.tools.ResUtil;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;


public class ExitSDKActivity extends BaseActivity implements OnClickListener {
	/**用于跳转页面后，是否显示 分享按钮的key*/
	private String isShowShare ="isShowShare" ;
	
	/**领取礼包的View对象*/
//	private View gift_layout;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(com.hr.sdk.tools.ResUtil.getLayoutId(this, "ilong_activity_exit_sdk"));
		initView();
	
	}

	private void initView() {
		findViewById(ResUtil.getId(this, "ilong_exit_sdk_continue_btn")).setOnClickListener(this);
		findViewById(ResUtil.getId(this, "ilong_exit_sdk_out_btn")).setOnClickListener(this);
//		gift_layout = findViewById(ResUtil.getId(this, "gift_layout"));
//		gift_layout.setOnClickListener(this);
//		findViewById(ResUtil.getId(this, "help_layout")).setOnClickListener(this);
//		if(IlongSDK.TYPE_USER.equals(Constant.TYPE_USER_NORMAL)){
//			gift_layout.setVisibility(View.VISIBLE);
//		}else{
//			gift_layout.setVisibility(View.GONE);
//		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == ResUtil.getId(this, "ilong_exit_sdk_continue_btn")) {
			Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_EXIT_CONTINUE_BUTTON);
		} else if (id == ResUtil.getId(this, "ilong_exit_sdk_out_btn")) {
			Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_EXIT_OK_BUTTON);
			Gamer.sdkCenter.ExitEvent(HrSDK.AccountId);
			HrSDK.getInstance().callbackExit.onExit();
			HrSDK.mToken = "";
			HrSDK.mUserInfo = null;
			
		} 
		finish();
	}
	


	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return ActivityName;
	}
	
	private String ActivityName = "com.hr.sdk.ac.ExitSDKActivity";
	
	
	
	

}
