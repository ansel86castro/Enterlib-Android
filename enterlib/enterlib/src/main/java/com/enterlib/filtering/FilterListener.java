package com.enterlib.filtering;

/**
 * <p>
 * Listener used to receive a notification upon completion of a filtering
 * operation.
 * </p>
 */
public interface FilterListener {
	/**
	 * <p>
	 * Notifies the end of a filtering operation.
	 * </p>
	 *
	 * @param count
	 *            the number of values computed by the filter
	 */
	public void onFilterComplete(Object values, int count);
}