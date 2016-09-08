package com.hr.sdk.tools;

import com.hr.sdk.HrSDK;
import com.longyuan.sdk.BuildConfig;

import android.util.Log;

/**
 * 日志
 * @author niexiaoqiang
 */
public class LogUtils {
	public static final String TAG = "haoranyou";

	public static void debug(Object msg) {
		if(HrSDK.getInstance().getDebugMode())
			Log.d(TAG, null == msg ? "" : msg.toString());
	}

	public static void info(Object msg) {
		if(HrSDK.getInstance().getDebugMode())
			Log.i(TAG, null == msg ? "" : msg.toString());
	}

	public static void error(Object msg) {
		if(HrSDK.getInstance().getDebugMode())
			Log.e(TAG, null == msg ? "" : msg.toString());
		if (BuildConfig.DEBUG && msg instanceof Throwable) {
			((Throwable) msg).printStackTrace();
		}
	}

	public static void warn(Object msg) {
		if(HrSDK.getInstance().getDebugMode())
			Log.w(TAG, null == msg ? "" : msg.toString());
	}
}
