package com.enterlib.threading;


public interface IResultNotifyCallback<T> {
	void operationCompleted(T value, Exception e);
}
