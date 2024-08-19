package com.enterlib.app;


import java.util.ArrayList;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.FilterListener;
import com.enterlib.filtering.IFilterable;

/**
 * <p>
 * A filter constrains data with a filtering pattern.
 * </p>
 *
 * <p>
 * Filters are usually created by {@link Filterable} classes.
 * </p>
 *
 * <p>
 * Filtering operations performed by calling CharSequence or
 * CharSequence, Filter.FilterListener are
 * performed asynchronously. When these methods are called, a filtering request
 * is posted in a request queue and processed later. Any call to one of these
 * methods will cancel any previous non-executed filtering request.
 * </p>
 *
 * @see Filterable
 */
public abstract class ConditionFilterHandler implements IFilterable {

	private static final String LOG_TAG = "Filter";

	private static final String THREAD_NAME = "Filter";
	private static final int FILTER_TOKEN = 0xD0D0F00D;
	private static final int FINISH_TOKEN = 0xDEADBEEF;

	private Handler mThreadHandler;
	private Handler mResultHandler;

	private Delayer mDelayer;

	protected final Object mLock = new Object();

	/**
	 * <p>
	 * Creates a new asynchronous filter.
	 * </p>
	 */
	public ConditionFilterHandler() {
		mResultHandler = new ResultsHandler();
	}

	/**
	 * Provide an interface that decides how long to delay the message for a
	 * given query. Useful for heuristics such as posting a delay for the delete
	 * key to avoid doing any work while the user holds down the delete key.
	 *
	 * @param delayer
	 *            The delayer.
	 * @hide
	 */
	public void setDelayer(Delayer delayer) {
		synchronized (mLock) {
			mDelayer = delayer;
		}
	}

	/**
	 * <p>
	 * Starts an asynchronous filtering operation. Calling this method cancels
	 * all previous non-executed filtering requests and posts a new filtering
	 * request that will be executed later.
	 * </p>
	 *
	 * <p>
	 * Upon completion, the listener is notified.
	 * </p>
	 */

	@Override
	public final void doFilter(ArrayList<FilterCondition> constraints,
			FilterListener listener) {
		synchronized (mLock) {
			if (mThreadHandler == null) {
				HandlerThread thread = new HandlerThread(THREAD_NAME,
						Process.THREAD_PRIORITY_BACKGROUND);
				thread.start();
				mThreadHandler = new RequestHandler(thread.getLooper());
			}

			final long delay = (mDelayer == null) ? 0 : mDelayer
					.getPostingDelay(constraints);

			Message message = mThreadHandler.obtainMessage(FILTER_TOKEN);

			RequestArguments args = new RequestArguments();
			// make sure we use an immutable copy of the constraint, so that
			// it doesn't change while the filter operation is in progress
			args.constraints = constraints;
			args.listener = listener;
			message.obj = args;

			mThreadHandler.removeMessages(FILTER_TOKEN);
			mThreadHandler.removeMessages(FINISH_TOKEN);
			mThreadHandler.sendMessageDelayed(message, delay);
		}
	}

	/**
	 * <p>
	 * Invoked in a worker thread to filter the data according to the
	 * constraint. Subclasses must implement this method to perform the
	 * filtering operation. Results computed by the filtering operation must be
	 * returned as a Filter.FilterResults that will then
	 * be published in the UI thread through
	 * .
	 * </p>
	 *
	 * <p>
	 * <strong>Contract:</strong> When the constraint is null, the original data
	 * must be restored.
	 * </p>
	 *
	 * @param constraints
	 *            the constraint used to filter the data
	 * @return the results of the filtering operation
	 *
	 */
	protected abstract FilterResults performFiltering(
			ArrayList<FilterCondition> constraints);

	/**
	 * <p>
	 * Invoked in the UI thread to publish the filtering results in the user
	 * interface. Subclasses must implement this method to display the results
	 * computed in {@link #performFiltering}.
	 * </p>
	 *
	 * @param constraints
	 *            the constraint used to filter the data
	 * @param results
	 *            the results of the filtering operation
	 *
	 */
	protected abstract void publishResults(
			ArrayList<FilterCondition> constraints, FilterResults results);

	/**
	 * <p>
	 * Converts a value from the filtered set into a CharSequence. Subclasses
	 * should override this method to convert their results. The default
	 * implementation returns an empty String for null values or the default
	 * String representation of the value.
	 * </p>
	 *
	 * @param resultValue
	 *            the value to convert to a CharSequence
	 * @return a CharSequence representing the value
	 */
	public CharSequence convertResultToString(Object resultValue) {
		return resultValue == null ? "" : resultValue.toString();
	}

	/**
	 * <p>
	 * Holds the results of a filtering operation. The results are the values
	 * computed by the filtering operation and the number of these values.
	 * </p>
	 */
	protected static class FilterResults {
		public FilterResults() {
			// nothing to see here
		}

		/**
		 * <p>
		 * Contains all the values computed by the filtering operation.
		 * </p>
		 */
		public Object values;

		/**
		 * <p>
		 * Contains the number of values computed by the filtering operation.
		 * </p>
		 */
		public int count;
	}

	/**
	 * <p>
	 * Worker thread handler. When a new filtering request is posted from
	 * {@link Filter#filter(CharSequence, Filter.FilterListener)}
	 * , it is sent to this handler.
	 * </p>
	 */
	private class RequestHandler extends Handler {
		public RequestHandler(Looper looper) {
			super(looper);
		}

		/**
		 * <p>
		 * Handles filtering requests by calling
         * and then sending a message with the results to the results handler.
		 * </p>
		 *
		 * @param msg
		 *            the filtering request
		 */
		@Override
		public void handleMessage(Message msg) {
			int what = msg.what;
			Message message;
			switch (what) {
			case FILTER_TOKEN:
				RequestArguments args = (RequestArguments) msg.obj;
				try {
					args.results = performFiltering(args.constraints);
				} catch (Exception e) {
					args.results = new FilterResults();
					Log.e(LOG_TAG,"An exception occured during performFiltering()!",e);
				} finally {
					message = mResultHandler.obtainMessage(what);
					message.obj = args;
					message.sendToTarget();
				}

				synchronized (mLock) {
					if (mThreadHandler != null) {
						Message finishMessage = mThreadHandler
								.obtainMessage(FINISH_TOKEN);
						mThreadHandler.sendMessageDelayed(finishMessage, 3000);
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
	private class ResultsHandler extends Handler {
		/**
		 * <p>
		 * Messages received from the request handler are processed in the UI
		 * thread. The processing involves calling
		 * to post the results back in the UI and then notifying the listener,
		 * if any.
		 * </p>
		 *
		 * @param msg
		 *            the filtering results
		 */
		@Override
		public void handleMessage(Message msg) {
			RequestArguments args = (RequestArguments) msg.obj;

			publishResults(args.constraints, args.results);
			if (args.listener != null) {
				if (args.results != null) {
					args.listener.onFilterComplete(args.results.values,
							args.results.count);
				} else {
					args.listener.onFilterComplete(null, -1);
				}
			}
		}
	}

	/**
	 * <p>
	 * Holds the arguments of a filtering request as well as the results of the
	 * request.
	 * </p>
	 */
	private static class RequestArguments {
		/**
		 * <p>
		 * The constraint used to filter the data.
		 * </p>
		 */
		ArrayList<FilterCondition> constraints;

		/**
		 * <p>
		 * The listener to notify upon completion. Can be null.
		 * </p>
		 */
		FilterListener listener;

		/**
		 * <p>
		 * The results of the filtering operation.
		 * </p>
		 */
		FilterResults results;
	}

	/**
	 * @hide
	 */
	public interface Delayer {

		/**
		 * @param constraints
		 *            The constraint passed to
		 * @return The delay that should be used for
		 *         {@link Handler#sendMessageDelayed(Message, long)}
		 */
		long getPostingDelay(ArrayList<FilterCondition> constraints);
	}

}
