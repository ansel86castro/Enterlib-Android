package com.enterlib.threading;

/**
 * @author ansel Define a class that implements an asyncronic behavior. This
 *         class defines an IAsyncInvocator behavior but instead it receives the
 *         result of the operations as a parameter in the OnComplete Callback
 *         method
 * */
public interface IAsyncWorker<T> {

	/**
	 * asyncronous methods ,this method runs in a another Thread. Waring Do not
	 * modified the UI inside this method
	 */
	T DoAsyncWork() throws Exception;

	/**
	 * Method called when an exception is raised from DoAsyncWork. This method
	 * is executed int the UI Thread
	 */
	void OnException(Exception e);

	/**
	 * Method called when the DoAsyncWork method finished its task. This method
	 * is executed in the UI Thread
	 */
	void OnComplete(T result);
}
