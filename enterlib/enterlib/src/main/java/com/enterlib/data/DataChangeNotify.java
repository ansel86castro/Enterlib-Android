package com.enterlib.data;

import java.util.ArrayList;

public class DataChangeNotify implements IDataChangeNotify {

	ArrayList<IDataChangeListener> listeners;

	@Override
	public void registerDataChangeListener(IDataChangeListener listener) {
		if (listeners == null) {
			listeners = new ArrayList<IDataChangeNotify.IDataChangeListener>(1);
		}

		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}

	@Override
	public void removeDataChangeListener(IDataChangeListener listener) {
		if (listeners == null) {
			return;
		}
		listeners.remove(listener);
	}

	@Override
	public void notifyDataChange() {
		if (listeners == null) {
			return;
		}
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onDataChange(this);
		}
	}

	@Override
	public void notifyDataInvalid() {
		if (listeners == null) {
			return;
		}
		for (int i = 0; i < listeners.size(); i++) {
			listeners.get(i).onDataInvalid(this);
		}

	}

}
