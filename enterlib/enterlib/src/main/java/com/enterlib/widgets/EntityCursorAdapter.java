package com.enterlib.widgets;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.enterlib.R;
import com.enterlib.app.IFilterPredicate;
import com.enterlib.app.IFilterableAdapter;
import com.enterlib.data.IPagedCursor;
import com.enterlib.data.ISortable;
import com.enterlib.data.OnPageLoadedListener;
import com.enterlib.data.IDataChangeNotify;
import com.enterlib.data.IDataChangeNotify.IDataChangeListener;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.OnGetItemListener;
import com.enterlib.data.OnSortingListener;
import com.enterlib.data.SortingElement;
import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.FilterListener;
import com.enterlib.filtering.IFilterable;
import com.enterlib.filtering.StringFilterCondition;

public class EntityCursorAdapter<T> extends BaseAdapter implements
		IFilterable, IDataChangeListener, OnPageLoadedListener, IFilterableAdapter, ISortable {

	private IEntityCursor<T> mCursor;
	private LayoutInflater mInflater;
	private int mLayout;
	private View loadingView;
    private int pageLoadingIdx = -1;
	private Class<? extends View> loadingViewClass;
	
	public EntityCursorAdapter(Context context, int resLayout, IEntityCursor<T> cursor) {
		mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mLayout = resLayout;
		mCursor = cursor;
	}
	
	public void setLoadingView(View view){
		this.loadingView = view;
	}
	
	private View createDefaultLoadingView(){
		View view = mInflater.inflate(R.layout.layout_load_item, null);
		loadingViewClass = view.getClass();
		return view;
	}

	@Override
	public int getCount() {
		if(mCursor instanceof IPagedCursor<?>) {
			IPagedCursor<T> pagedCursor = (IPagedCursor<T>) mCursor;
			if(!pagedCursor.isLoaded()){
				pagedCursor.loadPageAsync(0, this);
				pageLoadingIdx = 0;
				return 0;
			}
		}
		return mCursor.getCount();
	}

	@Override
	public Object getItem(int position) {
		if(mCursor instanceof IPagedCursor<?>) {
			IPagedCursor<T> pagedCursor = (IPagedCursor<T>) mCursor;
			if(!pagedCursor.isLoaded(position)){
				return null;
			}
		}

		return mCursor.getItem(position);
	}

//	public void getItem(int position, OnGetItemListener listener){
//		if(mCursor instanceof IPagedCursor<?>) {
//			IPagedCursor<T> pagedCursor = (IPagedCursor<T>) mCursor;
//			if(pagedCursor.isLoaded(position)) {
//				T item = pagedCursor.getItem(position);
//				listener.onItemResolved(item);
//			}else{
//				int elementPage = pagedCursor.getPageIndex(position);
//				if(pageLoadingIdx != elementPage)
//					pagedCursor.loadPageAsync(position, this);
//				pageLoadingIdx = elementPage;
//
//			}
//		}
//	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = mInflater.inflate(mLayout, parent, false);
		}

		if(mCursor instanceof IPagedCursor<?>){
			IPagedCursor<T> pagedCursor = (IPagedCursor<T>) mCursor;
			
			if(pagedCursor.isLoaded(position)){
				T item = pagedCursor.getItem(position);
				if(view.getClass() == loadingViewClass){
					view = mInflater.inflate(mLayout, parent, false);
				}
				updateView(view, item, position);
			}else{										
				int elementPage = pagedCursor.getPageIndex(position);
				if(pageLoadingIdx != elementPage)				
					pagedCursor.loadPageAsync(position, this);
				pageLoadingIdx = elementPage;
								
				if(loadingViewClass == null || view.getClass() != loadingViewClass)
					view = createDefaultLoadingView();
			}
		}
		else{
			T item = mCursor.getItem(position);
			updateView(view, item, position);
		}
		
		return view;
	}

	public View getView(int position, View convertView, ViewGroup parent, OnGetItemListener listener) {
		View view = null;
		if (convertView != null) {
			view = convertView;
		} else {
			view = mInflater.inflate(mLayout, parent, false);
		}

		if(mCursor instanceof IPagedCursor<?>){
			IPagedCursor<T> pagedCursor = (IPagedCursor<T>) mCursor;

			if(pagedCursor.isLoaded(position)){
				T item = pagedCursor.getItem(position);
				if(view.getClass() == loadingViewClass){
					view = mInflater.inflate(mLayout, null);
				}
				updateView(view, item, position);

				if(listener!=null){
					listener.onItemResolved(item);
				}

			}else{
				int elementPage = pagedCursor.getPageIndex(position);
				if(pageLoadingIdx != elementPage)
					pagedCursor.loadPageAsync(position, this);

				pageLoadingIdx = elementPage;

				if(loadingViewClass == null || view.getClass() != loadingViewClass)
					view = createDefaultLoadingView();
			}
		}
		else{
			T item = mCursor.getItem(position);
			updateView(view, item, position);

			if(listener!=null){
				listener.onItemResolved(item);
			}

		}

		return view;
	}

	protected void updateView(View view, T item, int position) {

	}

	@Override
	public void doFilter(ArrayList<FilterCondition> fixedConditions,
			final FilterListener listener) {

		if (mCursor instanceof IFilterable) {
			IFilterable filterable = (IFilterable) mCursor;

			filterable.doFilter(fixedConditions, new FilterListener() {
				@Override
				public void onFilterComplete(Object values, int count) {
					if(count <= 0){
						notifyDataSetInvalidated();
					}else {
						notifyDataSetChanged();
					}

					if(listener!=null)
						listener.onFilterComplete(values, count);
				}
			});
		}
	}

	@Override
	public void onDataChange(IDataChangeNotify sender) {
		// TODO Auto-generated method stub
		notifyDataSetChanged();
	}

	@Override
	public void onDataInvalid(IDataChangeNotify sender) {
		notifyDataSetInvalidated();

	}

	@Override
	public void onPageLoaded(Exception e) {
		notifyDataSetChanged();		
	}

	@Override
	public IFilterPredicate<?> getFilterPredicate() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFilterPredicate(IFilterPredicate<?> value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void filter(CharSequence constraint, final android.widget.Filter.FilterListener listener) {
		
		FilterCondition condition = new StringFilterCondition<T>("", "General Search");
		condition.setQueryValue(constraint);
		ArrayList<FilterCondition>fixedConditions = new ArrayList<FilterCondition>();
		fixedConditions.add(condition);

		
		doFilter(fixedConditions, new FilterListener() {
			@Override
			public void onFilterComplete(Object values, int count) {
				if(count <= 0){
					notifyDataSetInvalidated();
				}else {
					notifyDataSetChanged();
				}

				if(listener!=null)
					listener.onFilterComplete(count);
			}
		});
	}


	@Override
	public void sort(ArrayList<SortingElement> sorts, final OnSortingListener listener) {
		if(mCursor instanceof ISortable){
			((ISortable) mCursor).sort(sorts, new OnSortingListener() {
				@Override
				public void onSortingCompleted() {
					notifyDataSetChanged();
					if(listener!=null){
						listener.onSortingCompleted();
					}
				}
			});
		}
	}
}
