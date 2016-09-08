package com.lygame.model.coin;

public class PaymentSuccess {
	public String event = "paymentSuccess";
	public String accountId ="";
	public String orderId ="";
	public String iapId ="";
	public String amount ="";
	public String currencyType ="";
	public String paymentType ="";
	public long ts ;
	public String getEvent() {
		return event;
	}
	public void setEvent(String event) {
		this.event = event;
	}
	public String getAccountId() {
		return accountId;
	}
	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}
	public String getOrderId() {
		return orderId;
	}
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	public String getIapId() {
		return iapId;
	}
	public void setIapId(String iapId) {
		this.iapId = iapId;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getCurrencyType() {
		return currencyType;
	}
	public void setCurrencyType(String currencyType) {
		this.currencyType = currencyType;
	}
	public String getPaymentType() {
		return paymentType;
	}
	public void setPaymentType(String paymentType) {
		this.paymentType = paymentType;
	}
	public long getTs() {
		return ts;
	}
	public void setTs(long ts) {
		this.ts = ts;
	}
	public PaymentSuccess() {
		super();
	}
	public PaymentSuccess(String accountId, String orderId,
			String iapId, String amount, String currencyType,
			String paymentType, long ts) {
		super();
		this.accountId = accountId;
		this.orderId = orderId;
		this.iapId = iapId;
		this.amount = amount;
		this.currencyType = currencyType;
		this.paymentType = paymentType;
		this.ts = ts;
	}
	
	
	
}
