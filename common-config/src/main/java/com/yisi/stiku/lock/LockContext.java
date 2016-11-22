package com.yisi.stiku.lock;

import org.apache.commons.lang.StringUtils;

import com.yisi.stiku.common.exception.BaseRuntimeException;

/**
 * @author shangfeng
 *
 */
public class LockContext {

	private final String lockId;
	private final String host;
	private final String projectName;

	private final String threadId;

	// private String lockNodeName; // zk上锁节点的名称
	private boolean lockSuccess;
	private String nsNode; // 锁的命名空间，即在zk上命名空间节点的路径
	private String lockNodeName; // 锁的名称，锁节点全路径除去nsNode部分后的字符串

	public LockContext(String lockId, String host, String projectName) {

		this.lockId = lockId;
		this.host = host;
		this.projectName = projectName;
		this.threadId = StringUtils.isNotBlank(Thread.currentThread().getName()) ? Thread.currentThread().getName() + "-"
				+ Thread.currentThread().getId() : Thread.currentThread().getId() + "";
	}

	public String getLockId() {

		return lockId;
	}

	public String getHost() {

		return host;
	}

	public String getProjectName() {

		return projectName;
	}

	public String getThreadId() {

		return threadId;
	}

	/**
	 * 将获取锁成功的字段标记为true
	 */
	public void tagLockSuccess() {

		if (this.lockSuccess) {
			throw new BaseRuntimeException("REPEATED_CALL", "method can be called only once.");
		}
		this.lockSuccess = true;
	}

	public String getLockNodeName() {

		return lockNodeName;
	}

	public boolean isLockSuccess() {

		return lockSuccess;
	}

	/**
	 * 
	 * @param lockNodeName
	 *            设置锁节点的名称，该方法只有第一次被调用时有效
	 */
	public void setLockNodeName(String lockNodeName) {

		if (StringUtils.isNotBlank(this.lockNodeName)) {
			throw new BaseRuntimeException("REPEATED_CALL", "method can be called only once.");
		}
		this.lockNodeName = lockNodeName;
	}

	public String getNsNode() {

		return nsNode;
	}

	public void setNsNode(String nsNode) {

		if (StringUtils.isNotBlank(this.nsNode)) {
			throw new BaseRuntimeException("REPEATED_CALL", "method can be called only once.");
		}
		this.nsNode = nsNode;
	}

	@Override
	public String toString() {

		return "lockId:" + lockId + ",host:" + host + ",projectName:" + projectName + ",threadId:" + threadId;
	}

}
