package com.ujigu.secure.web.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ujigu.secure.common.bean.ResultModel;
import com.ujigu.secure.common.utils.BaseConfig;
import com.ujigu.secure.common.utils.JsonUtil;
import com.ujigu.secure.common.utils.LogUtils;
import com.ujigu.secure.common.utils.StringUtil;

/**
*@comment 
*@author tujianjun
*@time 2017年7月24日 下午1:56:39
*/
public class DnsUtils {
	private final static Logger LOG = LoggerFactory.getLogger(DnsUtils.class);
	
	public static String NDS_BX = "baoxian";
	public static String NDS_DK = "daikuan";
	
	/*private static String LOGIN_TOKEN = "34559,711239e9c9e03f0352858de97350e543";
	
	public static String M_POINT = "m.";
	
	private static String[] DOMAIN_EXCEPT = BaseConfig.getValueArr("wz.domain.except");
	private static String IPVALUE = BaseConfig.getValue("server.ip", "106.3.136.54");
	private static String DOMAIN = StringUtils.isBlank(BaseConfig.getValue("wz.domain.siteSuffix")) ? "" :
        (BaseConfig.getValue("wz.domain.siteSuffix").startsWith(".") ? BaseConfig.getValue("wz.domain.siteSuffix").substring(1) : BaseConfig.getValue("wz.domain.siteSuffix") );
	
	public static DnsUtils init(String busiType) {
		DnsUtils dnsUtils = new DnsUtils();
		
		if("BAOXIAN".equals(busiType)) {
			DnsUtils.M_POINT = "m-";
			DnsUtils.DOMAIN_EXCEPT = BaseConfig.getValueArr("wz.bx.domain.except");
			DnsUtils.IPVALUE = BaseConfig.getValue("server.ip", "106.3.136.54");
			DnsUtils.DOMAIN = StringUtils.isBlank(BaseConfig.getValue("bx.wz.domain.siteSuffix")) ? "" :
                (BaseConfig.getValue("bx.wz.domain.siteSuffix").startsWith(".") ? BaseConfig.getValue("bx.wz.domain.siteSuffix").substring(1) : BaseConfig.getValue("bx.wz.domain.siteSuffix") );
		}
		
		return dnsUtils;
	}
	public static DnsUtils init() {
		return init("DAIKUAN");
	}*/
	
	/**调用接口添加一条记录
	 * @param subDomianName 子域名
	 * @return 
	 */
	public static ResultModel doCreate(String subDomianName, String dnsType){
		if(StringUtil.isNull(subDomianName)){
			LogUtils.warn("param subDomianName is null");
			return new ResultModel("ILLEGAL_PARAM", "二级域名不能为空！");
		}
		String LOGIN_TOKEN = getLoginToken(dnsType);
		String[] DOMAIN_EXCEPT = getDomainExcept(dnsType);
		String IPVALUE = getIpvalue(dnsType);
		String DOMAIN = getDomain(dnsType);
		
		//过滤
		for(String except : DOMAIN_EXCEPT) {
			if(except.equals(subDomianName)) {
				LogUtils.info("subDomianName %s is a exception domain", subDomianName);
				return new ResultModel(true);
			}
		}
		
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("login_token", LOGIN_TOKEN);
		formParams.put("format", "json");
		formParams.put("domain", DOMAIN);
		formParams.put("sub_domain", subDomianName);
		formParams.put("record_type", "A");
		formParams.put("record_line", "默认");
		formParams.put("value", IPVALUE);
		DnsPodReback addReback = RestClientUtil.doPost("https://dnsapi.cn/Record.Create", formParams, DnsPodReback.class);
		if("1".equals(addReback.getStatus().code)){
			LogUtils.info("subDomianName %s has record success", subDomianName);
			return new ResultModel(true);
		}else if("104".equals(addReback.getStatus().code)){
			LogUtils.warn("subDomianName %s has already record", subDomianName);

			return new ResultModel(true);
		}
		
		LogUtils.warn("subDomianName %s record error,code:%s, errorMsg:%s", subDomianName, addReback.getStatus().getCode(), addReback.getStatus().getMessage());
		
		return new ResultModel(addReback.getStatus().getCode(), addReback.getStatus().getMessage());
	}
	
	/**根据提供的二级域名，得到在dnspod中的recordid
	 * @param subDomian
	 * @return
	 */
	public static String doInfo(String subDomian, String dnsType){
		if(StringUtil.isNull(subDomian)){
			LOG.error("param subDomianName is null");
			new ResultModel("ILLEGAL_PARAM", "二级域名不能为空！");
		}
		
		String LOGIN_TOKEN = getLoginToken(dnsType);
		String DOMAIN = getDomain(dnsType);
		
		Map<String, Object> formParams = new HashMap<>();
		formParams = new HashMap<>();
		formParams.put("login_token", LOGIN_TOKEN);
		formParams.put("format", "json");
		formParams.put("domain", DOMAIN);
		formParams.put("sub_domain", subDomian);
		DnsPodReback recordReback = RestClientUtil.doPost("https://dnsapi.cn/Record.List", formParams, DnsPodReback.class);
		List<DnsPodRebackRecord> record = recordReback.getRecords();
		if(null != record && record.size() > 0 && null != record.get(0)){
			return record.get(0).getId();
		}else{
			return "";
		}
	}
	
	/**调用借口删除一条记录
	 * @param recordId 对应的子域名生成时产生的记录id
	 * @return
	 */
	public static ResultModel doRemove(String subDomian, String dnsType){
		if(StringUtil.isNull(subDomian)){
			LogUtils.warn("param subDomianName is null");
			return new ResultModel("ILLEGAL_PARAM", "二级域名不能为空！");
		}
		String LOGIN_TOKEN = getLoginToken(dnsType);
		String[] DOMAIN_EXCEPT = getDomainExcept(dnsType);
		//过滤
		for(String except : DOMAIN_EXCEPT) {
			if(except.equals(subDomian)) {
				LogUtils.info("subDomianName %s is a exception domain", subDomian);
				return new ResultModel(true);
			}
		}
		
		String recordId = doInfo(subDomian, dnsType);
		Map<String, Object> formParams = new HashMap<>();
		formParams = new HashMap<>();
		formParams.put("login_token", LOGIN_TOKEN);
		formParams.put("format", "json");
		formParams.put("domain", DOMAIN_EXCEPT);
		formParams.put("record_id", recordId);
		DnsPodReback delReback = RestClientUtil.doPost("https://dnsapi.cn/Record.Remove", formParams, DnsPodReback.class);
		if("1".equals(delReback.getStatus().code)){
			LogUtils.info("subDomian %s has been removed success", subDomian);
			return new ResultModel(true);
		}else if("8".equals(delReback.getStatus().code)){
			LogUtils.warn("subDomian %s not exist in DNSPOD", subDomian);
			return new ResultModel(true);
		}
		LogUtils.warn("subDomian %s record error,code:%s, errorMsg:%s", subDomian, delReback.getStatus().getCode(), delReback.getStatus().getMessage());
		return new ResultModel(false);
	}
	
	public static class DnsPodReback {
		private DnsPodRebackStatus status;
		private DnsPodRebackDomain domain;
		private DnsPodRebackInfo info;
		private DnsPodRebackRecord record;
		private List<DnsPodRebackRecord> records;
		DnsPodRebackStatus getStatus() {
			return status;
		}
		void setStatus(DnsPodRebackStatus status) {
			this.status = status;
		}
		DnsPodRebackDomain getDomain() {
			return domain;
		}
		void setDomain(DnsPodRebackDomain domain) {
			this.domain = domain;
		}
		DnsPodRebackInfo getInfo() {
			return info;
		}
		void setInfo(DnsPodRebackInfo info) {
			this.info = info;
		}
		DnsPodRebackRecord getRecord() {
			return record;
		}
		void setRecord(DnsPodRebackRecord record) {
			this.record = record;
		}
		List<DnsPodRebackRecord> getRecords() {
			return records;
		}
		void setRecords(List<DnsPodRebackRecord> records) {
			this.records = records;
		}
	}
	public static class DnsPodRebackStatus {
		private String code;
		private String message;
		private String created_at;
		String getCode() {
			return code;
		}
		void setCode(String code) {
			this.code = code;
		}
		String getMessage() {
			return message;
		}
		void setMessage(String message) {
			this.message = message;
		}
		String getCreated_at() {
			return created_at;
		}
		void setCreated_at(String created_at) {
			this.created_at = created_at;
		}
	}
	public static class DnsPodRebackDomain {
		private String id;
		private String name;
		private String punycode;
		private String grade;
		private String owner;
		private String ext_status;
		private String ttl;
		private String min_ttl;
		private List<String> dnspod_ns;
		private String status;
		String getId() {
			return id;
		}
		void setId(String id) {
			this.id = id;
		}
		String getName() {
			return name;
		}
		void setName(String name) {
			this.name = name;
		}
		String getPunycode() {
			return punycode;
		}
		void setPunycode(String punycode) {
			this.punycode = punycode;
		}
		String getGrade() {
			return grade;
		}
		void setGrade(String grade) {
			this.grade = grade;
		}
		String getOwner() {
			return owner;
		}
		void setOwner(String owner) {
			this.owner = owner;
		}
		String getExt_status() {
			return ext_status;
		}
		void setExt_status(String ext_status) {
			this.ext_status = ext_status;
		}
		String getTtl() {
			return ttl;
		}
		void setTtl(String ttl) {
			this.ttl = ttl;
		}
		String getMin_ttl() {
			return min_ttl;
		}
		void setMin_ttl(String min_ttl) {
			this.min_ttl = min_ttl;
		}
		List<String> getDnspod_ns() {
			return dnspod_ns;
		}
		void setDnspod_ns(List<String> dnspod_ns) {
			this.dnspod_ns = dnspod_ns;
		}
		String getStatus() {
			return status;
		}
		void setStatus(String status) {
			this.status = status;
		}
	}
	public static class DnsPodRebackInfo {
		private String sub_domains;
		private String record_total;
		String getSub_domains() {
			return sub_domains;
		}
		void setSub_domains(String sub_domains) {
			this.sub_domains = sub_domains;
		}
		String getRecord_total() {
			return record_total;
		}
		void setRecord_total(String record_total) {
			this.record_total = record_total;
		}
	}
	public static class DnsPodRebackRecord {
		private String id;
		private String name;
		private String line;
		private String line_id;
		private String type;
		private String mx;
		private String enabled;
		private String monitor_status;
		private String remark;
		private String updated_on;
		private String use_aqb;
		private String value;
		private String status;
		private String weight;
		private String ttl;
		private String hold;
		String getId() {
			return id;
		}
		void setId(String id) {
			this.id = id;
		}
		String getName() {
			return name;
		}
		void setName(String name) {
			this.name = name;
		}
		String getLine() {
			return line;
		}
		void setLine(String line) {
			this.line = line;
		}
		String getLine_id() {
			return line_id;
		}
		void setLine_id(String line_id) {
			this.line_id = line_id;
		}
		String getType() {
			return type;
		}
		void setType(String type) {
			this.type = type;
		}
		String getMx() {
			return mx;
		}
		void setMx(String mx) {
			this.mx = mx;
		}
		String getEnabled() {
			return enabled;
		}
		void setEnabled(String enabled) {
			this.enabled = enabled;
		}
		String getMonitor_status() {
			return monitor_status;
		}
		void setMonitor_status(String monitor_status) {
			this.monitor_status = monitor_status;
		}
		String getRemark() {
			return remark;
		}
		void setRemark(String remark) {
			this.remark = remark;
		}
		String getUpdated_on() {
			return updated_on;
		}
		void setUpdated_on(String updated_on) {
			this.updated_on = updated_on;
		}
		String getUse_aqb() {
			return use_aqb;
		}
		void setUse_aqb(String use_aqb) {
			this.use_aqb = use_aqb;
		}
		String getValue() {
			return value;
		}
		void setValue(String value) {
			this.value = value;
		}
		String getStatus() {
			return status;
		}
		void setStatus(String status) {
			this.status = status;
		}
		String getWeight() {
			return weight;
		}
		void setWeight(String weight) {
			this.weight = weight;
		}
		String getTtl() {
			return ttl;
		}
		void setTtl(String ttl) {
			this.ttl = ttl;
		}
		String getHold() {
			return hold;
		}
		void setHold(String hold) {
			this.hold = hold;
		}
	}
	
	public static String getM_Point(String dnsType) {
		return BaseConfig.getValue(dnsType + ".wz.m.point", "m.");
	}
	public static String getLoginToken(String dnsType) {
		return BaseConfig.getValue(dnsType + ".wz.login.token", "34559,711239e9c9e03f0352858de97350e543");
	}
	public static String[] getDomainExcept(String dnsType) {
		return BaseConfig.getValueArr(dnsType + ".wz.domain.except");
	}
	public static String getIpvalue(String dnsType) {
		return BaseConfig.getValue(dnsType + ".server.ip", "106.3.136.54");
	}
	public static String getDomain(String dnsType) {
		return BaseConfig.getValue(dnsType + ".wz.domain.siteSuffix");
	}
	
	
	public static void main(String[] args) {
		
		Map<String, Object> formParams = new HashMap<>();
		formParams.put("login_token", getLoginToken(DnsUtils.NDS_DK));
		formParams.put("format", "json");
		formParams.put("domain", "baoxianjie.net");
		formParams.put("sub_domain", "app-test");
		formParams.put("record_type", "A");
		formParams.put("record_line", "默认");
		formParams.put("value", "210.73.209.73");
		DnsPodReback addReback = RestClientUtil.doPost("https://dnsapi.cn/Record.Create", formParams, DnsPodReback.class);
		System.out.println(JsonUtil.create().toJson(addReback));
	}
	
}




