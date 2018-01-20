package com.ujigu.statbg.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.DateUtil;
import com.ujigu.statbg.util.BootUtil;

@Controller
@RequestMapping("/data/test")
public class DataTestController {
	
	@RequestMapping("daystat")
	@ResponseBody
	public ResultModel dataDayStat(String configFile, String startDate, String endDate){
		if(StringUtils.isBlank(configFile)){
			return new ResultModel("DATA_ERR","configFile不能为空，如果统计的spring文件放在context/stat目录下，指定spring文件名即可!");
		}
		if(null==startDate||null==endDate){
			return new ResultModel("DATA_ERR","开始或结束日期不能为空!");
		}
		Date sDate = DateUtil.parseDate(startDate.toString());
		Date eDate = DateUtil.parseDate(endDate.toString());
		int day = DateUtil.differentDays(sDate, eDate);
		if(day < 0){
			return new ResultModel("DATA_ERR","开始日期不能大于结束日期");
		}
		
		if(!configFile.startsWith("context/stat/") || !configFile.startsWith("/context/stat/")){
			configFile = "context/stat/" + configFile;
		}
		
		List<String> days = DateUtil.genDays(sDate, eDate, true);
		for(String daystr : days){
			List<String> params = new ArrayList<>();
			params.add("-configFile");
			params.add(configFile);
			params.add("-startTime");
			params.add(daystr);
			params.add("-endTime");
			Date statEndDate = DateUtil.timeAddByDays(DateUtil.parseDate(daystr), 1);
			params.add(DateUtil.getDateStr(statEndDate));
			BootUtil.execStat(params.toArray(new String[0]));
		}
		
		return new ResultModel(true);
	}
	
	
	@RequestMapping("weekstat")
	@ResponseBody
	public ResultModel dataWeekStat(String configFile, String startDate, String endDate){
		if(StringUtils.isBlank(configFile)){
			return new ResultModel("DATA_ERR","configFile不能为空，如果统计的spring文件放在context/stat目录下，指定spring文件名即可!");
		}
		if(null==startDate||null==endDate){
			return new ResultModel("DATA_ERR","开始或结束日期不能为空!");
		}
		Date sDate = DateUtil.parseDate(startDate.toString());
		Date eDate = DateUtil.parseDate(endDate.toString());
		int day = DateUtil.differentDays(sDate, eDate);
		if(day<7){
			return new ResultModel("DATA_ERR","开始日期和结束日期之间至少要间隔7天");
		}
		
		if(eDate.getTime() > System.currentTimeMillis()){
			eDate = new Date();
		}
		
		if(!configFile.startsWith("context/stat/") || !configFile.startsWith("/context/stat/")){
			configFile = "context/stat/" + configFile;
		}
		
		Date firstMonday = DateUtil.getWeekMondayDate(sDate);
		Date nextMonday = DateUtil.timeAddByDays(firstMonday, 7);
		while(nextMonday.getTime() < eDate.getTime() + 7l * 24 * 3600 * 1000){
			List<String> params = new ArrayList<>();
			params.add("-configFile");
			params.add(configFile);
			params.add("-startTime");
			params.add(DateUtil.getDateStr(firstMonday));
			params.add("-endTime");
			params.add(DateUtil.getDateStr(nextMonday));
			BootUtil.execStat(params.toArray(new String[0]));
			
			firstMonday = nextMonday;
			nextMonday = DateUtil.timeAddByDays(firstMonday, 7);	
		}
		
		return new ResultModel(true);
	}
	
	@RequestMapping("monthstat")
	@ResponseBody
	public ResultModel dataMonthStat(String configFile, String startDate, String endDate){
		if(StringUtils.isBlank(configFile)){
			return new ResultModel("DATA_ERR","configFile不能为空，如果统计的spring文件放在context/stat目录下，指定spring文件名即可!");
		}
		if(null==startDate||null==endDate){
			return new ResultModel("DATA_ERR","开始或结束日期不能为空!");
		}
		Date sDate = DateUtil.parseDate(startDate.toString());
		Date eDate = DateUtil.parseDate(endDate.toString());
		int day = DateUtil.differentDays(sDate, eDate);
		if(day<28){
			return new ResultModel("DATA_ERR","开始日期和结束日期之间至少要间隔28天");
		}
		
		if(eDate.getTime() > System.currentTimeMillis()){
			eDate = new Date();
		}
		
		if(!configFile.startsWith("context/stat/") || !configFile.startsWith("/context/stat/")){
			configFile = "context/stat/" + configFile;
		}
		
		Date firstDay = DateUtil.parseDate(DateUtil.getFirstDayOfMonth(sDate));
		Date nextFirstDay = DateUtil.timeAddByMonth(firstDay, 1);
		while(nextFirstDay.getTime() < eDate.getTime() + 30l * 24 * 3600 * 1000){
			List<String> params = new ArrayList<>();
			params.add("-configFile");
			params.add(configFile);
			params.add("-startTime");
			params.add(DateUtil.getDateStr(firstDay));
			params.add("-endTime");
			params.add(DateUtil.getDateStr(nextFirstDay));
			BootUtil.execStat(params.toArray(new String[0]));
			
			firstDay = nextFirstDay;
			nextFirstDay = DateUtil.timeAddByMonth(firstDay, 1);
		}
		
		return new ResultModel(true);
	}

}
