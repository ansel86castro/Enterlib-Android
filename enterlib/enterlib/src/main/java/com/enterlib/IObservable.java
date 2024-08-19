package com.enterlib;

/**An specification of the Observer Pattern*/
/**
 * @author Ansel
 *
 */
public interface IObservable {

	/** Adds an observer to the list of observers */
	void addObserver(IObserver observer);

	/**
	 * @param observer
	 *            the observer to remove
	 * @return true if the observer was removed or false if the observer was not
	 *         registered
	 */
	boolean removeObserver(IObserver observer);


	void notifyObservers();
}
