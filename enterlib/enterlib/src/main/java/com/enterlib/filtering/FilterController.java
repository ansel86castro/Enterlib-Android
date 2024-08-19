package com.enterlib.filtering;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;
import android.widget.ListView;

import com.enterlib.R;
import com.enterlib.app.CollectionAdapter;
import com.enterlib.widgets.EntityCursorAdapter;
import com.enterlib.fields.ListField;
import com.enterlib.filtering.ConditionFilterDialog.OnItemSelectedListener;
import com.enterlib.filtering.ConditionsAdapter.IConditionStateCallback;
import com.enterlib.serialization.JSonSerializer;

public abstract class FilterController implements FilterListener,
		IConditionStateCallback {

	protected final static String SHOWING_CONDITIONS = "SHOWING_CONDITIONS";
	protected final static String SHOWING_FILTERING = "SHOWING_FILTERING";
	protected final static String SHOWING_FILTERING_INDX = "SHOWING_FILTERING_INDX";

	protected ArrayList<FilterCondition> mConditions;
	protected ArrayList<FilterCondition> mActiveConditions = new ArrayList<FilterCondition>();
	protected ArrayList<FilterCondition> mInvokeConditions = new ArrayList<FilterCondition>();
	protected AlertDialog mConditionDialog;
	protected Context mContext;
	protected android.widget.ListAdapter mAdapter;
	protected ListField mListField;
	private IFilterable mFilterCallback;
	protected OnItemSelectedListener mOnSelectedItemlistener;
	protected int mFilterConditionindex = -1;
	protected int mAdapterSelectedPosition;
	private ExternalFilterListener mExternalFilterListener = new ExternalFilterListener();
	private FilterCondition mSelectedCondition;
    private FilterListener filterCompletedListener;
	private IStopWordContainer stopWordContainer;

	public class ExternalFilterListener implements FilterListener {

		@Override
		public void onFilterComplete(Object values, int count) {
            android.widget.ListAdapter adapter = getAdapter();
            if (adapter != null) {
                if (adapter instanceof CollectionAdapter<?>) {
                    CollectionAdapter<?> cAdapter = (CollectionAdapter<?>) adapter;
                    if (count < 0) {
                        cAdapter.clear();
                        cAdapter.notifyDataSetInvalidated();
                    } else {
                        cAdapter.reset(values);
                    }
                } else if (adapter instanceof EntityCursorAdapter<?>) {
                    EntityCursorAdapter<?> cursorAdapter = (EntityCursorAdapter<?>) adapter;
                    if (count < 0) {
                        cursorAdapter.notifyDataSetInvalidated();
                    } else {
                        cursorAdapter.notifyDataSetChanged();
                    }
                }
            }

            if (filterCompletedListener != null) {
                filterCompletedListener.onFilterComplete(values, count);
            }
        }
	}

	public FilterController() {
		super();
	}

	public FilterController(Context context, ListField field, IStopWordContainer stopWordContainer, FilterCondition... conditions){
		this(context, field, conditions);
		setStopWordContainer(stopWordContainer);
	}

	public FilterController(Context context, ListField field, FilterCondition... conditions) {
		this.mContext = context;
		this.mListField = field;

		this.mConditions = new ArrayList<FilterCondition>(conditions.length);
		for (int i = 0; i < conditions.length; i++) {
			FilterCondition c = conditions[i];
			c.index = i;
			this.mConditions.add(c);
			if (c.isActive) {
				mActiveConditions.add(c);
			}
		}
	}

	public FilterController(Context context, IStopWordContainer stopWordContainer, FilterCondition... conditions){
		this(context, conditions);
		setStopWordContainer(stopWordContainer);
	}

	public FilterController(Context context, FilterCondition... conditions) {
		this.mContext = context;
		this.mConditions = new ArrayList<FilterCondition>(conditions.length);
		for (int i = 0; i < conditions.length; i++) {
			FilterCondition c = conditions[i];
			c.index = i;
			this.mConditions.add(c);
			if (c.isActive) {
				mActiveConditions.add(c);
			}
		}
	}

	public IStopWordContainer getStopWordContainer() {
		return stopWordContainer;
	}

	public void setStopWordContainer(IStopWordContainer stopWordContainer) {
		this.stopWordContainer = stopWordContainer;
		if(stopWordContainer!=null){
			for (int i = 0; i < mConditions.size(); i++) {
				mConditions.get(i).setStopWordContainer(stopWordContainer);
			}
		}
	}

	public FilterCondition getSelectedCondition() {
		if (mSelectedCondition == null && mConditions.size() == 1) {
			mSelectedCondition = mConditions.get(0);
		}
		return mSelectedCondition;
	}

    public FilterController setFilterCompleteListener(FilterListener listener){
        this.filterCompletedListener = listener;
        return this;
    }

	public void setAdapter(android.widget.ListAdapter value) {
		this.mAdapter = value;
	}

	public android.widget.ListAdapter getAdapter() {
		return mListField != null ? mListField.getAdapter() : mAdapter;
	}

	public void setAdapterSelectedPosition(int value) {
		this.mAdapterSelectedPosition = value;
	}

	public int getConditionsCount() {
		return mConditions.size();
	}

	public int getActiveConditionsCount() {
		return mActiveConditions.size();
	}

	public FilterCondition getCondition(int idx) {
		return mConditions.get(idx);
	}

	public FilterCondition getActiveCondition(int idx) {
		return mActiveConditions.get(idx);
	}

	public ListField getListField() {
		return mListField;
	}

	public ArrayList<FilterCondition> getFilterConditions(){
        return mInvokeConditions;
	}

	public FilterController setListField(ListField field) {
		this.mListField = field;
		return this;
	}

	public FilterController setFilterCallback(IFilterable filterCallback) {
		this.mFilterCallback = filterCallback;
		return this;
	}

	public FilterController setOnSelectedItemlistener(
			ConditionFilterDialog.OnItemSelectedListener onSelectedItemlistener) {
		this.mOnSelectedItemlistener = onSelectedItemlistener;
		return this;
	}

	@Override
	public void onFilterComplete(Object values, int count) {
        if (filterCompletedListener != null) {
            filterCompletedListener.onFilterComplete(values, count);
        }
	}

	public void setActive(String queryName, boolean active) {
		FilterCondition item = null;
		for (int i = 0; i < mConditions.size(); i++) {
			FilterCondition c = mConditions.get(i);
			if (c.queryName.equals(queryName)) {
				item = c;
				break;
			}
		}
		if (item != null) {
			if (active && !item.isActive) {
				item.isActive = true;
				mActiveConditions.add(item);
			} else if (!active && item.isActive) {
				item.isActive = false;
				mActiveConditions.remove(item);
			}
		}
	}

	@Override
	public void setActive(FilterCondition item, boolean active) {
		if (active && !item.isActive) {
			item.isActive = true;
			mActiveConditions.add(item);
		} else if (!active && item.isActive) {
			item.isActive = false;
			mActiveConditions.remove(item);
		}
	}

	public void performFiltering(FilterCondition c) {
		mInvokeConditions.clear();
		if (c != null && !c.isActive) {
			mInvokeConditions.add(c);
			mInvokeConditions.addAll(mActiveConditions);
		} else {
			mInvokeConditions.addAll(mActiveConditions);
		}

		onFilter(mInvokeConditions);
	}



	protected void onFilter(ArrayList<FilterCondition> conditions) {
		if (mFilterCallback != null) {
			mFilterCallback.doFilter(conditions, mExternalFilterListener);
		} else if (mListField != null) {
			Adapter adapter = mListField.getAdapter();
			if (adapter instanceof IFilterable) {
				((IFilterable) adapter).doFilter(conditions, this);
			}
		} else if (this.mAdapter != null
				&& this.mAdapter instanceof IFilterable) {
			((IFilterable) this.mAdapter).doFilter(conditions, this);
		}
	}

	public void showConditionsDialog() {
		if (mConditionDialog != null && mConditionDialog.isShowing()) {
			mConditionDialog.dismiss();
			mConditionDialog = null;
		}

		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.dialog_search_conditions, null);

		ListView listView = (ListView) view.findViewById(R.id.listItems);
		listView.setAdapter(new ConditionsAdapter(mContext, this, mConditions));

		mConditionDialog = builder
				.setTitle(R.string.dialog_conditions_title)
				.setIcon(R.drawable.ic_dialog_filter)
				.setView(view)
				.setPositiveButton(R.string.dialog_contition_searchbutton,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								performFiltering(null);
							}
						})
				.setNeutralButton(R.string.dialog_contition_resetbutton,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								resetConditions();
							}
						})
				.setNegativeButton(R.string.dialog_contition_closebutton,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								mConditionDialog.dismiss();
							}
						}).show();
	}

	public void resetConditions() {
		for (int i = 0; i < mConditions.size(); i++) {
			FilterCondition c = mConditions.get(i);
			if(c.getField()!=null){
				c.getField().setValue(null);
			}
			c.isActive = false;
			c.queryValue = null;
		}
		mActiveConditions.clear();
		mInvokeConditions.clear();

		onFilter(mInvokeConditions);
	}

	public void update() {
		for (int i = 0; i < mConditions.size(); i++) {
			mConditions.get(i).update();
		}
	}

	public void saveState(Bundle bundle) {
		JSonSerializer serializer = new JSonSerializer();
		for (FilterCondition filterCondition : mConditions) {
			filterCondition.saveState(bundle, serializer);
		}
		if (mConditionDialog != null) {
			bundle.putBoolean(SHOWING_CONDITIONS, mConditionDialog.isShowing());
		}

	}

	public void restoreState(Bundle bundle) {
		JSonSerializer serializer = new JSonSerializer();
		mActiveConditions.clear();

		for (FilterCondition filterCondition : mConditions) {
			filterCondition.restoreState(bundle, serializer);
			if (filterCondition.isActive) {
				mActiveConditions.add(filterCondition);
			}
		}

		if (bundle.getBoolean(SHOWING_FILTERING, false)) {
			mFilterConditionindex = bundle.getInt(SHOWING_FILTERING_INDX, -1);
		}

		if (bundle.getBoolean(SHOWING_CONDITIONS, false)) {
			showConditionsDialog();
		}
	}

	public void restoreFilterView() {

		FilterCondition c = mConditions.get(mFilterConditionindex);
		mFilterConditionindex = -1;
		onConditionSelected(c);
	}

	@Override
	public void onConditionSelected(FilterCondition condition) {
		mSelectedCondition = condition;
	}
}