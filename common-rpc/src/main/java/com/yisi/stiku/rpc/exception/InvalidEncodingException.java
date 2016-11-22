package com.yisi.stiku.rpc.exception;



public class InvalidEncodingException extends SystemException {

	private static final long serialVersionUID = -55738658639905065L;
	private static final String MESSAGE = "Encoding is invalid, the from encoding is: %s, to encoding is: %s.";
	
	public InvalidEncodingException(final String fromEncoding, final String toEncoding) {
		super(MESSAGE, fromEncoding, toEncoding);
	}
}
