package com.ujigu.secure.upfile.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.upfile.service.ProjUploadService;

public class ThreadExecutor {

	private final static Logger LOG = LoggerFactory.getLogger(ThreadExecutor.class);
	private static ExecutorService executorService;
	
	//如果有gif的帧数超过了指定的限制，则往该线程变量中增加一个标识，以避免到处调用命令identify来查看图片帧数，提高处理效率
	private final static ThreadLocal<Boolean> isTooMuchFrames = new ThreadLocal<Boolean>();

	static {
		init();
	}
	
	private static void init(){
		int threadSize = 8;

		String sizeStr = BaseConfig.getValue("exec.thread.pool.size", "8");
		if (StringUtils.isNotBlank(sizeStr)) {
			threadSize = Integer.valueOf(sizeStr);
		}

		executorService = Executors.newFixedThreadPool(threadSize);
	}
	
	public static void addMuchFramesFlag(){
		isTooMuchFrames.set(true);
	}
	
	public static boolean isMuchFrames(){
		return isTooMuchFrames.get() == null ? false : isTooMuchFrames.get();
	}
	
	public static void removeMuchFramesFlag(){
		isTooMuchFrames.remove(); 
	}

	/**
	 * 
	 * @param upServ
	 * @param localFilePath
	 * @param projectName
	 */
	public static void exec(ProjUploadService upServ, String localFilePath,
			String projectName,HttpServletRequest request) {
		LOG.debug(LOG.isDebugEnabled() ? "handle other size for '" + localFilePath + "'" : null);
		if(executorService == null){
			synchronized (executorService) {
				if(executorService == null){
					init();
				}
			}
		}

		executorService.execute(new ExecThread(upServ, localFilePath, projectName,request));
		
	}
	
	static class ExecThread implements Runnable {
		private ProjUploadService upServ;
		private String localFilePath;
		private String projectName;
		private HttpServletRequest request;
		
		private boolean isMuchFrames;
		
		public ExecThread(ProjUploadService upServ, String localFilePath,
				String projectName,HttpServletRequest request){
			this.upServ = upServ;
			this.localFilePath = localFilePath;
			this.projectName = projectName;
			this.request = request;
			
			isMuchFrames = isMuchFrames();
		}

		@Override
		public void run() {
			if(upServ != null){
				if(isMuchFrames){
					upServ.handleReturnImg(localFilePath, projectName, request);
				}
				
				upServ.handleOtherSizes(localFilePath, projectName);
			}
		}
		
	}

}
