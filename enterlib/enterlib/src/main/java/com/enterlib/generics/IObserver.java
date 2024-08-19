package com.enterlib.generics;

/** The generic counterpart of {@link com.enterlib.IObserver} */
public interface IObserver<T> {

	void onNotify(T value);
}
