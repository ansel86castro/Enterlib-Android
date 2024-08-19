package com.enterlib.widgets;

import com.enterlib.widgets.MultipleSelectionView.OnMultipleSelectionListener;

public interface ISelectionView {

	void setOnSelectionListener(OnMultipleSelectionListener onSelectionListener);

	void updateSelectedItems(int count);	

}