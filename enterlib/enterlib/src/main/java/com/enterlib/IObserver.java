package com.enterlib;

/**
 * Defines an object that is notified when something has changed in the
 * {@link IObservable}
 */
public interface IObserver {
	/**
	 * Callback, called when the observable's state changes
	 * */
	void update(IObservable observable);
}
