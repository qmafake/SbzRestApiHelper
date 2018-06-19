package com.isorest.helper;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.Logger;

import com.isorest.domain.IsoRestRequest;
import com.sp.exceptions.RetryUnsuccessfulException;

import postilion.realtime.sdk.message.bitmap.Iso8583Post;
import postilion.realtime.sdk.util.XPostilion;

public class PostilionConnector implements Runnable {

	private static Logger logger = Logger.getLogger(PostilionConnector.class);	

	public void run() { } //Not Used: This already runs of a Tomcat thread	

	public byte[] processTranReqFromClient(IsoRestRequest isoRestRequest) {

		MessageConvetor mc = new MessageConvetor();
		byte[] _msg_to_transmit = null;

		try {
			_msg_to_transmit = mc.constructReqMsgToPostilion(isoRestRequest);
		} catch (XPostilion e) {

			logger.info("Problem occured creating 0200 msg.");
			e.printStackTrace();
		}

		return _msg_to_transmit;
	}

	public byte[] prepareTranReversalReqFromAPI(Iso8583Post msg_req) {

		MessageConvetor mc = new MessageConvetor();
		byte[] _msg_to_transmit = null;

		_msg_to_transmit = mc.constructReversalReqMsgToPostilion(msg_req);

		return _msg_to_transmit;
	}

	/**
	 * This method - receives a byte array msg object to transmit to Postilion.
	 * @param _msg_to_transmit	 
	 * @returns An Iso858Post msg response
	 */
	public Iso8583Post processTranReqToPostilion(byte[] _msg_to_transmit) {

		SocketConn sc = new SocketConn();
		Socket socket = sc.getConnection();			
		OutputStream out2 = null;
		InputStream is = null;
		Iso8583Post msg_rsp = new Iso8583Post();
		long startTime = System.currentTimeMillis();
		long rspTime = 0L;
		try
		{
			/**---------------------------------------------------------------------- Client Request */																													//showMsgDetail(_msg_to_transmit); System.out.print("\n");			 
			out2 = socket.getOutputStream();			
			out2.write(_msg_to_transmit);		//write msg to Postilion Postbridge port.	

			/**---------------------------------------------------------------------- Server Response */		
			is = socket.getInputStream();		

			byte[] resultBuff = new byte[0];
			byte[] buff = new byte[Api2PostilionConfig.expectedRespLen];
			int k = -1; 

			socket.setSoTimeout(1000 * Api2PostilionConfig.readTimeOut);	/* 1000*5 => 5 seconds read timeout. If read operation has
																		       blocked for >5 seconds a SocketTimeOutException occurs */	
			while((k = is.read(buff, 0, buff.length)) > -1) {				//Read server response						
				byte[] tbuff = new byte[resultBuff.length + k]; 			//temp buffer size = bytes already read + bytes last read
				System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); //copy previous bytes
				System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  	//copy current lot
				resultBuff = tbuff; 										//call the temp buffer as your result buff

				if (is.available() == 0) break;
			}

			rspTime = System.currentTimeMillis() - startTime;

			if (resultBuff.length > 2)								//just doubly make sure that there is a result
			{				
				byte[] tbuff = new byte[resultBuff.length-2]; 		/*I will save the response without the 1st 2 bytes 
																	  which are the msg header (body length) */
				System.arraycopy(resultBuff, 2, tbuff, 0, resultBuff.length-2);
				//					showMsgDetail(tbuff);										
				msg_rsp.fromMsg(tbuff, 0);

				logger.info("Postilion Response\n" + msg_rsp.toString());
			}	
			//TODO: Logic is getting here Throw ReadTime out if buffer is empty or throw custom ResponseIsEmpty() exception
			
			out2.flush();  out2.close();  is.close();
		} 
		catch (SocketTimeoutException e1) {
			logger.error( "A timeout occured - intiate 0420 msg ...");
			logger.error( e1.getMessage() );			

			Iso8583Post msg_req = cleanMsgForReversal(_msg_to_transmit); //TODO: Encapsulate

			byte[] msg_rvsal_b = prepareTranReversalReqFromAPI(msg_req); 
			msg_rsp = processTranReversalReqToPostilion(msg_rvsal_b, sc);		
		}		
		catch(SocketException e){
			logger.error(this.getClass().getSimpleName()
					+ " Network error occurred socket error in Postilion connection:\n"
					+ e.getMessage()
					+e.getCause() 
					+e.getStackTrace().toString());			
		}

		catch (Exception e) {
			logger.error("Error occurred in msg transmission to/from Postilion:\n"
					+ e.getMessage()
					+e.getCause() 
					+e.getStackTrace());
//			e.printStackTrace();
		}		
		finally{
			sc.closeSocket(socket);
			logger.info("Transmission ended. RESPONSE TIME(ms):"   + (rspTime!=0L ? rspTime : "N/A") );
		}

		return msg_rsp;
	}

	/**
	 * This msg 
	 * @param msg_prior_b - The previous message that was just transmitted
	 * @return - Iso8583Post reversal msg
	 */
	private Iso8583Post cleanMsgForReversal(byte[] msg_prior_b) { //TODO: Encapsulate

		byte[] msg_to_reverse = new byte[msg_prior_b.length -2];  
		System.arraycopy(msg_prior_b, 2, msg_to_reverse, 0, msg_to_reverse.length);

		Iso8583Post msg_rvsal = new Iso8583Post();

		try {
			msg_rvsal.fromMsg(msg_to_reverse, 0);
		} catch (XPostilion e) {

			e.printStackTrace();
		}

		//		logger.error( "Check original msg: " + msg_rvsal.toString() );
		return msg_rvsal;
	}


	private Iso8583Post processTranReversalReqToPostilion(byte[] msg_rvsal_b, SocketConn sc) {

		Iso8583Post msg_rsp = new Iso8583Post();
		boolean isSuccessfulReversal = false;

		RetryOnExceptionStrategy retry = new RetryOnExceptionStrategy();
		while (retry.shouldRetry() && !isSuccessfulReversal ) {

			logger.info("Sending Request Reversal request...");

			Socket socket = sc.getConnection();			
			OutputStream out2 = null;
			InputStream is = null;

			long startTime = System.currentTimeMillis();
			long rspTime = 0L;
			try
			{
				/**---------------------------------------------------------------------- Client Request */																													//showMsgDetail(_msg_to_transmit); System.out.print("\n");			 
				out2 = socket.getOutputStream();			
				out2.write(msg_rvsal_b);		//write msg to Postilion Postbridge port.	

				/**---------------------------------------------------------------------- Server Response */		
				is = socket.getInputStream();		

				byte[] resultBuff = new byte[0];
				byte[] buff = new byte[Api2PostilionConfig.expectedRespLen];
				int k = -1; 
				//TODO: Remove below or Encapsulate
				socket.setSoTimeout(1000 * Api2PostilionConfig.readTimeOut);	/* 1000*5 => 5 seconds read timeout. If read operation has
																			       blocked for >5 seconds a SocketTimeOutException occurs */	
				while((k = is.read(buff, 0, buff.length)) > -1) {				//Read server response						
					byte[] tbuff = new byte[resultBuff.length + k]; 			//temp buffer size = bytes already read + bytes last read
					System.arraycopy(resultBuff, 0, tbuff, 0, resultBuff.length); //copy previous bytes
					System.arraycopy(buff, 0, tbuff, resultBuff.length, k);  	//copy current lot
					resultBuff = tbuff; 										//call the temp buffer as your result buff

					if (is.available() == 0) break;
				}

				rspTime = System.currentTimeMillis() - startTime;

				if (resultBuff.length > 2)								//just doubly make sure that there is a result
				{				
					byte[] tbuff = new byte[resultBuff.length-2]; 		

					System.arraycopy(resultBuff, 2, tbuff, 0, resultBuff.length-2);

					msg_rsp.fromMsg(tbuff, 0);

					logger.info("Postilion Response\n" + msg_rsp.toString());
				}			

				try {
					if ( msg_rsp.getResponseCode() != null ) {
						if( msgRespHasReversalResp(msg_rsp) ){

							logger.info("Valid reversal response received ...");
							isSuccessfulReversal  = true;
						}						
					}
				} catch (XPostilion e) {

					e.printStackTrace();
				}

				out2.flush();  out2.close();  is.close();
			} 
			catch (SocketTimeoutException e1) {
				logger.error( "A timeout re-occured - Initiate a 0421 msg ...");
				logger.error( e1.getMessage() );			

				try {
					retry.errorOccured();
				} catch (Exception e2) {

					e2.printStackTrace();
				}

				Iso8583Post msg_req = cleanMsgForReversal(msg_rvsal_b); //TODO: Encapsulate

				byte[] msg_repeat_revsal = prepareTranReversalReqFromAPI(msg_req);

				msg_rsp = processTranReversalReqToPostilion(msg_repeat_revsal, sc);			

			}
			catch (Exception e) {
				logger.error(this.getClass().getSimpleName()
						+ " Error occurred in msg transmission to/from Postilion:\n"
						+ e.getMessage()
						+e.getCause() 
						+e.getStackTrace());
				e.printStackTrace();
			}		
			finally{
				sc.closeSocket(socket);
				logger.info("Reversal Transmission ended. RESPONSE TIME(ms):"   + (rspTime!=0L ? rspTime : "N/A") );
			}
		}
		return msg_rsp;
	}

	private boolean msgRespHasReversalResp(Iso8583Post msg_rsp) {

		try {
			String rc = msg_rsp.getResponseCode();
			if (rc.equals("00") || rc.equals("25") || rc.equals("94") || rc.equals("96")){
				return true;
			}
		} catch (XPostilion e) {

			e.printStackTrace();
		}
		return false;
	}


	static class RetryOnExceptionStrategy {
		public static final int DEFAULT_RETRIES = Api2PostilionConfig.max_retry_count; // 2;
		public static final long DEFAULT_WAIT_TIME_IN_SEC = Api2PostilionConfig.retry_wait_time; //  5000;

		private int numberOfRetries;
		private int numberOfTriesLeft;
		private long timeToWait;

		public RetryOnExceptionStrategy() {
			this(DEFAULT_RETRIES, DEFAULT_WAIT_TIME_IN_SEC);
		}

		public RetryOnExceptionStrategy(int numberOfRetries, long timeToWait) {
			this.numberOfRetries = numberOfRetries;
			numberOfTriesLeft = numberOfRetries;
			this.timeToWait = timeToWait * 1000;
			//			logger.info("Going to retry : " + numberOfRetries + " time(s)");
		}

		/**
		 * @return true if there are tries left
		 */
		public boolean shouldRetry() {
			return numberOfTriesLeft > 0;
		}

		public void errorOccured() throws Exception {

			if (numberOfTriesLeft < DEFAULT_RETRIES){
				logger.info("Number Of Tries Left = " + numberOfTriesLeft);
				logger.info("Going to wait : " + timeToWait/1000 + " seconds");
				waitUntilNextTry();	
			}

			numberOfTriesLeft--;
			if (!shouldRetry()) {
				throw new RetryUnsuccessfulException("Retry Failed: Total " + numberOfRetries + " attempts made at interval "
						+ getTimeToWait() + "ms");
			}
		}

		public long getTimeToWait() {
			return timeToWait;
		}

		private void waitUntilNextTry() {
			try {
				Thread.sleep(getTimeToWait());
			} catch (InterruptedException ignored) {
			}
		}
	}



}