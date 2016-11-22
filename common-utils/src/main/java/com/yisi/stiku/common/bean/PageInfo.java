package com.yisi.stiku.common.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 这个类是公共翻页
 *
 * @author Sean
 */
public class PageInfo implements Serializable, Pageable {


    /**
     *
     */
    private static final long serialVersionUID = 9184978320374555409L;
    /**
     * 一共条数
     */
    private int totalCounts = 0;
    /**
     * 拥有页数
     */
    private int allPage = 0;
    /**
     * 当前页数
     */
    private int page = 1;
    
    /**
     * 当前页返回条数
     */
    private int pagesize = 20;

    private int startIndex = 0;

    /**
     * 是否有上一页
     */
    private boolean hasPrev;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    /**
     * 当前翻页列表
     */
    private List<Integer> listPages = new ArrayList<Integer>();

    private String orderBySql;
    
    public PageInfo(int page, int pagesize) {
        super();
        if(page<1)
            page = 1;
        this.page = page;
        this.pagesize = pagesize < 0 ? this.pagesize : pagesize;
        this.startIndex = (page - 1) * pagesize;
    }

    public String getOrderBySql() {
        return orderBySql;
    }

    public void setOrderBySql(String orderBySql) {
        this.orderBySql = orderBySql;
    }

    public boolean hasPrev() {
		return hasPrev;
	}

	public boolean hasNext() {
		return hasNext;
	}

	public List<Integer> getListPages() {
        return this.listPages;
    }

    @Override
    public String toString() {
        return "PageInfo [allPage=" + allPage + 
                ", isNext=" + hasNext() + ", isPrev=" + hasPrev() + ", listPages="
                + listPages + ", page=" + page + ", pagesize=" + pagesize
                + ", startIndex=" + startIndex
                + ", totalCounts=" + totalCounts + "]";
    }

    public int getTotalCounts() {
        return totalCounts;
    }

    public void setTotalCounts(int totalCounts) {
        this.totalCounts = totalCounts;
        executePage();
    }

    public void executePage() {
        // 分页算法
        if (totalCounts > 0) {
            allPage = totalCounts / pagesize;
            if (totalCounts % pagesize != 0)
                allPage++;
        }
        int startX = 1;
        // 计算开始位置
        if (page > 4 && page <= allPage)
            startX = page - 4;
        // 上一页
        if (page > 1 && page <= allPage)
            hasPrev = page - 1 > 0;
        for (int i = startX; i < startX + 10 && i <= allPage; i++)
            listPages.add(i);
        // 下一页
        if (page < allPage)
            hasNext = page + 1 <= allPage;
    }

    public int getAllPage() {
        return allPage;
    }

    public void setAllPage(int allPage) {
        this.allPage = allPage;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        //    if (page < 20) {
    	if(page<1)
    		page=1;
        this.page = page;
        this.startIndex = (page - 1) * pagesize;
        //    }
    }

    public int getPagesize() {
        return pagesize;
    }

    public void setPagesize(int pagesize) {
        this.pagesize = pagesize;
        executePage();
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setListPages(List<Integer> listPages) {
        this.listPages = listPages;
    }

    public static enum OrderDirection{
    	asc, desc;
    }

	@Override
	public int getPageNumber() {
		return this.getPage();
	}

	@Override
	public int getPageSize() {
		return this.getPagesize();
	}

	@Override
	public int getOffset() {
		return this.getStartIndex();
	}

	@Override
	public Sort getSort() {
		return null;
	}

	@Override
	public Pageable next() {
		return null;
	}

	@Override
	public Pageable previousOrFirst() {
		return null;
	}

	@Override
	public Pageable first() {
		return null;
	}

	@Override
	public boolean hasPrevious() {
		return this.hasPrev();
	}

}
