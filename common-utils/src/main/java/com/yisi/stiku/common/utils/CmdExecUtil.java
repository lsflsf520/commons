package com.yisi.stiku.common.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.exec.ExecuteResultHandler;
import org.apache.commons.exec.PumpStreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yisi.stiku.common.exception.BaseRuntimeException;

/**
 * 
 * @author lsf
 *
 */
public class CmdExecUtil {

	private final static Logger LOG = LoggerFactory.getLogger(CmdExecUtil.class);

	public static String execCmdForStdout(String cmdLineStr) {

		return execCmdForStdout(cmdLineStr, null);
	}

	/**
	 * 
	 * @param cmdLineStr
	 * @return
	 */
	public static String execCmdForStdout(String cmdLineStr, ExecuteResultHandler resultHandler)
	{

		LOG.debug("exec cmd '" + cmdLineStr + "'");
		String out = null;
		CommandLine cmdLine = CommandLine.parse(cmdLineStr);
		DefaultExecutor executor = new DefaultExecutor();
		ByteArrayOutputStream outputStream = null;
		ByteArrayOutputStream errorStream = null;
		try {
			outputStream = new ByteArrayOutputStream();
			errorStream = new ByteArrayOutputStream();
			PumpStreamHandler streamHandler = new PumpStreamHandler(
					outputStream, errorStream);

			executor.setStreamHandler(streamHandler);
			if (resultHandler != null) {
				executor.execute(cmdLine, resultHandler);
			} else {
				executor.execute(cmdLine);
			}

			out = outputStream.toString("UTF-8");

		} catch (ExecuteException e) {
			// throw new RuntimeException("exec cmd '" + cmdLineStr + "' error",
			// e);
			String errInfo = "";
			try {
				if (errorStream != null) {
					errInfo = errorStream.toString("UTF-8");
				}
				if (outputStream != null) {
					out = outputStream.toString("UTF-8");
				}
			} catch (Exception ue) {
				LOG.error(ue.getMessage(), ue);
			}
			throw new BaseRuntimeException("EXEC_CMD_ERROR", "命令(" + cmdLineStr + ")执行出错了", errInfo, out, e);
		} catch (IOException e) {
			throw new RuntimeException("exec cmd '" + cmdLineStr + "' error", e);
		} finally {
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					LOG.error("close stdout stream for cmdLine '"
							+ cmdLineStr + "' error", e);
				}
			}
			if (errorStream != null) {
				try {
					errorStream.close();
				} catch (IOException e) {
					LOG.error("close error stream for cmdLine '"
							+ cmdLineStr + "' error", e);
				}
			}
		}

		return out;
	}

	public static void execCmd(String cmdLineStr) {

		execCmd(cmdLineStr, null);
	}

	/**
	 * 
	 * @param cmdLineStr
	 */
	public static void execCmd(String cmdLineStr, ExecuteResultHandler resultHandler) {

		LOG.debug("exec cmd '" + cmdLineStr + "'");
		CommandLine cmdLine = CommandLine.parse(cmdLineStr);
		DefaultExecutor executor = new DefaultExecutor();
		try {
			if (resultHandler != null) {
				executor.execute(cmdLine, resultHandler);
			} else {
				executor.execute(cmdLine);
			}
		} catch (ExecuteException e) {
			throw new RuntimeException("exec cmd '" + cmdLineStr + "' error", e);
		} catch (IOException e) {
			throw new RuntimeException("exec cmd '" + cmdLineStr + "' error", e);
		}
	}

}
