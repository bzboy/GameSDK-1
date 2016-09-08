package com.hr.sdk.ac;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONObject;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hr.sdk.HrSDK;
import com.hr.sdk.dialog.LyProgressDialog;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.ToastUtils;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdk.tools.http.HttpUtil;
import com.hr.sdk.tools.http.NetException;
import com.hr.sdk.tools.http.SdkJsonReqHandler;
import com.hr.util.DeviceUtil;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;
/**
 * 账号升级
 * @author LY
 *
 */
public class ActivityUpdateAccount extends BaseActivity{
	
	private LyProgressDialog progressDialog;
	private EditText ilong_reg_usernme;
	private EditText ilong_reg_pwd;
	
	
	
	public Dialog dialogSwitchAccount;
	public Dialog dialogBindSuccess;
	
	public static boolean isFromNotRegister = false;
	private Dialog dialogBindCancel;
	
	@Override
	public void onCreate(Bundle b){
		super.onCreate(b);
	
		setContentView(ResUtil.getLayoutId(this, "ilong_layout_account_update"));
		
		isFromNotRegister = getIntent().getBooleanExtra(Constant.TYPE_FROM_PAY, false);
		if(isFromNotRegister){
			View switchAcc = findViewById(ResUtil.getId(this, "ilong_update_switch_account"));
			switchAcc.setVisibility(View.INVISIBLE);
		}
		initView();
	}
	
	
	
	private void initView(){
		
		ilong_reg_usernme = (EditText)findViewById(ResUtil.getId(this, "ilong_reg_usernme"));
		ilong_reg_pwd = (EditText)findViewById(ResUtil.getId(this, "ilong_reg_pwd"));
		//返回按钮
		findViewById(ResUtil.getId(this, "ilong_update_user_cancel")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_YOUKE_UPDATE_USER_CANCLE);
				dissmissProgressDialog();
				payCallBack();
				finish();
			}
		});
		//确定按钮
		findViewById(ResUtil.getId(this, "ilong_update_user_confirm")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_YOUKE_UPDATE_USER_CONFIRM );
				String userName = ilong_reg_usernme.getText().toString().trim();
				String pwd = ilong_reg_pwd.getText().toString().trim();
				EditText editPwd2 = (EditText) findViewById(ResUtil.getId(ActivityUpdateAccount.this, "v_edittext"));
				String pwd2 = editPwd2.getText().toString().trim();
				if(! verifyRegParam(ActivityUpdateAccount.this, userName, pwd, pwd2)){
					return;
				}				
				setUser(userName, pwd);
			}
		});
		
		//切换账号
		findViewById(ResUtil.getId(this, "ilong_update_switch_account")).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_YOUKE_UPDATE_SWITCH_ACCOUNT );
				v.setEnabled(false);
				showSwitchAccount();
				v.setEnabled(true);
			}
		});		
	}
	
	
	private String makeUserContent(String userName, String pwd) throws Exception{
		JSONObject json = new JSONObject();
		json.put(Constant.KEY_LOGIN_USERNAME, userName);
		json.put(Constant.KEY_LOGIN_PWD, pwd);
		return DeviceUtil.getencodeData(json.toString());
	}
	
	/**
	 * 绑定
	 */
	public void setUser(final String userName, final String pwd) {
		showProgressDialog();
		String sign = "";
		try {			
			sign = makeUserInfo(userName, pwd);
		} catch (Exception e) {
			e.printStackTrace();
			DeviceUtil.showToast(this, "请求失败，请重试");
			return;
		}
		String url = Constant.httpHost + Constant.USER_RENAME + "?version=201512&sign=" + sign;
		Map<String, Object> params = new HashMap<String, Object>(0);
		HttpUtil.newIntance().httpPost(this, url, params, new SdkJsonReqHandler(params)  {

			@Override
			public void ReqYes(Object reqObject, final String content) {
 				try {
					JSONObject json = new JSONObject(content);
					int errno = json.getInt("errno");
					if(errno == 200){
                        DeviceUtil.showToast(ActivityUpdateAccount.this, "账户升级成功");
						
						final HashMap<String, String> map = new HashMap<String, String>();
						map.put(Constant.KEY_DATA_TYPE, Constant.TYPE_USER_NORMAL);
						map.put(Constant.KEY_DATA_USERNAME, userName);
						map.put(Constant.KEY_DATA_CONTENT, makeUserContent(userName, pwd));
						
						DeviceUtil.writeUserToFile(map, ActivityUpdateAccount.this);
						HrSDK.TYPE_USER = Constant.TYPE_USER_NORMAL;
						dissmissProgressDialog();
						showBindSuccess(userName);
						//增加绑定手机数据上传
//						Gamer.sdkCenter.MobileBind(accountId, phone, true);
						showBindSuccess(userName);
					}else{
						dissmissProgressDialog();
						Constant.paseError(errno);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				dissmissProgressDialog();
				ToastUtils.show(ActivityUpdateAccount.this, "请求失败: " + slException.getMessage());
			}
		});
	}

	

	
	
	
	/**
	 * 显示加载框
	 */
	private void showProgressDialog() {
		try {
			if (progressDialog == null) {
				progressDialog = new LyProgressDialog(this);
			}
			progressDialog.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * 隐藏加载框
	 */
	private void dissmissProgressDialog() {
		try {
			if (progressDialog != null && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			if(dialogBindCancel != null && dialogBindCancel.isShowing()){
				dialogBindCancel.cancel();
			}else{
				showBindCancel();
			}
        	return true;
        }
		
        return super.onKeyDown(keyCode, event);
    }
	
	
	public void showBindSuccess(String phone){
		dialogBindSuccess = new Dialog(this, ResUtil.getStyleId(this,"ilongyuanAppUpdataCanCancle"));
		View view = getLayoutInflater().inflate(ResUtil.getLayoutId(this,"ilong_dialog_update_success"), null);
		View continueBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_go"));
		TextView text = (TextView) view.findViewById(ResUtil.getId(this, "ilong_text_phone"));
		text.setText(phone);
		
		continueBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if(dialogBindSuccess != null && dialogBindSuccess.isShowing()){
						dialogBindSuccess.cancel();
						finish();
						payCallBack();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		dialogBindSuccess.setCancelable(false);
		dialogBindSuccess.setCanceledOnTouchOutside(false);
		dialogBindSuccess.setContentView(view);
		dialogBindSuccess.show();
	}
	
	
	
	/**显示绑定手机界面*/
	public void showSwitchAccount(){
		dialogSwitchAccount = new Dialog(this, ResUtil.getStyleId(this,"ilongyuanAppUpdataCanCancle"));
		View view = getLayoutInflater().inflate(ResUtil.getLayoutId(this,"ilong_dialog_update_switch_alert"), null);
		View CancleBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_cancel"));
		View continueBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_go"));
		View cancelBtnRight = view.findViewById(ResUtil.getId(this, "ilong_bind_close"));
		cancelBtnRight.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ILONG_DIALOG_CLOSE);
				try {
					if(dialogSwitchAccount != null && dialogSwitchAccount.isShowing()){
						dialogSwitchAccount.cancel();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		//取消绑定，此时会退出到游戏主界面，并回调支付是否成功
		CancleBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ILONG_BIND_CANCLE_A);
				try {
					if(dialogSwitchAccount != null && dialogSwitchAccount.isShowing()){
						dialogSwitchAccount.cancel();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});	
		//继续绑定
		continueBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ILONG_DIALOG_GO_A);
				try {
					v.setEnabled(false);
					if(dialogSwitchAccount != null && dialogSwitchAccount.isShowing()){
						dialogSwitchAccount.cancel();
					}
                    //					切换按钮允许返回
					HrSDK.getInstance().setBackEable(true);
					Intent intent = new Intent(ActivityUpdateAccount.this, SdkLoginActivity.class);
					intent.putExtra(Constant.TYPE_IS_LOGIN_SWITCH_ACCOUNT, true);
					startActivity(intent);
					finish();
					v.setEnabled(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		dialogSwitchAccount.setCancelable(true);
		dialogSwitchAccount.setCanceledOnTouchOutside(false);
		dialogSwitchAccount.setContentView(view);
		dialogSwitchAccount.show();
	}
	
	
	/**显示取消绑定界面*/
	public void showBindCancel(){
		dialogBindCancel = new Dialog(this, ResUtil.getStyleId(this,"ilongyuanAppUpdataCanCancle"));
		View view = getLayoutInflater().inflate(ResUtil.getLayoutId(this,"ilong_dialog_update_user_cancel"), null);
		View CancleBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_go"));
		View continueBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_cancel"));
		//取消绑定，此时会退出到游戏主界面，并回调支付是否成功
		CancleBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE＿BUTTON_ILONG_DIALOG_BIND_CANCLE);
				try {
					if(dialogBindCancel != null && dialogBindCancel.isShowing()){
						dialogBindCancel.cancel();
					}
					//回调是否支付成功
					payCallBack();
					finish();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});	
		//继续绑定
		continueBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_DIALOG_BIND_GO);
				try {
					if(dialogBindCancel != null && dialogBindCancel.isShowing()){
						dialogBindCancel.cancel();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
		dialogBindCancel.setCancelable(true);
		dialogBindCancel.setCanceledOnTouchOutside(false);
		dialogBindCancel.setContentView(view);
		dialogBindCancel.show();
	}
	
	private void payCallBack(){
		if(isFromNotRegister){
			HrSDK.getInstance().callbackPay.onSuccess();
//			Gamer.paymentSuccess();
		}
		
	}
	
	public String makeUserInfo(String userName, String pwd) throws Exception{
		JSONObject json = new JSONObject();
		json.put("username", userName);
		json.put("password", pwd);
		json.put("access_token", HrSDK.mToken);
		return DeviceUtil.getencodeData(json.toString());
	}
	/**
	 * 验证用户名和密码合法性
	 * @param context 上下文
	 * @param userName 用户明
	 * @param pwd 密码
	 * @param pwd2 确认密码
	 * @return 是否合法
	 */
	public  boolean verifyRegParam(Context context, String userName, String pwd, String pwd2){
		if(userName.length() < 6 || userName.length() > 12){
			Toast.makeText(context, "用户名请输入6-12位字母或数字", Toast.LENGTH_LONG).show();
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
	
	private String ActivityName = "com.hr.sdk.ac.ActivityUpdateAccount";
	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return ActivityName;
	}
	
	
	
}
