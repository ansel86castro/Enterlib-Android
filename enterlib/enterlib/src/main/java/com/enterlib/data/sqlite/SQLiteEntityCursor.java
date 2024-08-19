package com.enterlib.data.sqlite;

import android.database.Cursor;
import android.widget.Filter;

import com.enterlib.StringUtils;
import com.enterlib.app.ConditionFilterHandler;
import com.enterlib.data.DataChangeNotify;
import com.enterlib.data.EntityCursorIterator;
import com.enterlib.data.IEntityCursor;
import com.enterlib.data.ISortable;
import com.enterlib.data.OnSortingListener;
import com.enterlib.data.SortingElement;
import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.FilterListener;
import com.enterlib.filtering.IFilterable;

import java.util.ArrayList;
import java.util.Iterator;

public class SQLiteEntityCursor<T> extends DataChangeNotify implements IEntityCursor<T> ,
		IFilterable,
		ConditionFilterHandler.Delayer,
		ISortable
{

	int lastFetchPosition = -1;

	Cursor cursor;
	EntityMap<T> entityFactory;
	SQLQuery<T>query;
	private FilterHandler filterHandler;
	private String filterExpression;
	private String sortExpression;
	private SortHandler sortHandler;


	public SQLiteEntityCursor(Cursor cursor, EntityMap<T> entityFactory, SQLQuery<T>query){
		this.cursor = cursor;
		this.entityFactory = entityFactory;
		this.query = query;
	}

	public void setSortExpression(String sortExpression){
		this.sortExpression = sortExpression;
	}

	public EntityMap<T> getEntityFactory() {
		return entityFactory;
	}

	@Override
	public long getPostingDelay(ArrayList<FilterCondition> constraints) {
		return 500;
	}

	@Override
	public void close() {
		if (cursor != null) {
			cursor.close();
			cursor = null;
		}
	}

	@Override
	public int getCount() {
		if(cursor != null)
			return cursor.getCount();
		return 0;
	}

	@Override
	public T getItem(int position) {
		if (position == lastFetchPosition + 1) {
			cursor.moveToNext();
		} else if (position == lastFetchPosition - 1) {
			cursor.moveToPrevious();
		} else {
			cursor.moveToPosition(position);
		}

		lastFetchPosition = position;

		return createEntity(cursor);
	}

	protected T createEntity(Cursor cursor){
		if(entityFactory!=null){
			return entityFactory.getFromCursor(cursor);
		}
		throw new RuntimeException("Not implemented");
	}

	@Override
	public  Iterator<T> iterator(){
		return new EntityCursorIterator<T>(this);
	}

	@Override
	public void doFilter(ArrayList<FilterCondition> fixedConditions,
						 FilterListener listener) {

		if (filterHandler == null) {
			filterHandler = new FilterHandler();
			filterHandler.setDelayer(this);
		}

		filterHandler.doFilter(fixedConditions, listener);
	}

	@Override
	public void sort(ArrayList<SortingElement> sorts, final OnSortingListener listener) {
		if(sorts == null || sorts.size() == 0){
			sortExpression = null;
		}else {
			sortExpression = StringUtils.aggregate(sorts, ", ", 0, sorts.size());
		}

		if(sortHandler == null){
			sortHandler = new SortHandler();
		}

		sortHandler.filter(sortExpression, new Filter.FilterListener() {
			@Override
			public void onFilterComplete(int count) {
				if(listener !=null){
					listener.onSortingCompleted();
				}
			}
		});
	}

	class FilterHandler extends ConditionFilterHandler {

		@Override
		protected FilterResults performFiltering(
				ArrayList<FilterCondition> constraints) {

			filterExpression = FilterCondition.getFilterExpString(constraints);

			SQLQuery<T> newQuery = query.clone();
			newQuery.where(filterExpression);
			if(sortExpression!=null){
				newQuery.orderBy(sortExpression);
			}

			Cursor cursor = entityFactory.getCursor(newQuery.toSql());
			FilterResults result = new FilterResults();
			result.values =cursor;
			result.count = cursor.getCount();
			return result;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(ArrayList<FilterCondition> constraints,
									  FilterResults results) {

			if(cursor!=null){
				cursor.close();
			}

			lastFetchPosition = -1;
			cursor = (Cursor) results.values;

            notifyDataChange();
		}

	}

	class SortHandler extends Filter {


		/**
		 * <p>Invoked in a worker thread to filter the data according to the
		 * constraint. Subclasses must implement this method to perform the
		 * filtering operation. Results computed by the filtering operation
		 * must be returned as a {@link FilterResults} that
		 * will then be published in the UI thread through
		 * {@link #publishResults(CharSequence,
		 * FilterResults)}.</p>
		 * <p/>
		 * <p><strong>Contract:</strong> When the constraint is null, the original
		 * data must be restored.</p>
		 *
		 * @param constraint the constraint used to filter the data
		 * @return the results of the filtering operation
		 * @see #filter(CharSequence, FilterListener)
		 * @see #publishResults(CharSequence, FilterResults)
		 * @see FilterResults
		 */
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {

			SQLQuery<T> newQuery = query.clone();
			newQuery.where(filterExpression);

			if(constraint!=null) {
				newQuery.orderBy(constraint.toString());
			}

			Cursor cursor = entityFactory.getCursor(newQuery.toSql());

			FilterResults result = new FilterResults();
			result.values =cursor;
			result.count = cursor.getCount();
			return result;
		}

		/**
		 * <p>Invoked in the UI thread to publish the filtering results in the
		 * user interface. Subclasses must implement this method to display the
		 * results computed in {@link #performFiltering}.</p>
		 *
		 * @param constraint the constraint used to filter the data
		 * @param results    the results of the filtering operation
		 * @see #filter(CharSequence, FilterListener)
		 * @see #performFiltering(CharSequence)
		 * @see FilterResults
		 */
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			if(cursor!=null){
				cursor.close();
			}

			lastFetchPosition = -1;
			cursor = (Cursor) results.values;

			notifyDataChange();
		}
	}
}
