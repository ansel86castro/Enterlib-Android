package com.enterlib.databinding;

public interface INotifyPropertyChanged {

	void addPropertyChangeListener(IPropertyChangedListener listener);

	boolean removePropertyChangeListener(IPropertyChangedListener listener);
}
