package com.xyz.tools.cache.exception;

import com.xyz.tools.common.exception.BaseRuntimeException;

public class JedisClientException extends BaseRuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2841616768865276154L;

	public JedisClientException(String code, String logMsg, Throwable cause) {
		super(code, logMsg, cause);
	}

    public JedisClientException(String code, String logMsg) {
        super(code, logMsg);
    }

}
