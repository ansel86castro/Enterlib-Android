package com.enterlib.mvvm;

import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnActionExpandListener;
import android.view.View;

import com.enterlib.DialogUtil;
import com.enterlib.R;
import com.enterlib.fields.Field;
import com.enterlib.fields.Form;
import com.enterlib.fields.ListField;
import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.SearchViewFilterController;
import com.enterlib.filtering.SearchViewFilterController.ISearchListener;
import com.enterlib.widgets.ConditionFilterView;
import com.enterlib.widgets.MultipleSelectionView;
import com.enterlib.widgets.OnConfirmResultListener;
import com.enterlib.widgets.OnSelectionConfirmListener;

public abstract class ListFragment extends FormFragment implements ISearchListener, OnSelectionConfirmListener {
	
	public static final String ATTACH_MODE = "ATTACH_MODE";

	public static final String SEARCH_MODE = "SEARCH_MODE";
	
	SearchViewFilterController filterController;
	boolean mInSearchMode;	
	ConditionFilterView searchView;
	MenuItem searchItem;
	boolean isSearchEstrict;
	Activity activity;
	private int search_itemId;
	private int delete_itemId;
	private String deleteProgressDialogMessage;
	private String deleteDialogTitle;
    private boolean keepSearchOpen;
	
	public int MenuResource = R.menu.actionbar_filterable_fragment;

	private MenuItem deleteItem;

	private MultipleSelectionView deleteView;
	
	@Override
	public void onAttach(Activity activity) {	
		super.onAttach(activity);
		
		this.activity = activity;
	}
	
	public void setSearchItemId(int resId){
		this.search_itemId = resId;
	}
	
	public void setDeleteItemId(int resId){
		this.delete_itemId = resId;
	}

    public SearchViewFilterController getFilterController(){
        return filterController;
    }

	public void setDeleteDialogTitle(String title){
		this.deleteDialogTitle = title;
	}
	
	public void setDeleteProgressDialogMessage(String message){
		this.deleteProgressDialogMessage = message;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle args = getArguments();
		if(args!=null){						
			if(args.containsKey(SEARCH_MODE)){
				mInSearchMode = args.getBoolean(SEARCH_MODE);
				isSearchEstrict = true;
			}
		}
		setHasOptionsMenu(true);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {	
		super.onActivityCreated(savedInstanceState);
		
		if(filterController == null){
			filterController = createFilterController(savedInstanceState);
			
			if(filterController!=null){
				
				if(searchView != null){
					filterController.setFilterView(searchView);
				}
				
				if(mInSearchMode && searchItem!=null){
			    	initSearchView();
			    	searchItem.expandActionView();
			    }		 				
			}
		}
		
	}
	
	protected abstract SearchViewFilterController createFilterController(Bundle savedInstanceState);
	
	public void onInitSearch(int menuItemId, Menu menu){
		setSearchItemId(menuItemId);
		onInitSearch(menu);
	}
		
	@SuppressLint("NewApi")
	public void onInitSearch(Menu menu){
		if(search_itemId == 0) return;				
		searchItem = menu.findItem(search_itemId);		
		searchView = (ConditionFilterView)searchItem.getActionView();

		if(filterController!=null){		    		    	   
			filterController.setFilterView(searchView);

			if(mInSearchMode){
				initSearchView();
				searchItem.expandActionView();		    	
			}		   
		}

		searchItem.setOnActionExpandListener(new OnActionExpandListener() {

			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				// Do something when expanded	        	
				initSearchView();

				return true;  // Return true to expand action view
			}

			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				// Do something when collapsed
                if(!keepSearchOpen) {
                    mInSearchMode = false;
                }

                keepSearchOpen = false;

				if(isSearchEstrict){
					if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){						
						activity.onNavigateUp();
					}else
						activity.onBackPressed();
				}						
				return true;  // Return true to collapse action view
			}
		});		
	}
	
	
	public void onInitDelete(int menuItemId, Menu menu){
		setDeleteItemId(menuItemId);
		onInitDelete(menu);
	}
	
	public void onInitDelete(Menu menu){
		if(delete_itemId == 0) return;
		
		deleteItem = menu.findItem(delete_itemId);
		deleteView = (MultipleSelectionView) deleteItem.getActionView();
		if(deleteView ==null)
			return;
		
		initSelectionView(deleteView);
		
		deleteItem.setOnActionExpandListener(new OnActionExpandListener() {
			
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				enableSelectionMode(true);
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				enableSelectionMode(false);
				return true;
			}
		});
	}
	
	protected void initSelectionView(MultipleSelectionView deleteView) {
		ListField listField = getListField();
		if(listField!=null){
			deleteView.initView(listField);
			listField.setOnSelectionConfirmListener(this);
		}
	}
	
	protected ListField getListField(){
		 Form form = getForm();	
		 for (int i = 0; i < form.getFieldsCount(); i++) {
			Field field = form.getField(i);
			if(field instanceof ListField){
				return (ListField) field;								
			}
		}
		 return null;
	}

	protected void enableSelectionMode(boolean value) {
		ListField listField = getListField();
		if(listField!=null){
			listField.enableSelectionMode(value);
		}		
	}

	private void initSearchView() {
		if(searchView == null)
			return;
		
		mInSearchMode = true;
		FilterCondition selectedCondition = filterController.getSelectedCondition();
		if(selectedCondition == null){
			selectedCondition = filterController.getCondition(0);
		}
		
		FilterCondition viewCondition = searchView.getCondition();
		if(viewCondition == null || viewCondition!=selectedCondition){
			searchView.setCondition(selectedCondition);
		}
	}
	
	@Override
	public void onViewStateRestored(Bundle savedInstanceState) {		
		super.onViewStateRestored(savedInstanceState);
		
		if(savedInstanceState!=null && filterController!=null){
			mInSearchMode =  savedInstanceState.getBoolean(SEARCH_MODE);
			filterController.restoreState(savedInstanceState);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {		
		super.onSaveInstanceState(outState);
		
		outState.putBoolean(SEARCH_MODE, mInSearchMode);
		if(filterController!=null)
			filterController.saveState(outState);
	}
	
	@Override
	public void enterSearchMode(View searchView) {
		if(mInSearchMode)
			return;
		
		if(searchItem!=null)
			searchItem.expandActionView();
		
	}

	@Override
	public boolean isInSearchMode() {		
		return mInSearchMode;
	}

	@Override
	public void exitSearchMode() {
		if(!mInSearchMode)
			return;
		
		if(searchItem!=null)
			searchItem.collapseActionView();
		
	}
	
	@Override
	public void onStop() {
		if(!isSearchEstrict){
            keepSearchOpen = true;
			exitSearchMode();
            keepSearchOpen = false;
		}
		
		if(deleteView!=null){
			deleteItem.collapseActionView();
		}
		
		super.onStop();
	}

	@Override
	public void onLoadCompleted() {
		super.onLoadCompleted();
		
		if(filterController!=null){
			filterController.update();
		}
		
		 if(mInSearchMode && searchItem!=null){
		    	initSearchView();
		    	searchItem.expandActionView();
		    	searchView.doFilter();
		 }
	}
	
	@Override
	public void onSelectionConfirm(final List<Object> items, final OnConfirmResultListener resultListener) {
		if(items.size() == 0){
			resultListener.onConfirmResult(false);
			return;
		}
		
		if(getActivity().getIntent().getBooleanExtra(ATTACH_MODE, false)){
			attachList(items);
			resultListener.onConfirmResult(true);
			return;
		}
		
		if(deleteProgressDialogMessage == null){
			deleteProgressDialogMessage = getString(com.enterlib.R.string.deleting);
		}
		
		if(deleteDialogTitle == null){
			deleteDialogTitle = getString(com.enterlib.R.string.deleteDialogTitle);
		}
		
		DialogUtil.showAlertDialog(getActivity(), deleteDialogTitle, 
		 new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				IViewModel viewModel = getViewModel();
				if (viewModel instanceof ListViewModel) {
					ListViewModel listViewModel = (ListViewModel) viewModel;
					listViewModel.delete(deleteProgressDialogMessage, items);
				}
				resultListener.onConfirmResult(true);
			}
		},new DialogInterface.OnClickListener(){			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				resultListener.onConfirmResult(false);
				
			}
		});				
	}
		
}
