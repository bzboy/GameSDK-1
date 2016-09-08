package com.longyuan.sdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.longyuan.sdk.IlongSDK;
import com.longyuan.sdk.tools.ResUtil;

public class LyProgressDialog extends Dialog {

	private Context context;

	public LyProgressDialog(Context context) {
		super(context, ResUtil.getStyleId(context, "dialogStyle"));
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(IlongSDK.ISLONG){
			context.setTheme(ResUtil.getStyleId(context, "Ilong_Theme"));
		}else{
			context.setTheme(ResUtil.getStyleId(context, "HR_Theme"));
		}
		setContentView(ResUtil.getLayoutId(context, "ilong_progress"));
	}

}
