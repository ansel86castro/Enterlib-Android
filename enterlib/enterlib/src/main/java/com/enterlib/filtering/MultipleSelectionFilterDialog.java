package com.enterlib.filtering;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.Filter.FilterListener;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.enterlib.R;
import com.enterlib.app.IFilterableAdapter;
import com.enterlib.widgets.ConditionFilterView;

public class MultipleSelectionFilterDialog extends AlertDialog implements
		TextWatcher, OnEditorActionListener, OnClickListener, FilterListener {

	public static interface OnSelectionListener {
		void onItemsSelected(List<CheckedState> selectedItems);
	}

	private ListAdapter mInnerAdapter;
	private EditText mSearchEdit;
	private ListView mListView;
	private OnSelectionListener onSelectionAcepterListener;
	private SparseArray<CheckedState> mCheckedArray = new SparseArray<CheckedState>();
	private AdapterPickListView mAdapter;
	private SearchViewFilterController mFilterController;
	private ConditionFilterView mFilterView;

	public void setOnSelectionListener(OnSelectionListener listener) {
		this.onSelectionAcepterListener = listener;
	}

	public MultipleSelectionFilterDialog(Context context, ListAdapter adapter,
			SearchViewFilterController filterController) {
		super(context);

		this.mInnerAdapter = adapter;

		if (mInnerAdapter != null) {
			mAdapter = new AdapterPickListView();
		}

		setButton(BUTTON_POSITIVE,
				context.getText(R.string.multiple_filter_dialog_accept), this);
		setButton(BUTTON_NEGATIVE,
				context.getText(R.string.multiple_filter_dialog_cancel), this);

		setIcon(0);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view;
		if (filterController != null) {
			view = inflater.inflate(R.layout.search_list_popup_extended, null);

			mFilterController = filterController;
			mFilterView = (ConditionFilterView) view
					.findViewById(R.id.searchView);
			mFilterController.setFilterView(mFilterView);
			mFilterController.setAdapter(mAdapter);
			mFilterView.setCondition(mFilterController.getSelectedCondition());

		} else {
			view = inflater.inflate(R.layout.search_list_popup, null);
			mSearchEdit = (EditText) view.findViewById(R.id.searchEdit);
			mSearchEdit.addTextChangedListener(this);
			mSearchEdit.setOnEditorActionListener(this);

			int hintColor = context.getResources().getColor(R.color.hint);
			mSearchEdit.setHintTextColor(hintColor);
		}

		setView(view);

		mListView = (ListView) view.findViewById(R.id.listItems);
		mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
		mListView.setAdapter(mAdapter);

		// keep backward compatibility
		if (adapter instanceof IFilterableAdapter && filterController == null) {
			((IFilterableAdapter) adapter).filter(null, null);
		}
	}

	public void setFilterCriteria(String value) {
		mSearchEdit.setHint(value);
	}

	public void setFilterCriteria(int resid) {
		mSearchEdit.setHint(resid);
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
	}

	@Override
	public void afterTextChanged(Editable s) {
		if (mInnerAdapter != null) {
			if (mInnerAdapter instanceof IFilterableAdapter) {
				String text = mSearchEdit.getText().toString();
				((IFilterableAdapter) mInnerAdapter).filter(text, this);
			}
		}

	}

	@Override
	public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_SEARCH) {
			// Handle search key click
			if (mInnerAdapter != null) {

				if (mInnerAdapter instanceof IFilterableAdapter) {
					String text = mSearchEdit.getText().toString();
					((IFilterableAdapter) mInnerAdapter).filter(text, this);
				}
			}
			InputMethodManager imm = (InputMethodManager) getContext()
					.getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
			return true;
		}
		return false;

	}

	@Override
	public void onFilterComplete(int count) {
		mCheckedArray.clear();
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case BUTTON_POSITIVE:
			invokeSelectionCallback();
			break;
		case BUTTON_NEGATIVE:
			break;
		}
	}

	private void invokeSelectionCallback() {
		if (onSelectionAcepterListener == null) {
			return;
		}

		ArrayList<CheckedState> selection = new ArrayList<MultipleSelectionFilterDialog.CheckedState>();
		int count = mCheckedArray.size();
		for (int i = 0; i < count; i++) {
			CheckedState state = mCheckedArray.valueAt(i);
			if (state.Selected) {
				selection.add(state);
			}
		}
		if (selection.size() > 0) {
			onSelectionAcepterListener.onItemsSelected(selection);
		}
	}

	public class CheckedState implements OnCheckedChangeListener {
		public Object Item;
		public boolean Selected;
		public int Position;

		public CheckedState(Object item, int position, boolean selected) {
			super();
			Item = item;
			Selected = selected;
			Position = position;
		}

		@Override
		public int hashCode() {
			return Item.hashCode();
		}

		@Override
		public boolean equals(Object o) {
			return Item.equals(((CheckedState) o).Item);
		}

		@Override
		public void onCheckedChanged(CompoundButton buttonView,
				boolean isChecked) {
			Selected = isChecked;
		}
	}

	class AdapterPickListView extends BaseAdapter implements IFilterable {

		LayoutInflater mInflater;

		public AdapterPickListView() {

			mInflater = LayoutInflater.from(getContext());
		}

		@Override
		public int getCount() {
			return mInnerAdapter.getCount();
		}

		@Override
		public Object getItem(int position) {
			return mInnerAdapter.getItem(position);
		}

		@Override
		public long getItemId(int position) {
			return mInnerAdapter.getItemId(position);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewGroup view;
			View innerView = null;
			FrameLayout frame;

			if (convertView == null) {
				view = (ViewGroup) mInflater.inflate(
						com.enterlib.R.layout.adapter_pick_list_view, null);
				frame = (FrameLayout) view
						.findViewById(com.enterlib.R.id.itemContent);
			} else {
				view = (ViewGroup) convertView;
				frame = (FrameLayout) view
						.findViewById(com.enterlib.R.id.itemContent);
				if (frame.getChildCount() > 0) {
					innerView = frame.getChildAt(0);
				}
			}

			if (innerView == null) {
				innerView = mInnerAdapter.getView(position, null, frame);
				frame.addView(innerView);
			} else {
				mInnerAdapter.getView(position, innerView, frame);
			}

			CheckBox cb = (CheckBox) view.findViewById(com.enterlib.R.id.cbSelect);
			cb.setOnCheckedChangeListener(null);

			Object item = mInnerAdapter.getItem(position);
			if(item == null){
				mCheckedArray.remove(position);
				cb.setChecked(false);
				cb.setOnCheckedChangeListener(null);
				return  view;
			}

			CheckedState state = mCheckedArray.get(position);
			if (state == null) {
				state = new CheckedState(item, position, false);
				mCheckedArray.append(position, state);
			}

			cb.setChecked(state.Selected);
			cb.setOnCheckedChangeListener(state);

			return view;
		}

		@Override
		public void doFilter(ArrayList<FilterCondition> fixedConditions,
				com.enterlib.filtering.FilterListener listener) {

			if (mInnerAdapter instanceof IFilterable) {
				((IFilterable) mInnerAdapter).doFilter(fixedConditions,
						listener);
			}

		}
	}

}
