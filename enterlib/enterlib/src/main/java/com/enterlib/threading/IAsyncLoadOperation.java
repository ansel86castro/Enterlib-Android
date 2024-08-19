package com.enterlib.threading;

public interface IAsyncLoadOperation {

	boolean loadAsync() throws Exception;

	void onDataLoaded();

}
