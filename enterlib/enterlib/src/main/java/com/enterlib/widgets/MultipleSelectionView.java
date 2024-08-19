package com.enterlib.widgets;

import com.enterlib.app.SimpleSpinnerAdapter;
import com.enterlib.fields.ListField;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;

public class MultipleSelectionView extends FrameLayout implements View.OnClickListener, OnItemSelectedListener, ISelectionView {

	public interface OnMultipleSelectionListener{
		void onConfirmSelection();
		void onSelectAll();
		void onClearSelection();
	}	
	
	private ViewGroup mRoot;
	private Spinner spinner;
	private String selectAllMessage;
	private String ClearAllMessage;
	
	SimpleSpinnerAdapter adapter;
	
	SelectionCountItem countItem = new SelectionCountItem();
	SelectAllItem allItem = new SelectAllItem(true);
	SelectAllItem cleartem = new SelectAllItem(false);
	
	OnMultipleSelectionListener onSelectionListener;	
	
	public MultipleSelectionView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public MultipleSelectionView(Context context) {
		this(context, null);
	}

	public MultipleSelectionView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		LayoutInflater inflater = LayoutInflater.from(context);
		mRoot = (ViewGroup) inflater.inflate(com.enterlib.R.layout.layout_delete_box, null, false);
		
		spinner = (Spinner) mRoot.findViewById(com.enterlib.R.id.infoSpinner);
		ImageButton accept = (ImageButton) mRoot.findViewById(com.enterlib.R.id.imageBtnAccept);
		accept.setOnClickListener(this);
		
		adapter =new SimpleSpinnerAdapter(context, android.R.layout.simple_list_item_single_choice, 
				new Object[]{ countItem, allItem, cleartem});
		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(this);
		
		selectAllMessage = context.getString(com.enterlib.R.string.select_all);
		ClearAllMessage = context.getString(com.enterlib.R.string.clear_selection);
		
		addView(mRoot);
	}
	
	

	/* (non-Javadoc)
	 * @see com.enterlib.widgets.ISelectionView#setOnSelectionListener(com.enterlib.widgets.MultipleSelectionView.OnMultipleSelectionListener)
	 */
	@Override
	public void setOnSelectionListener(OnMultipleSelectionListener onSelectionListener) {
		this.onSelectionListener = onSelectionListener;
	}
	
	

	public void setSelectAllMessage(String selectAllMessage) {
		this.selectAllMessage = selectAllMessage;
	}

	public void setClearAllMessage(String clearAllMessage) {
		ClearAllMessage = clearAllMessage;
	}
	
	public void setSelectAllMessage(int selectAllMessage) {
		this.selectAllMessage = getContext().getString(selectAllMessage);
	}

	public void setClearAllMessage(int clearAllMessage) {
		ClearAllMessage = getContext().getString(clearAllMessage);
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
				
		if(position == 1){			
			if(onSelectionListener!=null)
					onSelectionListener.onSelectAll();													
		}
		else if(position == 2){
			if(onSelectionListener!=null)
				onSelectionListener.onClearSelection();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.enterlib.widgets.ISelectionView#updateSelectedItems(int)
	 */
	@Override
	public void updateSelectedItems(int count){
		countItem.Count = count;
		adapter.notifyDataSetChanged();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onClick(View v) {
		if(onSelectionListener!=null){			
			onSelectionListener.onConfirmSelection();
		}
		
	}	
	
	public void initView(ListField listField){
		setOnSelectionListener(listField);
		listField.setSelectionView(this);
	}
	
	/* (non-Javadoc)
	 * @see com.enterlib.widgets.ISelectionView#setSelectAll(boolean)
	 */
	
	public void setSelectAll(boolean value){
		allItem.SelectAll = value;
		adapter.notifyDataSetChanged();
	}
	
	class SelectionCountItem{
		public int Count;
		@Override
		public String toString() {		
			return String.valueOf(Count);
		}
	}
	
	class SelectAllItem{					
		public boolean SelectAll;
		
		
		public SelectAllItem(boolean selectAll) {
			super();
			SelectAll = selectAll;
		}


		@Override
		public String toString() {		
			return SelectAll? selectAllMessage : ClearAllMessage;
		}
	}

	
}
