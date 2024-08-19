package com.enterlib.widgets;

import com.enterlib.widgets.SelectionCheckedState.OnSeletedChangedListener;

import android.content.Context;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ListAdapter;

public class SelectionCheckedAdapter extends BaseAdapter {

	private LayoutInflater mInflater;	
	private ListAdapter mInnerAdapter;
	private SparseArray<SelectionCheckedState> mCheckedArray;
	private OnSeletedChangedListener selectedChangeListener;

	public SelectionCheckedAdapter(Context context, ListAdapter innerAdapter, SparseArray<SelectionCheckedState> checkedArray) {		
		mInflater = LayoutInflater.from(context);
		mInnerAdapter= innerAdapter;
		mCheckedArray = checkedArray;
	}
	
	public ListAdapter getInnerAdapter() {
		return mInnerAdapter;
	}

	public void setInnerAdapter(ListAdapter mInnerAdapter) {
		this.mInnerAdapter = mInnerAdapter;
	}

	public void setSelectedChangeListener(
			OnSeletedChangedListener selectedChangeListener) {
		this.selectedChangeListener = selectedChangeListener;
	}

	@Override
	public int getCount() {
		if (mInnerAdapter == null) {
			return 0;
		}
		return mInnerAdapter.getCount();
	}

	@Override
	public Object getItem(int position) {
		if (mInnerAdapter == null) {
			return null;
		}
		return mInnerAdapter.getItem(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewGroup view;
		View innerView = null;
		FrameLayout frame;
		if (convertView == null) {
			view = (ViewGroup) mInflater.inflate(com.enterlib.R.layout.adapter_pick_list_view, null);
			frame = (FrameLayout) view.findViewById(com.enterlib.R.id.itemContent);
		} else {
			view = (ViewGroup) convertView;
			frame = (FrameLayout) view.findViewById(com.enterlib.R.id.itemContent);
			if (frame.getChildCount() > 0) {
				innerView = frame.getChildAt(0);
			}
		}
		
		if (innerView == null) {
			innerView = mInnerAdapter.getView(position, null, frame);
			frame.addView(innerView);
		} else {
			innerView = mInnerAdapter.getView(position, innerView, frame);
		}

		final CheckBox cb = (CheckBox) view.findViewById(com.enterlib.R.id.cbSelect);

		Object item = mInnerAdapter.getItem(position);

		if(item == null){
			mCheckedArray.remove(position);
			cb.setOnCheckedChangeListener(null);
			cb.setChecked(false);
			return view;
		}

		SelectionCheckedState state = mCheckedArray.get(position);
		if (state == null) {
			state = new SelectionCheckedState(item, position, false);
			state.setSelectedChangeListener(selectedChangeListener);
			mCheckedArray.append(position, state);
		}

		cb.setOnCheckedChangeListener(null);
		cb.setChecked(state.Selected);
		cb.setOnCheckedChangeListener(state);

		/*
		 * if(newView && !innerView.isClickable()){
		 * innerView.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) {
		 * cb.setChecked(!cb.isChecked());
		 * 
		 * } }); }
		 */

		return view;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
}
