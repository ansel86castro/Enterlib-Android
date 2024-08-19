package com.enterlib.filtering;

import java.util.Locale;

import android.text.InputType;
import android.text.TextUtils;

import com.enterlib.fields.Field;

public class StringFilterCondition<T> extends FilterCondition {
	int inputType = InputType.TYPE_CLASS_TEXT;

	public StringFilterCondition() {
		this.filterOp = FilterOperators.LIKE;
	}

	public StringFilterCondition(String queryHint) {
		super(queryHint);
		this.filterOp = FilterOperators.LIKE;
	}

	public StringFilterCondition(String queryName, String queryHint,
			boolean isActive, Field field) {
		super(queryName, queryHint, isActive, field);
		this.filterOp = FilterOperators.LIKE;
	}

	public StringFilterCondition(String queryName, String queryHint,
			boolean isActive) {
		super(queryName, queryHint, isActive);
		this.filterOp = FilterOperators.LIKE;
	}

	public StringFilterCondition(String queryName, String queryHint, Field field) {
		super(queryName, queryHint, field);
		this.filterOp = FilterOperators.LIKE;
	}

	public StringFilterCondition(String queryName, String queryHint) {
		super(queryName, queryHint);
		this.filterOp = FilterOperators.LIKE;
	}

	@Deprecated
	public StringFilterCondition(String queryName, String queryHint,
			int inputType) {
		super(queryName, queryHint);
		this.inputType = inputType;
		this.filterOp = FilterOperators.LIKE;

	}

	@Deprecated
	public int getInputType() {
		return inputType;
	}

	@Deprecated
	public StringFilterCondition<T> setInputType(int value) {
		this.inputType = value;
		return this;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final boolean eval(Object item) {
		String prefix = (String) queryValue;
		if (TextUtils.isEmpty(prefix)) {
			return true;
		}

		return eval(prefix.toLowerCase(Locale.getDefault()), (T) item);
	}

	protected boolean eval(String prefix, T item){ return false;}
}
