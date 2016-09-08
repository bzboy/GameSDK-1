package com.hr.sdk.dialog;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hr.sdk.tools.ResUtil;

public class WelcomeToast extends Toast {
	private LayoutInflater inflater;
	private View layout;

	public WelcomeToast(Context context, String username) {
		super(context);
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		layout = inflater.inflate(ResUtil.getLayoutId(context, "ly_toast_view"), null);
		TextView welcomeUsername = (TextView) layout.findViewById(ResUtil.getId(context, "ly_welcome_toast_username"));
		welcomeUsername.setText(username + "  ");
		setView(layout);
		setGravity(Gravity.TOP, 0, 0);
		setDuration(Toast.LENGTH_LONG);
	}
}
