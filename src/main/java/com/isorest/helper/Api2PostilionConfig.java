package com.isorest.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * 
 * @author Quincy M.
 * This class reads the configurations from the .ini file
 */
public class Api2PostilionConfig {

	private static Logger logger = Logger.getLogger(Api2PostilionConfig.class);

	public static String serverIpAddress; 
	public static String serverName;
	public static int serverPort;
	public static int readTimeOut;
	public static int expectedRespLen;

	public static int max_retry_count;

	public static long retry_wait_time; 

	
	public static void main( String[] args ) {		
		init();		
	}
	
	public static void init() {

		Properties prop = new Properties();
		InputStream input = null;

		try {	 

			input = getEnvSettings();
			// load a properties file
			prop.load(input);		
			
			serverIpAddress = prop.getProperty("realtimeServer");
			serverPort =  Integer.parseInt( prop.getProperty("realtimeServerPort") );	
			readTimeOut = Integer.parseInt( prop.getProperty("readTimeOut") );
			expectedRespLen = Integer.parseInt( prop.getProperty("expectedRespLen") );
			
			max_retry_count = Integer.parseInt( prop.getProperty("max_retry_count") );
			retry_wait_time = Long.parseLong( prop.getProperty("retry_wait_time") );

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static InputStream getEnvSettings() throws FileNotFoundException {

		if ( getEnviromentType().equals("WINDOWS")){
			String path = "C:/StewardBank/config/rest_postilion.ini";
			logger.info("Windows System: Reading configuration settings: " + path);
			return new FileInputStream(path);		
		}
		else{
			logger.info("None Windows System");
			
//			String path = "/Users/artwelm/RestPostilionAPI/config/rest_postilion.ini";
			String path = System.getProperty("user.home")+ File.separator + "RestPostilionAPI" +  File.separator + "config" +  File.separator  + "rest_postilion.ini";
			
			logger.info("Linux System: Reading configuration settings: " + path);
			return new FileInputStream(path);
		}			
	}

	public static String getEnviromentType()
	{
		Map<String, String> env = System.getenv();
		//		Map<String, String> envLinux = System.getenv();

		if (env != null){
			if (env.containsKey("COMPUTERNAME") && env.containsKey("USERNAME") ) {
				return "WINDOWS";
			}
		}
		return "LINUX";
	}
}
