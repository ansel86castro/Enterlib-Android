package com.enterlib.threading;

import java.util.UUID;

import android.os.*;
import android.util.Log;

public class LoaderHandler {
	private static final String LOG_TAG = LoaderHandler.class.getName();
	private static final String THREAD_NAME = "LoeaderHandleThread";
	private static final int TASK_TOKEN = 0xD0D0F00D;
	private static final int FINISH_TOKEN = 0xDEADBEEF;
	
	private static int instanceCount = 0;

	
	/**
	 * @hide
	 */
	public interface Delayer {
		long getPostingDelay();
	}

	public static interface LoadTask {
		Object runAsync(Object args) throws Exception;

		void onComplete(Object result, Exception e);
	}

	static class RequestArguments {
		public LoadTask Task;
		public Exception TaskException;
		public Object Args;
		public Object Result;
	}

	HandlerThread thread;
	private RequestHandler mThreadHandler;
	private Handler mResultHandler;
	private Delayer mDelayer;
	private boolean mClearOnPost;
	boolean autofinish = true;
	private int index;

	public boolean isAutofinish() {
		return autofinish;
	}

	public void setAutofinish(boolean autofinish) {
		this.autofinish = autofinish;
	}

	protected final Object mLock = new Object();

	public LoaderHandler() {
		mResultHandler = new ResultsHandler();
		index = instanceCount++;
	}

	public LoaderHandler(boolean clearOnPost, Delayer delayer) {
		this();

		mClearOnPost = clearOnPost;
		mDelayer = delayer;	
	}

	public void setDelayer(Delayer delayer) {
		synchronized (mLock) {
			mDelayer = delayer;
		}
	}

	public void setClearOnPost(boolean value) {
		this.mClearOnPost = value;
	}

	public boolean getClearOnPost() {
		return mClearOnPost;
	}

	public void postTask(LoadTask task) {
		postTask(task, null);
	}

	public void postTask(LoadTask task, Object data) {
		Log.d(LOG_TAG, "called postTask");

		synchronized (mLock) {
			if (mThreadHandler == null || thread == null || thread.getState() == Thread.State.TERMINATED) {
				thread = new HandlerThread(THREAD_NAME , android.os.Process.THREAD_PRIORITY_DEFAULT);
				thread.start();
				mThreadHandler = new RequestHandler(thread.getLooper());
				Log.d(LOG_TAG, "Thread Created");
			}

			Message message = mThreadHandler.obtainMessage(TASK_TOKEN);

			RequestArguments arg = new RequestArguments();
			arg.Task = task;
			arg.Args = data;
			message.obj = arg;

			if (mClearOnPost) {
				mThreadHandler.removeMessages(TASK_TOKEN);
			}

			if (autofinish) {
				mThreadHandler.removeMessages(FINISH_TOKEN);
			}

			Log.d(LOG_TAG, "Posting Task");

			if (mDelayer == null) {
				mThreadHandler.sendMessage(message);
			} else {
				mThreadHandler.sendMessageDelayed(message, mDelayer.getPostingDelay());
			}
		}

	}

	public void stop() {
		if (mThreadHandler != null) {
			mThreadHandler.getLooper().quit();
			mThreadHandler = null;
		}
	}

	private class RequestHandler extends Handler {
		public RequestHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			Message message;
			switch (what) {
			case TASK_TOKEN:
				RequestArguments request = (RequestArguments) msg.obj;
				try {
					Log.d(LOG_TAG, "Running Task");
					request.Result = request.Task.runAsync(request.Args);
				} catch (Exception e) {
					request.TaskException = e;
					Log.d(LOG_TAG,
							"An exception occured during the task execution()!",
							e);
				} finally {
					message = mResultHandler.obtainMessage(what);
					message.obj = request;
					message.sendToTarget();
				}
				if (autofinish) {
					synchronized (mLock) {
						if (mThreadHandler != null) {
							Message finishMessage = mThreadHandler.obtainMessage(FINISH_TOKEN);
							mThreadHandler.sendMessageDelayed(finishMessage, 3000);
						}
					}
				}
				break;
			case FINISH_TOKEN:
				synchronized (mLock) {
					if (mThreadHandler != null) {
						mThreadHandler.getLooper().quit();
						mThreadHandler = null;
					}
				}
				break;
			}
		}
	}

	/**
	 * <p>
	 * Handles the results of a filtering operation. The results are handled in
	 * the UI thread.
	 * </p>
	 */
	private static class ResultsHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			RequestArguments request = (RequestArguments) msg.obj;

			Log.d(LOG_TAG, "Completed Task");

			request.Task.onComplete(request.Result, request.TaskException);
		}
	}
}
