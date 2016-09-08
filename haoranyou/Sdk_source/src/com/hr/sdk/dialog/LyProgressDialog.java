package com.hr.sdk.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.hr.sdk.tools.ResUtil;

public class LyProgressDialog extends Dialog {

	private Context context;

	public LyProgressDialog(Context context) {
		super(context, ResUtil.getStyleId(context, "dialogStyle"));
		this.context = context;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(ResUtil.getLayoutId(context, "ilong_progress"));
	}

}
