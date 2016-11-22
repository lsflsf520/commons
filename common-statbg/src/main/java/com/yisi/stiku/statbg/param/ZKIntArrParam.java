package com.yisi.stiku.statbg.param;

import java.util.ArrayList;

import com.yisi.stiku.conf.ConfigOnZk;
import com.yisi.stiku.statbg.GlobalParam;

public class ZKIntArrParam implements GlobalParam<ArrayList<Integer>>{
	
	private String path;
	
	private String key;

	@Override
	public ArrayList<Integer> generateParam() {
		ArrayList<Integer> schoolIdList = new ArrayList<Integer>();
		int[] schoolIds = ConfigOnZk.getIntArr(path, key);
		for(int i=0;i<schoolIds.length;i++){
			schoolIdList.add(schoolIds[i]);
		}
		return schoolIdList;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
}
