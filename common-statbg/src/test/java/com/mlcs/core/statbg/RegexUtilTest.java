package com.mlcs.core.statbg;

import java.util.List;

import com.yisi.stiku.statbg.util.RegexHelperUtil;

/**
 * 
 * @author lsf
 *
 */
public class RegexUtilTest {

//	@org.testng.annotations.Test
	public void testGetSqlParamNames(){
		String sql = "select * from table where id= ${p.id } and start_time >= ${ p.startTime} and end_time < ${ p.endTime }";
		
		List<String> results = RegexHelperUtil.getParamNames(sql);
		System.out.println(results);
	}
	
	@org.testng.annotations.Test
	public void testExtractGroups(){
//		String str = "tm(1404140399939-16.171-6313)ip(10.14.118.132)mjid(-)ml(-)mc(-)mcps(-)dm(-)uri(-)rf(-)ag(-)fw(-)args(-)ot(mlf(-))";
//		String regex = "tm\\((.*)\\)ip\\((.*)\\)mjid\\((.*)\\)ml\\((.*)\\)mc\\((.*)\\)mcps\\((.*)\\)dm\\((.*)\\)uri\\((.*)\\)rf\\((.*)\\)ag\\((.*)\\)fw\\((.*)\\)args\\((.*)\\)ot\\((.*)\\)";
		
		String str = "tm(1404140399938-16.171-6312)ip(114.249.115.112)mjid(14041388757523155)ml(138899200000002094737)mc(mlw-1404138895622069209)mcps(-)dm(tj.meiliwan.com)uri(/u.gif)rf(http://www.meiliwan.com/)ag(Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.1 (KHTML, like Gecko) Chrome/21.0.1180.89 Safari/537.1)fw(-)args(uqid=14041388757523155&referer=http%3A%2F%2Fwww.meiliwan.com%2F)ot()";
		String regex = "tm\\((.*)\\)ip\\((.*)\\)mjid\\((.*)\\)ml\\((.*)\\)mc\\((.*)\\)mcps\\((.*)\\)dm\\((.*)\\)uri\\(/u\\.gif\\)rf\\((.*)\\)ag\\((.*)\\)fw\\((.*)\\)args\\((.*)\\)ot\\((.*)\\)";
		
//		List<String> list = RegexUtil.extractGroups(regex, str);
//		
//		System.out.println(list);
		
		String[] parts = regex.split("\\([^(]+\\)");
		
		for(String part : parts){
			System.out.println(part);
		}
	}
	
}
