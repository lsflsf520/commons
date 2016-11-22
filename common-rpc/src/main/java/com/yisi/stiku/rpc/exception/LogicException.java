package com.yisi.stiku.rpc.exception;

public abstract class LogicException extends Exception {
	
	private static final long serialVersionUID = 5975218893665147705L;
	
	protected LogicException() {
		super();
	}
	
	protected LogicException(final String message) {
		super(message);
	}
}
