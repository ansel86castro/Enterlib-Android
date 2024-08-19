package com.enterlib.app;

import android.widget.ListAdapter;
import android.widget.SpinnerAdapter;
import android.widget.Filter.FilterListener;

/**
 * Defines operations that and adapter must implements in order to allow
 * customize the display value for each item
 * */
public interface IFilterableAdapter extends ListAdapter, SpinnerAdapter {

	IFilterPredicate<?> getFilterPredicate();

	void setFilterPredicate(IFilterPredicate<?> value);
	
	void filter(CharSequence constraint, FilterListener listener);

}
