package com.yisi.stiku.rpc.exception;

public final class ServerStopException extends SystemException {
	
	private static final long serialVersionUID = -878241930137362120L;
	
	private static final String MESSAGE = "Can't stop this server, because the server didn't start yet.";
	
	public ServerStopException() {
		super(MESSAGE);
	}
}
