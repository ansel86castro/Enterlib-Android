package com.enterlib.generics;

/**
 * Same as {@link Observable} but it stores the value and notifies the
 * {@link IObserver} when {@code setValue} is called
 */
public class ObservableValue<T> extends Observable<T> {
	private T value;

	public ObservableValue() {

	}

	public ObservableValue(T value) {
		this.value = value;
	}

	public void setValue(T value) {
		this.value = value;
		notifyObservers(value);
	}

	public T getValue() {
		return value;
	}
}
