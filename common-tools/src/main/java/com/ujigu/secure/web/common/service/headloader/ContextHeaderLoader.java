package com.ujigu.secure.web.common.service.headloader;


import java.util.Map;

import org.springframework.util.CollectionUtils;

public class ContextHeaderLoader extends AbstractHeaderLoader {
	
//	private final static Logger LOG = LoggerFactory.getLogger(ContextHeaderLoader.class);
	
	@Override
	public CommonHeader loadHeader(Map<String, Object> paramMap) {
		if(CollectionUtils.isEmpty(paramMap)){
			return null;
		}
		Object title = paramMap.get("title");
		Object kword = paramMap.get("kword");
		Object descp = paramMap.get("descp");
		
		
		return new CommonHeader(title == null ? null : title.toString(), 
				kword == null ? null : kword.toString(), 
						descp == null ? null : descp.toString());
	}

}
