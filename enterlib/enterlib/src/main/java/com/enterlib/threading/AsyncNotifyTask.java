package com.enterlib.threading;

import android.os.AsyncTask;

public abstract class AsyncNotifyTask extends AsyncTask<Void, Void, Exception> {

	IAsyncCallback _callback;

	public AsyncNotifyTask(IAsyncCallback callback) {
		_callback = callback;
	}

	@Override
	protected Exception doInBackground(Void... arg0) {
		try {
			doInBackground();
			return null;
		} catch (Exception e) {
			return e;
		}
	}

	protected abstract void doInBackground() throws Exception;

	@Override
	protected void onPostExecute(Exception result) {
		_callback.operationCompleted(result);
	}

	public AsyncNotifyTask run() {
		execute((Void) null);
		return this;
	}
}
