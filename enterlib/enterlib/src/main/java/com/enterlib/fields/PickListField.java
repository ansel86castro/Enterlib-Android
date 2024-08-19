package com.enterlib.fields;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.ListAdapter;

import com.enterlib.app.CollectionAdapter;
import com.enterlib.app.IFilterableAdapter;
import com.enterlib.widgets.IModelStateObserver;
import com.enterlib.databinding.AdapterForm;
import com.enterlib.databinding.BindingExpression;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.filtering.SearchViewFilterController;
import com.enterlib.widgets.OnDataChangeListener;
import com.enterlib.widgets.PickListView;

public class PickListField extends SelectionField implements
		OnDataChangeListener {

	public static final BindingProperty<PickListField> SearchAdapterProviderProperty = registerProperty(
			PickListField.class, new BindingProperty<PickListField>(
					"SearchAdapterProvider") {
				@Override
		public void set(PickListField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.sourceAdapterProvider = (AdapterProvider) dc
								.get(member.getValueString());
			}
				}

				@Override
		public Object get(
						com.enterlib.databinding.DependencyObject object) {
			return ((PickListField) object).sourceAdapterProvider;
		};
	});

	public static final BindingProperty<PickListField> PickingItemTemplateProperty = registerProperty(
			PickListField.class, new BindingProperty<PickListField>(
					"PickingItemTemplate") {
				@Override
		public void set(PickListField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.pickingTemplateResourceId = object
								.findResourceId(dc, member.getValueString(),
										"R.layout");
						object.sourceAdapterProvider = new PickingAdapterProvider(
								object.getView().getContext(),
								object.pickingTemplateResourceId);
			}
		}

				@Override
		public Object get(
						com.enterlib.databinding.DependencyObject object) {
			return ((PickListField) object).pickingTemplateResourceId;
		};
	});

	public static final BindingProperty<PickListField> FilterControllerProperty = registerProperty(
			PickListField.class, new BindingProperty<PickListField>(
					"FilterController") {
				@Override
		public void set(PickListField object, ExpressionMember member,
						BindingResources dc) {
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
			return ((PickListField) object).mFilterController;
		};
	});

	public static final BindingProperty<PickListField> ModelStateObserverProperty = registerProperty(
			PickListField.class, "ModelStateObserver");

	ArrayList<Object> pickingItems;
	AdapterProvider sourceAdapterProvider;
	int pickingTemplateResourceId;

	private SearchViewFilterController mFilterController;

	public PickListField(PickListView view) {
		super(view);
	}

	public PickListField() {
	}

	public PickListField(PickListView view, boolean required) {
		super(view, required);
	}

	public PickListField(PickListView view, String display, boolean required) {
		super(view, display, required);
	}

	public PickListField(PickListView view, String valueBinding,
			String display, boolean required) {
		super(view, valueBinding, display, required);
	}

	public PickListField(PickListView view, String valueBinding) {
		super(view, valueBinding);
	}

	public void setFilterController(SearchViewFilterController filterController) {
		mFilterController = filterController;
		PickListView listView = (PickListView) getView();
		listView.setFilterController(mFilterController);
	}

	public SearchViewFilterController getFilterController() {
		return mFilterController;
	}

	public IModelStateObserver getModelStateObserver() {
		return ((PickListView) getView()).getModelStateManager();
	}

	public void setModelStateObserver(IModelStateObserver observer) {
		((PickListView) getView()).setModelStateManager(observer);
	}

	public ArrayList<?> getPickingItems() {
		return pickingItems;
	}

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
		PickListView listView = (PickListView) getView();
		listView.setItemsAdapter(adapter);
		if (!(adapter instanceof OnDataChangeListener)) {
			listView.setOnDataChangeListener(this);
		}
	}
	
	@Override
	public ListAdapter getAdapter() {
		PickListView listView = (PickListView) getView();
		if(listView == null)
			return null;
		return listView.getItemsAdapter();
	}

	public void setSearchAdapter(IFilterableAdapter adapter) {
		PickListView listView = (PickListView) getView();
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
			PickListView listView = (PickListView) getView();
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
		if (itemsSource == null) {
			this.pickingItems = null;
			setSearchAdapter(null);
			return;
		}

		if (itemsSource instanceof ArrayList<?>) {
			this.pickingItems = (ArrayList<Object>) itemsSource;
		}

		if (sourceAdapterProvider != null) {
			sourceAdapterProvider.onItemsSet(this);
			return;
		}

		if (itemsSource.getClass().isArray()) {
			Object[] objects = (Object[]) itemsSource;
			ArrayList<Object> list = new ArrayList<Object>(objects.length);
			for (int i = 0; i < objects.length; i++) {
				list.add(objects[i]);
			}
			pickingItems = list;
			setSearchAdapter(new CollectionAdapter<Object>(view.getContext(),
					android.R.layout.simple_list_item_1, list));
		} else {

			setSearchAdapter(new CollectionAdapter<Object>(view.getContext(),
					android.R.layout.simple_list_item_1, pickingItems));

		}
	}

	public static class PickingAdapterProvider extends AdapterProvider {

		public PickingAdapterProvider(Context context, int adapterLayoutRes) {
			super(context, adapterLayoutRes);

		}

		public PickingAdapterProvider() {
		}

		@Override
		@SuppressWarnings("unchecked")
		public final void onItemsSet(ItemsField listField) {
			PickListField pickingField = (PickListField) listField;
			if (pickingField.pickingItems == null
					|| pickingField.pickingItems.size() == 0) {
				pickingField.setSearchAdapter(null);
				return;
			}

			if (adapterLayoutRes == 0) {
				pickingField
						.setSearchAdapter(getAdapter(pickingField.pickingItems));
				return;
			}
			pickingField.setSearchAdapter(new AdapterForm(context,
					adapterLayoutRes, pickingField.pickingItems, listField));

		}

		@Override
		public IFilterableAdapter getAdapter(ArrayList<Object> collection) {
			return null;
		}
	}
}
