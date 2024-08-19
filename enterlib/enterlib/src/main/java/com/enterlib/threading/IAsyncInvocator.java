package com.enterlib.threading;

/** Define a class that implements an asyncronic behavior */
public interface IAsyncInvocator {

	/**
	 * asyncronous methods ,this method runs in a another Thread. Waring Do not
	 * modified the UI inside this method
	 */
	void DoAsyncOperation() throws Exception;

	/**
	 * Method called when an exception is raised from DoAsyncWork. This method
	 * is executed int the UI Thread
	 */
	void OnAsyncOperationComplete();

	/**
	 * Method called when the DoAsyncWork method finished its task. This method
	 * is executed in the UI Thread
	 */
	void OnAsyncOperationException(Exception e);
}
