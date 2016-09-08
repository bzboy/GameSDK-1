package com.hr.sdk.modle;

import java.io.Serializable;

/**
 * 登陆之前验证是否显示客服，更新等
 * @author niexiaoqiang
 */
public class PackInfoModel implements Serializable {
	//{"update":0,"kf":0}
	private static final long serialVersionUID = 1L;
	private int update;
	private int kf;
	private String uri;
	private String bbs;
	
	private String notice;
	
	private int force ;
	public String update_msg = "";
	
	public String getNotice(){
		return notice;
	}
	
	public void setNotice(String notice){
		this.notice = notice;
	}
	
	public String getUpdate_msg(){
		return update_msg;
	}
	
	public void setUpdate_msg(String update_msg){
		this.update_msg = update_msg;
	}
	
	public int getForce() {
		return force;
	}

	public void setForce(int force) {
		this.force = force;
	}

	public String getBbs() {
		return bbs;
	}

	public void setBbs(String bbs) {
		this.bbs = bbs;
	}

	public int getUpdate() {
		return update;
	}

	public void setUpdate(int update) {
		this.update = update;
	}

	public int getKf() {
		return kf;
	}

	public void setKf(int kf) {
		this.kf = kf;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

}
