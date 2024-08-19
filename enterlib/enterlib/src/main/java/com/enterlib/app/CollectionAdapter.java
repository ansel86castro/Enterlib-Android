package com.enterlib.app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.enterlib.StringUtils;
import com.enterlib.Utils;
import com.enterlib.filtering.FilterCondition;
import com.enterlib.filtering.FilterListener;
import com.enterlib.filtering.IFilterable;
import com.enterlib.widgets.OnDataChangeListener;


public class CollectionAdapter<T> extends BaseAdapter implements
		IFilterableAdapter, ISortableAdapter<T>, IFilterable,
		OnDataChangeListener, Filterable {
	/**
	 * Contains the list of objects that represent the data of this
	 * ArrayAdapter. The content of this list is referred to as "the array" in
	 * the documentation.
	 */
	protected List<T> mObjects;

	/**
	 * Lock used to modify the content of {@link #mObjects}. Any write operation
	 * performed on the array should be synchronized on this lock. This lock is
	 * also used by the filter (see {@link #getFilter()} to make a synchronized
	 * copy of the original array of data.
	 */
	protected final Object mLock = new Object();

	/**
	 * The resource indicating what views to inflate to display the content of
	 * this array adapter.
	 */
	private int mResource;

	/**
	 * The resource indicating what views to inflate to display the content of
	 * this array adapter in a drop down widget.
	 */
	private int mDropDownResource;

	/**
	 * If the inflated resource is not a TextView, {@link #mFieldId} is used to
	 * find a TextView inside the inflated views hierarchy. This field must
	 * contain the identifier that matches the one defined in the resource file.
	 */
	private int mFieldId = 0;

	/**
	 * Indicates whether or not {@link #notifyDataSetChanged()} must be called
	 * whenever {@link #mObjects} is modified.
	 */
	private boolean mNotifyOnChange = true;

	private Context mContext;

	// A copy of the original mObjects array, initialized from and then used
	// instead as soon as
	// the mFilter ArrayFilter is used. mObjects will then only contain the
	// filtered values.
	protected ArrayList<T> mOriginalValues;

	private Filter mFilter;

	private LayoutInflater mInflater;

	/**
	 * Used for extract the object text in filtering operations
	 * */
	// private IDisplayValueExtractor textExtractor;

	private IFilterPredicate<T> mPredicate;

	private CollectionAdapter<T>.AdapterConditionFilter mConditionFilter;

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 */
	public CollectionAdapter(Context context, int resource) {
		init(context, resource, 0, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 */
	public CollectionAdapter(Context context, int resource,
			int textViewResourceId) {
		init(context, resource, textViewResourceId, new ArrayList<T>());
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public CollectionAdapter(Context context, int resource, T[] objects) {
		init(context, resource, 0, Arrays.asList(objects));
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public CollectionAdapter(Context context, int resource,
			int textViewResourceId, T[] objects) {
		init(context, resource, textViewResourceId, Arrays.asList(objects));
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a TextView to use
	 *            when instantiating views.
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public CollectionAdapter(Context context, int resource, List<T> objects) {
		init(context, resource, 0, objects);
	}

	/**
	 * Constructor
	 *
	 * @param context
	 *            The current context.
	 * @param resource
	 *            The resource ID for a layout file containing a layout to use
	 *            when instantiating views.
	 * @param textViewResourceId
	 *            The id of the TextView within the layout resource to be
	 *            populated
	 * @param objects
	 *            The objects to represent in the ListView.
	 */
	public CollectionAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		init(context, resource, textViewResourceId, objects);
	}

	@Override
	public IFilterPredicate<T> getFilterPredicate() {
		return mPredicate;
	}

	@Override
	public void setFilterPredicate(IFilterPredicate<?> value) {
		this.mPredicate = (IFilterPredicate<T>) value;
	}

	public void setFilter(Filter value) {
		this.mFilter = value;
	}

	public ArrayList<T> geOriginalObjects() {
		return mOriginalValues;
	}

	public void storeObjects() {
		if (mOriginalValues == null) {
			mOriginalValues = new ArrayList<T>(mObjects);
		}
	}

	public List<T> getObjects() {
		return mObjects;
	}

	/**
	 * Adds the specified object at the end of the array.
	 *
	 * @param object
	 *            The object to add at the end of the array.
	 */
	public void add(T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(object);
			}
			mObjects.add(object);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified Collection at the end of the array.
	 *
	 * @param collection
	 *            The Collection to add at the end of the array.
	 */
	public void addAll(Collection<? extends T> collection) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.addAll(collection);
			}
			mObjects.addAll(collection);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Adds the specified items at the end of the array.
	 *
	 * @param items
	 *            The items to add at the end of the array.
	 */
	public void addAll(T... items) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.addAll(mOriginalValues, items);
			}
			Collections.addAll(mObjects, items);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Inserts the specified object at the specified index in the array.
	 *
	 * @param object
	 *            The object to insert into the array.
	 * @param index
	 *            The index at which the object must be inserted.
	 */
	public void insert(T object, int index) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.add(index, object);
			}
			mObjects.add(index, object);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Removes the specified object from the array.
	 *
	 * @param object
	 *            The object to remove.
	 */
	public void remove(T object) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues.remove(object);
			}
			mObjects.remove(object);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/**
	 * Remove all elements from the list.
	 */
	public void clear() {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				mOriginalValues = null;
			}
			mObjects.clear();
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	public void reset(Collection<T> items) {
		setNotifyOnChange(false);
		clear();
		setNotifyOnChange(true);
		addAll(items);
	}

	public void reset(Object items) {
		List<T> list = (List<T>) Utils.getList(items);
		setNotifyOnChange(false);
		clear();
		setNotifyOnChange(true);
		addAll(list);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.enterlib.app.ISortableAdapter#sort(java.util.Comparator)
	 */
	@Override
	public void sort(Comparator<? super T> comparator) {
		synchronized (mLock) {
			if (mOriginalValues != null) {
				Collections.sort(mOriginalValues, comparator);
			}
			Collections.sort(mObjects, comparator);
		}
		if (mNotifyOnChange) {
			notifyDataSetChanged();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.enterlib.app.ISortableAdapter#notifyDataSetChanged()
	 */

	@Override
	public void notifyDataSetChanged() {
		super.notifyDataSetChanged();
		mNotifyOnChange = true;
	}

	/**
	 * Control whether methods that change the list ({@link #add},
	 * {@link #insert}, {@link #remove}, {@link #clear}) automatically call
	 * {@link #notifyDataSetChanged}. If set to false, caller must manually call
	 * notifyDataSetChanged() to have the changes reflected in the attached
	 * view.
	 *
	 * The default is true, and calling notifyDataSetChanged() resets the flag
	 * to true.
	 *
	 * @param notifyOnChange
	 *            if true, modifications to the list will automatically call
	 *            {@link #notifyDataSetChanged}
	 */
	public void setNotifyOnChange(boolean notifyOnChange) {
		mNotifyOnChange = notifyOnChange;
	}

	private void init(Context context, int resource, int textViewResourceId,
			List<T> objects) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mResource = mDropDownResource = resource;
		mObjects = objects;
		mFieldId = textViewResourceId;
	}

	/**
	 * Returns the context associated with this array adapter. The context is
	 * used to create views from the resource passed to the constructor.
	 *
	 * @return The Context associated with this adapter.
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getCount() {
		return mObjects.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public T getItem(int position) {
		return mObjects.get(position);
	}

	/**
	 * Returns the position of the specified item in the array.
	 *
	 * @param item
	 *            The item to retrieve the position of.
	 *
	 * @return The position of the specified item.
	 */
	public int getPosition(T item) {
		return mObjects.indexOf(item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent, mResource);
	}

	private View createViewFromResource(int position, View convertView,
			ViewGroup parent, int resource) {
		View view;
		TextView text;

		if (convertView == null) {
			view = mInflater.inflate(resource, parent, false);
		} else {
			view = convertView;
		}

		try {
			if (mFieldId == 0) {
				// If no custom field is assigned, assume the whole resource is
				// a TextView
				text = (TextView) view;
			} else {
				// Otherwise, find the TextView field within the layout
				text = (TextView) view.findViewById(mFieldId);
			}
		} catch (ClassCastException e) {
			Log.e("ArrayAdapter",
					"You must supply a resource ID for a TextView");
			throw new IllegalStateException(
					"ArrayAdapter requires the resource ID to be a TextView", e);
		}

		T item = getItem(position);
		if (item instanceof CharSequence) {
			text.setText((CharSequence) item);
		} else {
			text.setText(item.toString());
		}

		return view;
	}

	/**
	 * <p>
	 * Sets the layout resource to create the drop down views.
	 * </p>
	 *
	 * @param resource
	 *            the layout resource defining the drop down views
	 * @see #getDropDownView(int, android.view.View, android.view.ViewGroup)
	 */
	public void setDropDownViewResource(int resource) {
		this.mDropDownResource = resource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		return createViewFromResource(position, convertView, parent,
				mDropDownResource);
	}

	/**
	 * Creates a new ArrayAdapter from external resources. The content of the
	 * array is obtained through
	 * {@link android.content.res.Resources#getTextArray(int)}.
	 *
	 * @param context
	 *            The application's environment.
	 * @param textArrayResId
	 *            The identifier of the array to use as the data source.
	 * @param textViewResId
	 *            The identifier of the layout used to create views.
	 *
	 * @return An ArrayAdapter<CharSequence>.
	 */
	public static ArrayAdapter<CharSequence> createFromResource(
			Context context, int textArrayResId, int textViewResId) {
		CharSequence[] strings = context.getResources().getTextArray(
				textArrayResId);
		return new ArrayAdapter<CharSequence>(context, textViewResId, strings);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Filter getFilter() {
		if (mFilter == null) {
			mFilter = new ArrayFilter();
		}
		return mFilter;
	}

	/**
	 * <p>
	 * An array filter constrains the content of the array adapter with a
	 * prefix. Each item that does not start with the supplied prefix is removed
	 * from the list.
	 * </p>
	 */
	private class ArrayFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence prefix) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<T>(mObjects);
				}
			}

			if (prefix == null || prefix.length() == 0) {
				ArrayList<T> list;
				synchronized (mLock) {
					list = new ArrayList<T>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {

				String prefixString = prefix.toString().toLowerCase(
						Locale.getDefault());
				ArrayList<T> values;
				synchronized (mLock) {
					values = new ArrayList<T>(mOriginalValues);
				}

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<T>();

				for (int i = 0; i < count; i++) {
					final T value = values.get(i);
					String toString = value.toString();
					if(toString == null)
						toString = "";

					if (mPredicate != null) {
						if (mPredicate.eval(prefixString,
								CollectionAdapter.this, value)) {
							newValues.add(value);
						}
					} else if (StringUtils.startsWordWith(prefixString, toString.toLowerCase())) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();
			}

			return results;
		}

		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			// noinspection unchecked
			mObjects = (List<T>) results.values;
			if(mObjects == null)
				mObjects = new ArrayList<T>();
			if (results.count > 0) {
				notifyDataSetChanged();
			} else {
				notifyDataSetInvalidated();
			}
		}
	}

	private class AdapterConditionFilter extends ConditionFilterHandler {

		@Override
		protected FilterResults performFiltering(
				ArrayList<FilterCondition> constraints) {
			FilterResults results = new FilterResults();

			if (mOriginalValues == null) {
				synchronized (mLock) {
					mOriginalValues = new ArrayList<T>(mObjects);
				}
			}

			if (constraints.size() == 0) {
				ArrayList<T> list;
				synchronized (mLock) {
					list = new ArrayList<T>(mOriginalValues);
				}
				results.values = list;
				results.count = list.size();
			} else {

				ArrayList<T> values;
				synchronized (mLock) {
					values = new ArrayList<T>(mOriginalValues);
				}

				final int count = values.size();
				final ArrayList<T> newValues = new ArrayList<T>();

				for (int i = 0; i < count; i++) {
					T value = values.get(i);
					boolean suceed = true;

					for (int j = 0; j < constraints.size(); j++) {
						FilterCondition c = constraints.get(j);
						if (!c.eval(value)) {
							suceed = false;
							break;
						}
					}
					if (suceed) {
						newValues.add(value);
					}
				}

				results.values = newValues;
				results.count = newValues.size();

			}

			return results;
		}

		@Override
		protected void publishResults(ArrayList<FilterCondition> constraints,
				FilterResults results) {
			// noinspection unchecked
			mObjects = (List<T>) results.values;
			if(mObjects == null)
                mObjects = new ArrayList<T>();

            notifyDataSetChanged();
		}

	}

	@Override
	public void doFilter(ArrayList<FilterCondition> fixedConditions,
			FilterListener listener) {
		if (mConditionFilter == null) {
			mConditionFilter = new AdapterConditionFilter();
		}
		mConditionFilter.doFilter(fixedConditions, listener);

	}

	@Override
	public void onAdd(Object item) {
		mObjects.add((T) item);
		notifyDataSetChanged();
	}

	@Override
	public void onRemove(Object item, int position) {
		mObjects.remove(item);
		notifyDataSetChanged();
	}

	@Override
	public void filter(CharSequence constraint,
			android.widget.Filter.FilterListener listener) {
		
		getFilter().filter(constraint, listener);
		
	}
}
