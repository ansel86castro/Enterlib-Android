package com.enterlib.databinding;

import java.util.HashMap;

import android.annotation.SuppressLint;

import com.enterlib.converters.Converters;

@SuppressLint("NewApi")
public class BindingResources {

	HashMap<String, Object> hastMap = new HashMap<String, Object>();
	BindingResources parent;

	public BindingResources getParent() {
		return parent;
	}

	public void setParent(BindingResources parent) {
		this.parent = parent;
	}

	public BindingResources(BindingResources parent) {
		this();
		this.parent = parent;
	}

	public BindingResources() {
		put("DateToStringConverter", Converters.DateToStringConverter);
		put("IntegerToStringConverter", Converters.IntegerToStringConverter);
		put("DoubleToStringConverter", Converters.DoubleToStringConverter);
		put("TimeToStringConverter", Converters.TimeToStringConverter);
	}

	public BindingResources put(String key, Object value) {
		hastMap.put(key, value);
		return this;
	}

	public Object get(String key) {
		Object value = hastMap.get(key);
		if (parent != null) {
			value = parent.get(key);
		}
		return value;
	}

	public boolean hasValue(String key) {
		return hastMap.containsKey(key)
				|| (parent != null && parent.hasValue(key));
	}

	// public <T, V> BindingResources putItemSource(Property<T, V> property ,T
	// target){
	// put("itemSource", new ItemSource<T,V>(property, target));
	// return this;
	// }
	//
	// public <T, V> BindingResources putItemSource(Class<T>targetType, Class<V>
	// valueType, T target, String name){
	// put("itemSource", new ItemSource<T,V>(Property.of(targetType, valueType,
	// name), target));
	// return this;
	// }
	//
	// static class ItemSource<T,V> implements IItemSourceProvider{
	// public Property<T,V> property;
	// public T target;
	//
	// public ItemSource(Property<T, V> property, T target) {
	// this.property = property;
	// this.target = target;
	// }
	//
	//
	//
	// @SuppressWarnings("unchecked")
	// @Override
	// public void setItems(Field field, Object data) {
	// SelectionField selectionField = (SelectionField)field;
	// Object value = target!=null ? property.get(target)
	// :property.get((T)data);
	//
	// if(value instanceof Collection<?>){
	// selectionField.setItems((Collection<?>)value);
	// }
	// }
	// }

}
