/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.isorest.domain;

/**
 *
 * @author Artwell Mamvura
 */
public class StewardBankApiResponse {

    private static final long serialVersionUID = -0x32EA84E37FFB508EL;

    private String statusCode;
    private String message;    
    private StewardResponseBody responseBody;

    public StewardBankApiResponse() {
        statusCode = "SUCCESS";
        message = "SUCCESS";
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

	public StewardResponseBody getResponseBody() {
		return responseBody;
	}

	public void setResponseBody(StewardResponseBody responseBody) {
		this.responseBody = responseBody;
	}

    
    
}
