package com.enterlib.filtering;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;

import com.enterlib.StringUtils;
import com.enterlib.converters.IValueConverter;
import com.enterlib.data.QueryHelper;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.fields.Field;
import com.enterlib.serialization.IStringSerializer;

public class FilterCondition {
	static final String Prefix = "FilterCondition";

	protected String queryName;
	protected String queryHint;
	protected Object queryValue;
	protected boolean isActive;
	protected Object tag;
	protected int index;
	private Field field;
	protected IValueConverter converter;
	protected int filterOp;
	protected boolean isCombinable = true;
	private IStopWordContainer stopWordContainer;

	public FilterCondition() {

	}

	public FilterCondition(String queryHint) {
		this.queryHint = queryHint;
	}

	public FilterCondition(String queryName, String queryHint) {
		this.queryName = queryName;
		this.queryHint = queryHint;
	}

	public FilterCondition(String queryName, String queryHint, boolean isActive) {
		this.queryName = queryName;
		this.queryHint = queryHint;
		this.isActive = isActive;
	}

	public FilterCondition(String queryName, String queryHint, Field field) {
		this.queryName = queryName;
		this.queryHint = queryHint;
		this.field = field;
	}

	public FilterCondition(String queryName, String queryHint,
			boolean isActive, Field field) {
		this.queryName = queryName;
		this.queryHint = queryHint;
		this.isActive = isActive;
		this.field = field;
	}

	public String getFilterExpression(){
        if(queryValue == null || queryName == null){
            return null;
        }

        if(queryValue instanceof String){
            return QueryHelper.createStringQuery(queryName, (String)queryValue, filterOp, stopWordContainer);
        }else{
             return String.format("%s %s %s", queryName, getOpString(), queryValue);
        }
	}

	public static String getFilterExpString(ArrayList<FilterCondition>conditions, int offset, int size){
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (int i = offset; i < size; i++) {
			FilterCondition c = conditions.get(i);
			String filter = c.getFilterExpression();
			if(filter != null && filter.length() > 0) {

				if(count > 0){
					sb.append(" AND ");
				}

				sb.append(filter);
				count++;
			}
		}

		return sb.toString();
	}

	public static String getFilterExpString(ArrayList<FilterCondition>conditions){
		return getFilterExpString(conditions, 0, conditions.size());
	}

    public String getOpString(){
        switch (filterOp) {
            case FilterOperators.EQUALS: return "=";
            case FilterOperators.LESS: return "<";
            case FilterOperators.GREATHER: return ">";
            case FilterOperators.LESS_EQUALS: return "<=";
            case FilterOperators.GREATHER_EQUALS: return ">=";
            case FilterOperators.LIKE: return "LIKE";
            case FilterOperators.NOT_EQUALS: return "!=";
            default:
                return "";
        }
    }

	public IValueConverter getConverter() {
		return converter;
	}

	public FilterCondition setConverter(IValueConverter converter) {
		this.converter = converter;
		return this;
	}
	
	public boolean isCombinable(){
		return isCombinable;
	}
	
	public void setCombinable(boolean value){
		isCombinable = value;
	}

	public final Object getQueryValue() {
		return queryValue;
	}

	public Object getConvertedValue() {
		if (converter != null) {
			return converter.convertBack(queryValue);
		}
		return queryValue;
	}

	public final void setQueryValue(Object queryValue) {
		this.queryValue = queryValue;
	}

	public final Object getTag() {
		return tag;
	}

	public final void setTag(Object tag) {
		this.tag = tag;
	}

	public final String getQueryName() {
		return queryName;
	}

	public final String getQueryHint() {
		return queryHint;
	}

	public final boolean isActive() {
		return isActive;
	}

	/** FilterConditionOp value */
	public void setFilterOp(int value) {
		filterOp = value;
	}

	public int getFilterOp() {
		return filterOp;
	}

	public void updateQueryValue() {
		if (field != null) {
			queryValue = field.getValue();
		}
	}

	protected Field getField() {
		return field;
	}
	
	protected void setField(Field field){
		this.field = field;
	}

	public boolean eval(Object item) {
		return false;
	}

	public void saveState(Bundle bundle, IStringSerializer serializer) {
		if (queryValue instanceof Serializable) {
			bundle.putSerializable(Prefix + String.valueOf(index),
					(Serializable) queryValue);
		} else if (queryValue != null) {
			try {
				String str = serializer.serialize(queryValue);
				bundle.putString(Prefix + String.valueOf(index), str);
				bundle.putSerializable(Prefix + String.valueOf(index) + "Type",
						queryValue.getClass());
			} catch (InvalidOperationException e) {

			}
		}

		bundle.putBoolean(Prefix + String.valueOf(index) + "Active", isActive);
		if (field != null) {
			field.saveState(bundle, serializer);
		}
	}

	public void restoreState(Bundle bundle, IStringSerializer serializer) {
		Class<?> valueType = (Class<?>) bundle.getSerializable(Prefix
				+ String.valueOf(index) + "Type");
		if (valueType != null) {
			String str = bundle.getString(Prefix + String.valueOf(index));
			if (str != null) {
				try {
					queryValue = serializer.deserialize(valueType, str);
				} catch (InvalidOperationException e) {
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		} else {
			queryValue = bundle.getSerializable(Prefix + String.valueOf(index));
		}

		isActive = bundle.getBoolean(Prefix + String.valueOf(index) + "Active",
				false);

		if (field != null) {
			field.restoreState(bundle, serializer);
		}
	}

	public void update() {

	}
	
	public FilterCondition asCombinable(){
		isCombinable = true;
		return this;
	}
	
	public FilterCondition asNonCombinable(){
		isCombinable = false;
		return this;
	}

	public boolean hideClear(Object value){
		if(value instanceof String)
			return StringUtils.isNullOrWhitespace((String) value);
		
		else if(value instanceof Boolean)
			return true;
		
		return value == null;
	}
	
	public View getView(){
		if(field == null)
			return null;
		return field.getView();
	}
	
	public void clear(){
		if(field!=null){
			field.setValue(null);
			queryValue = field.getValue();
		}
		setQueryValue(null);
	}


	public void setStopWordContainer(IStopWordContainer stopWordContainer) {
		this.stopWordContainer = stopWordContainer;
	}

	public IStopWordContainer getStopWordContainer() {
		return stopWordContainer;
	}
}