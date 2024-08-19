package com.enterlib.data.web;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;

import android.database.DatabaseUtils;
import android.text.TextUtils;
import android.util.SparseArray;
import android.widget.Filter;

import com.enterlib.StringUtils;
import com.enterlib.app.ConditionFilterHandler;
import com.enterlib.app.ConditionFilterHandler.Delayer;
import com.enterlib.data.IPagedCursor;
import com.enterlib.data.ISortable;
import com.enterlib.data.OnPageLoadedListener;
import com.enterlib.data.DataChangeNotify;
import com.enterlib.data.EntityCursorIterator;
import com.enterlib.data.OnSortingListener;
import com.enterlib.data.QueryHelper;
import com.enterlib.data.SortingElement;
import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.FilterListener;
import com.enterlib.filtering.IFilterable;
import com.enterlib.threading.LoaderHandler;
import com.enterlib.threading.LoaderHandler.LoadTask;

public class WebEntityCursor<T> extends DataChangeNotify implements IPagedCursor<T>, Delayer, IFilterable, ISortable {

	private final WebApiClient<T> client;
	private SortHandler sortHandler;

	public class DataPage {
		public int Index;
		public long LasAccessTime;
		public ArrayList<T> Data;
	}	

	String staticFilter;
	String staticOrderby;
	String includes;
	int skip;
	
	String requestFilter;
	String requestOrderBy;

	WebEntityCursor<T>.FilterHandler filterHandler;
	LoaderHandler loaderHandler;	
	int count = -1;
	int nbPages;	
	int pageSize = 30;
	int maxNbLoadedPages = 10;	
	SparseArray<DataPage>pages;
	DataPage current;
	int currentPageIndex = -1;
	
	public WebEntityCursor(WebApiClient<T> client){
		this(client, 10, 30);
	}
	
	public WebEntityCursor(WebApiClient<T> client, int maxNbLoadedPages, int pageSize){
		this.client = client;
		this.maxNbLoadedPages = maxNbLoadedPages;
		this.pageSize = pageSize;
		this.pages = new SparseArray<DataPage>(maxNbLoadedPages);
	}

	@Override
	public void close() {		
		pages.clear();		
		currentPageIndex = -1;
		current = null;
		count = -1;
	}

	public void doFilter(ArrayList<FilterCondition> fixedConditions,
			FilterListener listener) {
		if (filterHandler == null) {
			filterHandler = new FilterHandler();
			filterHandler.setDelayer(this);
		}
		
		filterHandler.doFilter(fixedConditions, listener);
		
	}
	
	public void setFilter(String filter) {
		this.staticFilter = filter;
	}

	public String getFilter(){
		return staticFilter;
	}
	
	protected void setRequestFilter(String constraint){
		this.requestFilter = constraint;
	}
		
	protected String getRequestFilter(){
		return requestFilter;
	}
	
	public void setOrderBy(String orderBy){
		this.staticOrderby = orderBy;
	}
	
	public String getOrderBy(){
		return staticOrderby;
	}
	
//	protected void setRequestOrderBy(String orderBy){
//		this.requestOrderBy = orderBy;
//	}
//
//	protected String getRequestOrderBy(){
//		return requestOrderBy;
//	}

	public String getIncludes(){
		return includes;
	}

	public void setIncludes(String includes){
		this.includes = includes;
	}

	@Override
	public long getPostingDelay(ArrayList<FilterCondition> constraints) {
		return 500;
	}

	@Override
	public boolean isLoaded() {
		return count >= 0;
	}

	@Override
	public int getCount() {	
		if(count < 0){
			loadPage(0);
		}
		return count;
	}

	@Override
	public T getItem(int position) {		
		if(count >= 0 && position > count)
			throw new IndexOutOfBoundsException("position");
		
		int pageIdx = getPageIndex(position);				
		DataPage page = pages.get(pageIdx);		
		if(page == null){
			loadPage(pageIdx);
		}
		
		int offset =getPageOffset(position);		
		page.LasAccessTime = System.nanoTime();

		try {
			return page.Data.get(offset);
		}catch (java.lang.IndexOutOfBoundsException e){
			throw e;
		}
	}
	
	public int getPageIndex(int position){
		return position / pageSize;
	}
	
	public int getPageOffset(int position){
		return position % pageSize;
	}
	
	@Override
	public int getPageSize() {
		return pageSize;
	}
	
	@Override
	public int getCurrentPage() {	
		return currentPageIndex;
	}
		
	@Override
	public int getPageCount() {
		return nbPages;
	}
	

	/* (non-Javadoc)
	 * @see com.enterlib.conetivity.IPagedCursor#isLoaded(int)
	 */
	@Override
	public boolean isLoaded(int position){		
		int pageIdx = getPageIndex(position);				
		DataPage page = pages.get(pageIdx);		
		return page!=null;
	}

	@Override
	public void loadPage(int pageIdx) {
		loadPage(pageIdx, requestFilter, requestOrderBy);
	}

	class LoadPageResult<T>{
		public int count;
		public int nbPages;
		public int pageIdx;
		public ArrayList<T>elements;
		public String requestFilter;
		public String requestOrderBy;
	}

	public void loadPage(int pageIdx, String requestFilter, String requestOrderBy){
		this.requestFilter = requestFilter;
		String filter = QueryStringBuilder.combineEncodedFilterWithAND(staticFilter, requestFilter);
		String orderBy = QueryHelper.combineOrderBy(staticOrderby, requestOrderBy, QueryStringBuilder.encode(","));

		QueryStringBuilder qb = new QueryStringBuilder();
		qb.add("filter",filter);
		qb.add("orderby", orderBy);
		qb.add("skip", (skip > 0 ? skip : 0)+ pageIdx * pageSize);
		qb.add("top",  pageSize);
		qb.add("include", includes);

		count = client.actionCount(filter!=null? "filter="+filter:null);

		if(count <= 0){
			currentPageIndex = -1;
			current = null;
			nbPages = 0;
			return;
		}

		ArrayList<T> elements = client.getList("get/", qb.toQueryString());
		nbPages = count / pageSize + ((count % pageSize) > 0 ? 1 : 0);				

		while(pageIdx >= nbPages){
			pageIdx = nbPages - 1;

			qb.clear();
			qb.add("filter", filter);
			qb.add("orderby", orderBy);
			qb.add("skip", (skip > 0 ? skip : 0)+ pageIdx * pageSize);
			qb.add("top",  pageSize);
			qb.add("include", includes);

			count = client.actionCount(filter!=null? "filter="+filter:null);
			elements = client.getList("get/", qb.toQueryString());

			nbPages = count / pageSize + ((count % pageSize) > 0 ? 1 : 0);
		}

		currentPageIndex = pageIdx;
		current = findPage(pageIdx);
		current.Data = elements;
	}

	public void loadPage(int pageIdx, String requestFilter, String sortExpression, LoadPageResult<T> result){
		result.pageIdx = pageIdx;
		result.requestFilter = requestFilter;
		result.requestOrderBy = sortExpression;

		String filter = QueryStringBuilder.combineEncodedFilterWithAND(staticFilter, requestFilter);
		String orderBy = QueryHelper.combineOrderBy(staticOrderby, sortExpression, QueryStringBuilder.encode(","));

		QueryStringBuilder qb = new QueryStringBuilder();
		qb.add("filter",filter);
		qb.add("orderby", orderBy);
		qb.add("skip", (skip > 0 ? skip : 0)+ pageIdx * pageSize);
		qb.add("top",  pageSize);
		qb.add("include", includes);

		result.count = client.actionCount(filter!=null? "filter="+filter:null);

		if(result.count <= 0){
			result.nbPages = 0;
			return;
		}

		result.elements = client.getList("get/", qb.toQueryString());
		result.nbPages = result.count / pageSize + ((result.count % pageSize) > 0 ? 1 : 0);

		while(result.pageIdx >= result.nbPages){
			result.pageIdx = result.nbPages - 1;

			qb.clear();
			qb.add("filter", filter);
			qb.add("orderby",orderBy);
			qb.add("skip", (skip > 0 ? skip : 0)+ pageIdx * pageSize);
			qb.add("top",  pageSize);
			qb.add("include", includes);

			result.count = client.actionCount(filter!=null? "filter="+filter:null);
			result.elements = client.getList("get/", qb.toQueryString());

			result.nbPages = result.count / pageSize + ((result.count % pageSize) > 0 ? 1 : 0);
		}
	}
	
	private DataPage findPage(int pageIdx) {
		DataPage page = pages.get(pageIdx);
		if(page == null){
			if(pages.size() <= maxNbLoadedPages){
				page = new DataPage();				
				pages.put(pageIdx, page);
			}else{				
				long minAccesTime = Long.MAX_VALUE;
				for (int i = 0; i < pages.size(); i++) {
					 DataPage dataPage = pages.valueAt(i);
					 if(dataPage.LasAccessTime <= minAccesTime){
						 page = dataPage;
						 minAccesTime = dataPage.LasAccessTime;
					 }
				}
				pages.remove(page.Index);
				pages.put(pageIdx, page);
			}
		}
		
		page.Index = pageIdx;
		page.LasAccessTime = System.nanoTime();
		return page;
	}

	
	/* (non-Javadoc)
	 * @see com.enterlib.conetivity.IPagedCursor#loadPageAsync(int, com.enterlib.conetivity.OnPageLoadedListener)
	 */
	@Override
	public void loadPageAsync(final int position, final OnPageLoadedListener pageLoadedListener){
		if(loaderHandler == null){
			loaderHandler = new LoaderHandler();
			loaderHandler.setAutofinish(false);
		}
						
		loaderHandler.postTask(new LoadTask() {
			
			@Override
			public Object runAsync(Object taskArgs) throws Exception {													
				int pageIdx = getPageIndex(position);
				LoadPageResult<T> result = new LoadPageResult<T>();
				loadPage(pageIdx, requestFilter, requestOrderBy, result);
				return result;
			}
			
			@SuppressWarnings("unchecked")
			@Override
			public void onComplete(Object result, Exception e) {
				if(result!=null){
					LoadPageResult<T> r = (LoadPageResult<T>) result;
					requestFilter = r.requestFilter;
					requestOrderBy = r.requestOrderBy;
					count = r.count;
					if(r.count <= 0){
						currentPageIndex = -1;
						current = null;
						nbPages = 0;
						return;
					}else{
						currentPageIndex = r.pageIdx;
						nbPages = r.nbPages;
						current = findPage(r.pageIdx);
						current.Data = r.elements;
					}
				}
				if(pageLoadedListener!=null)
					pageLoadedListener.onPageLoaded(e);				
			}
		});
	}
	
	class FilterHandler extends ConditionFilterHandler {

		@Override
		protected FilterResults performFiltering(ArrayList<FilterCondition> constraints) {
			LoadPageResult<T> args = new LoadPageResult<T>();

			String requestFilter = client.buildWhere(FilterCondition.getFilterExpString(constraints, 0, constraints.size()));
			loadPage(0, requestFilter, requestOrderBy, args);
			
			FilterResults result = new FilterResults();
			result.values = args;
			result.count = args.count;
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(ArrayList<FilterCondition> constraints, FilterResults results) {

			pages.clear();
			LoadPageResult<T> data = (LoadPageResult<T>) results.values;
            if(data == null)
            	return;
            
			count = data.count;
			requestFilter = data.requestFilter;
			nbPages = count / pageSize + ((count % pageSize) > 0 ? 1 : 0);			

			pages.clear();
			if(count == 0){
				currentPageIndex = -1;				
				current = null;
			}
			else{
				currentPageIndex = 0;				
				current = findPage(0);						
				current.Data =  data.elements;
			}
						
			notifyDataChange();
		}
	}

	@Override
	public void sort(ArrayList<SortingElement> sorts, final OnSortingListener listener) {
        String sortExp;
		if(sorts == null || sorts.size() == 0){
			sortExp = null;
		}else {
			sortExp = StringUtils.aggregate(sorts, ", ", 0, sorts.size());
		}

		if(sortHandler == null){
			sortHandler = new SortHandler();
		}

		sortHandler.filter(sortExp, new Filter.FilterListener() {
			@Override
			public void onFilterComplete(int count) {
				if(listener !=null){
					listener.onSortingCompleted();
				}
			}
		});


	}

	class SortHandler extends Filter {

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			LoadPageResult<T> args = new LoadPageResult<T>();

			String sortExpression = null;
			if(!TextUtils.isEmpty(constraint)) {
				sortExpression = client.buildOrderBy(constraint.toString());
			}

			loadPage(0, requestFilter, sortExpression,  args);

			FilterResults result = new FilterResults();
			result.values = args;
			result.count = args.count;
			return result;
		}


		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			pages.clear();
			LoadPageResult<T> data = (LoadPageResult<T>) results.values;
			if(data == null)
				return;

			count = data.count;
            requestOrderBy = data.requestOrderBy;

			nbPages = count / pageSize + ((count % pageSize) > 0 ? 1 : 0);

			pages.clear();

			if(count == 0){
				currentPageIndex = -1;
				current = null;
			}
			else{
				if(currentPageIndex == -1)
					currentPageIndex = 0;
				current = findPage(currentPageIndex);
				current.Data =  data.elements;
			}

			notifyDataChange();
		}
	}

		@Override
	public  Iterator<T> iterator(){
		return new EntityCursorIterator<T>(this);
	}


}
