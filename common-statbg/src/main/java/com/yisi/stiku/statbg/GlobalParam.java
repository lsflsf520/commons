package com.yisi.stiku.statbg;

import java.io.Serializable;

/**
 * 
 * @author lsf
 *
 */
public interface GlobalParam<T extends Serializable> {

	T generateParam();
	
}
