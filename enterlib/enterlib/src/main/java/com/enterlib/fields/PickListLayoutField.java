package com.enterlib.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.util.Log;
import android.widget.ListAdapter;

import com.enterlib.app.CollectionAdapter;
import com.enterlib.app.IFilterableAdapter;
import com.enterlib.data.IPagedCursor;
import com.enterlib.data.IEntityCursor;
import com.enterlib.widgets.IModelStateObserver;
import com.enterlib.databinding.AdapterEntityCursorForm;
import com.enterlib.databinding.AdapterForm;
import com.enterlib.databinding.BindingExpression;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.filtering.SearchViewFilterController;
import com.enterlib.mvvm.BaseViewModel;
import com.enterlib.threading.IAsyncLoadOperation;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;
import com.enterlib.widgets.OnDataChangeListener;
import com.enterlib.widgets.PickListLayout;

public class PickListLayoutField extends SelectionField implements
		OnDataChangeListener {

	public static final BindingProperty<PickListLayoutField> SearchAdapterProviderProperty = registerProperty(
			PickListLayoutField.class,
			new BindingProperty<PickListLayoutField>("SearchAdapterProvider") {
				@Override
		public void set(PickListLayoutField object,
						ExpressionMember member, BindingResources dc) {
					if (dc != null) {
						object.sourceAdapterProvider = (AdapterProvider) dc.get(member.getValueString());
			}
				}

				@Override
		public Object get(
						com.enterlib.databinding.DependencyObject object) {
			return ((PickListLayoutField) object).sourceAdapterProvider;
		};
	});

	public static final BindingProperty<PickListLayoutField> PickingItemTemplateProperty = registerProperty(
			PickListLayoutField.class,
			new BindingProperty<PickListLayoutField>("PickingItemTemplate") {
				@Override
		public void set(PickListLayoutField object,
						ExpressionMember member, BindingResources dc) {
					if (dc != null) {
						object.pickingTemplateResourceId = object.findResourceId(dc, member.getValueString(),"R.layout");
						object.sourceAdapterProvider = new PickingAdapterProvider(object.getView().getContext(), object.pickingTemplateResourceId);
			}
		}

				@Override
		public Object get(
						com.enterlib.databinding.DependencyObject object) {
			return ((PickListLayoutField) object).pickingTemplateResourceId;
		};
	});

	public static final BindingProperty<PickListLayoutField> FilterControllerProperty = registerProperty(
			PickListLayoutField.class,
			new BindingProperty<PickListLayoutField>("FilterController") {
				@Override
		public void set(PickListLayoutField object,
						ExpressionMember member, BindingResources dc) {
					if (dc != null) {
						object.setFilterController((SearchViewFilterController) dc
								.get(member.getValueString()));
					}

					if (object.getFilterController() == null
							&& object.getViewModel() != null) {
				object.setFilterController((SearchViewFilterController) ReflectionResolver
								.getValue(member.getValueString(),
										object.getViewModel()));
			}
		}

				@Override
		public Object get(
						com.enterlib.databinding.DependencyObject object) {
			return ((PickListLayoutField) object).mFilterController;
		};
	});

	public static final BindingProperty<PickListLayoutField> ModelStateObserverProperty = registerProperty(
			PickListLayoutField.class, "ModelStateObserver");

	Object pickingItems;
	AdapterProvider sourceAdapterProvider;
	int pickingTemplateResourceId;

	private SearchViewFilterController mFilterController;

	public PickListLayoutField(PickListLayout view) {
		super(view);
		
		setRestorable(false);
	}

	public PickListLayoutField() {
	}

	public PickListLayoutField(PickListLayout view, boolean required) {
		super(view, required);
	}

	public PickListLayoutField(PickListLayout view, String display,
			boolean required) {
		super(view, display, required);
	}

	public PickListLayoutField(PickListLayout view, String valueBinding,
			String display, boolean required) {
		super(view, valueBinding, display, required);
	}

	public PickListLayoutField(PickListLayout view, String valueBinding) {
		super(view, valueBinding);
	}

	public void setFilterController(SearchViewFilterController filterController) {
		mFilterController = filterController;
		PickListLayout listView = (PickListLayout) getView();
		listView.setFilterController(mFilterController);
	}

	public SearchViewFilterController getFilterController() {
		return mFilterController;
	}

	public IModelStateObserver getModelStateObserver() {
		return ((PickListLayout) getView()).getModelStateManager();
	}

	public void setModelStateObserver(IModelStateObserver observer) {
		((PickListLayout) getView()).setModelStateManager(observer);
	}

//	public ArrayList<?> getPickingItems() {
//		return pickingItems;
//	}

	public void setPickingItems(ArrayList<Object> items) {
		pickingItems = items;
		if (sourceAdapterProvider != null) {
			sourceAdapterProvider.onItemsSet(this);
		} else if (templateResourceId != 0) {
			sourceAdapterProvider = new AdapterProvider(getContext(),
					templateResourceId);
			sourceAdapterProvider.onItemsSet(this);
		}
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		PickListLayout listView = (PickListLayout) getView();
		listView.setItemsAdapter(adapter);
		if (!(adapter instanceof OnDataChangeListener)) {
			listView.setOnDataChangeListener(this);
		}
	}
	
	@Override
	public ListAdapter getAdapter() {
		PickListLayout listView = (PickListLayout) getView();
		if(listView == null)
			return null;
		return listView.getItemsAdapter();
	}

	public void setSearchAdapter(ListAdapter adapter) {
		PickListLayout listView = (PickListLayout) getView();
		listView.setSearchAdapter(adapter);
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

		if (adapterProvider != null) {
			adapterProvider.onItemsSet(this);
		} else if (items != null) {
			if (items.getClass().isArray()) {
				setAdapter(new CollectionAdapter<Object>(view.getContext(),
						android.R.layout.simple_list_item_1, (Object[]) items));

			} else if (items instanceof ArrayList<?>) {
				setAdapter(new CollectionAdapter<Object>(view.getContext(),
						android.R.layout.simple_list_item_1,
						(ArrayList<Object>) items));
			}
		} else {
			PickListLayout listView = (PickListLayout) getView();
			listView.setItemsAdapter(null);
		}
	}

	@Override
	public void onAdd(Object item) {
		ArrayList<Object> collection;

		try {
			collection = (ArrayList<Object>) items;
		} catch (ClassCastException e) {
			throw new RuntimeException(
					"Only ArrayList are supported for the Value property");
		}

		collection.add(item);

		if (adapterProvider != null) {
			adapterProvider.onItemsSet(this);
		}
	}

	@Override
	public void onRemove(Object item, int position) {
		ArrayList<Object> collection;

		try {
			collection = (ArrayList<Object>) items;
		} catch (ClassCastException e) {
			throw new RuntimeException(
					"Only ArrayList are supported for the Value property");
		}

		collection.remove(position);

		if (adapterProvider != null) {
			adapterProvider.onItemsSet(this);
		}
	}

	@Override
	public void initBinding(BindingExpression binding,
			BindingResources dataContext) {
		super.initBinding(binding, dataContext);

		Object viewModel = getViewModel();
		if (viewModel instanceof IModelStateObserver) {
			setModelStateObserver((IModelStateObserver) viewModel);
		}

	}

	@Override
	protected void setDefaultAdapter(List<Object> list) {

	}

	@Override
	public void setItems(Object itemsSource) {				
		this.pickingItems = itemsSource;		
		
		if (pickingItems == null) {			
			setSearchAdapter(null);
			return;
		}		
		
		if (sourceAdapterProvider != null) {
			sourceAdapterProvider.onItemsSet(this);
		} else {
			
			ArrayList<Object> list = null;		
			if (pickingItems instanceof IEntityCursor<?>) {				
				throw new RuntimeException("Must specified an ItemTemplate");				
			} else if (pickingItems.getClass().isArray()) {
				Object[] objects = (Object[]) pickingItems;
				list = new ArrayList<Object>(objects.length);
				for (int i = 0; i < objects.length; i++) {
					list.add(objects[i]);
				}
			} else if (pickingItems instanceof ArrayList<?>) {
				list = (ArrayList<Object>) pickingItems;
			} else if (pickingItems instanceof Collection<?>) {
				list = new ArrayList<Object>((Collection<?>) pickingItems);
			} else {
				throw new UnsupportedOperationException(
						"Collection not supported");
			}

			setSearchAdapter(new CollectionAdapter<Object>(getContext(), android.R.layout.simple_list_item_1, list));
		}				

//		if (itemsSource instanceof ArrayList<?>) {
//			this.pickingItems = (ArrayList<Object>) itemsSource;
//		}
//
//		if (sourceAdapterProvider != null) {
//			sourceAdapterProvider.onItemsSet(this);
//			return;
//		}
//
//		if (itemsSource.getClass().isArray()) {
//			Object[] objects = (Object[]) itemsSource;
//			ArrayList<Object> list = new ArrayList<Object>(objects.length);
//			for (int i = 0; i < objects.length; i++) {
//				list.add(objects[i]);
//			}
//			pickingItems = list;
//			setSearchAdapter(new CollectionAdapter<Object>(view.getContext(),
//					android.R.layout.simple_list_item_1, list));
//		} else {
//			setSearchAdapter(new CollectionAdapter<Object>(view.getContext(),
//					android.R.layout.simple_list_item_1, pickingItems));
//
//		}
	}

	public static class PickingAdapterProvider extends AdapterProvider {

		class LoadOperation implements IAsyncLoadOperation, IWorkPost {
			PickListLayoutField pickingField;
			IPagedCursor<Object> pagedCursor;

			public LoadOperation(PickListLayoutField pickingField, IPagedCursor<Object> pagedCursor) {
				this.pickingField = pickingField;
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
					pickingField.onSetErrorMessage(workException.getLocalizedMessage());
					return;
				}

				pickingField.setSearchAdapter(new AdapterEntityCursorForm(context,
						adapterLayoutRes, pagedCursor,
						pickingField));
			}
		}
		public PickingAdapterProvider(Context context, int adapterLayoutRes) {
			super(context, adapterLayoutRes);

		}

		public PickingAdapterProvider() {
		}

		@Override
		@SuppressWarnings("unchecked")
		public final void onItemsSet(ItemsField listField) {
			PickListLayoutField pickingField = (PickListLayoutField) listField;
			Object items = pickingField.pickingItems;
			
			if (items == null) {
				pickingField.setSearchAdapter(null);
				return;
			}

			if (items instanceof IEntityCursor<?>) {
				if (adapterLayoutRes == 0) {
					throw new RuntimeException("Must specified an ItemTemplate");
				}
				if(items instanceof IPagedCursor<?>){
					IPagedCursor<Object> pagedCursor = (IPagedCursor<Object>) items;
					if(!pagedCursor.isLoaded()){
						Object viewModel = listField.getViewModel();
						if(viewModel instanceof BaseViewModel){
							BaseViewModel baseViewModel = (BaseViewModel) viewModel;
							baseViewModel.doLoadOperationAsync(new LoadOperation(pickingField, pagedCursor));
						}else{
							AsyncManager.postAsync(new LoadOperation(pickingField, pagedCursor));
						}
					}else{
						pickingField.setSearchAdapter(new AdapterEntityCursorForm(context,
								adapterLayoutRes, (IEntityCursor<Object>) items,
								pickingField));
					}

				}else {
					pickingField.setSearchAdapter(new AdapterEntityCursorForm(context,
							adapterLayoutRes, (IEntityCursor<Object>) items,
							pickingField));
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
				throw new RuntimeException("Items is not an array or Collection");
			}

			if (adapterLayoutRes == 0) {
				pickingField.setSearchAdapter(getAdapter(collection));
				return;
			}
			pickingField.setSearchAdapter(new AdapterForm(context, adapterLayoutRes, collection, pickingField));			
			
			//****************************						
			
//			if (pickingField.pickingItems == null || pickingField.pickingItems.size() == 0) {
//				pickingField.setSearchAdapter(null);
//				return;
//			}
//
//			if (adapterLayoutRes == 0) {
//				pickingField
//						.setSearchAdapter(getAdapter(pickingField.pickingItems));
//				return;
//			}
//			pickingField.setSearchAdapter(new AdapterForm(context,
//					adapterLayoutRes, pickingField.pickingItems, listField));

		}

		@Override
		public IFilterableAdapter getAdapter(ArrayList<Object> collection) {
			return null;
		}
	}
}
