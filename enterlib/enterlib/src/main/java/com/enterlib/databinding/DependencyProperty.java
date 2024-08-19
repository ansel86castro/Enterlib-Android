package com.enterlib.databinding;

public abstract class DependencyProperty {
	private final String name;
	private final Class<?> type;

	public DependencyProperty(Class<?> type, String name) {
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public Class<?> getType() {
		return type;
	}

	public boolean isReadOnly() {
		return false;
	}

	public void set(DependencyObject object, Object value) {
		throw new UnsupportedOperationException("Property " + getName()
				+ " is read-only");
	}

	public abstract Object get(DependencyObject object);
}
