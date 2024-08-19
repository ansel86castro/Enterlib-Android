package com.enterlib.threading;

public interface IWorkPost {
	boolean runWork() throws Exception;

	void onWorkFinish(Exception workException);
}