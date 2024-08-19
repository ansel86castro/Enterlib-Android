package com.enterlib.generics;

import java.util.ArrayList;

/** Implementation of the {@link IObservable} contract */
public class Observable<T> implements IObservable<T> {

	ArrayList<IObserver<T>> observers = new ArrayList<IObserver<T>>();

	@Override
	public void addObserver(IObserver<T> observer) {
		observers.add(observer);
	}

	@Override
	public boolean removeObserver(IObserver<T> observer) {
		return observers.remove(observer);
	}

	/** notifies the observer with the value state */
	@Override
	public void notifyObservers(T value) {
		int count = observers.size();
		for (int i = 0; i < count; i++) {
			observers.get(i).onNotify(value);
		}
	}

	/** removes all the observers */
	public void clearObservers() {
		observers.clear();
	}
}
