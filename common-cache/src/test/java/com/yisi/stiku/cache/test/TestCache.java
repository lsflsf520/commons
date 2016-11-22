package com.yisi.stiku.cache.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.yisi.stiku.cache.eh.EhCacheTool;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class TestCache {

	public static void main(String[] args) throws InterruptedException {
//		CacheManager manager = CacheManager.create("D:/repos/git/stiku_all/stiku-parent/common-parent/common-cache/src/test/resources/ehcache.xml"); 
		
		
//		manager.addCache("tmpCache");
//		Cache cache = manager.getCache("tmpCache");  
//		
//		cache.put(new Element("name", "好样的", false, 2, 2));
		
//		List<TblAuthPrivilege> privList = new ArrayList<TblAuthPrivilege>();
//		privList.add(new TblAuthPrivilege());
//		privList.add(new TblAuthPrivilege());
//		privList.add(new TblAuthPrivilege());
//		
//		EhCacheTool.getInstance().put(DefaultCacheNS.SYS_MENU, privList);
//		
//		List<TblAuthPrivilege> list = EhCacheTool.getInstance().getValue(DefaultCacheNS.SYS_MENU);
//		
//		System.out.println(list);
		
		System.out.println(System.getProperty("os.name"));
	}
	
}
