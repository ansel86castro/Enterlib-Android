package com.enterlib.data;

public interface IPagedCursor<T> extends IEntityCursor<T> {

	int getPageIndex(int position);
	
	int getPageOffset(int position);
	
	boolean isLoaded(int position);

	void loadPage(int pageIdx);

	void loadPageAsync(int pageIdx, OnPageLoadedListener pageLoadedListener);

	int getPageSize();
	
	int getCurrentPage();
	
	int getPageCount();

	boolean isLoaded();
}