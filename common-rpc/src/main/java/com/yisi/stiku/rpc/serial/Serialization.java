package com.yisi.stiku.rpc.serial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 
 * @author shangfeng
 *
 */
public interface Serialization {
	
	/**
	 * 
	 * @param out
	 * @param message
	 * @throws IOException
	 */
	void serialize(OutputStream out, Object message) throws IOException;
	
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	Object deserialize(InputStream in) throws IOException;
}
