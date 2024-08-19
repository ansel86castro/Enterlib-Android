package com.enterlib.generics;

/** The generic counterpart of {@link com.enterlib.IObservable} */
public interface IObservable<T> {

	void addObserver(IObserver<T> observer);

	boolean removeObserver(IObserver<T> observer);

	void notifyObservers(T value);
}
