package com.longyuan.sdk.i;

import com.longyuan.sdk.modle.UserInfo;

/**
 * 使用token来获取用户信息
 * @author niexiaoqiang
 */
public interface IToken2UserInfo {

	public void onSuccess(UserInfo userInfo);
	public void onFailed();
}
