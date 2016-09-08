package com.hr.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.crypto.Cipher;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.text.StaticLayout;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hr.sdk.HrSDK;
import com.hr.sdk.ac.SdkLoginActivity;
import com.hr.sdk.tools.http.Constant;

public class DeviceUtil {
	public static final String DATA_FIELD = "filed_user";
	public static final String KEY_UID = "key_uid";
	public static final String KEY_UTYPE = "key_utype";
	public static final String KEY_UPWD = "key_upwd";
	public static final String KEY_LOGOUT = "key_logout";
	
	
	/**这个 key是 游戏公告id 
	 */
	public static final String KEY_NOTICE_ID = "notice_id";
	
	public static final String SDK_VERSION="3.6.4";
	public static final String TAG = "DeviceUtil";
	
	private static final String USERINFONAME="data";
	private static final String USEREQUIPMENTCODE="EquipmentCode";
	private static final String APPLICATIONUSERINFO="applicationuserinfo";
	
	/**是否已经迁移登录数据*/
	private static final String KEY_LOGIN_MOVED = "keyLoginMoved";
	
	private static final String FOULD_HAORAN = "/GameCenterhr";
	private static final String FOULD_LONGYUAN = "/longyuan";
	
	
	public static void saveData(Context c, String key, String value){
		try {
			//实例化SharedPreferences对象（第一步） 
			SharedPreferences mySharedPreferences= c.getSharedPreferences(DATA_FIELD, Activity.MODE_PRIVATE); 
			//实例化SharedPreferences.Editor对象（第二步） 
			SharedPreferences.Editor editor = mySharedPreferences.edit(); 
			//用putString的方法保存数据 
			editor.putString(key, value); 
			//提交当前数据 
			editor.commit();
			Logd.d(TAG, key + ", " + value);
		} catch (Exception e) {
			e.printStackTrace();
		}
		 
	}
	
	
	
	/**是否已经做了登录数据的迁移*/
	public static boolean isMoved(Context c){
		try {
			Log.e(TAG, "isMoved calling..");
			String isMoved = getData(c, KEY_LOGIN_MOVED);
			if(isMoved != null && isMoved.equals("true")){
				return true;
			}
			//如果不存在longyuan文件夹，则直接返回
			if(!isLyExist()){
				saveData(c, KEY_LOGIN_MOVED, "true");
				return true;
			}
			//是否包含且只有当前游戏的缓存文件
			
			String path = getBasePathLy(c) + "/data";
			File file = new File(path);
			File[] files = file.listFiles();
			//计算文件数量，如果只有一个，并且是当前游戏的，则开始复制
			int appidFileCount = 0;
			String appidFileName = "";
			File temp;
			for(int i = 0; files != null && i < files.length; i++){
				temp = files[i];
				if(temp.isDirectory()) continue;
				if(temp.getName().length() == 16){
					appidFileCount ++;
					appidFileName = temp.getName();
				}
			}
			//只有一个文件，则复制过去
			Log.e(TAG, appidFileCount + ", " + appidFileName);
			if(appidFileCount == 1 && appidFileName.equals(HrSDK.getInstance().getAppId())){
				doMove(c);
			}
			//文件只有一个，并且是当前游戏的, 则删除ly文件夹;一个没有并且存在data目录
			if((appidFileCount == 1 && appidFileName.equals(HrSDK.appId)) || 
					(appidFileCount == 0 && file.exists())){
				Log.e(TAG, "delete ly");
				File fileLy = new File(getBasePathLy(c));
				deleteFile(fileLy);
				fileLy.delete();
				Log.e(TAG, "delete ly, " + fileLy.getAbsolutePath());
			}
			
			
			saveData(c, KEY_LOGIN_MOVED, "true");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void deleteFile(File oldPath) {
		try {
			if (oldPath.isDirectory()) {
				File[] files = oldPath.listFiles();
				for (File file : files) {
					deleteFile(file);
					file.delete();
				}
			} else {
				oldPath.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static void doMove(Context c){
		try {
			//ly 文件
			String path = getBasePathLy(c) + "/data/" + HrSDK.getInstance().getAppId();
			File fileLy = new File(path);
			BufferedReader br = new BufferedReader(new FileReader(fileLy)); 
			//haoran文件 不存在则创建
			String basePathHr = getBasePath(c) + "/data/" + HrSDK.getInstance().getAppId();
						
			File fileHr = new File(basePathHr);
			Log.e(TAG, fileHr.getAbsolutePath() + ", " + fileHr.exists());
			
			if(! fileHr.exists()){
				
				fileHr.createNewFile();
			}
	        PrintWriter pw = new PrintWriter(fileHr);
	        String str = null;
	        while((str = br.readLine()) != null){
	        	pw.write(str + "\n");
	        }
	        br.close();
	        pw.close();
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isLogout(Context c){
		try {
			if(getData(c, KEY_LOGOUT).equals("true")){
				return true;
			}			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return false;
	}
	
	public static void setLogout(Context mActivity, boolean isLogout){
		DeviceUtil.saveData(mActivity, DeviceUtil.KEY_LOGOUT, isLogout + "");
	}
	
	public static boolean isNotRegister(Context c){
		String userType = DeviceUtil.getData(c, DeviceUtil.KEY_UTYPE);
		return userType.equals(Constant.TYPE_USER_NOT_REGISTER);
	}
	
	public static String getData(Context c, String key){
		try {
			//同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象 
			SharedPreferences sharedPreferences= c.getSharedPreferences(DATA_FIELD, Activity.MODE_PRIVATE); 
			// 使用getString方法获得value，注意第2个参数是value的默认值 
			String value =sharedPreferences.getString(key, ""); 
			return value;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	/**
	 * 获取设备码流程，先试去longyuan文件夹下EquipmentCode.cr获取保存的设备码。
	 * 如果存在则不用使用该设备码，这个是防止设备码改变的情况下用户不能正常登录的问题。
	 * 如果没有获取到设备码，则系统产生设备码
	 * @param activity 当前的Activity 
	 * @return 设备码
	 */
	public static String getUniqueCode(Activity activity){
		String uniquecodename = "uniquecode";
		String Suffix=".cr";
		String uniqueCode="";
		File file=new File(getSecurityPath((Context)activity, USEREQUIPMENTCODE)+"/"+USEREQUIPMENTCODE+Suffix);
		try {
			if(! file.exists()){
				uniqueCode=produceUniqueCode(activity);
				file.createNewFile();
				PrintWriter pw = new PrintWriter(new FileWriter(file));  
				saveData(activity, uniquecodename, uniqueCode);
				pw.println(uniqueCode);
				pw.flush();
				pw.close();
			}
			else{
				 BufferedReader br = new BufferedReader(new FileReader(file)); 
			     uniqueCode=br.readLine();
			     br.close();
			     if(uniqueCode==null || uniqueCode.equals("")){
			    	 file.delete();
			    	 uniqueCode = getData(activity, uniquecodename);
			    	 Log.d(TAG, "get code from application");
			     }
			     if(uniqueCode==null || uniqueCode.equals("")){
			    	 uniqueCode=produceUniqueCode(activity);
			    	 Log.d(TAG, "create code");
			     }
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			uniqueCode=produceUniqueCode(activity);
		} 
		
        Log.d(TAG,"uniquecode:"+uniqueCode);
		return uniqueCode;
		
	}
	//产生设备码
	private static String produceUniqueCode(Activity activity) {
		try {
			TelephonyManager tm = (TelephonyManager) activity.getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

	        String imei=tm.getDeviceId();

	        String simSerialNumber = "";
	        if(tm.getSimSerialNumber()!=null){
	            simSerialNumber=tm.getSimSerialNumber();
	        }

//	        String androidId =android.provider.Settings.Secure.getString(activity.getContentResolver(),android.provider.Settings.Secure.ANDROID_ID);
	        if(imei == null) imei = "";
	        String wifiInfo = getWifiInfo(activity);
	        if(wifiInfo == null) wifiInfo = "";
	        if(simSerialNumber == null) simSerialNumber = "";
	        
	        Logd.d("info", "wifi: " + wifiInfo + ", imei: " + imei + ", simSweianumber: " + simSerialNumber);
	        UUID deviceUuid = new UUID((long)wifiInfo.hashCode(), ((long)imei.hashCode() << 32) |simSerialNumber.hashCode());
	        Logd.d("uuid", deviceUuid.toString());
	        String uniqueId= deviceUuid.toString();
	        uniqueId = MD5(uniqueId);
	        if(HrSDK.getInstance().getDebugMode()){
				DeviceUtil.appendToDebug("unique : " + uniqueId.toString());
			}
	        return uniqueId;
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
		
	}

	public static String getSign(Context context) {
		  PackageManager pm = context.getPackageManager();
		  List<PackageInfo> apps = pm.getInstalledPackages(PackageManager.GET_SIGNATURES);
		  Iterator<PackageInfo> iter = apps.iterator();
		  while(iter.hasNext()) {
		       PackageInfo packageinfo = iter.next();
		       String packageName = packageinfo.packageName;
		       if (packageName.equals(context.getPackageName())) {
		         
		          return packageinfo.signatures[0].toCharsString();
		       }
		}
		  return "";
		}

	
	public static String getWifiInfo(Activity a){
		try {

			WifiManager wifi = (WifiManager) a.getSystemService(Context.WIFI_SERVICE); 
			WifiInfo info = wifi.getConnectionInfo();
			return info.getMacAddress();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}

	public final static String MD5(String s) {
		char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'A', 'B', 'C', 'D', 'E', 'F' };

		try {
			byte[] btInput = s.getBytes();
			// 获得MD5摘要算法的 MessageDigest 对象
			MessageDigest mdInst = MessageDigest.getInstance("MD5");
			// 使用指定的字节更新摘要
			mdInst.update(btInput);
			// 获得密文
			byte[] md = mdInst.digest();
			// 把密文转换成十六进制的字符串形式
			int j = md.length;
			char str[] = new char[j * 2];
			int k = 0;
			for (int i = 0; i < j; i++) {
				byte byte0 = md[i];
				str[k++] = hexDigits[byte0 >>> 4 & 0xf];
				str[k++] = hexDigits[byte0 & 0xf];
			}
			return new String(str);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	public static String getBasePathLy(Context context){
		//判断是否有存储卡
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String pathBase = Environment.getExternalStorageDirectory().getAbsolutePath() + FOULD_LONGYUAN;
			File file = new File(pathBase);
			if(! file.exists()) file.mkdirs();
			return pathBase;
		}else{
			String pathBase = context.getFilesDir().getAbsolutePath() + FOULD_LONGYUAN;
			File file = new File(pathBase);
			if(!file.exists()){
				file.mkdirs();
			}
			return pathBase;
		}
	}
	
	private static boolean isLyExist(){
		String pathBase = Environment.getExternalStorageDirectory().getAbsolutePath() + FOULD_LONGYUAN;
		File file = new File(pathBase);
		return file.exists();
	}
	
	public static String getBasePath(Context context){
		//判断是否有存储卡
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
			String pathBase = Environment.getExternalStorageDirectory().getAbsolutePath() + FOULD_HAORAN;
			File file = new File(pathBase);
			if(! file.exists()) file.mkdirs();
			File fileHrPath = new File(file.getAbsolutePath() + "/data");
			if(!fileHrPath.exists()) {
				fileHrPath.mkdirs();
			}
			return pathBase;
		}else{
			String pathBase = context.getFilesDir().getAbsolutePath() + FOULD_HAORAN;
			File file = new File(pathBase);
			if(!file.exists()){
				file.mkdirs();
			}
			return pathBase;
		}
	}
	
	public static String getSecurityPath(Context context,String filename){
		String path = getBasePath(context) + "/"+filename;
		File file = new File(path);
		if(!file.exists()){
			file.mkdirs();
		}
		return path;		
	}
	
	private static String currentPath(Context context){
		return getSecurityPath(context,USERINFONAME) + "/" + HrSDK.getInstance().getAppId();
	}
	
	public static String encodeData(String content){
		int MaxBlockSize=88;
		int datalength=content.length();
		int byteread = 0;
		StringBuilder data=new StringBuilder();
		if(datalength>MaxBlockSize )
		{
			for(int i=0;i<datalength/MaxBlockSize ;i++)
			{
				data.append(getencodeData(content.substring(byteread, (i+1)*MaxBlockSize))+"|");
				byteread=(i+1)*MaxBlockSize ;
			}
			data.append(getencodeData(content.substring(byteread, datalength)));
		}
		else
		return getencodeData(content);
		return data.toString();
	}
	public static String getencodeData(String content)
	{
		try {
			Cipher pkCipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			PublicKey publicKey = RsaUtilLoad.loadPublicKey(Constant.USER_PUBLIC_KEY);
	        pkCipher.init(Cipher.ENCRYPT_MODE, publicKey);
	        byte[] yy = it.sauronsoftware.base64.Base64.encode(pkCipher.doFinal(content.getBytes("UTF-8")));
	        return new String(yy);
		} catch (Exception e) {
			e.printStackTrace();
			Logd.e(TAG, "encode data failed !!");
			return "";
		}
	}
	public static void deleteUserInfof(Context context){
		try {
			String path = currentPath(context);
			File file = new File(path);
			if(file.exists()){
				file.delete();
				if(HrSDK.getInstance().getDebugMode())
					showToast((Activity)context, "debug 提示:注销成功，删除成功");
			}else{
				Logd.e(TAG, "file is not exit!!");
				if(HrSDK.getInstance().getDebugMode())
					showToast((Activity)context, "debug提示：注销成功，文件不存在，不需要删除");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static HashMap<String, String> readUserFromFile(Context context){
		HashMap<String, String> map = new HashMap<String, String>();
		try {
			String path = currentPath(context);
			File file = new File(path);
			if(! file.exists()){
				Logd.e(TAG, "userinfo data is not exit !!");
				return map;
			}
			
	        BufferedReader br = new BufferedReader(new FileReader(file)); 
	        map.put(Constant.KEY_DATA_TYPE, br.readLine());
	        map.put(Constant.KEY_DATA_USERNAME, br.readLine());
	        map.put(Constant.KEY_DATA_CONTENT, br.readLine());
	        br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return map;
	}
	/**
	 * 读取用户步骤：
	 * 1.读取浩然游文件夹下appid文件
	 * 2.从应用空间读取用户信息
	 * 3.从应用空间读取游客用户信息，如果以上三部得到的都为空。判定该用户是第一次安装该款游戏
	 * 4.从浩然游文件夹下读取其它本公司用户信息。
	 * @param context 上下文
	 * @return 用户信息map
	 */
	public static HashMap<String, String> readUserFromFiles(Context context){
		HashMap<String, String> target = readUserFromFile(context);
		if(target.size() != 0){
			return target;
		}
		target = readUserToApplication(context);
		if(target.size()!=0){
			return target;
		}
		if(!IsTourist(context)){
			return new HashMap<String, String>();
		}
		try {
			String path = getSecurityPath(context,USERINFONAME);
			File root = new File(path);
			if(! root.exists()){
				Logd.e(TAG, "userinfo data is not exit !!");
				return new HashMap<String, String>();
			}
			
			//排序  将日期较大的排前面
			List<File> files = Arrays.asList(root.listFiles());
			Collections.sort(files, new Comparator<File>() {

				@Override
				public int compare(File lhs, File rhs) {
					
					return (int) (rhs.lastModified() - lhs.lastModified());
				}
			});
			for(int i = 0; i < files.size(); i++){
				File file = files.get(i);		
				if(file.getName().length() > 16){
					file.delete();
					continue;
				}
//				if(file.getName().length() != 16){
//					Logd.e("", file.getName() + ", " + file.getName().length());
//					continue;
//				}
				HashMap<String, String> map = new HashMap<String, String>();
		        BufferedReader br = new BufferedReader(new FileReader(file)); 
		        map.put(Constant.KEY_DATA_TYPE, br.readLine());
		        map.put(Constant.KEY_DATA_USERNAME, br.readLine());
		        map.put(Constant.KEY_DATA_CONTENT, br.readLine());
		        br.close();
		        String type = map.get(Constant.KEY_DATA_TYPE);
		        if(type == null || !type.equals(Constant.TYPE_USER_NORMAL)){
		        	Log.e("", "type is guest, continue");
		        	continue;
		        }
		        target = map;
		     // 文件内容的每一项，都不能为空
		        if(target!=null 
		        		&& !map.get(Constant.KEY_DATA_TYPE).equals("")
		        		&& !map.get(Constant.KEY_DATA_USERNAME).equals("")
		        		&& !map.get(Constant.KEY_DATA_CONTENT).equals(""))
		        	return target;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return target;
	}
	
	/**
	 * 从应用控件读取数据
	 * @param context 上下文
	 */
	private static HashMap<String, String> readUserToApplication(Context context) {
		String userinfoString = getData(context, APPLICATIONUSERINFO);
		HashMap<String, String> userinfo = new HashMap<String, String>();
		if(userinfoString.equals("")){
			return userinfo;
		}else{
			userinfo = JSON.parseObject(userinfoString, new HashMap<String, String>().getClass());
			return userinfo;
		}
			
	}

	/**
	 * 从应用空间去读取游客信息
	 * @param context 上下文
	 */
	private static boolean IsTourist(Context context) {
		// TODO Auto-generated method stub
		String target = getData(context, HrSDK.getInstance().getAppId());
		return target.equals("");
	}

	/**
	 * 路径为基础路径 + appid
	 * */
	public static void writeUserToFile(HashMap<String, String> map, Context context){
		try {
			if(map == null || !map.containsKey(Constant.KEY_DATA_TYPE) || 
					! map.containsKey(Constant.KEY_DATA_USERNAME) ||
					! map.containsKey(Constant.KEY_DATA_CONTENT)){
				Logd.e("lysdk", "error ! save map is error");
				Toast.makeText(context, "save map is error !", Toast.LENGTH_LONG).show();
				return;
			}
			String type = map.get(Constant.KEY_DATA_TYPE);
			String username = map.get(Constant.KEY_DATA_USERNAME);
			String content = map.get(Constant.KEY_DATA_CONTENT);
			String path = currentPath(context);
			File file = new File(path);
			if(! file.exists()){
				file.createNewFile();
			}
	        PrintWriter pw = new PrintWriter(new FileWriter(file));  
			pw.println(type);
			pw.println(username);
			pw.println(content);
			pw.flush();
			pw.close();
			saveData(context, KEY_UID, username);
			saveData(context, KEY_UTYPE, map.get(Constant.KEY_DATA_TYPE));
			/**将用户信息写入到应用空间*/
			saveData(context,APPLICATIONUSERINFO ,JSON.toJSONString(map));
			Logd.d(TAG, map.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//判断是否有用户存储信息
	public static boolean isDataEnable(Context context){
		File file = new File(currentPath(context));
		if(! file.exists()){
			return false;
		}else{
			return true;
		}
	}
	
	public static void showToast(final Activity context, final String msg){
		context.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			}
		});
	}
	public static void initDebug(){
		try {
			//如果不是debug模式 则退出
			if(! HrSDK.getInstance().getDebugMode()){
				return;
			}
			File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/debug.text");
			if(file.exists()){
				file.delete();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void appendToDebug(String text){
		try {
			HrSDK.debugInfo = HrSDK.debugInfo + text + "\n\n";
			File file = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/debug.text");
			if(!file.exists()){
				file.createNewFile();
			}
			PrintWriter pw = new PrintWriter(new FileWriter(file));  
			pw.write(HrSDK.debugInfo);
			pw.flush();
			pw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
    /**
     * 清除应用控件目录
     * @param context 上下文
     */
	public static void clearApplicationData(Context context){
		//实例化SharedPreferences对象（第一步） 
		SharedPreferences mySharedPreferences= context.getSharedPreferences(DATA_FIELD, Activity.MODE_PRIVATE); 
		//实例化SharedPreferences.Editor对象（第二步） 
		SharedPreferences.Editor editor = mySharedPreferences.edit(); 
		editor.clear().commit();
	}
	
	/** 
     * 使用递归删除文件目录
     *  
     * @param deleteThisPath 文佳夹目录
     * @param filepath 是否删除
     * @return 
     */  
    public static void deleteFolderFile(String filePath, boolean deleteThisPath){  
        try {
			if (!TextUtils.isEmpty(filePath)) {  
			    File file = new File(filePath);  
  
			    if (file.isDirectory()) {// 处理目录  
			        File files[] = file.listFiles();  
			        for (int i = 0; i < files.length; i++) {  
			            deleteFolderFile(files[i].getAbsolutePath(), true);  
			        }  
			    }  
			    if (deleteThisPath) {  
			        if (!file.isDirectory()) {// 如果是文件，删除  
			            file.delete();  
			        } else {// 目录  
			            if (file.listFiles().length == 0) {// 目录下没有文件或者目录，删除  
			                file.delete();  
			            }  
			        }  
			    }  
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    }  
 
//	public static void putUserName(Context context, String userName){
//		SharedPreferences mySharedPreferences = context.getSharedPreferences("longyuan_username",
//				Activity.MODE_PRIVATE);
//		// 实例化SharedPreferences.Editor对象（第二步）
//		SharedPreferences.Editor editor = mySharedPreferences.edit();
//		// 用putString的方法保存数据
//		editor.putString("username", userName);
//		// 提交当前数据
//		editor.commit();
//	}
//	
//	public static String getUserName(Context context){
//		//同样，在读取SharedPreferences数据前要实例化出一个SharedPreferences对象 
//		SharedPreferences sharedPreferences= context.getSharedPreferences("longyuan_username", 
//		 Activity.MODE_PRIVATE); 
//		 // 使用getString方法获得value，注意第2个参数是value的默认值 
//		String name =sharedPreferences.getString("username", ""); 
//		return name;
//	}

}




