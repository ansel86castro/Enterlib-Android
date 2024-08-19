package com.enterlib.threading;

import android.os.AsyncTask;

public abstract class AsyncResultTask<T> extends AsyncTask<Void, Void, T> {

	private IResultNotifyCallback<T> _callback;
	Exception _exception;

	public AsyncResultTask(IResultNotifyCallback<T> callback) {
		this._callback = callback;
		this._exception = null;
	}

	@Override
	protected T doInBackground(Void... arg0) {
		try {
			T result = doInBackground();
			return result;
		} catch (Exception e) {
			this._exception = e;
			return null;
		}
	}

	protected abstract T doInBackground() throws Exception;

	@Override
	protected void onPostExecute(T result) {
		_callback.operationCompleted(result, _exception);
	}

	public AsyncResultTask<T> run() {
		execute((Void) null);
		return this;
	}

}
