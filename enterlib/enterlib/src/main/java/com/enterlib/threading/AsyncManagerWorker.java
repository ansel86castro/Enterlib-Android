package com.enterlib.threading;

import android.util.Log;

public class AsyncManagerWorker implements Runnable {

	static class UIRunnable implements Runnable {
		public Exception workException;
		private IWorkPost post;

		public UIRunnable(IWorkPost post) {
			this.post = post;
		}

		@Override
		public void run() {
			post.onWorkFinish(workException);
		}
	}

	IWorkPost post;
	UIRunnable uiRunnable;

	public AsyncManagerWorker(IWorkPost post) {
		this.post = post;
		uiRunnable = new UIRunnable(post);
	}

	@Override
	public void run() {
		uiRunnable.workException = null;
		boolean callonFinish;
		try {
			callonFinish = post.runWork();
		} catch (Exception e) {
			Log.d(AsyncManagerWorker.class.getName() , e.getMessage(), e);
			uiRunnable.workException = e;
			callonFinish = true;
		}

		if (callonFinish) {
			AsyncManager.postUISync(uiRunnable);
		}
	}

}
