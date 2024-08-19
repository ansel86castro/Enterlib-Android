package com.enterlib.databinding;

import java.util.ArrayList;

public class NotifyPropertyChanged implements INotifyPropertyChanged {

	private ArrayList<IPropertyChangedListener> listeners;

	@Override
	public void addPropertyChangeListener(IPropertyChangedListener listener) {
		if (listener == null) {
			throw new NullPointerException("listener can not be null");
		}

		if (listeners == null) {
			listeners = new ArrayList<IPropertyChangedListener>();
		}

		if (listeners.contains(listener)) {
			return;
		}
		listeners.add(listener);

	}

	@Override
	public boolean removePropertyChangeListener(
			IPropertyChangedListener listener) {
		if (listeners == null) {
			return false;
		}

		return listeners.remove(listener);
	}

	public void onPropertyChange(String propName) {
		if (listeners == null) {
			return;
		}

		int length = listeners.size();
		for (int i = 0; i < length; i++) {
			listeners.get(i).onPropertyChange(this, propName);
		}
	}
}
