package com.ujigu.secure.common.bean;

import java.util.List;

public class PageList<T> {

	private int pageNo = 1; // 当前页
	private int pageSize = 1; // 每页数量
	private int recordSize = 1; // 总数量
	private int pageCount = 1; // 页数量
	private String url = "";
	private List<T> results;

	public PageList(int pageNo, int pageSize, int recordSize, int pageCount,
			String url, List<T> results) {

		this.pageNo = pageNo;
		this.pageSize = pageSize;
		this.recordSize = recordSize;
		this.pageCount = pageCount;
		this.url = url;
		this.results = results;
	}

	public PageList() {

	}

	public int getRecordSize() {

		return recordSize;
	}

	public void setRecordSize(int recordSize) {

		this.recordSize = recordSize;
	}

	public int getPageNo() {

		return pageNo;
	}

	public void setPageNo(int pageNo) {

		this.pageNo = pageNo;
	}

	public int getPageSize() {

		return pageSize;
	}

	public void setPageSize(int pageSize) {

		this.pageSize = pageSize;
	}

	public int getPageCount() {

		return pageCount;
	}

	public void setPageCount(int pageCount) {

		this.pageCount = pageCount;
	}

	public String getUrl() {

		return url;
	}

	public void setUrl(String url) {

		this.url = url;
	}

	public List<T> getResults() {

		return results;
	}

	public void setResults(List<T> results) {

		this.results = results;
	}
}