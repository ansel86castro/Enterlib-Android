package com.enterlib.databinding;

import com.enterlib.R;
import com.enterlib.data.IPagedCursor;
import com.enterlib.data.OnPageLoadedListener;
import com.enterlib.data.IDataChangeNotify;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.IDataChangeNotify.IDataChangeListener;
import com.enterlib.fields.ViewPagerField;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

public class AdapterPageEntityCursor extends BasePageAdapter implements IDataChangeListener, OnPageLoadedListener  {

	private IEntityCursor<Object> cursor;
	
	public AdapterPageEntityCursor(Context context, int layout, IEntityCursor<Object> cursor, ViewPagerField field, BindingResources bindingRes, 
			boolean asListView) {		
		super(context ,layout ,field, bindingRes, asListView);
		this.cursor =cursor;
	}

	@Override
	public int getCount() {		
		return cursor.getCount();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		View view = null;
		Object item = null;
		if(cursor instanceof IPagedCursor<?>){
			IPagedCursor<Object> pagedCursor = (IPagedCursor<Object>) cursor;
			
			if(!pagedCursor.isLoaded(position)){
				int elementPage = pagedCursor.getPageIndex(position);
				if(pageLoadingIdx != elementPage)				
					pagedCursor.loadPageAsync(position, this);
				
				pageLoadingIdx = elementPage;	
				
				view = createDefaultLoadingView();				
				computeSizeWeight(container, position, view);
				container.addView(view);	
				return view;
			}
		}
		
		item = cursor.getItem(position);
		return instantiateItem(container, position, item);	
	}
	
	@Override
	public void onDataChange(IDataChangeNotify sender) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onDataInvalid(IDataChangeNotify sender) {
		
	}

	@Override
	public void onPageLoaded(Exception e) {
		notifyDataSetChanged();		
	}

	protected View createDefaultLoadingView() {
		View view = inflater.inflate(R.layout.layout_load_item, null);	
		return view;
	}

}
