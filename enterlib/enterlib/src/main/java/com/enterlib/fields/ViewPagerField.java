package com.enterlib.fields;

import java.util.ArrayList;

import com.enterlib.data.IPagedCursor;
import com.enterlib.data.IEntityCursor;
import com.enterlib.databinding.AdapterPageEntityCursor;
import com.enterlib.databinding.AdapterPageForm;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.generics.IObserver;
import com.enterlib.mvvm.Command;
import com.enterlib.mvvm.BaseViewModel;
import com.enterlib.threading.IAsyncLoadOperation;
import com.enterlib.serialization.IStringSerializer;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;

import android.os.Bundle;
import android.util.Log;

import androidx.viewpager.widget.ViewPager;

public class ViewPagerField extends Field implements IObserver<Boolean> {

	public static final BindingProperty<ViewPagerField> ItemTemplateProperty = registerProperty(
			ViewPagerField.class, new BindingProperty<ViewPagerField>("ItemTemplate") {
		@Override
		public void set(ViewPagerField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.templateResourceId = object.findResourceId(dc, member.getValueString(), "R.layout");						
			}
		}

		@Override
		public Object get(com.enterlib.databinding.DependencyObject object) {
			return ((ItemsField) object).templateResourceId;
		};
	});
	
	public static final BindingProperty<ViewPagerField> AsListProperty = registerProperty(ViewPagerField.class, 
			new BindingProperty<ViewPagerField>("AsList") {
			@Override
			public void set(ViewPagerField object, ExpressionMember value,
						BindingResources dc) {
					object.asList = value.isValueTrue();
		}
	});
	
	public static final BindingProperty<ViewPagerField> SelectionCommandProperty = registerProperty(ViewPagerField.class, 
			new BindingProperty<ViewPagerField>("SelectionCommand") {
			@Override
			public void set(ViewPagerField field, ExpressionMember value,
						BindingResources dc) {
				
					String sourceProperty = value.getValueString();											
					Object viewModel = field.getViewModel();					
					field.setSelectionCommand((Command) ReflectionResolver.getValue(sourceProperty, viewModel));					
		}
	});
	
	private Object items;
	private int layout;
	private int templateResourceId;
	private ViewPager viewPager;
	private boolean asList;
	private Command selectionCommand;

	private int selectedPosition;
	
	public ViewPagerField(ViewPager viewPager){
		super(viewPager);
		setRestorable(false);
		this.viewPager = viewPager;
	}
	
	public int getSelectedPosition() {
		return selectedPosition;
	}



	public void setSelectedPosition(int selectedPosition) {
		this.selectedPosition = selectedPosition;
	}

	
	public Command getSelectionCommand() {
		return selectionCommand;
	}


	public void setSelectionCommand(Command selectionCommand) {
		this.selectionCommand = selectionCommand;
		if (selectionCommand != null) {
			setEnabled(selectionCommand.isEnabled());
			selectionCommand.getEnabledObservable().addObserver(this);			
		}
	}

	@Override
	public void onNotify(Boolean value) {
		setEnabled(value);		
	}

	@Override
	protected void onViewChanged() {		
		this.viewPager = (ViewPager) getView();
	}
		
	@Override
	protected void onSetErrorMessage(String errorMessage) {		
		
	}

	@Override
	protected Object getViewValue() {
		return items;
	}

	@Override
	protected void setViewValue(Object value) {
		items = value;
		viewPager.post(new Runnable() {
			
			@Override
			public void run() {
				onSetViewValue();
				viewPager.setCurrentItem(selectedPosition);				
			}
		});		
	}

	protected void onSetViewValue(){
		if (items == null) {
			viewPager.setAdapter(null);
			return;
		}

		if (items instanceof IEntityCursor<?>) {
			if (templateResourceId == 0) {
				Log.e(getClass().getSimpleName(), "Must specified an ItemTemplate");
				throw new RuntimeException("Must specified an ItemTemplate");
			}

			if(items instanceof IPagedCursor<?>){
				IPagedCursor<Object> pagedCursor = (IPagedCursor<Object>) items;
				if(!pagedCursor.isLoaded()){
					Object viewModel = getViewModel();
					if(viewModel instanceof BaseViewModel){
						BaseViewModel baseViewModel = (BaseViewModel) viewModel;
						baseViewModel.doLoadOperationAsync(new LoadOperation(pagedCursor));
					}else{
						AsyncManager.postAsync(new LoadOperation(pagedCursor));
					}
				}else{
					viewPager.setAdapter(new AdapterPageEntityCursor(getContext(),
							templateResourceId, (IEntityCursor<Object>) items,
							this, null, asList));
				}
			}else {
				viewPager.setAdapter(new AdapterPageEntityCursor(getContext(),
						templateResourceId, (IEntityCursor<Object>) items,
						this, null, asList));
			}

			return;
		}

		ArrayList<Object> collection = null;
		if (items.getClass().isArray()) {
			Object[] objects = (Object[]) items;
			collection = new ArrayList<Object>(objects.length);
			for (int i = 0; i < objects.length; i++) {
				collection.add(objects[i]);
			}

		} else if (items instanceof ArrayList<?>) {
			collection = (ArrayList<Object>) items;
		}

		if (collection == null) {
			Log.e(getClass().getSimpleName(), "Items is not an array or Collection");
			throw new RuntimeException("Items is not an array or Collection");
		}
		
		viewPager.setAdapter(new AdapterPageForm(getContext(), templateResourceId, collection, this, null, asList));
	}

	@Override
	public void saveState(Bundle outState, IStringSerializer serializer) {
		super.saveState(outState, serializer);

		String storeId = getStoreId();
		String selectedid = storeId + "_position";		

		outState.putInt(selectedid, selectedPosition);		
	}

	@Override
	public void restoreState(Bundle savedInstanceState,
			IStringSerializer serializer) {
		
		super.restoreState(savedInstanceState, serializer);
		
		String storeId = getStoreId();
		String selectedId = storeId + "_position";
		
		selectedPosition = savedInstanceState.getInt(selectedId, -1);			
	}

	private class LoadOperation implements IAsyncLoadOperation, IWorkPost {
		IPagedCursor<Object> pagedCursor;

		public LoadOperation(IPagedCursor<Object> pagedCursor) {
			this.pagedCursor = pagedCursor;
		}

		@Override
		public boolean loadAsync() throws Exception {
			return runWork();
		}

		@Override
		public void onDataLoaded() {
			onWorkFinish(null);
		}

		@Override
		public boolean runWork() throws Exception {
			pagedCursor.loadPage(0);
			return true;
		}

		@Override
		public void onWorkFinish(Exception workException) {
			if(workException!=null){
				Log.e("AdapterProvider", workException.getLocalizedMessage(), workException);
				onSetErrorMessage(workException.getLocalizedMessage());
				return;
			}

			viewPager.setAdapter(new AdapterPageEntityCursor(getContext(),
					templateResourceId, pagedCursor,
					ViewPagerField.this, null, asList));
		}
	}

}
