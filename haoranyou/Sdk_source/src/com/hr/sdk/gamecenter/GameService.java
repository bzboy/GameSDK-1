package com.hr.sdk.gamecenter;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.hr.sdk.HrSDK;
import com.hr.sdk.ac.ActivityUser;
import com.hr.sdk.tools.ResUtil;
import com.nineoldandroids.view.ViewHelper;

@SuppressWarnings("deprecation")
public class GameService extends Service {
	//吸边延时
	public static final int SUCTION_SIDE_DELAY = 3000;
	//半透明延时
	public static final int HALF_TRANLATE_DELAYT = 1500;
	
	private static WindowManager mWindowManager;
	private View mHiddenView;
	private View mFloatView;
	private WindowManager.LayoutParams paramFloatView;
	private WindowManager.LayoutParams paramHiddenView;
	private ImageView hidden_image;
	private GradientDrawable hiddenImageDrawable;
	private TextView hidden_tip_text;
	private ImageView float_imageview;
	private boolean ismovecenter=false;

	@Override
	public void onCreate() {
		super.onCreate();
		mWindowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		mHander = new GameServiceHandler(this);
		initView();
		paramsUpdate = new ParamsUpdate(mFloatView, paramFloatView, mWindowManager);
		suctionSide();
	}

	/**重新测量了屏幕*/
	public void  getScreenInfo() {
		DisplayMetrics screenInfo=new DisplayMetrics();
		mWindowManager.getDefaultDisplay().getMetrics(screenInfo);
		screenInfo.widthPixels+=getNavigationBarHeight(GameService.this.getBaseContext());
		HrSDK.screenInfo=screenInfo;
	}
	
	/**
	 * 判断是否有虚拟按键
	 * @param context 上下文
	 * @return true是有，false事没有
	 */
	 private  boolean checkDeviceHasNavigationBar(Context context) {
	        boolean hasNavigationBar = false;
	        Resources rs = context.getResources();
	        int id = rs.getIdentifier("config_showNavigationBar", "bool", "android");
	        if (id > 0) {
	            hasNavigationBar = rs.getBoolean(id);
	        }
	        try {
	            Class systemPropertiesClass = Class.forName("android.os.SystemProperties");
	            Method m = systemPropertiesClass.getMethod("get", String.class);
	            String navBarOverride = (String) m.invoke(systemPropertiesClass, "qemu.hw.mainkeys");
	            if ("1".equals(navBarOverride)) {
	                hasNavigationBar = false;
	            } else if ("0".equals(navBarOverride)) {
	                hasNavigationBar = true;
	            }
	        } catch (Exception e) {
	        }
	     return hasNavigationBar;
	   }
	/**
	 * 获取虚拟按键的高度
	 * @param context 上下文
	 * @return 虚拟按键的高度;
	 */
	   private  int getNavigationBarHeight(Context context) {
	        int navigationBarHeight = 0;
	        Resources rs = context.getResources();
	        int id = rs.getIdentifier("navigation_bar_height", "dimen", "android");
	        if (id > 0 && checkDeviceHasNavigationBar(context)) {
	            navigationBarHeight = rs.getDimensionPixelSize(id);
	        }
	        return navigationBarHeight;
	    }
	

	private ParamsUpdate paramsUpdate;
	private GameServiceHandler mHander;
	//半透明消息
	public static final int TRANS_50 = 0;

	static class GameServiceHandler extends Handler {
		private WeakReference<GameService> mOuter;

		public GameServiceHandler(GameService gameService) {
			mOuter = new WeakReference<GameService>(gameService);
		}

		@Override
		public void handleMessage(Message msg) {
			GameService outer = mOuter.get();
			if (outer != null) {
				switch (msg.what) {
					case TRANS_50:
						ViewHelper.setAlpha(outer.float_imageview, 0.5f);
						//显示三角
						showHalf(outer);
						break;
				}
			}
		}
	}

	private static void showHalf(GameService gameService) {
		//获取位置
		gameService.float_imageview.getLocationOnScreen(gameService.floatViewLocation);
		if (gameService.floatViewLocation[0] <= 0) {
			ViewHelper.setTranslationX(gameService.float_imageview, -gameService.mFloatView.getWidth() / 2);
		} else {
			ViewHelper.setTranslationX(gameService.float_imageview, gameService.mFloatView.getWidth() / 2);
		}
	}

	/**
	 * 移除掉所有的消息
	 */
	private void removeAutoMessage() {
		mHander.removeCallbacks(suctionSideRunnable);
		mHander.removeMessages(TRANS_50);
		ViewHelper.setAlpha(float_imageview, 1.0f);
		ViewHelper.setTranslationX(float_imageview, 0);
	}

	/**
	 * 吸边
	 */
	private void suctionSide() {
		mHander.postDelayed(suctionSideRunnable, SUCTION_SIDE_DELAY);
	}

	/**
	 * 吸边线程
	 */
	private Runnable suctionSideRunnable = new Runnable() {

		@Override
		public void run() {
			//刷新屏幕信息
//			refreshScreenInfo();
//			隐藏悬浮窗
			floatView();
			//发送隐藏消息
			mHander.sendEmptyMessageDelayed(TRANS_50, HALF_TRANLATE_DELAYT);
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mFloatView != null)
			mWindowManager.removeView(mFloatView);
		if (mHiddenView != null)
			mWindowManager.removeView(mHiddenView);
	}

	private void initView() {
		LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
		//隐藏区域定义
		paramHiddenView = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		paramHiddenView.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
		mHiddenView = layoutInflater.inflate(ResUtil.getLayoutId(this, "hidden_tipview"), null);
		hidden_image = (ImageView) mHiddenView.findViewById(ResUtil.getId(this, "hidden_image"));
		hidden_tip_text = (TextView) mHiddenView.findViewById(ResUtil.getId(this, "hidden_tip_text"));
		mHiddenView.setVisibility(View.GONE);
		hiddenImageDrawable = new GradientDrawable();
		hiddenImageDrawable.setShape(GradientDrawable.OVAL);
		hiddenImageDrawable.setStroke(3, Color.argb(0XFF, 0X7A, 0X7A, 0X7A));
		hidden_image.setBackgroundDrawable(hiddenImageDrawable);
		mWindowManager.addView(mHiddenView, paramHiddenView);
		//悬浮框定义
		paramFloatView = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_PHONE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
		paramFloatView.gravity = Gravity.TOP | Gravity.LEFT;
		paramFloatView.x = HrSDK.screenInfo.widthPixels ;
		paramFloatView.y = HrSDK.screenInfo.heightPixels / 2;
		mFloatView = layoutInflater.inflate(ResUtil.getLayoutId(this, "ilong_float_view"), null);
		float_imageview = (ImageView) mFloatView.findViewById(ResUtil.getId(this, "float_imageview"));
		mWindowManager.addView(mFloatView, paramFloatView);
		mFloatView.setOnTouchListener(touchListener);
		mFloatView.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				Intent intent = new Intent(GameService.this, ActivityUser.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
		});
	}

	private View.OnTouchListener touchListener = new View.OnTouchListener() {
		private int initialX;
		private int initialY;
		private float initialTouchX;
		private float initialTouchY;
		private float downx, downy;
		private boolean moved = false;

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					removeAutoMessage();
					initialX = paramFloatView.x;
					initialY = paramFloatView.y;
					initialTouchX = event.getRawX();
					initialTouchY = event.getRawY();
					//记录按下的位置，并且初始化 允许点击
					downx = event.getRawX();
					downy = event.getRawY();
					moved = false;
					return false;
				case MotionEvent.ACTION_UP:
					mHiddenView.setVisibility(View.GONE);
					//计算位置绝对是否隐藏
					checkHiddenPostion(true,event);
					//如果移动过则不执行点击
					if (!moved) {
						v.performClick();
					}
					suctionSide();
					return true;
				case MotionEvent.ACTION_MOVE:
					//移动距离很小不执行显示隐藏区域
					if (!moved && Math.abs(event.getRawX() - downx) > 10 && Math.abs(event.getRawY() - downy) > 10) {
						moved = true;
					}
					if (moved) {
						mHiddenView.setVisibility(View.VISIBLE);
						checkHiddenPostion(false,event);
					}
					paramFloatView.x = initialX + (int) (event.getRawX() - initialTouchX);
					paramFloatView.y = initialY + (int) (event.getRawY() - initialTouchY);
					if(!ismovecenter){
						ParamsUpdate.updateParams(mFloatView, paramFloatView, mWindowManager);
					}
					return true;
			}
			return false;
		}
	};
	//悬浮窗隐藏逻辑实现
	private int[] floatViewLocation = new int[2];
	private int[] hiddenViewLocation = new int[2];

	private void checkHiddenPostion(boolean hidden,MotionEvent event) {
		//隐藏框和手指间的距离
		int event_distance_hiddenImage;
		//计算当前位置--L,T
		mFloatView.getLocationOnScreen(floatViewLocation);
		hidden_image.getLocationOnScreen(hiddenViewLocation);
		//计算中心点坐标
		int floatViewR = mFloatView.getWidth() / 2;
		int hiddenImageR = hidden_image.getWidth() / 2;
		floatViewLocation[0] = floatViewR + floatViewLocation[0];
		floatViewLocation[1] = floatViewR + floatViewLocation[1];
		hiddenViewLocation[0] = hiddenImageR + hiddenViewLocation[0];
		hiddenViewLocation[1] = hiddenImageR + hiddenViewLocation[1];
		int distance = (int) Math.sqrt(Math.pow(Math.abs(hiddenViewLocation[0] - floatViewLocation[0]), 2) + Math.pow(Math.abs(hiddenViewLocation[1] - floatViewLocation[1]), 2));
		if (distance >= (hiddenImageR + floatViewR)) {
			hiddenImageDrawable.setStroke(3, Color.argb(0XFF, 0X7A, 0X7A, 0X7A));
			hiddenImageDrawable.setColor(Color.argb(0, 0X7A, 0X7A, 0X7A));
			hidden_image.setBackgroundDrawable(hiddenImageDrawable);
			hidden_tip_text.setTextColor(Color.argb(0XFF, 0X7A, 0X7A, 0X7A));
		}else {
			float alpha = (float) (hiddenImageR + floatViewR - distance) / (float) (floatViewR * 2);
			if (alpha >= 1f) {
				hiddenImageDrawable.setStroke(3, Color.argb(0XFF, 0X24, 0X72, 0XD2));
				hiddenImageDrawable.setColor(Color.argb((int) (255 * (alpha < 0.3f ? alpha : 0.8f)), 0X24, 0X72, 0XD2));
				hidden_image.setBackgroundDrawable(hiddenImageDrawable);
				hidden_tip_text.setTextColor(Color.argb(0XFF, 0X24, 0X72, 0XD2));
				if (hidden) {
					HrSDK.getInstance().hideFloatView();
				}
			} else {
				hiddenImageDrawable.setStroke(3, Color.argb(0XFF, 0X7A, 0X7A, 0X7A));
				hiddenImageDrawable.setColor(Color.argb((int) (255 * (alpha < 0.3f ? alpha : 0.3f)), 0X7A, 0X7A, 0X7A));
				hidden_image.setBackgroundDrawable(hiddenImageDrawable);
				hidden_tip_text.setTextColor(Color.argb(0XFF, 0X7A, 0X7A, 0X7A));
				if(!ismovecenter){
					moveToCenter(hiddenViewLocation);
				}
			}
			//通过判断隐藏标和手指的距离，去改变是否要将图标至于隐藏图标中间
			if(ismovecenter){
				event_distance_hiddenImage=(int) Math.sqrt(Math.pow(Math.abs(hiddenViewLocation[0] - event.getRawX()), 2) + Math.pow(Math.abs(hiddenViewLocation[1] - event.getRawY()), 2));
				ismovecenter=(event_distance_hiddenImage>(hiddenImageR + floatViewR)*1.5)?false:true;
			}
		}
	}
	
//	强浮标移动到中心点
	private void moveToCenter(int[] hiddenViewLocations) {
		ismovecenter=true;
		paramFloatView.x = hiddenViewLocations[0]-mFloatView.getWidth()/2;
		paramFloatView.y =  hiddenViewLocations[1]-mFloatView.getHeight()/2;
		ParamsUpdate.updateParams(mFloatView, paramFloatView, mWindowManager);
	}

	//	监听横竖屏切换
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
         super.onConfigurationChanged(newConfig);
         floatView();
    } 
	
//	隐藏悬浮图标，具体实现
	public void floatView(){
		 getScreenInfo();
		 mFloatView.getLocationOnScreen(floatViewLocation);
			if (floatViewLocation[0] > (HrSDK.screenInfo.widthPixels - mFloatView.getWidth()) / 2) {
				//靠右
				paramsUpdate.startUpdateX((HrSDK.screenInfo.widthPixels - mFloatView.getWidth()) - paramFloatView.x);
			} else {
				//靠左
				paramsUpdate.startUpdateX(0 - paramFloatView.x);
			}
	}
}
