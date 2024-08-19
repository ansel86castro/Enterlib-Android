package com.enterlib.app;

import java.util.Comparator;

public interface ISortableAdapter<T> extends android.widget.ListAdapter {

	/**
	 * Sorts the content of this adapter using the specified comparator.
	 *
	 * @param comparator
	 *            The comparator used to sort the objects contained in this
	 *            adapter.
	 */
	void sort(Comparator<? super T> comparator);

	/**
	 * {@inheritDoc}
	 */
	void notifyDataSetChanged();

}