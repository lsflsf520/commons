package com.ujigu.secure.db.utils;

public class MybatisUtil {

	public static String parseShortStmtName(String stmtName){
		String[] parts = stmtName.split("\\.dao\\.");
		if(parts.length == 2){
			stmtName = parts[1];
		    if(parts[1].startsWith("impl.")){
		    	stmtName = stmtName.substring("impl.".length());
		    }
		}
		
		return stmtName;
	}
	
}
