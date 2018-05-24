package com.xyz.tools.statbg;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author lsf
 *
 */
public interface GlobalParam<T extends Serializable> {

	T generateParam(Map<String, List<FlowData>> globalParamMap);
	
}