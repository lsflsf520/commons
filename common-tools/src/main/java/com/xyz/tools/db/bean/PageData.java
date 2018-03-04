package com.xyz.tools.db.bean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.miemiedev.mybatis.paginator.domain.PageList;
import com.github.miemiedev.mybatis.paginator.domain.Paginator;
import com.xyz.tools.common.exception.BaseRuntimeException;

public class PageData<E> {
	
	private final List<E> datas;
	
	private int totalCount;
	private int totalPages;
	private int currPage;
	private int maxRows;
	private int startRow;
	private int endRow;
	private int offset;
	private Integer[] slider;
	private int prePage;
	private int nextPage;
	private boolean firstPage;
	private boolean hasNextPage;
	private boolean hasPrePage;
	private boolean lastPage;
	
	public PageData(List<E> datas, int currPage, int maxRows, int totalCount, int totalPages){
		this.datas = datas;
		this.currPage = currPage;
		this.maxRows = maxRows;
		this.totalCount = totalCount;
		this.totalPages = totalPages;
	}
	
	public PageData(PageList<E> dataPage){
		if(dataPage == null){
			throw new BaseRuntimeException("ILLEGAL_PARAM", "dataPage cannot be null");
		}
		this.datas = new ArrayList<>(dataPage);
		Paginator pager = dataPage.getPaginator();
		if(pager != null){
			this.totalCount = pager.getTotalCount();
			this.totalPages = pager.getTotalPages();
			this.currPage = pager.getPage();
			this.maxRows = pager.getLimit();
			this.startRow = pager.getStartRow();
			this.endRow = pager.getEndRow();
			this.offset = pager.getOffset();
			this.slider = pager.getSlider();
			this.prePage = pager.getPrePage();
			this.nextPage = pager.getNextPage();
			this.firstPage = pager.isFirstPage();
			this.hasNextPage = pager.isHasNextPage();
			this.hasPrePage = pager.isHasPrePage();
			this.lastPage = pager.isLastPage();
		}
	}

	public List<E> getDatas() {
		return Collections.unmodifiableList(datas);
	}

	public int getTotalCount() {
		return totalCount;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public int getCurrPage() {
		return currPage;
	}

	public int getMaxRows() {
		return maxRows;
	}

	public int getStartRow() {
		return startRow;
	}

	public int getEndRow() {
		return endRow;
	}

	public int getOffset() {
		return offset;
	}

	public Integer[] getSlider() {
		return slider;
	}

	public int getPrePage() {
		return prePage;
	}

	public int getNextPage() {
		return nextPage;
	}

	public boolean isFirstPage() {
		return firstPage;
	}

	public boolean isHasNextPage() {
		return hasNextPage;
	}

	public boolean isHasPrePage() {
		return hasPrePage;
	}

	public boolean isLastPage() {
		return lastPage;
	}
	
}
