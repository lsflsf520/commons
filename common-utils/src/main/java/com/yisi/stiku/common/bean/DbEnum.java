package com.yisi.stiku.common.bean;

import java.io.Serializable;

public interface DbEnum<CType extends Serializable> {

	/**
	 * 
	 * @return
	 */
	CType getDbCode();
	
//	<T extends DbEnum<CType>> T[] getValues(); 
	
}
