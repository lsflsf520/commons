package com.xyz.tools.web.common.ftl;

import org.springframework.stereotype.Component;

@Component("LinkNRSubmit")
public class LinkNoRepeatSubmit extends NoRepeatSubmit {

	/*@Override
	protected String buildNoRepeatText(String name, String value) {
		return name + "=" + value;
	}*/
	
	@Override
	protected String buildNoRepeatText(String key, long version) {
		return NR_SUBMIT_NAME + "=" + key + "&" + NRS_VERSION_NAME + "=" + version;
	}
	
}
