package com.hr.sdk.modle;

import java.io.Serializable;

/**
 * 登陆返回的CODE
 * @author niexiaoqiang
 */
public class LoginCodeModel implements Serializable {

	private static final long serialVersionUID = 1L;
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

}
