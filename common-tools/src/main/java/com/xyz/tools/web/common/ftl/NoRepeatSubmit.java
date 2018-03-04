package com.xyz.tools.web.common.ftl;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.xyz.tools.cache.constant.DefaultJedisKeyNS;
import com.xyz.tools.cache.redis.ShardJedisTool;
import com.xyz.tools.common.exception.BaseRuntimeException;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@Component("NRSubmit")
public class NoRepeatSubmit implements TemplateMethodModelEx {

	public final static String NR_SUBMIT_NAME = "_nrsubmit_";
	public final static String NRS_VERSION_NAME = "_nrs_version_";

//	private final static String VALID_VAL = "1";
//	public final static String INVALID_VAL = "0";

	@Override
	public Object exec(List args) throws TemplateModelException {
		if (CollectionUtils.isEmpty(args) || args.get(0) == null || (!(args.get(0) instanceof Number) && !(args.get(0) instanceof String)) ) {
			// 如果参数为空，则代表用户想使用防重复提交
			String uuid = UUID.randomUUID().toString();
			long version = ShardJedisTool.incr(DefaultJedisKeyNS.nrsubmit, uuid);
			return buildNoRepeatText(uuid, version);
		}
		if (args.size() != 2) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "参数格式必须为 'formKey, type'");
		}
		String firstParam = args.get(0).toString().trim();

		String secondParam = args.get(1).toString().trim();

		FormType formType = null;
		try {
			formType = FormType.valueOf(secondParam);
		} catch (Exception e) {
			throw new BaseRuntimeException("ILLEGAL_PARAM", "not exist enum '" + secondParam + "'");
		}
		String key = formType + "!" + firstParam.trim();
		String val = ShardJedisTool.get(DefaultJedisKeyNS.nrsubmit, key);
		long version = 0;
		if(StringUtils.isBlank(val)){
			version = ShardJedisTool.incr(DefaultJedisKeyNS.nrsubmit, key);
		} else {
			version = Long.valueOf(val);
		}

		return buildNoRepeatText(key, version);
	}

	protected String buildNoRepeatText(String key, long version) {
		return "<input type='hidden' name='" + NR_SUBMIT_NAME + "' value='" + key + "'/>\n"
				+ "<input type='hidden' name='" + NRS_VERSION_NAME + "' value='" + version + "'/>";
	}

	public static boolean isVersionConflictType(String nrsubmitkey) {
		if (StringUtils.isNotBlank(nrsubmitkey)) {
			for (FormType formType : FormType.values()) {
				if (nrsubmitkey.startsWith(formType.name())) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isValid(String key, String version) {
		if(StringUtils.isBlank(key) || StringUtils.isBlank(version)){
			return false;
		}
		long nextVersion = ShardJedisTool.incr(DefaultJedisKeyNS.nrsubmit, key.trim());
		boolean result = version.trim().equals("" + (nextVersion - 1));
		if(!result){
			//如果校验不成功，需要把版本号还原，注意：这个地方有事务性的风险，不过理论上发生的几率特小，可以忽略不计
			ShardJedisTool.decr(DefaultJedisKeyNS.nrsubmit, key.trim());
		}
		return result;
	}

	private static enum FormType {
		AGENT_COMPANY
	}
}
