package com.enterlib.fields;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.enterlib.app.CollectionAdapter;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.CommandHandlerItemClick;
import com.enterlib.databinding.DependencyObject;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.mvvm.SelectionCommand;
import com.enterlib.serialization.IStringSerializer;
import com.enterlib.widgets.ISelectionView;
import com.enterlib.widgets.MultipleSelectionView.OnMultipleSelectionListener;
import com.enterlib.widgets.OnConfirmResultListener;
import com.enterlib.widgets.OnSelectionConfirmListener;
import com.enterlib.widgets.SelectionCheckedAdapter;
import com.enterlib.widgets.SelectionCheckedState;
import com.enterlib.widgets.SelectionCheckedState.OnSeletedChangedListener;

public class ListField extends ItemsField 
	implements OnSeletedChangedListener, OnMultipleSelectionListener , OnConfirmResultListener {

	public static final BindingProperty<ListField> ItemClickCommandProperty = registerProperty(
			ListField.class,
			new BindingProperty<ListField>("ItemClickCommand") {

				@Override
		public void set(ListField object, ExpressionMember member,
						BindingResources dc) {
			ListCommandHandlerClick handler = new ListCommandHandlerClick(member.getValueString());
			object.addBindingHandler(handler);
			object.commandHandlerClick = handler;
		}

		@Override
		public Object get(DependencyObject object) {			
			return ((ListField) object).commandHandlerClick.getCommand();
		}
	});

	static class ListCommandHandlerClick extends CommandHandlerItemClick {

		public ListCommandHandlerClick(String sourceProperty) {
			super(sourceProperty);
		}

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			if (!enableItemClick) {
				return;
			}

			ListField listView = (ListField) field;
			listView.selectedIndex = position;
			super.onItemClick(parent, view, position, id);
		}
	}

	int selectedIndex = -1;
	private AdapterView.OnItemClickListener onItemClickListener;
	protected ListCommandHandlerClick commandHandlerClick;
	private boolean selectionModeEnabled;
	private SparseArray<SelectionCheckedState> mCheckedArray;
	private int selectedCount;
	private ISelectionView selectionView;
	private AdapterView<ListAdapter> adapterView;
	
	private OnSelectionConfirmListener onSelectionConfirmListener;
	
	public ListField() {
		setRestorable(false);
	}

	public ListField(AdapterView<? extends ListAdapter> view, boolean required) {
		super(view, required);
		setRestorable(false);
		
		this.adapterView = (AdapterView<ListAdapter>) view;
	}

	public ListField(AdapterView<? extends ListAdapter> view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);
		setRestorable(false);
		
		this.adapterView = (AdapterView<ListAdapter>) view;
	}

	public ListField(AdapterView<? extends ListAdapter> view, String display, boolean required) {
		super(view, display, required);
		setRestorable(false);
		
		this.adapterView = (AdapterView<ListAdapter>) view;
	}

	public ListField(AdapterView<? extends ListAdapter> view, String valueBinding) {
		super(view, valueBinding);
		setRestorable(false);
		
		this.adapterView = (AdapterView<ListAdapter>) view;
	}

	public ListField(AdapterView<? extends ListAdapter> view) {
		super(view);
		setRestorable(false);
		this.adapterView = (AdapterView<ListAdapter>) view;
	}
	
	@Override
	protected void onViewChanged() {
		super.onViewChanged();
		this.adapterView = (AdapterView<ListAdapter>) getView();
				
	}

	
	public void setOnSelectionConfirmListener(
			OnSelectionConfirmListener onSelectionConfirmListener) {
		this.onSelectionConfirmListener = onSelectionConfirmListener;
	}

	public void enableSelectionMode(boolean value){
		if(selectionModeEnabled != value){
			selectionModeEnabled = value;					
			ListAdapter listAdapter = adapterView.getAdapter();								
			selectedCount = 0;
			
			if(listAdapter == null)
				return;
			
			if(selectionModeEnabled){
				mCheckedArray = new SparseArray<SelectionCheckedState>();
				SelectionCheckedAdapter selectionAdapter = new SelectionCheckedAdapter(getContext(), listAdapter, mCheckedArray);
				selectionAdapter.setSelectedChangeListener(this);
				int firstPosition = adapterView.getFirstVisiblePosition();
				adapterView.setAdapter(selectionAdapter);
				adapterView.setSelection(firstPosition);

			}else if(listAdapter instanceof SelectionCheckedAdapter){
				SelectionCheckedAdapter selectionAdapter = (SelectionCheckedAdapter) listAdapter;
				ListAdapter innerAdapter = selectionAdapter.getInnerAdapter();

				int firstPosition = adapterView.getFirstVisiblePosition();
				adapterView.setAdapter(innerAdapter);
				adapterView.setSelection(firstPosition);

				mCheckedArray = null;			
			}
			
			if(selectionView!=null){
				selectionView.updateSelectedItems(selectedCount);
			}
			
			if(selectedIndex >= 0){
				if(listAdapter!=null && selectedIndex >= listAdapter.getCount())
					selectedIndex = listAdapter.getCount() - 1;
				
				adapterView.setSelection(selectedIndex);
			}
			
			adapterView.invalidate();
		}
	}
		
	
	public void setSelectionView(ISelectionView selectionView) {
		this.selectionView = selectionView;
	}

	public boolean isSelectionModeEnable(){
		return selectionModeEnabled;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int index) {
		this.selectedIndex = index;
	}

	public SelectionCommand getSelectionCommand() {
		if (commandHandlerClick == null) {
			return null;
		}
		return commandHandlerClick.getCommand();
	}

	public void setOnItemClickListener(
			AdapterView.OnItemClickListener onItemClickListener) {
		this.onItemClickListener = onItemClickListener;
		if (onItemClickListener != null) {
			adapterView.setOnItemClickListener(
					new AdapterView.OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							selectedIndex = position;
							ListField.this.onItemClickListener.onItemClick(
									parent, view, position, id);
						}
					});
		} else {
			adapterView.setOnItemClickListener(null);
		}
	}

	public void setItems(Object[] values) {
		
		items = values;
		if (values == null) {
			setAdapter(null);
		}else {
            adapterView.setAdapter(new CollectionAdapter<Object>(
                    adapterView.getContext(),
                    android.R.layout.simple_list_item_single_choice, values));
        }

	}

	public void setItems(List<Object> values) {
		
		items = values;
		if (values == null) {
			setAdapter(null);
		}else {
            setAdapter(new CollectionAdapter<Object>(
                    adapterView.getContext(),
                    android.R.layout.simple_list_item_single_choice, values));
        }

		return;
	}

	@Override
	public void setAdapter(ListAdapter adapter) {

		if(selectionModeEnabled && adapter!=null){
			if(mCheckedArray == null){
				mCheckedArray = new SparseArray<SelectionCheckedState>();
				selectedCount = 0;
			}
			
			SelectionCheckedAdapter selectionAdapter = new SelectionCheckedAdapter(getContext(), adapter, mCheckedArray);
			selectionAdapter.setSelectedChangeListener(this);
			adapterView.setAdapter(selectionAdapter);
			
			if(selectionView!=null){
				selectionView.updateSelectedItems(selectedCount);
			}
		}
		else{
			adapterView.setAdapter(adapter);
		}

        if(selectedIndex >= 0 && adapter!=null) {
            int count = adapter.getCount();
            if(count <= 0){
                selectedIndex = 0;
            }
            else if(selectedIndex >= count){
                selectedIndex = count-1;
            }
            adapterView.setSelection(selectedIndex);
        }else {
            selectedIndex = -1;
        }
	}

	@Override
	protected Object getViewValue() {
		return items;
	}

	@Override
	protected void setViewValue(Object value) {
		this.items = value;
		if (adapterProvider != null) {
			adapterProvider.onItemsSet(this);
		}
	}

	@Override
	protected void onSetErrorMessage(String errorMessage) {

	}

	@Override
	public void saveState(Bundle outState, IStringSerializer serializer) {
		super.saveState(outState, serializer);

		String storeId = getStoreId();
		String selectedid = storeId + "_position";
		String displayId = storeId + "_display";

		// int listViewSelectedPosition =
		// getListView().getSelectedItemPosition();
		// int checkedPosition = getListView().getCheckedItemPosition();
		int topPosition = adapterView.getFirstVisiblePosition();

		outState.putInt(selectedid, selectedIndex);
		outState.putInt(displayId, topPosition);
	}

	@Override
	public void restoreState(Bundle savedInstanceState,
			IStringSerializer serializer) {
		super.restoreState(savedInstanceState, serializer);

		String storeId = getStoreId();
		String selectedId = storeId + "_position";
		String displayId = storeId + "_display";

		selectedIndex = savedInstanceState.getInt(selectedId, -1);
		if (selectedIndex < 0) {
			selectedIndex = savedInstanceState.getInt(displayId, -1);
		}

		
		ListAdapter adapter = adapterView.getAdapter();
		if (adapter == null) {
			return;
		}

		int itemCount = adapter.getCount();
		if (itemCount == 0) {
			selectedIndex = 0;
		} else if (selectedIndex >= itemCount) {
			selectedIndex = itemCount - 1;
		}

        if(selectedIndex >= 0)
		    adapterView.setSelection(selectedIndex);
	}

	public ListAdapter getAdapter() {
		
		return adapterView.getAdapter();
	}

//	public ListView getListView() {
//		return (ListView) getView();
//	}

	@Override
	public void onConfirmSelection() {		
		if(selectedCount <= 0)
			return;
		
		ArrayList<Object> items = new ArrayList<Object>(mCheckedArray.size());
		for (int i = 0; i < mCheckedArray.size(); i++) {
			SelectionCheckedState state = mCheckedArray.valueAt(i);
			if(state.Selected)
				items.add(state.Item);
		}									
		
		if(onSelectionConfirmListener!=null){
			onSelectionConfirmListener.onSelectionConfirm(items, this);
		}
		
	}

	@Override
	public void onSelectAll() {
		if(!selectionModeEnabled)
			throw new RuntimeException("You must enable the selecion mode first");
			
		SelectionCheckedAdapter adapter = (SelectionCheckedAdapter) adapterView.getAdapter();
		if(adapter == null)
			return;
		
		int firstPosition = adapterView.getFirstVisiblePosition();
		int endPosition = adapterView.getLastVisiblePosition();
		
		selectedCount = 0;
		mCheckedArray.clear();
		for (int i = firstPosition; i <= endPosition; i++) {
			Object item = adapter.getItem(i);
			SelectionCheckedState state = new SelectionCheckedState(item, i, true);
			state.setSelectedChangeListener(this);
			mCheckedArray.put(i, state);
			selectedCount++;
		}
		
		adapter.notifyDataSetChanged();
		if(selectionView!=null){
			selectionView.updateSelectedItems(selectedCount);
		}
		
	}

	@Override
	public void onClearSelection() {		
		SelectionCheckedAdapter adapter = (SelectionCheckedAdapter) adapterView.getAdapter();
		if(adapter == null)
			return;
		
		mCheckedArray.clear();
		selectedCount = 0;
		adapter.notifyDataSetChanged();
		
		if(selectionView!=null){
			selectionView.updateSelectedItems(selectedCount);
		}
	}

	@Override
	public void onSelectedChanged(SelectionCheckedState state) {		
		if(!state.Selected){
			mCheckedArray.delete(state.Position);			
		}else{
			mCheckedArray.put(state.Position, state);
		}
		
		selectedCount += state.Selected ? 1: -1;	
		if(selectionView!=null){
			selectionView.updateSelectedItems(selectedCount);
		}
	}

	@Override
	public void onConfirmResult(boolean confirmed) {
		if(confirmed){
			mCheckedArray.clear();
			selectedCount = 0;
			
			SelectionCheckedAdapter adapter = (SelectionCheckedAdapter) adapterView.getAdapter();
			adapter.notifyDataSetChanged();
			
			if(selectionView!=null){
				selectionView.updateSelectedItems(selectedCount);
			}
		}
	}

}
