package com.yisi.stiku.lock;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.yisi.stiku.common.exception.BaseRuntimeException;
import com.yisi.stiku.common.utils.IPUtil;
import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.conf.ZkConstant;

/**
 * @author shangfeng
 *
 */
public class DistribLock {

	private final static Logger LOG = LoggerFactory.getLogger(DistribLock.class);
	private final static String LOCK_BASE_PATH = ZkConstant.ZK_ROOT_NODE + "/distrib_lock/";

	/**
	 * 竞争该锁的多个线程，只有获取到该锁的那一个线程有机会执行，其它线程将直接退出
	 * 
	 * @param namespace
	 *            命名空间，所有的锁节点将创建在该节点下。如果不填，命名空间默认为 defaultns
	 * @param lockId
	 *            锁资源的id。用来给多个线程竞争的资源id，在同一个命名空间下，锁资源id必须唯一
	 * @param execService
	 *            获取到锁之后，需要执行的业务逻辑对象，@LockExecService
	 */
	public static void lock4Onetime(String namespace, Serializable lockId, LockExecService execService) {

		if (execService == null) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "execService cannot be null");
		}

		LockContext context = null;
		try {
			context = lock4Onetime(namespace, lockId);
			if (context.isLockSuccess()) {
				execService.execute(context);
			}
		} catch (Throwable th) {
			LOG.error(context == null ? "namespace:" + namespace + ",lockId:" + lockId : context.toString(), th);
		} finally {
			unlock4Onetime(context);
		}
	}

	/**
	 * 竞争该锁的多个线程，只有获取到该锁的那一个线程有机会执行，其它线程将直接退出
	 * 
	 * @param lockId
	 *            锁资源的id。用来给多个线程竞争的资源id，在默認的命名空间下，锁资源id必须唯一
	 * @param execService
	 *            获取到锁之后，需要执行的业务逻辑对象，@LockExecService
	 */
	public static void lock4Onetime(Serializable lockId, LockExecService execService) {

		lock4Onetime(null, lockId, execService);
	}

	public static void unlock4Onetime(LockContext context) {

		if (context != null && StringUtils.isNotBlank(context.getNsNode())
				&& StringUtils.isNotBlank(context.getLockNodeName())) {
			if (context.isLockSuccess()) {
				List<String> childs = ConfigOnZk.getChildren(context.getNsNode());
				if (childs != null && !childs.isEmpty()) {
					Collections.sort(childs, new Comparator<String>() {

						@Override
						public int compare(String str1, String str2) {

							return StringUtils.isNotBlank(str2) ? str2.compareTo(str1) : 1;
						}
					});

					for (String child : childs) {
						ConfigOnZk.delNode(context.getNsNode() + "/" + child);
					}
				}
			} else {
				ConfigOnZk.delNode(context.getNsNode() + "/" + context.getLockNodeName());
			}
		}
	}

	/**
	 * 調用該方法之後，可調用 @see LockContext.isLockSuccess() 方法來判斷是否獲取鎖成功
	 * 在處理完業務邏輯之後，一定要調用@see unlock4Onetime(LockContext context)
	 * 方法釋放鎖資源，否則可能導致後續服務無法正常獲取到鎖
	 * 
	 * @param namespace
	 *            命名空间，所有的锁节点将创建在该节点下。如果不填，命名空间默认为 defaultns
	 * @param lockId
	 *            锁资源的id。用来给多个线程竞争的资源id，在同一个命名空间下，锁资源id必须唯一
	 * @return 返回一個鎖的上下文對象，可調用 @see LockContext.isLockSuccess() 方法來判斷是否獲取鎖成功
	 */
	public static LockContext lock4Onetime(String namespace, Serializable lockId) {

		if (lockId == null) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "lockId cannot be null");
		}

		if (StringUtils.isBlank(namespace)) {
			namespace = "defaultns";
		}
		namespace = "onetime/" + namespace;
		LockContext context = new LockContext(lockId.toString(), IPUtil.getLocalIp(), ZkConstant.ALIAS_PROJECT_NAME);
		String nsNode = LOCK_BASE_PATH + namespace;
		context.setNsNode(nsNode);

		boolean locked = hasLock(context, nsNode);
		if (locked) {
			context.tagLockSuccess();
		}

		return context;
	}

	/**
	 * 調用該方法之後，可調用 @see LockContext.isLockSuccess() 方法來判斷是否獲取鎖成功
	 * 在處理完業務邏輯之後，一定要調用@see unlock4Onetime(LockContext context)
	 * 方法釋放鎖資源，否則可能導致後續服務無法正常獲取到鎖
	 * 
	 * @param lockId
	 *            锁资源的id。用来给多个线程竞争的资源id，在默認的命名空间下，锁资源id必须唯一
	 * @return 返回一個鎖的上下文對象，可調用 @see LockContext.isLockSuccess() 方法來判斷是否獲取鎖成功
	 */
	public static LockContext lock4Onetime(Serializable lockId) {

		return lock4Onetime(null, lockId);
	}

	private static boolean hasLock(LockContext context, final String nsNode) {

		String contextStr = new Gson().toJson(context);
		String seqNodePath = ConfigOnZk.createEphemeralSequential(nsNode + "/" + context.getLockId(), contextStr);
		String seqNodeName = null;
		if (StringUtils.isBlank(seqNodePath) || (seqNodeName = seqNodePath.replace(nsNode + "/", "")) == null) {
			throw new BaseRuntimeException("LOCK_FAIL", "create lock node on zk failure.");
		}
		context.setLockNodeName(seqNodeName);
		List<String> childList = ConfigOnZk.getChildren(nsNode);
		Collections.sort(childList);
		String minNodeName = childList.get(0);
		// String minNodeContextStr = ConfigOnZk.getData(nsNode + "/" +
		// minNodeName);

		boolean result = seqNodeName.equals(minNodeName);
		String minNodeNameContextStr = null;
		if (!result) {
			minNodeNameContextStr = ConfigOnZk.getData(nsNode + "/" + minNodeName);
		}
		LOG.debug(LOG.isDebugEnabled() ? "Thread:" + Thread.currentThread().getId() + "-" + Thread.currentThread().getName()
				+ ",node:" + nsNode + "/" + context.getLockId() + ", seqNodeName:" + seqNodeName + ",minNodeName:"
				+ minNodeName
				+ (StringUtils.isNotBlank(minNodeNameContextStr) ? ",minNodeNameContextStr:" + minNodeNameContextStr : "")
				+ ",childList:" + childList
				+ ",lock result:" + result : null);

		return result;
		// LockContext minContext = new Gson().fromJson(minNodeContextStr,
		// LockContext.class);
		// if (context.getLockId().equals(minContext.getLockId()) &&
		// context.getThreadId().equals(minContext.getThreadId())) {
		// return true;
		// }
	}
}
