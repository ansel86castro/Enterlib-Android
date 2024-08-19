package com.enterlib.filtering;

import android.content.Context;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.Spinner;

import com.enterlib.converters.IValueConverter;
import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.fields.SelectionField;
import com.enterlib.fields.SpinnerField;
import com.enterlib.serialization.IStringSerializer;

public class SpinnerFilterCondition extends FilterCondition {

	public static interface OnGetItemCallback {
		Object onGetItems();
	}

	private SpinnerField spinnerField;
	private int position;
	private Spinner spinner;
	private OnGetItemCallback onGetItemCallback;	
	private String itemsBinding;
	private Object dataContext;
	
	public SpinnerFilterCondition(String queryName, String queryHint,
			SpinnerField field) {
		super(queryName, queryHint, field);
	}

	public SpinnerFilterCondition(Context context, String queryName,String queryHint) {
		super(queryName, queryHint, null);

		spinner = new Spinner(context);
		spinner.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		spinner.setSelection(position);

		spinnerField = new SpinnerField(spinner, "FilterCondition" + queryHint);

		setField(spinnerField);
	}
	
	public SpinnerFilterCondition(Context context, String queryName,String queryHint, IValueConverter valueConverter) {
		this(context, queryName, queryHint);
		this.spinnerField.setValueConverter(valueConverter);
	}

	public SpinnerFilterCondition(Context context, String queryName,String queryHint, String key) {
		this(context, queryName, queryHint);

		@SuppressWarnings("unchecked")
		BindingProperty<SelectionField> prop = (BindingProperty<SelectionField>)this.spinnerField.getProperty("Key");
		ExpressionMember member = new ExpressionMember("Key", key);
		prop.set(spinnerField, member, null);
	}
	
	public SpinnerFilterCondition(Context context, String queryName,String queryHint, IValueConverter valueConverter, String itemsBinding ,Object dataContext) {
		this(context, queryName, queryHint, valueConverter);
		
		this.itemsBinding = itemsBinding;
		this.dataContext = dataContext;
		
		@SuppressWarnings("unchecked")
		BindingProperty<SelectionField> prop = (BindingProperty<SelectionField>) this.spinnerField.getProperty("Items");
		ExpressionMember member = new ExpressionMember("Items", itemsBinding);
		prop.set(spinnerField, member, null);				
	}
	
	public SpinnerFilterCondition(Context context, String queryName,String queryHint, String key, String itemsBinding ,Object dataContext){
		this(context, queryName, queryHint, (IValueConverter)null, itemsBinding, dataContext);


		@SuppressWarnings("unchecked")
		BindingProperty<SelectionField> prop = (BindingProperty<SelectionField>)this.spinnerField.getProperty("Key");
		ExpressionMember member = new ExpressionMember("Key", key);
		prop.set(spinnerField, member, null);
	}

	public SpinnerFilterCondition(Context context, String queryName,String queryHint, String key, String itemsBinding ,
								  int templateId, Object dataContext){
		this(context, queryName,queryHint , key, itemsBinding, templateId, templateId, dataContext);
	}
	public SpinnerFilterCondition(Context context, String queryName,String queryHint, String key, String itemsBinding ,
								  int templateId, int dropdownTemplateId, Object dataContext){
		this(context, queryName, queryHint, key, itemsBinding, dataContext);


		this.spinnerField.setTemplateResource(templateId);
		this.spinnerField.setDropDownTemplateResource(dropdownTemplateId);
	}
	

	public SpinnerField getSpinnerField() {
		return spinnerField;
	}

	public SpinnerFilterCondition setOnGetItemCallback(
			OnGetItemCallback onGetItemCallback) {
		this.onGetItemCallback = onGetItemCallback;
		return this;
	}

	public void setItems(Object itemsSource) {
		spinnerField.setItems(itemsSource);
		spinner.setSelection(position);
	}

	public SpinnerFilterCondition(Context context, String queryHint) {
		this(context, null, queryHint);
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean eval(Object item) {
		Object value = (Object) spinner.getSelectedItem();
		if (value == null) {
			return true;
		}
		return eval(value, item);
	}

	@Override
	public void saveState(Bundle bundle, IStringSerializer serializer) {
		super.saveState(bundle, serializer);

		position = spinner.getSelectedItemPosition();

		bundle.putInt(Prefix + String.valueOf(index) + "_position", position);
	}

	@Override
	public void restoreState(Bundle bundle, IStringSerializer serializer) {
		super.restoreState(bundle, serializer);

		position = bundle.getInt(Prefix + String.valueOf(index) + "_position",0);
		spinner.setSelection(position);
	}

	@Override
	public void update() {
		if (onGetItemCallback != null) {
			setItems(onGetItemCallback.onGetItems());
		}else if(itemsBinding !=null){
			spinnerField.updateTarget(dataContext);			
		}
	}
	
	
	@Override
	public Object getConvertedValue() {
		Object value = spinner.getSelectedItem();
		if (converter != null) {
			return converter.convertBack(value);
		}
		return value;
	}

	protected boolean eval(Object selectedOption, Object item){ return false;}


	@Override
	public String getFilterExpression() {
		Integer id = (Integer) queryValue;
		if( id == null || id <= 0)
			return null;

        return String.format("%s %s %s", queryName, getOpString(), queryValue);
	}
}
