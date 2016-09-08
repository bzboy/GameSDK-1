package com.hr.sdkdemo;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.EditText;
import android.widget.Toast;
import com.hr.sdk.demo.R;
import com.hr.sdk.HrSDK;
import com.hr.sdk.i.ILongExitCallback;
import com.hr.sdk.i.ILongInitCallback;
import com.hr.sdk.i.ILongPayCallback;
import com.hr.sdk.i.IToken2UserInfo;
import com.hr.sdk.i.IlongLoginCallBack;
import com.hr.sdk.modle.UserInfo;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdkdemo.tools.http.AccessTokenModel;
import com.hr.sdkdemo.tools.http.HttpUtil;
import com.hr.sdkdemo.tools.http.Json;
import com.hr.sdkdemo.tools.http.NetException;
import com.hr.sdkdemo.tools.http.RespModel;
import com.hr.sdkdemo.tools.http.SdkJsonReqHandler;
/**
 * 主MainActivity
 * 
 * @author niexiaoqiang
 */
public class MainActivity extends Activity {
	private static String authorizationCode = "";
	public static final String TAG = "MainActivity";

	private boolean isExit = true;
	
	private static String appid = "4659078e8e56f3c1";
	static {
		if(!Constant.isRelease){
			appid = "22c51853df2cd8fc";
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_demo_main);
		
		
		HrSDK.getInstance().setDebugModel(true)// 是否是debug模式
				.setBackEable(true)// 登录界面是否响应返回键，默认true
				.setShowLoginView(true) //是否显示自动登录界面  默认为true
				.init(this,new ILongInitCallback() {

					@Override
					public void onSuccess() {
						// 初始化成功
						Log.e(TAG, "init success");
					}

					@Override
					public void onFailed() {
						// 初始化失败
						Log.e(TAG, "init failed");
					}
				}, new IlongLoginCallBack() {

					@Override
					public void onSuccess(String auth_code) {
						// 登录成功，获得auth_code，此时应该换取token
						authorizationCode = auth_code;
						getToken(authorizationCode);
						Log.e(TAG, "login success" + ", " + auth_code);
					}

					@Override
					public void onLogout() {
						// 如果会话失效，会调用此回调，此时游戏应当回到登录界面
						Log.e(TAG, "logout");
					}

					@Override
					public void onFailed(String msg) {
						// 登录失败
						Log.e(TAG, "login failed");
					}

					@Override
					public void onCancel() {
						// 取消登陆的回调
						Log.e(TAG, "login cancel");
					}

					@Override
					public void onSwitchAccount(String auth_code) {
						// 切换账号的回调
						Log.e(TAG, "onSwitchAccount" + ", " + auth_code);
						getToken(auth_code);
					}
				}, new ILongPayCallback() {

					@Override
					public void onSuccess() {
						// 支付成功的回调
						Log.e(TAG, "pay success");
					}

					@Override
					public void onFailed() {
						// 支付失败的回调
						Log.e(TAG, "pay failed");
					}

					@Override
					public void onCancel() {
						// 支付取消的回调
						Log.e(TAG, "pay cancel");
					}
				}, new ILongExitCallback() {

					@Override
					public void onExit() {
						// 退出登录的回调,此时游戏需要关闭游戏界面
						Log.e(TAG, "exit success");
						finish();
					}
				});
		
 
		
		findViewById(R.id.btn_login).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				HrSDK.getInstance().setShowLoginView(true);
				doLogin();
			}
		});
		findViewById(R.id.btn_login_hide).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HrSDK.getInstance().setShowLoginView(false);
				doLogin();

			}
		});
		findViewById(R.id.btn_pay).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				doPay();
			}
		});
		findViewById(R.id.btn_exit).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(isExit){
					doExit();
				}else{
					isExit = true;
				}
				
			}
		});

		findViewById(R.id.btn_logout).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				HrSDK.getInstance().logout();
			}
		});
		
		findViewById(R.id.btn_exit).setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				isExit = false;
				HrSDK.getInstance().deleteCache();
				Toast.makeText(MainActivity.this, "清除数据成功",Toast.LENGTH_LONG).show();
				return false;
			}
		});
		
		findViewById(R.id.btn_showuser).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HrSDK.getInstance().showUserCenter();
			}
		});
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			doExit();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void doExit() {
		HrSDK.getInstance().exitSDK();
	}

	private void doPay() {

		EditText editText = (EditText) findViewById(R.id.editText);
		// 金额 两位小数的浮点数，比如1分钱，就是0.01；一元钱，就是1.00
		String amount = editText.getText().toString().trim();
		// 订单号由游戏内部生成， 订单号不可重复提交
		String orderId = "orderId" + Math.random();
		// 商品名称
		String productName = "绝世好剑";
		// 游戏内部的角色id
		String appUid = "game_user_id";
		// 回调地址
		String notifyUrl = "http://api.sdk.tjhaoran.cn/api/pay/notify_test";
		// 商品id
		String product_id = "product_id";
		// 游戏内部的角色昵称
		String app_username = "app_username";
		startPay(amount, orderId, appUid, notifyUrl, productName, product_id,
				app_username);
	}

	private void startPay(final String amount, final String orderId,
			String app_uid, String notify_uri, String product_name,
			String product_id, String app_username) {
		Bundle bundle = new Bundle();
		bundle.putString("amount", amount);// 金额，两位小数的浮点数，如1分钱：1.01；1元钱：1.00
		bundle.putString("app_order_id", orderId);// 订单号
		bundle.putString("app_uid", app_uid);// 角色id
		bundle.putString("notify_uri", notify_uri);// 支付成功之后游戏接入放的需要填入的回调地址
		bundle.putString("product_name", product_name);// 游戏内部商品名称
		bundle.putString("product_id", product_id);// 游戏内部商品id
		bundle.putString("app_username", app_username);// 游戏内部角色昵称

		HrSDK.getInstance().pay(bundle);
	}

	public void doLogin() {
		HrSDK.getInstance().login();
	}

	/**
	 * 实现获取TOKEN
	 * 
	 * @param code
	 */
	public void getToken(String code) {
		System.out.println("code:"+code);
		String url = "http://api.sdk.tjhaoran.cn/api/test/login";
		if(!Constant.isRelease){
			url = Constant.httpHost + "/api/test/login";
		}
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("auth_code", code);
//		if(Constant.isRelease){
//			params.put("version", "release");		
//		}
		HttpUtil.newHttpsIntance(MainActivity.this).httpsPost(
				MainActivity.this, url, params, new SdkJsonReqHandler(params) {

					@Override
					public void ReqYes(Object reqObject, final String content) {
						Log.e("content", content);
						try {
							RespModel respModel = Json.StringToObj(content,
									RespModel.class);
							AccessTokenModel tokenModel = Json.StringToObj(
									respModel.getData(), AccessTokenModel.class);
							String token = tokenModel.getAccess_token();
							if (token != null) {
								// CODE换TOKEN成功
//								Log.d("gst", "token-->"+token);
								HrSDK.getInstance().setUserToken(MainActivity.this, token,
										new IToken2UserInfo() {

											@Override
											public void onSuccess(UserInfo userinfo) {
												HrSDK.getInstance().showFloatView();
												// 进入游戏
												Log.d("tag", "用户id：" + userinfo.getId());
												Toast.makeText(MainActivity.this, "登录成功，用户id是" + userinfo.getId(),Toast.LENGTH_LONG).show();
											}

											@Override
											public void onFailed() {
												Toast.makeText(MainActivity.this, "登录失败",Toast.LENGTH_LONG).show();
											}
										});
							} else {
								Toast.makeText(MainActivity.this, "登录失败",
										Toast.LENGTH_SHORT).show();
							}
						} catch (Exception e) {
							Toast.makeText(MainActivity.this, "登录失败",
									Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}

					@Override
					public void ReqNo(Object reqObject, NetException slException) {
						Toast.makeText(MainActivity.this,
								"登录失败," + slException.getMessage(),
								Toast.LENGTH_SHORT).show();
					}
				});
	}

	@Override
	protected void onPause() {
		super.onPause();
		// onPause时候调用，关闭悬浮窗
		HrSDK.getInstance().onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		// onResume时候调用, 显示悬浮窗
		HrSDK.getInstance().onResume();
	}

}
