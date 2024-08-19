package com.enterlib.databinding;

public abstract class BindingProperty<T> extends DependencyProperty {

	public BindingProperty(String name) {
		super(ExpressionMember.class, name);
	}

	@Override
	public Object get(DependencyObject object) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void set(DependencyObject object, Object value) {
		set((T) object, (ExpressionMember) value, null);
	}

	public abstract void set(T object, ExpressionMember value,
			BindingResources dc);

}