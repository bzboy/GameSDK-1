package com.hr.sdk.ac;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.hr.sdk.HrSDK;
import com.hr.sdk.modle.RespModel;
import com.hr.sdk.modle.UserInfo;
import com.hr.sdk.tools.IlongCode;
import com.hr.sdk.tools.Json;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.ToastUtils;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdk.tools.http.HttpUtil;
import com.hr.sdk.tools.http.NetException;
import com.hr.sdk.tools.http.SdkJsonReqHandler;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;

public class ActivityUser extends BaseActivity {
	private TextView longyuan_id;
	private TextView level_text;
	private TextView longyuanbi_text;
	
	private Dialog dialogBind;
	/**应用程序包名 信息*/

//	private String isShowShare ="isShowShare" ;
	/**用户mUserInfo的id*/
	private String id = "";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResUtil.getLayoutId(this, "ilong_activity_user_info"));
		initView();
		if (null != HrSDK.mToken && !"".equals(HrSDK.mToken)) {
			updateUserInfo(this, HrSDK.mToken);
		}
	}
	
	public void updateUserInfo(Context context, final String token) {
		String url = Constant.httpHost + Constant.USER_DETAIL;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("access_token", token);

		HttpUtil.newHttpsIntance(context).httpPost(context, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, final String content) {
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				if (null != respModel && respModel.getErrno() == IlongCode.S2C_SUCCESS_CODE) {
					UserInfo mUserInfo = Json.StringToObj(respModel.getData(), UserInfo.class);
					setUserInfo(mUserInfo);
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				Log.e("ActivityUser", "update user failed");
			}
		});
	}
	


	private void setUserInfo(UserInfo userInfo) {
			longyuan_id.setText("浩然游ID：" + userInfo.getUid());
			level_text.setText("LV." + userInfo.getLevel());
			longyuanbi_text.setText("￥" + userInfo.getMoney());
			id = userInfo.getId();
	}

	private void initView() {
		longyuan_id = (TextView) findViewById(ResUtil.getId(this, "longyuan_id"));
		level_text = (TextView) findViewById(ResUtil.getId(this, "level_text"));
		longyuanbi_text = (TextView) findViewById(ResUtil.getId(this, "longyuanbi_text"));
		findViewById(ResUtil.getId(this, "close_button")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USERWINDOWS_CLOSE_BUTTON );
				onBackPressed();
			}
		});
		//跳转个人中心
		findViewById(ResUtil.getId(this, "user_center_layout")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USER_CENTER );
				v.setEnabled(false);
				startBind(new BindListener() {
					
					@Override
					public void startNormal() {					
						Intent intent = new Intent(ActivityUser.this, ActivityWeb.class);
//						intent.putExtra(isShowShare, true);
						intent.putExtra("url", Constant.getUserCenterUri(HrSDK.mToken).toString());
						intent.putExtra("title", "个人中心");
						intent.putExtra("id", id);
						startActivity(intent);
					}
				});
				
				v.setEnabled(true);
				finish();
			}
		});
		//跳转礼包
		findViewById(ResUtil.getId(this, "gift_layout")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USER_GIFT );
				v.setEnabled(false);
				startBind(new BindListener() {
					
					@Override
					public void startNormal() {
						Intent intent = new Intent(ActivityUser.this, ActivityWeb.class);
//						intent.putExtra(isShowShare, false);
						intent.putExtra("url", Constant.getGiftUri(HrSDK.mToken).toString());
						intent.putExtra("title", "礼包");
						startActivity(intent);
					}
				});
				v.setEnabled(true);
				finish();
			}
		});
		//帮助
		findViewById(ResUtil.getId(this, "help_layout")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USER_HELP);
				v.setEnabled(false);
				if (null == HrSDK.mToken || HrSDK.mToken.isEmpty()) {
					ToastUtils.show(ActivityUser.this, "请重新登陆");
					return;
				}
				Intent intent = new Intent(ActivityUser.this, ActivityWeb.class);
//				intent.putExtra(isShowShare, false);
				intent.putExtra("url", Constant.getHelpUri(HrSDK.mToken).toString());
				
				intent.putExtra("title", "帮助");
				startActivity(intent);
				
				v.setEnabled(true);
				finish();
			}
		});
		//签到sign_layout
		findViewById(ResUtil.getId(this, "sign_layout")).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USER_INFORMATION);
				v.setEnabled(false);
				startBind(new BindListener() {
					
					@Override
					public void startNormal() {
						Intent intent = new Intent(ActivityUser.this, ActivityWeb.class);
//						intent.putExtra(isShowShare, false);
						intent.putExtra("url", Constant.getUSERNEWS(HrSDK.mToken).toString());
						intent.putExtra("title", "消息");
						startActivity(intent);
					}
				});
				v.setEnabled(true);
				finish();
			}
		});
		//活动active_layout
		findViewById(ResUtil.getId(this, "active_layout")).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USER_ACTIVE);
				v.setEnabled(false);
				startBind(new BindListener() {
					
					@Override
					public void startNormal() {
						Intent intent = new Intent(ActivityUser.this, ActivityWeb.class);
//						intent.putExtra(isShowShare, false);
						intent.putExtra("url", Constant.getActiveUri().toString());
						intent.putExtra("title", "活动");
						startActivity(intent);
					}
				});
				v.setEnabled(true);
				finish();
				
			}
		});
		
		//论坛
		findViewById(ResUtil.getId(this, "forum_layout")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_USER_BBS);
				v.setEnabled(false);
				startBind(new BindListener() {
					
					@Override
					public void startNormal() {
						Intent intent = new Intent(ActivityUser.this, ActivityWeb.class);
//						intent.putExtra(isShowShare, false);
						Log.e("luntan", Constant.getUserBBS(HrSDK.mToken).toString());
						intent.putExtra("url", Constant.getUserBBS(HrSDK.mToken).toString());
						intent.putExtra("title", "论坛");
						startActivity(intent);
						
					}
				});
				v.setEnabled(true);
				finish();
			}
		});
		
		//切换账号
		findViewById(ResUtil.getId(this, "ilong_text_switch_account")).setOnClickListener(new OnClickListener() {
					
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ILONG_TEXT_SWITCH_ACCOUNT);
				v.setEnabled(false);
				HrSDK.getInstance().setBackEable(true);
				Intent intent = new Intent(ActivityUser.this, SdkLoginActivity.class);
//				intent.putExtra(isShowShare, false);
				intent.putExtra(Constant.TYPE_IS_LOGIN_SWITCH_ACCOUNT, true);
				startActivity(intent);
				v.setEnabled(true);
				finish();
			}
		});	
		
		//升级账号
		findViewById(ResUtil.getId(this, "ilong_text_update_account")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ILONG_YOUKE_UPDATE_ACCOUNT);
				v.setEnabled(false);
				Intent intent = new Intent(ActivityUser.this, ActivityUpdateAccount.class);
				startActivity(intent);
				v.setEnabled(true);
				finish();
			}
		});		
		
		if(HrSDK.TYPE_USER.equals(Constant.TYPE_USER_NORMAL)){
			findViewById(ResUtil.getId(this, "ilong_text_switch_account")).setVisibility(View.VISIBLE);
			findViewById(ResUtil.getId(this, "ilong_text_update_account")).setVisibility(View.GONE);
		}else{
			findViewById(ResUtil.getId(this, "ilong_text_switch_account")).setVisibility(View.GONE);
			findViewById(ResUtil.getId(this, "ilong_text_update_account")).setVisibility(View.VISIBLE);
		}
	}
	
	
	/**显示绑定手机界面*/
	public void showBindPhone(){
		Activity activity = HrSDK.getActivity();
		dialogBind = new Dialog(activity, ResUtil.getStyleId(activity,"ilongyuanAppUpdataCanCancle"));
		View view = activity.getLayoutInflater().inflate(
				ResUtil.getLayoutId(activity,"ilong_dialog_bindphone"), null);
		View CancleBtn = view.findViewById(ResUtil.getId(activity, "ilong_bind_cancel"));
		View OKUpdataBtn = view.findViewById(ResUtil.getId(activity, "ilong_bind_go"));
		View cancel = view.findViewById(ResUtil.getId(activity, "ilong_bind_close"));
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dialogBind != null && dialogBind.isShowing()){
					dialogBind.cancel();
					HrSDK.getInstance().callbackPay.onSuccess();
//					Gamer.paymentSuccess();
				}
			}
		});
		CancleBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if(dialogBind != null && dialogBind.isShowing()){
						dialogBind.cancel();
						HrSDK.getInstance().callbackPay.onSuccess();
//						Gamer.paymentSuccess();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		OKUpdataBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if(dialogBind != null && dialogBind.isShowing()){
					dialogBind.cancel();
				}
				Intent it = new Intent(HrSDK.getActivity(), ActivityBindPhone.class);
				HrSDK.getActivity().startActivity(it);
			}
		});

		dialogBind.setCancelable(false);
		dialogBind.setCanceledOnTouchOutside(false);
		dialogBind.setContentView(view);
		dialogBind.show();
	}
	
	static interface BindListener{
		public void startNormal();
	}
	
	public void startBind(BindListener mListener){
		//如果是游客，需要绑定手机
		
		if(HrSDK.TYPE_USER.equals(Constant.TYPE_USER_NORMAL)){
			mListener.startNormal();
		}else{
			Intent intent = new Intent(this, ActivityUpdateAccount.class);
			startActivity(intent);
			
		}
		
	}

	private String ActivityName = "com.hr.sdk.ac.ActivityUser";
	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return ActivityName;
	}
	
}









