package com.ujigu.secure.ipdata.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.ipdata.mon17.Mon17IPSeeker;
import com.ujigu.secure.ipdata.qqseeker.IPLocation;
import com.ujigu.secure.ipdata.qqseeker.Util;

@Controller
@RequestMapping("ip")
public class IPSeekController {

	@RequestMapping("s")
	@ResponseBody
	public ResultModel seek(String ip){
		IPLocation loc = Mon17IPSeeker.getAddr(ip);
		if(loc != null){
			ResultModel resultModel = ResultModel.buildMapResultModel();
			resultModel.put("country", loc.getCountry());
			resultModel.put("provice", loc.getProvince());
			resultModel.put("city", loc.getCity());
			
			return resultModel;
		}
		
		return new ResultModel("NOT_FOUND", "没有检索到ip(" + ip + ")对应的地理信息");
	}
	
	@RequestMapping("i")
	@ResponseBody
	public ResultModel seek(int ip){
		String ipstr = Util.intToIP(ip);
		
		return seek(ipstr);
	}
	
}
