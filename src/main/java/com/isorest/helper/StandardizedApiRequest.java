package com.isorest.helper;

import java.util.List;

import org.apache.log4j.Logger;

import com.isorest.domain.Field;
import com.isorest.domain.IsoRestRequest;
import com.isorest.domain.SubIso;

import postilion.realtime.sdk.message.bitmap.Iso8583Post.PrivBit;

public class StandardizedApiRequest {
	
	private static Logger logger = Logger.getLogger(StandardizedApiRequest.class);
	
	private String msgType;
	private String pan;
	private String processingCode;
	private String tranAmount;	
	private String fromAccount;
	private String toAccount;
	private String MSISDN;
	
	private String sponsorBank;
	private String payeeReference;
	
	
	public StandardizedApiRequest unpackAPiRequest(IsoRestRequest apiReq) {
		
		List<Field> fieldsList = apiReq.getField();
		SubIso  privFields = apiReq.getIsomsg();
		List<Field> privFieldsList = privFields.getField();

		for (Field field : fieldsList){
			switch (Integer.parseInt( field.getId() ) ){ //TODO: API req must make ID an int
			case 0: this.setMsgType(field.getValue()); break;
			case 2: this.setPan(field.getValue());  break;
			case 3: this.setProcessingCode(field.getValue());  break;
			case 4: this.setTranAmount(field.getValue());  break;	//TODO: API req must send in minor denomination
			case 102: this.setFromAccount(field.getValue());  break;
			case 103: this.setToAccount(field.getValue()); break;
			}

		}		
		logger.info(this.getTranAmount()  + " cents. From Acct: " + this.getFromAccount() + " To Acct: " + this.getToAccount());
		
		for (Field field : privFieldsList){
			
//			logger.info(field.getId() + "  "+ field.getValue());
			
			switch ( Integer.parseInt( field.getId()) ){ //TODO: API req must make ID an int
			case PrivBit._014_SPONSOR_BANK : this.setSponsorBank(field.getValue());  break;
			case PrivBit._022_STRUCT_DATA : this.setMSISDN(field.getValue());  break;
			case PrivBit._024_PAYEE_REFERENCE : this.setPayeeReference(field.getValue());  break;
			}
		}
		
		return this;		
	}	

	
	public String getMsgType() {
		return msgType;
	}

	public void setMsgType(String tranType) {
		this.msgType = tranType;
	}

	public String getPan() {
		return pan;
	}


	public void setPan(String pan) {
		this.pan = pan;
	}


	public String getProcessingCode() {
		return processingCode;
	}

	public void setProcessingCode(String processingCode) {
		this.processingCode = processingCode;
	}

	public String getTranAmount() {
		return tranAmount;
	}

	public void setTranAmount(String tranAmount) {
		this.tranAmount = tranAmount;
	}

	public String getFromAccount() {
		return fromAccount;
	}

	public void setFromAccount(String fromAccount) {
		this.fromAccount = fromAccount;
	}

	public String getToAccount() {
		return toAccount;
	}

	public String getMSISDN() {
		return MSISDN;
	}


	public void setMSISDN(String mSISDN) {
		MSISDN = mSISDN;
	}


	public String getSponsorBank() {
		return sponsorBank;
	}


	public void setSponsorBank(String sponsorBank) {
		this.sponsorBank = sponsorBank;
	}


	public String getPayeeReference() {
		return payeeReference;
	}


	public void setPayeeReference(String payeeReference) {
		this.payeeReference = payeeReference;
	}


	public void setToAccount(String toAccount) {
		this.toAccount = toAccount;
	}
}
