package com.enterlib.databinding;

import java.util.List;

public class ExpressionMember {
	String key;
	Object value;

	public String getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}

	public ExpressionMember(String key, Object value) {
		this.key = key;
		this.value = value;
	}

	@Override
	public String toString() {
		return String.format("{%s:%s}", key, value);
	}

	public boolean isValueId() {
		return value instanceof String;
	}

	public boolean isValueTrue() {
		return value.equals("True") || value.equals("true");
	}

	public boolean isValueFalse() {
		return value.equals("False") || value.equals("false");
	}

	public boolean isValueBolean() {
		return isValueTrue() || isValueFalse();
	}

	public boolean isValueList() {
		return value instanceof List<?>;
	}

	public boolean isValueExpression() {
		return value instanceof BindingExpression;
	}

	public boolean isValueString() {
		return value instanceof String;
	}

	public String getValueString() {
		return (String) value;
	}

	public List<?> getValueList() {
		return (List<?>) value;
	}

	public BindingExpression getValueExpression() {
		return (BindingExpression) value;
	}

	public boolean getValueBoolean() {
		return isValueTrue();
	}

}
