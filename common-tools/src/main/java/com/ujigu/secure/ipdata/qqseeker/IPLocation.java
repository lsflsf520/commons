package com.ujigu.secure.ipdata.qqseeker;

import java.io.Serializable;

import org.apache.commons.lang.StringUtils;

/**
 * 
 * @author lsf
 *
 */
public class IPLocation implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5635271797368881200L;
	private String country;
	private String province;
	private String city;
	private String detail;
	private String area;
	
	public IPLocation(String country, String province, String city, String detail) {
		this.country = country;
		this.province = province;
		this.city = city;
		this.detail = detail;
				
	}
	
	public IPLocation() {
	}
	
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getProvince() {
		return province;
	}
	public void setProvince(String province) {
		this.province = province;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getCounty() {
		if(StringUtils.isBlank(detail)  || LexiconUtil.isUnknown(detail) || !(detail.contains("县") || detail.contains("市") || detail.contains("区"))){
			return LexiconUtil.UNKNOWN;
		}
		
		return detail.contains("县") ? detail.split("县")[0] + "县" : (detail.contains("区") ? detail.split("区")[0] + "区" : detail.split("市")[0] + "市");
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}
	
	@Override
	public String toString() {
		return this.getCountry() + "\t" + this.getProvince() + "\t"
				 + this.getCity() + "\t" + this.getCounty() + "\t" 
				 + this.getArea() + "\t" + this.getDetail();
	}
}
