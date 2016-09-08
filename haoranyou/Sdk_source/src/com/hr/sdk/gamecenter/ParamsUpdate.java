package com.hr.sdk.gamecenter;

import java.lang.ref.WeakReference;

import android.os.CountDownTimer;
import android.view.View;
import android.view.WindowManager;

/**
 * 布局的动画
 * @author niexiaoqiang
 */
public class ParamsUpdate {
	private WindowManager mWindowManager;
	private WeakReference<View> mTargetView;
	private WindowManager.LayoutParams mParams;
	private TimerDown timerDown;

	public ParamsUpdate(View view, WindowManager.LayoutParams params, WindowManager windowManager) {
		this.mTargetView = new WeakReference<View>(view);
		this.mParams = params;
		this.mWindowManager = windowManager;
	}

	public static final void updateParams(View view, WindowManager.LayoutParams params, WindowManager windowManager) {
		if (null != view && null != view.getParent()) {
			windowManager.updateViewLayout(view, params);
		}
	}

	public void startUpdateX(int moveDistanceX) {
		if (null != timerDown && timerDown.isRunning()) {
			timerDown.cancelTick();
		}
		//先计算时间
		int duantion = Math.abs(moveDistanceX);
		if (duantion > 200) {
			duantion = 200;
		}
		timerDown = new TimerDown(moveDistanceX, duantion);
		timerDown.startTick();
	}

	private class TimerDown extends CountDownTimer {
		//10毫秒刷新一次
		public static final int REFRESH_TIME = 20;
		private boolean running = false;
		private int mDunation = 0;
		private int mMoveDistanceX;
		private int finalX = 0;
		private long lastMillisUntilFinished = 0;
		private float speed;

		public TimerDown(int moveDistanceX, int dunation) {
			super(dunation, REFRESH_TIME);
			this.lastMillisUntilFinished = this.mDunation = dunation;
			this.finalX = this.mMoveDistanceX = moveDistanceX;
			this.speed = (float) mMoveDistanceX / (float) mDunation;
		}

		@Override
		public void onFinish() {
			running = false;
			mParams.x = mParams.x + finalX;
			updateParams(mTargetView.get(), mParams, mWindowManager);
		}

		public void cancelTick() {
			this.cancel();
			this.onFinish();
		}

		@Override
		public void onTick(long millisUntilFinished) {
			int moveX = (int) ((lastMillisUntilFinished - millisUntilFinished) * speed);
			lastMillisUntilFinished = millisUntilFinished;
			finalX = finalX - moveX;
			mParams.x = mParams.x + moveX;
			updateParams(mTargetView.get(), mParams, mWindowManager);
		}

		public void startTick() {
			running = true;
			this.start();
		}

		public boolean isRunning() {
			return running;
		}
	}
}
