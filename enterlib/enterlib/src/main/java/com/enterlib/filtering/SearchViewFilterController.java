package com.enterlib.filtering;

import android.content.Context;
import android.view.View;

import com.enterlib.fields.ListField;
import com.enterlib.widgets.ConditionFilterView;

public class SearchViewFilterController extends FilterController {

	public static interface ISearchListener {

		void enterSearchMode(View searchView);

		boolean isInSearchMode();

		void exitSearchMode();
	}

	ConditionFilterView mFilterView;
	ISearchListener searchContainer;

	public SearchViewFilterController(Context context,
			ISearchListener searchContainer, FilterCondition... conditions) {
		super(context, conditions);

		this.searchContainer = searchContainer;
	}

	public SearchViewFilterController(Context context,
			ISearchListener searchContainer, 
			ListField field,
			FilterCondition... conditions) {
		super(context, field, conditions);

		this.searchContainer = searchContainer;
	}

	public ConditionFilterView getFilterView() {
		return mFilterView;
	}

	public void setFilterView(ConditionFilterView _filterView) {
		this.mFilterView = _filterView;
		this.mFilterView.setController(this);
	}

	@Override
	public void onConditionSelected(FilterCondition condition) {
		super.onConditionSelected(condition);

		if (searchContainer == null) {
			return;
		}

		if (mConditionDialog != null && mConditionDialog.isShowing()) {
			mConditionDialog.dismiss();
			mConditionDialog = null;
		}

		mFilterView.setCondition(condition);
		searchContainer.enterSearchMode(mFilterView);
	}

	public void show() {
		if (mConditions.size() == 1) {
			onConditionSelected(mConditions.get(0));
		} else {
			showConditionsDialog();
		}
	}

	@Override
	public void resetConditions() {
		mFilterView.clearValue();

		super.resetConditions();
	}

}
