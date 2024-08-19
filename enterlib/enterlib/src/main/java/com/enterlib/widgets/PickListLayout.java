package com.enterlib.widgets;

import java.util.List;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import com.enterlib.R;
import com.enterlib.filtering.MultipleSelectionFilterDialog;
import com.enterlib.filtering.SearchViewFilterController;

public class PickListLayout extends FrameLayout implements OnClickListener,
		MultipleSelectionFilterDialog.OnSelectionListener,
		OnCheckedChangeListener {

	class CheckedState implements OnCheckedChangeListener {
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

	private SparseArray<CheckedState> mCheckedArray = new SparseArray<CheckedState>();
	private LinearLayout mLinearLayout;

	// private ArrayList<Object> mItems;

	// The list adapter
	private AdapterPickListView mAdapter;

	// the custom items adapter
	private ListAdapter mInnerAdapter;

	// the searchAdapter
	private ListAdapter mSearchAdapter;

	private MultipleSelectionFilterDialog mDialog;

	private OnDataChangeListener onDataChangeListener;
	private ImageButton btnAdd;
	private ImageButton btnDel;
	private IModelStateObserver mModelStateManager;
	private CheckBox cbSelecteAll;
	private SearchViewFilterController mFilterController;

	public PickListLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public PickListLayout(Context context) {
		this(context, null);
	}

	public PickListLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(com.enterlib.R.layout.layout_pick_list_panel, this, false);
		addView(view);
		
		if(!isInEditMode()){
			cbSelecteAll = (CheckBox) view.findViewById(R.id.checkBox1);
			cbSelecteAll.setOnCheckedChangeListener(this);
	
			mLinearLayout = (LinearLayout) view
					.findViewById(com.enterlib.R.id.container);
			btnAdd = (ImageButton) view.findViewById(com.enterlib.R.id.btnAdd);
			btnAdd.setOnClickListener(this);
			btnAdd.setEnabled(false);
	
			btnDel = (ImageButton) view.findViewById(com.enterlib.R.id.btnDelete);
			btnDel.setOnClickListener(this);
	
			btnDel.setEnabled(false);
		}else{
//			setItemsAdapter(new CollectionAdapter<Object>(context,
//					android.R.layout.simple_list_item_1, new String[] {
//							"Item 1", "Item 2", "Item 3" }));
		}		
	}

	public void setFilterController(SearchViewFilterController filterController) {
		mFilterController = filterController;
	}

	public SearchViewFilterController getFilterController() {
		return mFilterController;
	}

	@Override
	public void setEnabled(boolean enabled) {
		btnAdd.setEnabled(enabled);
		btnAdd.setClickable(enabled);
		btnAdd.setVisibility(enabled ? VISIBLE : GONE);

		btnDel.setEnabled(enabled);
		btnDel.setClickable(enabled);
		btnDel.setVisibility(enabled ? VISIBLE : GONE);

		cbSelecteAll.setEnabled(enabled);
		cbSelecteAll.setClickable(enabled);
		cbSelecteAll.setVisibility(enabled ? VISIBLE : GONE);

		super.setEnabled(enabled);

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public IModelStateObserver getModelStateManager() {
		return mModelStateManager;
	}

	public void setModelStateManager(IModelStateObserver mModelStateManager) {
		this.mModelStateManager = mModelStateManager;
	}

	public void setItemsAdapter(ListAdapter adapter) {
		mInnerAdapter = adapter;
		mAdapter = new AdapterPickListView();
		mAdapter.registerDataSetObserver(new DataSetObserver() {
			@Override
			public void onChanged() {
				createItemViews();
			}
		});
		createItemViews();

		if (adapter instanceof OnDataChangeListener) {
			setOnDataChangeListener((OnDataChangeListener) adapter);
		}
	}
	
	public ListAdapter getItemsAdapter(){
		return mInnerAdapter;
	}

	private void createItemViews() {
		mLinearLayout.removeAllViews();

		if (mAdapter == null) {
			return;
		}

		int count = mAdapter.getCount();
		for (int i = 0; i < count; i++) {
			View child = mAdapter.getView(i, null, mLinearLayout);
			mLinearLayout.addView(child);
		}
		mLinearLayout.invalidate();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int count = mCheckedArray.size();
		for (int i = 0; i < count; i++) {
			CheckedState item = mCheckedArray.valueAt(i);
			item.Selected = isChecked;
		}
		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}

	}

	public void setSearchAdapter(ListAdapter adapter) {
		this.mSearchAdapter = adapter;

		btnAdd.setEnabled(adapter != null);
	}

	public void setOnDataChangeListener(OnDataChangeListener listener) {
		this.onDataChangeListener = listener;
		btnDel.setEnabled(listener != null);
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.btnAdd) {
			showPickDialog();
		} else if (id == R.id.btnDelete) {
			removeSelectedItems();
		}

	}

	public void removeSelectedItems() {
		if (onDataChangeListener == null) {
			return;
		}

		int count = mCheckedArray.size();
		for (int i = 0; i < count; i++) {
			CheckedState state = mCheckedArray.valueAt(i);
			if (state.Selected) {

				if (mModelStateManager != null) {
					if (mModelStateManager.onRemoved(state.Item)) {
						onDataChangeListener.onRemove(state.Item,state.Position);
					}
				} else {
					onDataChangeListener.onRemove(state.Item, state.Position);
				}

				state.Selected = false;
			}
		}
		mCheckedArray.clear();

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	private void showPickDialog() {		
		if (mSearchAdapter != null) {
			if (mDialog != null && mDialog.isShowing()) {
				mDialog.dismiss();
			}

			mDialog = new MultipleSelectionFilterDialog(getContext(),
					mSearchAdapter, mFilterController);
			mDialog.setOnSelectionListener(this);
			mDialog.show();
		}		
	}
	
	@Override
	public void onItemsSelected(List<MultipleSelectionFilterDialog.CheckedState> selectedItems) {
		if (onDataChangeListener == null) {
			return;
		}

		for (int i = 0; i < selectedItems.size(); i++) {
			Object item = selectedItems.get(i).Item;

			if (mModelStateManager != null) {
				if (mModelStateManager.onAdded(item)) {
					onDataChangeListener.onAdd(item);
				}
			} else {
				onDataChangeListener.onAdd(item);
			}
		}

		if (mAdapter != null) {
			mAdapter.notifyDataSetChanged();
		}
	}

	public class AdapterPickListView extends BaseAdapter {

		LayoutInflater mInflater;

		public AdapterPickListView() {

			mInflater = LayoutInflater.from(getContext());
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
		public long getItemId(int position) {
			if (mInnerAdapter == null) {
				return 0;
			}
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
				innerView = mInnerAdapter.getView(position, innerView, frame);
			}

			Object item = mInnerAdapter.getItem(position);
			CheckedState state = mCheckedArray.get(position);
			if (state == null) {
				state = new CheckedState(item, position, false);
				mCheckedArray.append(position, state);
			}

			final CheckBox cb = (CheckBox) view
					.findViewById(com.enterlib.R.id.cbSelect);
			cb.setOnCheckedChangeListener(null);
			cb.setChecked(state.Selected);

			cb.setOnCheckedChangeListener(state);
			if (!PickListLayout.this.isEnabled()) {
				cb.setVisibility(GONE);
				cb.setClickable(false);
			} else {
				cb.setVisibility(VISIBLE);
				cb.setClickable(true);
			}

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
	}

}
