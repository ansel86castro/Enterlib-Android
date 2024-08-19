package com.enterlib.filtering;

import android.content.Context;
import android.os.Bundle;

import com.enterlib.fields.ListField;

public class DialogFilterController extends FilterController {

	private ConditionFilterDialog mFilterDialog;

	public DialogFilterController(Context context,
			FilterCondition... conditions) {
		super(context, conditions);
	}

	public DialogFilterController(Context context, ListField field,
			FilterCondition... conditions) {
		super(context, field, conditions);

	}

	public void showDialog() {
		if (mConditions.size() == 1) {
			onConditionSelected(mConditions.get(0));
		} else {
			showConditionsDialog();
		}
	}

	@Override
	public void onConditionSelected(FilterCondition c) {
		super.onConditionSelected(c);

		if (mFilterDialog != null && mFilterDialog.isShowing()) {
			mFilterDialog.dismiss();
			mFilterDialog = null;
		}

		if (mConditionDialog != null && mConditionDialog.isShowing()) {
			mConditionDialog.dismiss();
			mConditionDialog = null;
		}

		if (mListField != null && mListField.getAdapter() != null) {
			mFilterDialog = new ConditionFilterDialog(mContext, c, this,
					mListField.getAdapter(), mListField.getSelectedIndex());
			mFilterDialog.setOnSelectedItemlistener(mOnSelectedItemlistener);
			mFilterDialog.show();
		} else if (mAdapter != null) {
			mFilterDialog = new ConditionFilterDialog(mContext, c, this,
					mAdapter, mAdapterSelectedPosition);
			mFilterDialog.setOnSelectedItemlistener(mOnSelectedItemlistener);
			mFilterDialog.show();
		}
	}

	@Override
	public void saveState(Bundle bundle) {
		super.saveState(bundle);

		if (mFilterDialog != null) {
			if (mFilterDialog.isShowing()) {
				bundle.putBoolean(SHOWING_FILTERING, mFilterDialog.isShowing());
				mFilterConditionindex = mFilterDialog.getCondition().index;
				bundle.putInt(SHOWING_FILTERING_INDX, mFilterConditionindex);
			} else {
				bundle.putBoolean(SHOWING_FILTERING, false);
				mFilterConditionindex = -1;
				bundle.putInt(SHOWING_FILTERING_INDX, mFilterConditionindex);
			}
		}
	}

	@Override
	public void restoreFilterView() {
		if (mFilterConditionindex < 0) {
			if (mFilterDialog != null && mFilterDialog.isShowing()) {
				mFilterDialog.dismiss();
			}
			return;
		}

		super.restoreFilterView();
	}
}
