package com.enterlib.widgets;

import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SelectionCheckedState implements OnCheckedChangeListener {
	public interface OnSeletedChangedListener{
		void onSelectedChanged(SelectionCheckedState state);
	}
	
	public Object Item;
	public boolean Selected;
	public int Position;
	
	OnSeletedChangedListener selectedChangeListener;

	public SelectionCheckedState(Object item, int position, boolean selected) {
		super();
		Item = item;
		Selected = selected;
		Position = position;
	}

	@Override
	public int hashCode() {
		return Item.hashCode();
	}
	
	

	public void setSelectedChangeListener(
			OnSeletedChangedListener selectedChangeListener) {
		this.selectedChangeListener = selectedChangeListener;
	}

	@Override
	public boolean equals(Object o) {
		return Item.equals(((SelectionCheckedState) o).Item);
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		Selected = isChecked;
		if(selectedChangeListener!=null)
			selectedChangeListener.onSelectedChanged(this);
	}
}