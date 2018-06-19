package com.isorest.helper;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

public class SocketConn {

	private static Socket socket;
	private static Logger logger = Logger.getLogger(SocketConn.class);

	public static void main(String[] args) {
		
		Api2PostilionConfig.init();
		
		SocketConn sc = new SocketConn();
//		sc.getProperties();
		sc.createConnection();
	}

	public Socket getConnection() {

//		getProperties();

		if (socket == null){
			logger.info("Connecting to Postilion ...");
			createConnection();			
		}
		else{
			logger.info("Connection exists - " + socket.getInetAddress() + ":" + socket.getPort());
			/**issue: when this works an existing connection is reported but sending a trx yields 
			 * 'connection closed'. For now lets kill it and reconnect. Try again with direct cnxn to PBridge
			 */
			try {
				socket.close();
				createConnection();
			} catch (IOException e) {

				e.printStackTrace();
			}			

		}
		return socket;
	}

	public  void createConnection() {
		try {
			InetAddress server = InetAddress.getByName( Api2PostilionConfig.serverIpAddress );
						
			logger.info("Connecting to server address: "
					+ server.getHostAddress() + " server port: " + Api2PostilionConfig.serverPort );

			socket = new Socket(server.getHostAddress(), Api2PostilionConfig.serverPort );

			logger.info("----CONNECTED----");

		} catch (UnknownHostException e) {
			logger.error("FAILED to connect to Postilion on " + Api2PostilionConfig.serverIpAddress );
			e.printStackTrace();
		} catch (java.net.ConnectException e){
			logger.error("FAILED to connect to Postilion on " + Api2PostilionConfig.serverIpAddress + " - " + e.getMessage() );			
			
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	public void closeSocket(Socket socket) {
		try {
			SocketConn.socket.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

//	protected void getProperties() {
//
//		serverIpAddress = Api2PostilionConfig.serverIpAddress ; //prop.getProperty("realtimeServer");
//		serverPort = Api2PostilionConfig.serverPort ; //Integer.parseInt( prop.getProperty("realtimeServerPort") );			 
//	}
}
