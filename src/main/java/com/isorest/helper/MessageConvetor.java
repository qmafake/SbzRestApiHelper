package com.isorest.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.isorest.domain.IsoRestRequest;

import postilion.realtime.sdk.message.bitmap.Iso8583;
import postilion.realtime.sdk.message.bitmap.Iso8583.MsgType;
import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.message.bitmap.Iso8583Post.Bit;
import postilion.realtime.sdk.message.bitmap.Iso8583Post.PrivBit;
import postilion.realtime.sdk.message.bitmap.OriginalDataElements;
import postilion.realtime.sdk.message.bitmap.SignedAmount;
import postilion.realtime.sdk.message.bitmap.StructuredData;
import postilion.realtime.sdk.message.bitmap.XFieldUnableToConstruct;
import postilion.realtime.sdk.util.DateTime;
import postilion.realtime.sdk.util.XPostilion;
import postilion.realtime.sdk.util.convert.Pack;

public class MessageConvetor {

	private static Logger logger = Logger.getLogger(MessageConvetor.class);

	public MessageConvetor() {		
	}

	public Iso8583 constructNetworkSignOn() throws XPostilion {

		Iso8583 msg = new Iso8583();
		DateTime date = new DateTime();

		Date dt = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("MMdd");

		msg.putMsgType(MsgType._0800_NWRK_MNG_REQ);
		msg.putField(7, date.get("MMddhhmmss"));
		msg.putField(11, "888888");
		msg.putField(12, date.get("HHmmss"));
		msg.putField(13, fmt.format(dt));
		msg.putField(70, "001");
		msg.putField(100, "91911911777");
		return msg;
	}

	public byte[] new2byteHeaderPacket(byte data[]) {
		int len = data.length;
		byte buf[] = new byte[len + 2];
		// byte buf[] = new byte[len ]; //byte[] comes with two extra spaces already
		buf[0] = (byte) (len >> 8 & 0xff);
		buf[1] = (byte) (len & 0xff);
		System.arraycopy(data, 0, buf, 2, len);
		return buf;
	}

	public byte[] constructReqMsgToPostilion(IsoRestRequest apiReq) throws XPostilion {

		Iso8583Post msg = new Iso8583Post();
		DateTime dateTime = new DateTime();

		StandardizedApiRequest stdApiReq = new StandardizedApiRequest(); 
		stdApiReq = stdApiReq.unpackAPiRequest(apiReq);

		msg.setMessageType(stdApiReq.getMsgType());
		putDefaultFields(msg);
		msg.putField( Bit._003_PROCESSING_CODE, stdApiReq.getProcessingCode() );
		msg.putField(Bit._004_AMOUNT_TRANSACTION, Pack.resize(stdApiReq.getTranAmount(), 12, '0', false));	//TODO: Request in minor denomination
		
		msg.putField(Bit._014_DATE_EXPIRATION, "1808"); //TODO: Really now?
		msg.putField(Bit._015_DATE_SETTLE, dateTime.get("MMdd"));
		
		msg.putAmountTranFee(processCalcCharge("0", msg.getMsgType()));
		msg.putAmountTranProcFee(processCalcCharge("0", msg.getMsgType()));
		
		//		msg.putField(Bit._028_AMOUNT_TRAN_FEE, 
		msg.putField(Bit._026_POS_PIN_CAPTURE_CODE, "12");	
		msg.putField(Bit._032_ACQUIRING_INST_ID_CODE, "502195");
		msg.putField(Bit._033_FORWARDING_INST_ID_CODE, "502195");

//		msg.putField(Bit._037_RETRIEVAL_REF_NR, "049973000011"); //TODO: Remove this 
		msg.putField(Bit._056_MSG_REASON_CODE, "1510");
		msg.putField(Bit._059_ECHO_DATA, "0222346450");
		
		msg.putField(Bit._102_ACCOUNT_ID_1, stdApiReq.getFromAccount());
		msg.putField(Bit._103_ACCOUNT_ID_2, stdApiReq.getToAccount() );

//		msg.putPrivField(PrivBit._014_SPONSOR_BANK, Pack.resize(stdApiReq.getSponsorBank(), 8, ' ', true) );
//		msg.putPrivField(PrivBit._024_PAYEE_REFERENCE, stdApiReq.getPayeeReference()); 
		msg.putPrivField(PrivBit._002_SWITCH_KEY, dateTime.get("MMddhhmmss"));		
//		msg.putPrivField(PrivBit._003_ROUTING_INFO, "StewardVasSrStewardPbSnk000011000011            ");    //TODO: Remove this         
		msg.putPrivField(PrivBit._009_ADDITIONAL_NODE_DATA, "");
				
		msg.putPrivField(PrivBit._020_AUTHORIZER_DATE_SETTLEMENT, dateTime.get("YYYYMMdd"));
		
		StructuredData sd = new StructuredData();
		
		sd.put("MSISDN", stdApiReq.getMSISDN());
//		sd.put("BatchId", "49973");
		
		msg.putStructuredData(sd);  
		
		logger.info("MSISDN: " + sd.get("MSISDN") );
		
		logger.info("Request ISO8583 message\n" + msg.toString());

		return new2byteHeaderPacket(msg.toMsg()); //TODO: Encapsulate
	}

	public byte[] constructReversalReqMsgToPostilion(Iso8583Post msg_req) { //TODO: Encapsulate
		
		Iso8583Post msg = new Iso8583Post();
		DateTime dateTime = new DateTime();

		try {
			String msgType = null;
			
			if ( msg_req.getMsgType() == MsgType._0421_ACQUIRER_REV_ADV_REP || msg_req.getMsgType() == MsgType._0420_ACQUIRER_REV_ADV) 
				msgType = "0421";
			else if ( msg_req.getMsgType() == MsgType._0200_TRAN_REQ)
				msgType = "0420";
			else
				msgType = "0210"; // Error Shldnt happen
			
			msg.setMessageType(msgType);
			msg.putProcessingCode(msg_req.getProcessingCode());

			msg.copyFieldFrom(Bit._004_AMOUNT_TRANSACTION, msg_req);

			msg.copyFieldFrom(Bit._011_SYSTEMS_TRACE_AUDIT_NR, msg_req); 
			msg.copyFieldFrom(Bit._012_TIME_LOCAL, msg_req);
			msg.copyFieldFrom(Bit._013_DATE_LOCAL, msg_req);
			
			msg.copyFieldFrom(Bit._041_CARD_ACCEPTOR_TERM_ID, msg_req);
			msg.copyFieldFrom(Bit._042_CARD_ACCEPTOR_ID_CODE, msg_req);
			msg.copyFieldFrom(Bit._043_CARD_ACCEPTOR_NAME_LOC, msg_req);
			
			
			int ogMsgType = msg_req.getMsgType(); 
			String ogStan = msg_req.getField(Bit._011_SYSTEMS_TRACE_AUDIT_NR);
			String ogTransDateTime =  msg_req.getField(Bit._007_TRANSMISSION_DATE_TIME);
			String ogAcquirerId = msg_req.getField(Bit._032_ACQUIRING_INST_ID_CODE);
			String ogForwardingId =  msg_req.getField(Bit._033_FORWARDING_INST_ID_CODE);
			
			OriginalDataElements originalDataElements = new OriginalDataElements(ogMsgType, ogStan, ogTransDateTime,
					ogAcquirerId, ogForwardingId);
 
			msg.putOriginalDataElements(originalDataElements);
			
			msg.putPrivField(PrivBit._009_ADDITIONAL_NODE_DATA , msg_req.getPrivField(PrivBit._002_SWITCH_KEY) );
			
			msg.putPrivField(PrivBit._011_ORIGINAL_KEY, msg_req.getPrivField(PrivBit._002_SWITCH_KEY) );
			
			msg.putPrivField(PrivBit._002_SWITCH_KEY , dateTime.get("MMddhhmmss"));
						
			logger.info("Reversal ISO8583 message\n" + msg.toString());
			
			return new2byteHeaderPacket(msg.toMsg());
			
		} catch (XPostilion e) {
		
			e.printStackTrace();
		}
		
		logger.error("Very Unexpected Error. Failed to create reversal object");
		
		byte[] shouldNotHappen = new byte[0];
		
		return shouldNotHappen;
		
	}
	
	private void putDefaultFields(Iso8583Post msg) throws XPostilion {

		DateTime date = new DateTime();
		String cardAccptor = "SBZ MOBILE ECONET      Harare       04ZW";

		//		msg.setMessageType(postilion.realtime.sdk.message.bitmap.Iso8583.MsgType._0200_TRAN_REQ);
		//		msg.putField(2, "9999888888888882");		
		msg.putField(Bit._007_TRANSMISSION_DATE_TIME, date.get("MMddhhmmss"));
		msg.putField(Bit._011_SYSTEMS_TRACE_AUDIT_NR, "000002"); //TODO: Remove this
		msg.putField(Bit._012_TIME_LOCAL, date.get("HHmmss") );

		Date dt = new Date();
		SimpleDateFormat fmt = new SimpleDateFormat("MMdd");		
		msg.putField(Bit._013_DATE_LOCAL, fmt.format(dt));

		msg.putField(Bit._022_POS_ENTRY_MODE, "000");
		msg.putField(Bit._025_POS_CONDITION_CODE, "00");		
		msg.putField(Bit._041_CARD_ACCEPTOR_TERM_ID, Pack.resize("ZSS26377", 8, ' ', true) );
		msg.putField(Bit._042_CARD_ACCEPTOR_ID_CODE, Pack.resize("26377-SBZMOBILE", 15, ' ', true) );
		msg.putField(Bit._043_CARD_ACCEPTOR_NAME_LOC, Pack.resize(cardAccptor, 40, ' ', true));
		msg.putField(Bit._049_CURRENCY_CODE_TRAN, "840");		
		msg.putField(Bit._100_RECEIVING_INST_ID_CODE, "502195" );
		msg.putField(Bit._123_POS_DATA_CODE, "100450100130021" );

	}	

	private SignedAmount processCalcCharge(String charge, int msgtype) throws XFieldUnableToConstruct {

		charge =String.valueOf(Math.round(Float.parseFloat(charge)*100));
		charge =Pack.resize(charge, 8, '0', false);
		SignedAmount samt;
		if (isReversalMsgType(msgtype))
			samt = new SignedAmount("C",charge);
		else
			samt = new SignedAmount("D",charge);

		return samt;
	}

	private boolean isReversalMsgType(int msgType) {

		if (msgType==MsgType._0420_ACQUIRER_REV_ADV
				|| msgType==MsgType._0400_ACQUIRER_REV_REQ
				|| msgType==MsgType._0421_ACQUIRER_REV_ADV_REP
				|| msgType==MsgType._0401_ACQUIRER_REV_REQ_REP)
			return true;
		return false;
	}

	static byte data[] = { 0x30, 0x32, 0x30, 0x30, (byte) 0xF2, 0x36, 0x04,
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
			0x00, 0x00, 0x31, 0x39, 0x36, 0x30, 0x31, 0x32, 0x33, 0x37, 0x36,
			0x31, 0x31, 0x30, 0x30, 0x30, 0x30, 0x31, 0x35, 0x38, 0x31, 0x34,
			0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
			0x33, 0x38, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x34, 0x32,
			0x37, 0x31, 0x33, 0x30, 0x38, 0x35, 0x39, 0x30, 0x30, 0x32, 0x32,
			0x34, 0x37, 0x30, 0x35, 0x30, 0x34, 0x32, 0x37, 0x31, 0x35, 0x31,
			0x30, 0x35, 0x30, 0x31, 0x34, 0x31, 0x31, 0x30 };

}
