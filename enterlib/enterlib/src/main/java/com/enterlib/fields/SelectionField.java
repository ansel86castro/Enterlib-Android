package com.enterlib.fields;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.view.View;
import android.widget.Adapter;
import android.widget.Spinner;

import com.enterlib.IEqualityComparer;
import com.enterlib.converters.IValueConverter;
import com.enterlib.data.PropertyMap;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.CommandHanlderItemSelected;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.IPropertyChangedListener;
import com.enterlib.databinding.ReflectionResolver;
import com.enterlib.exceptions.ConversionFailException;

public abstract class SelectionField extends ItemsField {

	public static final BindingProperty<SelectionField> ComparerProperty = registerProperty(
			SelectionField.class, new BindingProperty<SelectionField>(
					"Comparer") {
				@Override
		public void set(SelectionField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.comparer = (IEqualityComparer) dc.get(member
								.getValueString());

				}
					if (object.comparer == null) {
					Object viewModel = object.getViewModel();
					if (viewModel != null) {
						object.comparer = (IEqualityComparer) ReflectionResolver
								.getValue(member.getValueString(), viewModel);
					}
				}
		}
	});


	public static final BindingProperty<SelectionField> ItemsProperty = registerProperty(
			SelectionField.class, "Items");

	public static final BindingProperty<SelectionField> SelectionCommandProperty = registerProperty(
			SelectionField.class, new BindingProperty<SelectionField>(
					"SelectionCommand") {
					@Override
			public void set(SelectionField object, ExpressionMember member,
							BindingResources dc) {
						object.addBindingHandler(new CommandHanlderItemSelected(
								member.getValueString()));
			}
	});
	
	
	public static final BindingProperty<SelectionField> KeyProperty = registerProperty(
			SelectionField.class, new BindingProperty<SelectionField>("Key") {
		@Override
		public void set(SelectionField object, ExpressionMember member, BindingResources dc) {					
			object.key = member.getValueString();			
			object.setValueConverter(new KeyConverter(object.key));
			
			if(object.comparer == null)
				object.setComparer(new KeyComparer(object.key));
		}		
		
		public Object get(com.enterlib.databinding.DependencyObject object) { return ((SelectionField)object).key; };
	});

	

	//private IItemSourceProvider itemSourceProvider;
	private IEqualityComparer comparer;
	protected Object value;
	private String key;
	
	public SelectionField() {

	}

	public SelectionField(View view, boolean required) {
		super(view, required);

	}

	/**
	 * @param view
	 * @param valueBinding
	 * @param display
	 * @param required
	 */
	public SelectionField(View view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);
	}

	public SelectionField(View view, String display, boolean required) {
		super(view, display, required);

	}

	public SelectionField(View view, String valueBinding) {
		super(view, valueBinding);

	}

	public SelectionField(View view) {
		super(view);

	}
	
	

	/**
	 * gets the {@link IEqualityComparer} used to find the selected position in
	 * the {@link Spinner} when the {@link Field}'s value is set
	 */
	public IEqualityComparer getComparer() {
		return comparer;
	}

	/**
	 * sets the {@link IEqualityComparer} used to find the selected position in
	 * the {@link Spinner} when the {@link Field}'s value is set
	 */
	public SelectionField setComparer(IEqualityComparer comparer) {
		this.comparer = comparer;
		return this;
	}

	@SuppressWarnings("unchecked")
	public void setItems(Object itemsSource) {
		this.items = itemsSource;

		if (items == null) {
			setAdapter(null);
			return;
		}

		if (adapterProvider != null) {
			adapterProvider.onItemsSet(this);
		} else {

			ArrayList<Object> list = null;

			if (items.getClass().isArray()) {
				Object[] objects = (Object[]) items;
				list = new ArrayList<Object>(objects.length);
				for (int i = 0; i < objects.length; i++) {
					list.add(objects[i]);
				}
			} else if (items instanceof ArrayList<?>) {
				list = (ArrayList<Object>) items;
			} else if (items instanceof Collection<?>) {
				list = new ArrayList<Object>((Collection<?>) items);
			} else {
				throw new UnsupportedOperationException(
						"Collection not supported");
			}

			setDefaultAdapter(list);
		}
		
//		if(valueConverter == null && value instanceof Integer){
//			ListAdapter adapter = getAdapter();
//			if(adapter!=null && adapter.getCount() > 0){
//				Object item = adapter.getItem(0);
//				if(item!=null && item.getClass() != Integer.class)
//					valueConverter =  PropertyMap.createValueConverter(item.getClass());
//			}
//		}
//		
//		if(comparer == null && value instanceof Integer){
//			ListAdapter adapter = getAdapter();
//			if(adapter!=null && adapter.getCount() > 0){
//				Object item = adapter.getItem(0);
//				if(item!=null && item.getClass()!= Integer.class)
//					comparer = PropertyMap.createValueComparer(item.getClass());
//			}
//		}
	}

	@Override
	protected void setViewValue(Object value) {
		this.value = value;		
	}

	protected abstract void setDefaultAdapter(List<Object> list);

	protected int findSelection(Object value, Adapter adapter) {
		if (adapter == null) {
			return -1;
		}
		
		if(comparer == null){			
			if(adapter.getCount() > 0){
				Object item = adapter.getItem(0);
				comparer = PropertyMap.createValueComparer(item.getClass());
			}
		}

		int selection = -1;
		for (int i = 0; i < adapter.getCount(); i++) {
			Object item = adapter.getItem(i);
			if (item != null) {
				// if(comparer == null && (item instanceof BaseModel) ){
				// comparer = new BaseModelComparer();
				// }
				//
				if (comparer != null) {
					if (comparer.equals(item, value)) {
						selection = i;
						break;
					}
				} else if (item.equals(value)) {
					selection = i;
					break;
				}
			}
		}

		return selection;
	}

	@Override
	protected void restoreViewValue(Object value) {
		if (valueConverter != null) {
			value = valueConverter.convertBack(value);
			setValue(value);
			return;
		}
		super.restoreViewValue(value);
	}


	static class KeyConverter implements IValueConverter{
		String key;
		
		public KeyConverter(String key) {
			super();
			this.key = key;
		}

		@Override
		public Object convert(Object value) throws ConversionFailException {
			return value;
		}

		@Override
		public Object convertBack(Object value) throws ConversionFailException {
			if(value == null)
				return null;
			Object keyObject = ReflectionResolver.getValue(key, value);
			if(keyObject instanceof Number) {
				Number keyValue = (Number) keyObject;
				if (keyValue != null && keyValue.doubleValue() == 0) {
					keyValue = null;
				}
				return keyValue;
			}else{
				return keyObject;
			}
		}
		
	}
	
	static class KeyComparer implements IEqualityComparer{
		String key;
		
		public KeyComparer(String key) {
			super();
			this.key = key;
		}

		@Override
		public boolean equals(Object item, Object value) {
			if(item == null)
				return value == null;
			
			Object itemId =ReflectionResolver.getValue(key, item);
			Object valueId;		
			if (value == null || (value instanceof  String) || (value instanceof  Number)) {
				valueId = value;
			}else{
				valueId = ReflectionResolver.getValue(key, value);
			}

			if(itemId != null)
				return itemId.equals(valueId);

			return itemId == valueId;
		}
		
	}
}
