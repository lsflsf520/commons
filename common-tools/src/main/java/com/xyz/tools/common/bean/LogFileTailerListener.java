package com.xyz.tools.common.bean;

/**
 * @author shangfeng
 *
 */
public abstract interface LogFileTailerListener {

	public abstract void newLine(String line);
}
