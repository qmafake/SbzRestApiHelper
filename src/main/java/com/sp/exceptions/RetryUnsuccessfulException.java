package com.sp.exceptions;

public class RetryUnsuccessfulException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RetryUnsuccessfulException(String message) {
        super(message);
    }

}