package com.zju.rchz.model;

public class PageInfo {
	public int pageSize;
	public int totalCounts;
	public int currentPage;
	public int totalPages;

	public boolean isNoMore() {
		return currentPage >= totalPages;
	}
}
