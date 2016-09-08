package com.hr.sdk.pay;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hr.sdk.HrSDK;
import com.hr.sdk.ac.ActivityBindPhone;
import com.hr.sdk.ac.ActivityWeb;
import com.hr.sdk.ac.BaseActivity;
import com.hr.sdk.dialog.IlongBasicDialog;
import com.hr.sdk.modle.LyPayOrder;
import com.hr.sdk.modle.OrderNumber;
import com.hr.sdk.modle.RespModel;
import com.hr.sdk.pay.alipay.LyAlipay;
import com.hr.sdk.pay.tenpay.LyCftPayActivity;
import com.hr.sdk.pay.uppay.LyUPPay;
import com.hr.sdk.tools.Json;
import com.hr.sdk.tools.LogUtils;
import com.hr.sdk.tools.ResUtil;
import com.hr.sdk.tools.ToastUtils;
import com.hr.sdk.tools.http.Constant;
import com.hr.sdk.tools.http.HttpUtil;
import com.hr.sdk.tools.http.NetException;
import com.hr.sdk.tools.http.SdkJsonReqHandler;
import com.hr.util.DeviceUtil;
import com.hr.util.Logd;
import com.lygame.constant.ButtonTypeConstant;
import com.lygame.tool.Gamer;

/**
 * 支付中心-NEW
 * @author niexiaoqiang
 */
public class LyPayActivity extends BaseActivity {
	public static final String M_ALIPAY = "支付宝";
	public static final String M_TEN = "财付通";
	public static final String M_UNIN = "银联";
	public static final String M_LONGYUAN = "浩然币";
	/**这个uid,其实传递过来的是 userInfo的id*/
	private String uid = "";
	private String amount = "";
	private String app_order_id = "";
	private String app_uid = "";
	private String product_name = "";
	private String product_id = "";
	private String app_username = "";
	private String access_token = "";
	private String notify_uri = "";
	private String pack_key="";
	private String user_coupon_id="";
	private ProgressDialog orderDialog;
	private LyPayOrder lyPayOrder;
	private Context context ;
	/**跳转设置 支付密码的 dialog*/
	private IlongBasicDialog ilongGoWebSetPayPwdDialog;
	
	private IlongBasicDialog GoWebLongBReCharge; 
	/**提示是否绑定*/
	public Dialog dialogBind;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResUtil.getLayoutId(this, "ilong_activity_pay"));
		Bundle bundle = getIntent().getExtras();
		if (bundle != null) {
			this.pack_key=HrSDK.getInstance().getSid();
			this.uid = bundle.getString("uid");
			this.amount = bundle.getString("amount");
			this.app_order_id = bundle.getString("app_order_id");
			this.app_uid = bundle.getString("app_uid");
			this.notify_uri = bundle.getString("notify_uri");
			this.product_name = bundle.getString("product_name");
			this.product_id = bundle.getString("product_id");
			this.app_username = bundle.getString("app_username");
			this.access_token = bundle.getString("access_token");
		}
		
		initView();
		initListener();
		View pay_ly = findViewById(ResUtil.getId(this, "longyuan_pay_layout"));
		if(HrSDK.TYPE_USER.equals(Constant.TYPE_USER_NORMAL)){
			pay_ly.setVisibility(View.VISIBLE);
		}else{
			pay_ly.setVisibility(View.INVISIBLE);
		}

	}

	private ViewGroup alipay_layout;
	private ViewGroup tecent_pay_layout;
	private ViewGroup unin_pay_layout;
	private ViewGroup longyuan_pay_layout;
	private TextView pay_method_text;
	private ViewGroup password_layout;
	private EditText ilong_pay_password;
	private TextView order_number_text;
	private TextView actual_pay_money;

	private void initView() {
		alipay_layout = (ViewGroup) findViewById(ResUtil.getId(this, "alipay_layout"));
		tecent_pay_layout = (ViewGroup) findViewById(ResUtil.getId(this, "tecent_pay_layout"));
		unin_pay_layout = (ViewGroup) findViewById(ResUtil.getId(this, "unin_pay_layout"));
		longyuan_pay_layout = (ViewGroup) findViewById(ResUtil.getId(this, "longyuan_pay_layout"));
		alipay_layout.setSelected(true);
		pay_method_text = (TextView) findViewById(ResUtil.getId(this, "pay_method_text"));
		password_layout = (ViewGroup) findViewById(ResUtil.getId(this, "password_layout"));
		password_layout.setVisibility(View.GONE);
		ilong_pay_password = (EditText) findViewById(ResUtil.getId(this, "ilong_pay_password"));
		order_number_text = (TextView) findViewById(ResUtil.getId(this, "order_number_text"));
		actual_pay_money = (TextView) findViewById(ResUtil.getId(this, "actual_pay_money"));
		order_number_text.setText("￥" + amount);
		actual_pay_money.setText("实付款：￥" + amount);
		//商品名称
		TextView textProductName = (TextView) findViewById(ResUtil.getId(this, "product_name"));
		textProductName.setText(product_name);
	}

	private void initListener() {
		///关闭按钮
		findViewById(ResUtil.getId(this, "close_button")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_PAY_ACTIVITY_CLOSE);
				ToastUtils.show(getActivity(), "支付取消");
				HrSDK.getInstance().callbackPay.onCancel();
				finish();
			}
		});
		alipay_layout.setOnClickListener(payMethodClick);
		tecent_pay_layout.setOnClickListener(payMethodClick);
		unin_pay_layout.setOnClickListener(payMethodClick);
		longyuan_pay_layout.setOnClickListener(payMethodClick);

		///立即支付
		findViewById(ResUtil.getId(this, "now_pay_button")).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_NOW_PAY);
				if (lyPayOrder == null) {
					lyPayOrder = new LyPayOrder(pack_key, amount, app_order_id, app_uid, notify_uri, product_name, product_id, app_username, access_token);
				}
				String paymethod = pay_method_text.getText().toString();
				if (M_LONGYUAN.equals(paymethod)) {
					if("0".equals(DeviceUtil.getData(LyPayActivity.this, uid+"haspay_pwd"))){
						goSetPWDDialog();
					}
					String password = ilong_pay_password.getText().toString();
					if (null == password || password.isEmpty()) {
						ToastUtils.show(getActivity(), "密码不能为空");
						return;
					}
					createOrderNumber(3, lyPayOrder, password);
				} else if (M_ALIPAY.equals(paymethod)) {
					createOrderNumber(0, lyPayOrder, "");
				} else if (M_UNIN.equals(paymethod)) {
					createOrderNumber(1, lyPayOrder, "");
				} else if (M_TEN.equals(paymethod)) {
					createOrderNumber(2, lyPayOrder, "");
				}
			}
		});
	}

	/**
	 * 生成订单
	 * @param orderType
	 * @param lyPayOrder
	 */
	private void createOrderNumber(final int orderType, LyPayOrder lyPayOrder, final String password) {
		
		showGetOrderDialog("订单获取中，请稍候....");
		String url = LyUrlConstant.BASE_URL + LyUrlConstant.PAY_ORDER;
		final Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("pack_key", lyPayOrder.getPack_key());
		params.put("amount", (int)(Float.parseFloat(amount)*100));
		params.put("app_order_id", lyPayOrder.getApp_order_id());
		params.put("app_uid", lyPayOrder.getApp_uid());
		params.put("notify_uri", lyPayOrder.getNotify_uri());
		params.put("product_name", lyPayOrder.getProduct_name());
		params.put("product_id", lyPayOrder.getProduct_id());
		params.put("app_username", lyPayOrder.getApp_username());
		params.put("access_token", lyPayOrder.getAccess_token());
		if (orderType == 0) {
			params.put("channel", "alipayquick");
		} else if (orderType == 1) {
//			params.put("channel", "upmp");
			params.put("channel", "acp");
		} else if (orderType == 2) {
			params.put("channel", "ten");
		}else if (orderType == 3) {
			params.put("channel", "ilypay");
		}
		if (!TextUtils.isEmpty(HrSDK.getInstance().getSid())){
			params.put("pack_key", HrSDK.getInstance().getSid());
		}
//		Gamer.setpayment("", "", "CNY", params.get("channel").toString(), Float.parseFloat(amount)*100);
//		Log.d("gst", "生成订单的url-->"+url);
		HttpUtil.newHttpsIntance(this).httpsPostJSON(this, url, params, new SdkJsonReqHandler(params) {

			@Override
			
			public void ReqYes(Object reqObject, final String content) {
				try {
					dissmissOrderDilaog();
					RespModel respModel = Json.StringToObj(content, RespModel.class);
					if (respModel.getErrno() == 200) {
						JSONObject dataObject=JSONObject.parseObject(respModel.getData());
						OrderNumber orderNumber = Json.StringToObj(respModel.getData(), OrderNumber.class);
						orderNumber.getOut_trade_no();
						if (null == orderNumber || null == orderNumber.getOut_trade_no() || orderNumber.getOut_trade_no().isEmpty()) {
							onMakeOrderFailed();
						} else{
//						    Gamer.payment.setOrderId(orderNumber.getOut_trade_no());
                            //pay_info这个字段是新加的，只有当支付宝支付是才会用其它时候为空						    
							doStartPay(dataObject.getString("pay_info"),orderType, orderNumber.getOut_trade_no(), password);
						}
					} else {
						onMakeOrderFailed();
					}
				} catch (Exception e) {
					e.printStackTrace();
					onMakeOrderFailed();
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				Log.e(TAG, slException.toString());
				dissmissOrderDilaog();
				ToastUtils.show(getActivity(), "生成订单失败");
				finish();
			}
		});
	}
	
	public void onMakeOrderFailed(){
		HrSDK.showToast("生成订单失败");
		HrSDK.getInstance().callbackPay.onFailed();
		finish();
	}
	
	private void doStartPay(String pay_info,int orderType, String orderId, String password){
		//已经生成订单号
		switch (orderType) {
			case 0:
				aliyPay(pay_info);
				break;
			case 1:
				upPay(orderId);
				break;
			case 2:
				cftPay(amount, orderId, LyUrlConstant.BASE_URL + LyUrlConstant.RETURN_NOTIFY);
				break;
			case 3:
				longPay(orderId, password);
				break;
			default:
				break;
		}
	}

	private static final int CFT_REQ = 20;

	/**
	 * 跳转财付通支付
	 * @param amount
	 * @param out_trade_no
	 * @param notify_uri
	 */
	private void cftPay(String amount, String out_trade_no, String notify_uri) {
		Intent i = new Intent(this, LyCftPayActivity.class);
		i.putExtra("amount", amount);
		i.putExtra("out_trade_no", out_trade_no);
		i.putExtra("notify_uri", notify_uri);
		startActivityForResult(i, CFT_REQ);
	}

	private LyAlipay alipay;

	/**
	 * 支付宝支付
	 */
	private void aliyPay(String pay_info) {
		if (alipay == null) {
			alipay = new LyAlipay(this, lyPayResult);
		}
		alipay.pay(pay_info);
	}

	private LyUPPay lyUPPay;

	/**
	 * 银联支付
	 */
	private void upPay(String tn) {
		if (lyUPPay == null) {
			lyUPPay = new LyUPPay(this);
		}
		lyUPPay.pay(tn);
	}

	/**
	 * 浩然币支付
	 * @param out_trade_no
	 * @param password
	 */
	private void longPay(String out_trade_no, String password) {
		String url = LyUrlConstant.BASE_URL + LyUrlConstant.LONGYUAN_PAY;
		Map<String, Object> params = new HashMap<String, Object>(0);
		params.put("out_trade_no", out_trade_no);
		params.put("password", password);
		Log.d("gst", "浩然币支付的url-->"+url);
		HttpUtil.newHttpsIntance(this).httpsPostJSON(this, url, params, new SdkJsonReqHandler(params) {

			@Override
			public void ReqYes(Object reqObject, final String content) {
				dissmissOrderDilaog();
				Log.d("gst","浩然游币支付的时候，返回的 content-->"+content);
				RespModel respModel = Json.StringToObj(content, RespModel.class);
				if (null == respModel) {
					lyPayResult.lyPayNo(3, "浩然游币支付失败");
				} else {
					if (respModel.getErrno() == 200) {
						lyPayResult.lyPayYes(3);
					} else {
						Log.d("gst", "浩然游币支付失败的--》+errno--》"+respModel.getErrno());
						if(411 == respModel.getErrno()){
							goWebIlongReCharge();
						}else{
							lyPayResult.lyPayNo(3, "支付失败," + Constant.paseError(respModel.getErrno()));
						}
						
					}
				}
			}

			@Override
			public void ReqNo(Object reqObject, NetException slException) {
				dissmissOrderDilaog();
				lyPayResult.lyPayNo(3, "浩然游币支付失败");
			}
		});
		showGetOrderDialog("支付中，请稍候....");
	}
	
	/**展示跳转设置支付密码的 dialog */
	public  void goSetPWDDialog(){
//		Log.d("gst", "弹出 setPWDdialog");
		ilongGoWebSetPayPwdDialog = new IlongBasicDialog(LyPayActivity.this, 
				ResUtil.getStyleId(LyPayActivity.this, "IlongBasicDialogStyle"));
		ilongGoWebSetPayPwdDialog.setCancelable(false);
		ilongGoWebSetPayPwdDialog.setCanceledOnTouchOutside(false);
		ilongGoWebSetPayPwdDialog.show();

		ilongGoWebSetPayPwdDialog.getDialogCloseBtn().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ilongGoWebSetPayPwdDialog.dismiss();
				longyuan_pay_layout.setSelected(false);
//				selectView(alipay_layout);
				alipay_layout.setSelected(true);
				pay_method_text.setText(M_ALIPAY);
//				lyPayResult.lyPayNo(3, "支付失败," + Constant.paseError(316));
			}
		});
		ilongGoWebSetPayPwdDialog.getDialogleftBtn().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ilongGoWebSetPayPwdDialog.dismiss();
				longyuan_pay_layout.setSelected(false);
//				selectView(alipay_layout);
				alipay_layout.setSelected(true);
				pay_method_text.setText(M_ALIPAY);
//				lyPayResult.lyPayNo(3, "支付失败," + Constant.paseError(316));
			}
		});
		ilongGoWebSetPayPwdDialog.getDialogrightBtn().setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
			Intent it = new Intent(LyPayActivity.this, ActivityWeb.class);
			it.putExtra("title", "设置支付密码");
			it.putExtra("url", Constant.getSetPayPWDUri(HrSDK.mToken).toString());
			it.putExtra("id",uid);
//			Log.d("gst","前去设置支付的网页的url--->"+Constant.getSetPayPWDUri(IlongSDK.mToken).toString());
			startActivity(it);
			ilongGoWebSetPayPwdDialog.dismiss();
			longyuan_pay_layout.setSelected(false);
//			selectView(alipay_layout);
			alipay_layout.setSelected(true);
			pay_method_text.setText(M_ALIPAY);
//			lyPayResult.lyPayNo(3, "支付失败," + Constant.paseError(316));
			}
		});
	}
	
	public void goWebIlongReCharge(){
		GoWebLongBReCharge = new IlongBasicDialog(LyPayActivity.this, 
				ResUtil.getStyleId(LyPayActivity.this, "IlongBasicDialogStyle"));
		GoWebLongBReCharge.show();
		GoWebLongBReCharge.setCancelable(false);
		GoWebLongBReCharge.setCanceledOnTouchOutside(false);
		GoWebLongBReCharge.getDialogtitletext().setText("充值浩然游币");
		GoWebLongBReCharge.getDialogcontent().setText("尊敬的玩家，您的浩然游币余额不足，无法完成支付，是否前去充值");
		GoWebLongBReCharge.getDialogCloseBtn().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GoWebLongBReCharge.dismiss();
				lyPayResult.lyPayNo(3, "支付失败," + Constant.paseError(411));
			}
		});
		GoWebLongBReCharge.getDialogleftBtn().setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GoWebLongBReCharge.dismiss();
				lyPayResult.lyPayNo(3, "支付失败," + Constant.paseError(411));
			}
		});
		GoWebLongBReCharge.getDialogrightBtn().setOnClickListener(new OnClickListener() {	
			@Override
			public void onClick(View v) {
				//关闭提示的dialog
				if(GoWebLongBReCharge != null && GoWebLongBReCharge.isShowing()){
					GoWebLongBReCharge.cancel();
				}
				//启动充值界面
				Intent it = new Intent(LyPayActivity.this, ActivityWeb.class);
				it.putExtra("url", Constant.goWebRechargeLongBi(HrSDK.mToken).toString());
				startActivity(it);
			
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {		
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CFT_REQ) {
			if (RESULT_OK == resultCode) {
				lyPayResult.lyPayYes(2);
			} else if (RESULT_CANCELED == resultCode) {
				lyPayResult.lyPayNo(2, "支付取消");
			}
		} else {
			//银联
			Bundle b = data.getExtras();
			String str = b.getString("pay_result");
			if (str.equalsIgnoreCase("success")) {
				lyPayResult.lyPayYes(1);
			} else if (str.equalsIgnoreCase("fail")) {
				lyPayResult.lyPayNo(0, "支付失败");
			} else if (str.equalsIgnoreCase("cancel")) {
				lyPayResult.lyPayNo(0, "支付取消");
			}
		}
	}

	private LyPayResult lyPayResult = new LyPayResult() {

		@Override
		public void lyPayYes(int payType) {
			String typePay = "";
			switch (payType) {
			case 1:
				typePay ="银联";
				break;
			case 2:
				typePay ="支付宝";
				break;
			case 3:
				typePay ="浩然币";
				break;
			default:
				typePay ="unKnown";
				break;
			}
			Gamer.sdkCenter.paymentSuccess(Gamer.DATA_ACCOUNT_ID, app_order_id, "unKnown", amount, "CNY",typePay );
			HrSDK.getInstance().callbackPay.onSuccess4Bind();
			Log.e("payacti", "success");
			Toast.makeText(getApplicationContext(), "支付成功", Toast.LENGTH_LONG).show();
			finish();
		}

		@Override
		public void lyPayNo(int payType, String errorInfo) {
			ToastUtils.show(getApplicationContext(), errorInfo);
			HrSDK.getInstance().callbackPay.onFailed();
			//浩然游币支付失败不隐藏
			if(payType!=3){
				finish();
			}
		}
	};

	public interface LyPayResult {
		void lyPayYes(int payType);

		void lyPayNo(int payType, String errorInfo);
	}
	
	
	private void selectView(View v){
		alipay_layout.setSelected(false);
		tecent_pay_layout.setSelected(false);
		unin_pay_layout.setSelected(false);
		longyuan_pay_layout.setSelected(false);
		v.setSelected(true);
	}
	/**
	 * 支付方式点击监听
	 */
	private OnClickListener payMethodClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			
			
			if (v.getId() == ResUtil.getId(getActivity(), "alipay_layout")) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_ALIPAY_PAY);
				pay_method_text.setText(M_ALIPAY);
				password_layout.setVisibility(View.GONE);
				selectView(v);
			} else if (v.getId() == ResUtil.getId(getActivity(), "tecent_pay_layout")) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_TEN_CAIFUTONG_PAY);
				pay_method_text.setText(M_TEN);
				password_layout.setVisibility(View.GONE);
				selectView(v);
			} else if (v.getId() == ResUtil.getId(getActivity(), "unin_pay_layout")) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_UNIN_PAY );
				pay_method_text.setText(M_UNIN);
				password_layout.setVisibility(View.GONE);
				selectView(v);
			} else if (v.getId() == ResUtil.getId(getActivity(), "longyuan_pay_layout")) {
				Gamer.sdkCenter.ButtonClick(HrSDK.AccountId,ButtonTypeConstant.TYPE_BUTTON_LONGYUANBI_PAY);
				//如果是游客，则不可以用龙远币支付
				if(!HrSDK.TYPE_USER.equals(Constant.TYPE_USER_NORMAL)){
					showBindPhone();
					return;
				}
				
				pay_method_text.setText(M_LONGYUAN);
				String haspay_pwd = DeviceUtil.getData(LyPayActivity.this, uid+"haspay_pwd");
				if(TextUtils.isEmpty(haspay_pwd)){
					Logd.d("SDK", "SDK没有取到本地文件中用户是否有支付密码，为空值");
				}
				//如果用户没有支付密码，则弹出设置支付密码的密码框
				if("0".equals(haspay_pwd)){
					goSetPWDDialog();
					password_layout.setVisibility(View.GONE);
				}else{
				password_layout.setVisibility(View.VISIBLE);
				}
				selectView(v);
			}
		}
	};
	
	/**显示绑定手机界面*/
	public void showBindPhone(){
		
		dialogBind = new Dialog(this, ResUtil.getStyleId(this,"ilongyuanAppUpdataCanCancle"));
		View view = this.getLayoutInflater().inflate(
				ResUtil.getLayoutId(this,"ilong_dialog_bindphone_lycoin"), null);
		View CancleBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_cancel"));
		View OKUpdataBtn = view.findViewById(ResUtil.getId(this, "ilong_bind_go"));
		View cancel = view.findViewById(ResUtil.getId(this, "ilong_bind_close"));
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(dialogBind != null && dialogBind.isShowing()){
					dialogBind.cancel();
				}
			}
		});
		CancleBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					if(dialogBind != null && dialogBind.isShowing()){
						dialogBind.cancel();
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
				it.putExtra(Constant.BIND_PAY_NOT_REGISTER, true);
				HrSDK.getActivity().startActivity(it);
			}
		});

		dialogBind.setCancelable(true);
		dialogBind.setCanceledOnTouchOutside(true);
		dialogBind.setContentView(view);
		dialogBind.show();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString("uid", uid);
		outState.putString("amount", amount);
		outState.putString("app_order_id", app_order_id);
		outState.putString("app_uid", app_uid);
		outState.putString("notify_uri", notify_uri);
		outState.putString("product_name", product_name);
		outState.putString("product_id", product_id);
		outState.putString("app_username", app_username);
		outState.putString("access_token", access_token);
	}

	/**
	 * 显示获取订单
	 */
	private void showGetOrderDialog(String message) {
		if (orderDialog == null) {
			orderDialog = new ProgressDialog(this);
			orderDialog.setCancelable(false);
			orderDialog.setCanceledOnTouchOutside(false);
		}
		orderDialog.setMessage(message);
		orderDialog.show();
	}

	private Activity getActivity() {
		return this;
	}
	
	/**
	 * 隐藏获取订单
	 */
	private void dissmissOrderDilaog() {
		try {
			if (orderDialog != null && orderDialog.isShowing()) {
				orderDialog.dismiss();
				orderDialog = null;
			}
		} catch (Exception e) {
			LogUtils.error(e);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
        		HrSDK.showToast("支付取消");
        		HrSDK.getInstance().callbackPay.onCancel();
        	return super.onKeyDown(keyCode, event);
        }
        return super.onKeyDown(keyCode, event);
    }

	private static final String ActivityName ="com.hr.sdk.pay.LyPayActivity";
	@Override
	public String getActivityName() {
		// TODO Auto-generated method stub
		return null;
	}
}
