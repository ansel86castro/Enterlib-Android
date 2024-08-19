package com.enterlib.threading;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;

/**
 * @author ansel Provides async utilities like invoking asyncronic taks with
 *         report callback when the task is finish
 */
public class AsyncManager {
	ExecutorService executor;
	Handler uiHandler;
	LooperThread looper;
	Object task;
	CyclicBarrier barrier = new CyclicBarrier(2);

	static AsyncManager manager;

	static class RunParam<T> {
		public Exception ex;
		public T result;
	}

	class LooperThread extends Thread {
		public Handler mHandler;

		@Override
		public void run() {
			Looper.prepare();

			mHandler = new Handler();
			try {
				barrier.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			Looper.loop();
		}

		public boolean post(Runnable callback) {
			return mHandler.post(callback);
		}

		public void stopLooper() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Looper myLooper = Looper.myLooper();
					myLooper.quit();
				}
			});
		}
	}

	private AsyncManager() {
		uiHandler = new Handler();
	}

	@Override
	protected void finalize() throws Throwable {
		stopLooper();
	}

	private void stopLooper() {
		if (looper != null) {
			looper.stopLooper();
		}
	}

	private void _startAsyncLooper() {
		if (looper == null) {
			looper = new LooperThread();
			looper.start();

			try {
				// waits Sync.wait(); created
				barrier.await();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			barrier.reset();
		}
	}

	public static AsyncManager getInstance() {
		if (manager == null) {
			manager = new AsyncManager();
		}
		return manager;
	}

	public ExecutorService GetExecutor() {
		if (executor == null) {
			executor = Executors.newFixedThreadPool(1);
		}
		return executor;
	}

	/**
	 * Invoke the loader
	 * */
	public static void InvokeAsync(final IAsyncInvocator loader) {
		InvokeAsync(loader, null);
	}

	public static void InvokeAsync(final IAsyncInvocator loader,
			final Dialog dialog) {
		getInstance()._InvokeAsync(loader, dialog);
	}

	/**
	 * Invoke the loader, also displays the progress dialog while the task is
	 * running
	 * */
	private void _InvokeAsync(final IAsyncInvocator loader, final Dialog dialog) {
		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}
		new AsyncTask<Void, Void, Exception>() {

			@Override
			protected Exception doInBackground(Void... arg0) {
				try {
					loader.DoAsyncOperation();
				} catch (Exception e) {
					e.printStackTrace();
					return e;
				}
				return null;
			}

			@Override
			protected void onPostExecute(Exception e) {
				if (e != null) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					loader.OnAsyncOperationException(e);
				} else {
					loader.OnAsyncOperationComplete();
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				}
			}

		}.execute((Void) null);
	}

	/**
	 * Run the AsynWorker
	 * */
	public static <T> void InvokeAsync(final IAsyncWorker<T> worker) {
		InvokeAsync(worker, null);
	}

	public static <T> void InvokeAsync(final IAsyncWorker<T> worker,
			final Dialog dialog) {
		getInstance()._InvokeAsync(worker, dialog);
	}

	private <T> void _InvokeAsync(final IAsyncWorker<T> worker,
			final Dialog dialog) {

		// CatApplication.getInstance().GetProgressBar().show();
		// final ProgressDialog progresbar = new
		// ProgressDialog(CatApplication.getInstance());
		// progresbar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		// progresbar.setMessage(CatApplication.getInstance().getString(R.string.loading));
		// progresbar.setCancelable(true);
		// progresbar.show();

		if (dialog != null && !dialog.isShowing()) {
			dialog.show();
		}

		task = new AsyncTask<Void, Void, RunParam<T>>() {

			@Override
			protected RunParam<T> doInBackground(Void... arg0) {
				RunParam<T> r = new RunParam<T>();
				try {
					r.result = worker.DoAsyncWork();
				} catch (Exception e) {
					e.printStackTrace();
					r.ex = e;
				}
				return r;
			}

			@Override
			protected void onPostExecute(RunParam<T> result) {
				if (result.ex != null) {
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
					worker.OnException(result.ex);
				} else {
					worker.OnComplete(result.result);
					if (dialog != null && dialog.isShowing()) {
						dialog.dismiss();
					}
				}
			}

		}.execute((Void) null);
	}

	/**
	 * Schedule a task to be executed in a async task. Do not modified the UI
	 * from the runnable passed
	 */
	public static boolean postAsync(Runnable runnable) {
		AsyncManager manager = getInstance();
		return manager._postAsync(runnable);
	}

	public static boolean postAsync(IWorkPost workPost) {
		AsyncManager manager = getInstance();
		return manager._postAsync(workPost);
	}

	public static boolean postUISync(Runnable runnable) {
		return getInstance()._postUIAsync(runnable);
	}

	private boolean _postAsync(Runnable runnable) {
		if (looper == null) {
			_startAsyncLooper();
		}
		return looper.post(runnable);
	}

	private boolean _postUIAsync(Runnable runnable) {
		return uiHandler.post(runnable);

	}

	private boolean _postAsync(IWorkPost workPost) {
		return _postAsync(new AsyncManagerWorker(workPost));
	}
}
