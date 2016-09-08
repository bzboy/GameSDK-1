package com.hr.util;

import com.hr.sdk.HrSDK;

import android.util.Log;

public class Logd {
	public static final String TAG = "ilongsdk";
	
	public static boolean isNull(String tag, String msg){
		if(tag == null){
			Log.e("Log", "tag is null");
			return true;
		}
		if(msg == null){
			Log.e("Log", "msg is null");
			return true;
		}
		return false;
	}
	
	public static void i(String tag, String msg){
		if(isNull(TAG, msg)){
			return;
		}
		if(HrSDK.getInstance().getDebugMode())
			Log.i(TAG, msg);
	}
	
	public static void d(String tag, String msg){
		if(isNull(TAG, msg)){
			return;
		}
		if(HrSDK.getInstance().getDebugMode())
			Log.d(TAG, msg);
	}
	
	public static void e(String tag, String msg){
		if(isNull(TAG, msg)){
			return;
		}
		if(HrSDK.getInstance().getDebugMode())
			Log.e(TAG, msg);
	}
}
