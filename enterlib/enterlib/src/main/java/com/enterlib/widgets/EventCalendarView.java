package com.enterlib.widgets;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.enterlib.R;

public class EventCalendarView extends FrameLayout {

	/**
	 * Tag for logging.
	 */
	private static final String LOG_TAG = EventCalendarView.class
			.getSimpleName();

	/**
	 * The number of milliseconds in a day.e
	 */
	private static final long MILLIS_IN_DAY = 86400000L;

	/**
	 * The number of day in a week.
	 */
	private static final int DAYS_PER_WEEK = 7;

	/**
	 * The number of milliseconds in a week.
	 */
	private static final long MILLIS_IN_WEEK = DAYS_PER_WEEK * MILLIS_IN_DAY;

	/**
	 * Affects when the month selection will change while scrolling upe
	 */
	private static final int SCROLL_HYST_WEEKS = 2;

	/**
	 * The duration of the adjustment upon a user scroll in milliseconds.
	 */
	private static final int ADJUSTMENT_SCROLL_DURATION = 500;

	/**
	 * How long to wait after receiving an onScrollStateChanged notification
	 * before acting on it.
	 */
	private static final int SCROLL_CHANGE_DELAY = 40;

	/**
	 * String for parsing dates.
	 */
	private static final String DATE_FORMAT = "MM/dd/yyyy";

	/**
	 * The default minimal date.
	 */
	private static final String DEFAULT_MIN_DATE = "01/01/1900";

	/**
	 * The default maximal date.
	 */
	private static final String DEFAULT_MAX_DATE = "01/01/2100";

	private static final int DEFAULT_DATE_TEXT_SIZE = 14;

	private static final int DEFAULT_SHOWN_WEEK_COUNT = 5;

	private static final int UNSCALED_WEEK_MIN_VISIBLE_HEIGHT = 12;

	private static final int UNSCALED_LIST_SCROLL_TOP_OFFSET = 2;

	private static final int UNSCALED_BOTTOM_BUFFER = 20;

	private static final int UNSCALED_WEEK_SEPARATOR_LINE_WIDTH = 1;

	private static final int UNSCALED_EVENT_MARGIN = 1;

	private static final int UNSCALED_ALL_DAY_EVENT_OFFSET = 7;
	/**
	 * The top offset of the weeks list.
	 */
	private int mListScrollTopOffset = 2;

	/**
	 * The visible height of a week view.
	 */
	private int mWeekMinVisibleHeight = 12;

	/**
	 * The visible height of a week view.
	 */
	private int mBottomBuffer = 20;

	/**
	 * The number of shown weeks.
	 */
	private int mShownWeekCount;

	/**
	 * Flag whether to show the week number.
	 */
	private boolean mShowWeekNumber;

	/**
	 * The number of day per week to be shown.
	 */
	private int mDaysPerWeek = 7;

	/**
	 * The friction of the week list while flinging.
	 */
	private float mFriction = .05f;

	/**
	 * Scale for adjusting velocity of the week list while flinging.
	 */
	private float mVelocityScale = 0.333f;

	/**
	 * The adapter for the weeks list.
	 */
	private WeeksAdapter mAdapter;

	/**
	 * The weeks list.
	 */
	private ListView mListView;

	/**
	 * The header with week day names.
	 */
	private ViewGroup mDayNamesHeader;
	/**
	 * The first day of the week.
	 */
	private int mFirstDayOfWeek = Calendar.MONDAY;

	/**
	 * Which month should be displayed/highlighted [0-11].
	 */
	private int mCurrentMonthDisplayed = -1;

	/**
	 * Used for tracking during a scroll.
	 */
	private long mPreviousScrollPosition;

	/**
	 * Used for tracking which direction the view is scrolling.
	 */
	private boolean mIsScrollingUp = false;

	/**
	 * The previous scroll state of the weeks ListView.
	 */
	private int mPreviousScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	/**
	 * The current scroll state of the weeks ListView.
	 */
	private int mCurrentScrollState = OnScrollListener.SCROLL_STATE_IDLE;

	/**
	 * Command for adjusting the position after a scroll/fling.
	 */
	private ScrollStateRunnable mScrollStateChangedRunnable = new ScrollStateRunnable();

	/**
	 * Temporary instance to avoid multiple instantiations.
	 */
	private Calendar mTempDate;

	/**
	 * The first day of the focused month.
	 */
	private Calendar mFirstDayOfMonth;

	/**
	 * The start date of the range supported by this picker.
	 */
	private Calendar mMinDate;

	/**
	 * The end date of the range supported by this picker.
	 */
	private Calendar mMaxDate;

	/**
	 * Date format for parsing dates.
	 */
	private final java.text.DateFormat mDateFormat = new SimpleDateFormat(
			DATE_FORMAT);

	/**
	 * The current locale.
	 */
	private Locale mCurrentLocale;

	private int mWeekSeperatorLineWidth = 1;

	private int mWeekSeparatorLineColor;

	private int mFocusedMonthDateColor;

	private int mUnfocusedMonthDateColor;

	private int mFocusedMonthNumberColor;

	private int mUnfocusedMonthNumberColor;

	private int mEventColor;

	private int mAllDayEventColor;

	private int mMarginEvent;

	private int mAllDayEventOffset;
	/**
	 * Listener for changes in the selected day.
	 */
	private OnDateChangeListener mOnDateChangeListener;

	private OnMonthChangeListener mOnMonthChangeListerner;

	private SparseArray<EventCalendarView.DayEvents> mEvents = new SparseArray<EventCalendarView.DayEvents>();

	/**
	 * The callback used to indicate the user changes the date.
	 */
	public interface OnDateChangeListener {

		/**
		 * Called upon change of the selected day.
		 *
		 * @param view
		 *            The view associated with this listener.
		 * @param year
		 *            The year that was set.
		 * @param month
		 *            The month that was set [0-11].
		 * @param dayOfMonth
		 *            The day of the month that was set.
		 */
		public void onSelectedDayChange(EventCalendarView view, int year,
				int month, int dayOfMonth);
	}

	public interface OnMonthChangeListener {
		public void onMonthChange(EventCalendarView view, int currentMonth,
				String monthName, Calendar calendar);
	}

	public EventCalendarView(Context context) {
		this(context, null);

	}

	public EventCalendarView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EventCalendarView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, 0);

		setCurrentLocale(Locale.getDefault());

		parseDate(DEFAULT_MIN_DATE, mMinDate);

		parseDate(DEFAULT_MAX_DATE, mMaxDate);

		if (mMaxDate.before(mMinDate)) {
			throw new IllegalArgumentException(
					"Max date cannot be before min date.");
		}

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
		mWeekMinVisibleHeight = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_WEEK_MIN_VISIBLE_HEIGHT,
				displayMetrics);
		mListScrollTopOffset = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_LIST_SCROLL_TOP_OFFSET,
				displayMetrics);
		mBottomBuffer = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_BOTTOM_BUFFER,
				displayMetrics);

		mWeekSeperatorLineWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP,
				UNSCALED_WEEK_SEPARATOR_LINE_WIDTH, displayMetrics);

		mMarginEvent = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_EVENT_MARGIN,
				displayMetrics);

		mAllDayEventOffset = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_ALL_DAY_EVENT_OFFSET,
				displayMetrics);

		mShownWeekCount = DEFAULT_SHOWN_WEEK_COUNT;

		Resources res = context.getResources();
		mWeekSeparatorLineColor = res
				.getColor(R.color.calendar_month_line_separator);
		mFocusedMonthDateColor = res
				.getColor(R.color.calendar_focused_month_day);
		mUnfocusedMonthDateColor = res
				.getColor(R.color.calendar_unfocused_month);
		mEventColor = res.getColor(R.color.calendar_event);
		mAllDayEventColor = res.getColor(R.color.gray);

		mFocusedMonthNumberColor = res
				.getColor(R.color.calendar_focused_month_number);
		mUnfocusedMonthNumberColor = res
				.getColor(R.color.calendar_unfocused_month_number);

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View content = layoutInflater.inflate(R.layout.event_calendar_view,
				null, false);
		addView(content);

		mListView = (ListView) findViewById(R.id.weekList);
		mDayNamesHeader = (ViewGroup) content.findViewById(R.id.day_names);

		setUpListView();
		setUpAdapter();

		goTo(Calendar.getInstance());

	}

	private void goTo(Calendar date) {
		if (date.before(mMinDate) || date.after(mMaxDate)) {
			throw new IllegalArgumentException("Time not between "
					+ mMinDate.getTime() + " and " + mMaxDate.getTime());
		}
		// Find the first and last entirely visible weeks
		int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
		View firstChild = mListView.getChildAt(0);
		if (firstChild != null && firstChild.getTop() < 0) {
			firstFullyVisiblePosition++;
		}
		int lastFullyVisiblePosition = firstFullyVisiblePosition
				+ mShownWeekCount - 1;
		if (firstChild != null && firstChild.getTop() > mBottomBuffer) {
			lastFullyVisiblePosition--;
		}
		// Get the week we're going to
		int position = getWeeksSinceMinDate(date);
		// Check if the selected day is now outside of our visible range
		// and if so scroll to the month that contains it
		if (position < firstFullyVisiblePosition
				|| position > lastFullyVisiblePosition) {
			mFirstDayOfMonth.setTimeInMillis(date.getTimeInMillis());
			mFirstDayOfMonth.set(Calendar.DAY_OF_MONTH, 1);

			setMonthDisplayed(mFirstDayOfMonth);

			// the earliest time we can scroll to is the min date
			if (mFirstDayOfMonth.before(mMinDate)) {
				position = 0;
			} else {
				position = getWeeksSinceMinDate(mFirstDayOfMonth);
			}

			mPreviousScrollState = OnScrollListener.SCROLL_STATE_FLING;

			mListView.setSelectionFromTop(position, mListScrollTopOffset);
			// Perform any after scroll operations that are needed
			onScrollStateChanged(mListView, OnScrollListener.SCROLL_STATE_IDLE);

		}

	}

	public int getCurrentMonthDisplayed() {
		return mCurrentMonthDisplayed;
	}

	public void setOnMonthChangeListerner(OnMonthChangeListener listener) {
		this.mOnMonthChangeListerner = listener;
	}

	public void setOnDateChangeListener(OnDateChangeListener listener) {
		this.mOnDateChangeListener = listener;
	}

	/**
	 * Sets the current locale.
	 *
	 * @param locale
	 *            The current locale.
	 */
	private void setCurrentLocale(Locale locale) {
		if (locale.equals(mCurrentLocale)) {
			return;
		}

		mCurrentLocale = locale;

		mTempDate = getCalendarForLocale(mTempDate, locale);
		mFirstDayOfMonth = getCalendarForLocale(mFirstDayOfMonth, locale);
		mMinDate = getCalendarForLocale(mMinDate, locale);
		mMaxDate = getCalendarForLocale(mMaxDate, locale);
	}

	/**
	 * Gets a calendar for locale bootstrapped with the value of a given
	 * calendar.
	 *
	 * @param oldCalendar
	 *            The old calendar.
	 * @param locale
	 *            The locale.
	 */
	private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
		if (oldCalendar == null) {
			return Calendar.getInstance(locale);
		} else {
			final long currentTimeMillis = oldCalendar.getTimeInMillis();
			Calendar newCalendar = Calendar.getInstance(locale);
			newCalendar.setTimeInMillis(currentTimeMillis);
			return newCalendar;
		}
	}

	/**
	 * Parses the given <code>date</code> and in case of success sets the result
	 * to the <code>outDate</code>.
	 *
	 * @return True if the date was parsed.
	 */
	private boolean parseDate(String date, Calendar outDate) {
		try {
			outDate.setTime(mDateFormat.parse(date));
			return true;
		} catch (ParseException e) {
			Log.w(LOG_TAG, "Date: " + date + " not in format: " + DATE_FORMAT);
			return false;
		}
	}

	// /**
	// * Sets up the strings to be used by the header.
	// */
	// private void setUpHeader() {
	// final String[] tinyWeekdayNames = new String[7];
	// Calendar calendar = Calendar.getInstance(Locale.getDefault());
	//
	// for (int i = 0; i < tinyWeekdayNames.length; i++) {
	// tinyWeekdayNames[i]=calendar.getDisplayName(Calendar.Da, style, locale)
	// }
	//
	// mDayLabels = new String[mDaysPerWeek];
	// for (int i = 0; i < mDaysPerWeek; i++) {
	// final int j = i + mFirstDayOfWeek;
	// final int calendarDay = (j > Calendar.SATURDAY) ? j - Calendar.SATURDAY :
	// j;
	// mDayLabels[i] = tinyWeekdayNames[calendarDay];
	// }
	// // Deal with week number
	// TextView label = (TextView) mDayNamesHeader.getChildAt(0);
	// if (mShowWeekNumber) {
	// label.setVisibility(View.VISIBLE);
	// } else {
	// label.setVisibility(View.GONE);
	// }
	// // Deal with day labels
	// final int count = mDayNamesHeader.getChildCount();
	// for (int i = 0; i < count - 1; i++) {
	// label = (TextView) mDayNamesHeader.getChildAt(i + 1);
	// if (i < mDaysPerWeek) {
	// label.setText(mDayLabels[i]);
	// label.setVisibility(View.VISIBLE);
	// } else {
	// label.setVisibility(View.GONE);
	// }
	// }
	// mDayNamesHeader.invalidate();
	// }

	/**
	 * Sets all the required fields for the list view.
	 */
	private void setUpListView() {
		// Configure the listview
		mListView.setDivider(null);
		mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
		mListView.setOnScrollListener(new OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				EventCalendarView.this.onScrollStateChanged(view, scrollState);
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				EventCalendarView.this.onScroll(view, firstVisibleItem,
						visibleItemCount, totalItemCount);
			}
		});
		// Make the scrolling behavior nicer
		mListView.setFriction(mFriction);
		mListView.setVelocityScale(mVelocityScale);
	}

	private void invokeDateChangeListener() {
		if (mOnDateChangeListener != null) {
			Calendar selectedDay = mAdapter.getSelectedDay();
			mOnDateChangeListener.onSelectedDayChange(EventCalendarView.this,
					selectedDay.get(Calendar.YEAR),
					selectedDay.get(Calendar.MONTH),
					selectedDay.get(Calendar.DAY_OF_MONTH));
		}
	}

	/**
	 * Creates a new adapter if necessary and sets up its parameters.
	 */
	private void setUpAdapter() {
		if (mAdapter == null) {
			mAdapter = new WeeksAdapter();
			// mAdapter.registerDataSetObserver(new DataSetObserver() {
			// @Override
			// public void onChanged() {
			// if (mOnDateChangeListener != null) {
			// Calendar selectedDay = mAdapter.getSelectedDay();
			// mOnDateChangeListener.onSelectedDayChange(EventCalendarView.this,
			// selectedDay.get(Calendar.YEAR),
			// selectedDay.get(Calendar.MONTH),
			// selectedDay.get(Calendar.DAY_OF_MONTH));
			// }
			// }
			// });
			mListView.setAdapter(mAdapter);
		}

		// refresh the view with the new parameters
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * @return Returns the number of weeks between the current <code>date</code>
	 *         and the <code>mMinDate</code>.
	 */
	private int getWeeksSinceMinDate(Calendar date) {
		if (date.before(mMinDate)) {
			throw new IllegalArgumentException("fromDate: "
					+ mMinDate.getTime() + " does not precede toDate: "
					+ date.getTime());
		}
		long endTimeMillis = date.getTimeInMillis()
				+ date.getTimeZone().getOffset(date.getTimeInMillis());
		long startTimeMillis = mMinDate.getTimeInMillis()
				+ mMinDate.getTimeZone().getOffset(mMinDate.getTimeInMillis());
		long dayOffsetMillis = (mMinDate.get(Calendar.DAY_OF_WEEK) - mFirstDayOfWeek)
				* MILLIS_IN_DAY;
		return (int) ((endTimeMillis - startTimeMillis + dayOffsetMillis) / MILLIS_IN_WEEK);
	}

	/**
	 * Called when a <code>view</code> transitions to a new <code>scrollState
	 * </code>.
	 */
	private void onScrollStateChanged(AbsListView view, int scrollState) {
		mScrollStateChangedRunnable.doScrollStateChange(view, scrollState);
	}

	/**
	 * Updates the title and selected month if the <code>view</code> has moved
	 * to a new month.
	 */
	private void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		WeekView child = (WeekView) view.getChildAt(0);
		if (child == null) {
			return;
		}

		// Figure out where we are
		long currScroll = view.getFirstVisiblePosition() * child.getHeight()
				- child.getBottom();

		// If we have moved since our last call update the direction
		if (currScroll < mPreviousScrollPosition) {
			mIsScrollingUp = true;
		} else if (currScroll > mPreviousScrollPosition) {
			mIsScrollingUp = false;
		} else {
			return;
		}

		// Use some hysteresis for checking which month to highlight. This
		// causes the month to transition when two full weeks of a month are
		// visible when scrolling up, and when the first day in a month reaches
		// the top of the screen when scrolling down.
		int offset = child.getBottom() < mWeekMinVisibleHeight ? 1 : 0;
		if (mIsScrollingUp) {
			child = (WeekView) view.getChildAt(SCROLL_HYST_WEEKS + offset);
		} else if (offset != 0) {
			child = (WeekView) view.getChildAt(offset);
		}

		// Find out which month we're moving into
		int month;
		if (mIsScrollingUp) {
			month = child.getMonthOfFirstWeekDay();
		} else {
			month = child.getMonthOfLastWeekDay();
		}

		// And how it relates to our current highlighted month
		int monthDiff;
		if (mCurrentMonthDisplayed == 11 && month == 0) {
			monthDiff = 1;
		} else if (mCurrentMonthDisplayed == 0 && month == 11) {
			monthDiff = -1;
		} else {
			monthDiff = month - mCurrentMonthDisplayed;
		}

		// Only switch months if we're scrolling away from the currently
		// selected month
		if ((!mIsScrollingUp && monthDiff > 0)
				|| (mIsScrollingUp && monthDiff < 0)) {
			Calendar firstDay = child.getFirstDay();
			if (mIsScrollingUp) {
				firstDay.add(Calendar.DAY_OF_MONTH, -DAYS_PER_WEEK);
			} else {
				firstDay.add(Calendar.DAY_OF_MONTH, DAYS_PER_WEEK);
			}
			setMonthDisplayed(firstDay);
		}
		mPreviousScrollPosition = currScroll;
		mPreviousScrollState = mCurrentScrollState;
	}

	/**
	 * Sets the month displayed at the top of this view based on time. Override
	 * to add custom events when the title is changed.
	 *
	 * @param calendar
	 *            A day in the new focus month.
	 */
	private void setMonthDisplayed(Calendar calendar) {
		mCurrentMonthDisplayed = calendar.get(Calendar.MONTH);
		mAdapter.setFocusMonth(mCurrentMonthDisplayed);
		final int flags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
		final long millis = calendar.getTimeInMillis();
		String newMonthName = DateUtils.formatDateRange(getContext(), millis,
				millis, flags);

		if (mOnMonthChangeListerner != null) {
			mOnMonthChangeListerner.onMonthChange(this, mCurrentMonthDisplayed,
					newMonthName, calendar);
		}
	}

	public String getCurrentMonthDisplayedString() {
		final int flags = DateUtils.FORMAT_SHOW_DATE
				| DateUtils.FORMAT_NO_MONTH_DAY | DateUtils.FORMAT_SHOW_YEAR;
		final long millis = mFirstDayOfMonth.getTimeInMillis();
		String newMonthName = DateUtils.formatDateRange(getContext(), millis,
				millis, flags);
		return newMonthName;
	}

	private int getDateCode(Calendar calendar) {
		return calendar.get(Calendar.YEAR) * 10000
				+ calendar.get(Calendar.MONTH) * 100
				+ calendar.get(Calendar.DAY_OF_MONTH);
	}

	public void addEvent(DayEvent event) {
		int code = getDateCode(event.date);
		DayEvents events = mEvents.get(code);
		if (events == null) {
			events = new DayEvents();
			events.mTimeInMiliseconds = event.timeInMiliseconds;

			mEvents.put(code, events);
		}
		;

		events.mEvents.add(event);
	}

	public void clearEvents() {
		mEvents.clear();
	}

	public void refreshEvents() {
		mAdapter.notifyDataSetChanged();
	}

	/**
	 * Command responsible for acting upon scroll state changes.
	 */
	private class ScrollStateRunnable implements Runnable {
		private AbsListView mView;

		private int mNewState;

		/**
		 * Sets up the runnable with a short delay in case the scroll state
		 * immediately changes again.
		 *
		 * @param view
		 *            The list view that changed state
		 * @param scrollState
		 *            The new state it changed to
		 */
		public void doScrollStateChange(AbsListView view, int scrollState) {
			mView = view;
			mNewState = scrollState;
			removeCallbacks(this);
			postDelayed(this, SCROLL_CHANGE_DELAY);
		}

		@Override
		 public void run() {
			mCurrentScrollState = mNewState;
			// Fix the position after a scroll or a fling ends
			if (mNewState == OnScrollListener.SCROLL_STATE_IDLE
					&& mPreviousScrollState != OnScrollListener.SCROLL_STATE_IDLE) {
				View child = mView.getChildAt(0);
				if (child == null) {
					// The view is no longer visible, just return
					return;
				}
				int dist = child.getBottom() - mListScrollTopOffset;
				if (dist > mListScrollTopOffset) {
					if (mIsScrollingUp) {
						mView.smoothScrollBy(dist - child.getHeight(),
								ADJUSTMENT_SCROLL_DURATION);
					} else {
						mView.smoothScrollBy(dist, ADJUSTMENT_SCROLL_DURATION);
					}
				}
			}
			mPreviousScrollState = mNewState;
		}
	}

	public void setSelectedDay(Calendar selectedDay) {
		this.mAdapter.setSelectedDay(selectedDay);
		goTo(selectedDay);
	}

	public Calendar getSelectedDay() {
		return mAdapter.getSelectedDay();
	}

	private class WeeksAdapter extends BaseAdapter implements OnTouchListener {
		private Calendar mSelectedDate;
		private final GestureDetector mGestureDetector;

		private int mSelectedWeek;

		private int mFocusedMonth;

		private int mTotalWeekCount;

		public WeeksAdapter() {
			mGestureDetector = new GestureDetector(getContext(),
					new CalendarGestureListener());
			init();
		}

		/**
		 * Set up the gesture detector and selected time
		 */
		private void init() {
			mSelectedDate = Calendar.getInstance(Locale.getDefault());
			mSelectedDate.setFirstDayOfWeek(mFirstDayOfWeek);

			mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
			mTotalWeekCount = getWeeksSinceMinDate(mMaxDate);
			if (mMinDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek
					|| mMaxDate.get(Calendar.DAY_OF_WEEK) != mFirstDayOfWeek) {
				mTotalWeekCount++;
			}

			notifyDataSetChanged();
		}

		/**
		 * Updates the selected day and related parameters.
		 *
		 * @param selectedDay
		 *            The time to highlight
		 */
		public void setSelectedDay(Calendar selectedDay) {
			if (selectedDay.get(Calendar.DAY_OF_YEAR) == mSelectedDate
					.get(Calendar.DAY_OF_YEAR)
					&& selectedDay.get(Calendar.YEAR) == mSelectedDate
							.get(Calendar.YEAR)) {
				return;
			}
			mSelectedDate.setTimeInMillis(selectedDay.getTimeInMillis());
			mSelectedWeek = getWeeksSinceMinDate(mSelectedDate);
			mFocusedMonth = mSelectedDate.get(Calendar.MONTH);

			invokeDateChangeListener();
			notifyDataSetChanged();
		}

		/**
		 * @return The selected day of month.
		 */
		public Calendar getSelectedDay() {
			return mSelectedDate;
		}

		@Override
		public int getCount() {
			return mTotalWeekCount;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			WeekView weekView = null;
			if (convertView != null) {
				weekView = (WeekView) convertView;
			} else {
				weekView = new WeekView(getContext());
				android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				weekView.setLayoutParams(params);
				weekView.setClickable(true);
				weekView.setOnTouchListener(this);
			}

			int selectedWeekDay = (mSelectedWeek == position) ? mSelectedDate
					.get(Calendar.DAY_OF_WEEK) : -1;
			weekView.init(position, selectedWeekDay, mFocusedMonth);

			return weekView;
		}

		/**
		 * Changes which month is in focus and updates the view.
		 *
		 * @param month
		 *            The month to show as in focus [0-11]
		 */
		public void setFocusMonth(int month) {
			if (mFocusedMonth == month) {
				return;
			}
			mFocusedMonth = month;
			notifyDataSetChanged();
		}

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if (mListView.isEnabled() && mGestureDetector.onTouchEvent(event)) {
				WeekView weekView = (WeekView) v;
				// if we cannot find a day for the given location we are done
				if (!weekView.getDayFromLocation(event.getX(), mTempDate)) {
					return true;
				}
				// it is possible that the touched day is outside the valid
				// range
				// we draw whole weeks but range end can fall not on the week
				// end
				if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
					return true;
				}
				onDateTapped(mTempDate);
				return true;
			}
			return false;
		}

		/**
		 * Maintains the same hour/min/sec but moves the day to the tapped day.
		 *
		 * @param day
		 *            The day that was tapped
		 */
		private void onDateTapped(Calendar day) {
			setSelectedDay(day);
			setMonthDisplayed(day);
		}

		/**
		 * This is here so we can identify single tap events and set the
		 * selected day correctly
		 */
		class CalendarGestureListener extends
				GestureDetector.SimpleOnGestureListener {
			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				return true;
			}
		}
	}

	private class WeekView extends View {

		private final Rect mTempRect = new Rect();

		private final Paint mDrawPaint = new Paint();

		private final Paint mMonthNumDrawPaint = new Paint();

		// Cache the number strings so we don't have to recompute them each time
		private String[] mDayNumbers;

		// Quick lookup for checking which days are in the focus month
		private boolean[] mFocusDay;

		// The first day displayed by this item
		private Calendar mFirstDay;

		// The month of the first day in this week
		private int mMonthOfFirstWeekDay = -1;

		// The month of the last day in this week
		private int mLastWeekDayMonth = -1;

		// The position of this week, equivalent to weeks since the week of Jan
		// 1st, 1900
		private int mWeek = -1;

		// Quick reference to the width of this view, matches parent
		private int mWidth;

		// The height this view should draw at in pixels, set by height param
		private int mHeight;

		// If this view contains the selected day
		private boolean mHasSelectedDay = false;

		// Which day is selected [0-6] or -1 if no day is selected
		private int mSelectedDay = -1;

		// The number of days + a spot for week number if it is displayed
		private int mNumCells;

		private int mSelectedDayIndex;

		DayEvents[] mDayEvents;

		public WeekView(Context context) {
			super(context);

			// Sets up any standard paints that will be used
			initilaizePaints();
		}

		/**
		 * Initializes this week view.
		 *
		 * @param weekNumber
		 *            The number of the week this view represents. The week
		 *            number is a zero based index of the weeks since
		 *            {@link CalendarView#getMinDate()}.
		 * @param selectedWeekDay
		 *            The selected day of the week from 0 to 6, -1 if no
		 *            selected day.
		 * @param focusedMonth
		 *            The month that is currently in focus i.e. highlighted.
		 */
		public void init(int weekNumber, int selectedWeekDay, int focusedMonth) {
			mSelectedDay = selectedWeekDay;
			mHasSelectedDay = mSelectedDay != -1;
			mNumCells = mShowWeekNumber ? mDaysPerWeek + 1 : mDaysPerWeek;
			mWeek = weekNumber;
			mSelectedDayIndex = -2;

			switch (mSelectedDay) {
			case Calendar.MONDAY:
				mSelectedDayIndex = 0;
				break;
			case Calendar.TUESDAY:
				mSelectedDayIndex = 1;
				break;
			case Calendar.WEDNESDAY:
				mSelectedDayIndex = 2;
				break;
			case Calendar.THURSDAY:
				mSelectedDayIndex = 3;
				break;
			case Calendar.FRIDAY:
				mSelectedDayIndex = 4;
				break;
			case Calendar.SATURDAY:
				mSelectedDayIndex = 5;
				break;
			case Calendar.SUNDAY:
				mSelectedDayIndex = 6;
				break;
			}

			mTempDate.setTimeInMillis(mMinDate.getTimeInMillis());

			mTempDate.add(Calendar.WEEK_OF_YEAR, mWeek);
			mTempDate.setFirstDayOfWeek(mFirstDayOfWeek);

			// Allocate space for caching the day numbers and focus values
			mDayNumbers = new String[mNumCells];
			mFocusDay = new boolean[mNumCells];
			mDayEvents = new DayEvents[mNumCells];

			// If we're showing the week number calculate it based on Monday
			int i = 0;
			if (mShowWeekNumber) {

				mSelectedDayIndex++;

				mDayNumbers[0] = String.format(Locale.getDefault(), "%d",
						mTempDate.get(Calendar.WEEK_OF_YEAR));
				i++;
			}

			// Now adjust our starting day based on the start day of the week
			int diff = mFirstDayOfWeek - mTempDate.get(Calendar.DAY_OF_WEEK);
			mTempDate.add(Calendar.DAY_OF_MONTH, diff);

			mFirstDay = (Calendar) mTempDate.clone();
			mMonthOfFirstWeekDay = mTempDate.get(Calendar.MONTH);

			for (; i < mNumCells; i++) {
				final boolean isFocusedDay = (mTempDate.get(Calendar.MONTH) == focusedMonth);
				mFocusDay[i] = isFocusedDay;
				// do not draw dates outside the valid range to avoid user
				// confusion
				if (mTempDate.before(mMinDate) || mTempDate.after(mMaxDate)) {
					mDayNumbers[i] = "";
				} else {
					mDayNumbers[i] = String.format(Locale.getDefault(), "%d",
							mTempDate.get(Calendar.DAY_OF_MONTH));

					int code = getDateCode(mTempDate);
					mDayEvents[i] = EventCalendarView.this.mEvents.get(code);
				}
				mTempDate.add(Calendar.DAY_OF_MONTH, 1);
			}
			// We do one extra add at the end of the loop, if that pushed us to
			// new month undo it
			if (mTempDate.get(Calendar.DAY_OF_MONTH) == 1) {
				mTempDate.add(Calendar.DAY_OF_MONTH, -1);
			}
			mLastWeekDayMonth = mTempDate.get(Calendar.MONTH);

			updateSelectionPositions();
		}

		/**
		 * Initialize the paint instances.
		 */
		private void initilaizePaints() {
			mDrawPaint.setFakeBoldText(false);
			mDrawPaint.setAntiAlias(true);
			mDrawPaint.setStyle(Style.FILL);

			mMonthNumDrawPaint.setFakeBoldText(true);
			mMonthNumDrawPaint.setAntiAlias(true);
			mMonthNumDrawPaint.setStyle(Style.FILL);
			mMonthNumDrawPaint.setTextAlign(Align.RIGHT);
			mMonthNumDrawPaint.setTextSize(DEFAULT_DATE_TEXT_SIZE);
		}

		/**
		 * Returns the month of the first day in this week.
		 *
		 * @return The month the first day of this view is in.
		 */
		public int getMonthOfFirstWeekDay() {
			return mMonthOfFirstWeekDay;
		}

		/**
		 * Returns the month of the last day in this week
		 *
		 * @return The month the last day of this view is in
		 */
		public int getMonthOfLastWeekDay() {
			return mLastWeekDayMonth;
		}

		/**
		 * Returns the first day in this view.
		 *
		 * @return The first day in the view.
		 */
		public Calendar getFirstDay() {
			return mFirstDay;
		}

		/**
		 * Calculates the day that the given x position is in, accounting for
		 * week number.
		 *
		 * @param x
		 *            The x position of the touch event.
		 * @return True if a day was found for the given location.
		 */
		public boolean getDayFromLocation(float x, Calendar outCalendar) {
			final boolean isLayoutRtl = false;// isLayoutRtl();

			int start;
			int end;

			if (isLayoutRtl) {
				start = 0;
				end = mShowWeekNumber ? mWidth - mWidth / mNumCells : mWidth;
			} else {
				start = mShowWeekNumber ? mWidth / mNumCells : 0;
				end = mWidth;
			}

			if (x < start || x > end) {
				outCalendar.clear();
				return false;
			}

			// Selection is (x - start) / (pixels/day) which is (x - start) *
			// day / pixels
			int dayPosition = (int) ((x - start) * mDaysPerWeek / (end - start));

			if (isLayoutRtl) {
				dayPosition = mDaysPerWeek - 1 - dayPosition;
			}

			outCalendar.setTimeInMillis(mFirstDay.getTimeInMillis());
			outCalendar.add(Calendar.DAY_OF_MONTH, dayPosition);

			return true;
		}

		@Override
		protected void onDraw(Canvas canvas) {
			drawWeekNumbersAndDates(canvas);
			drawWeekSeparators(canvas);
			drawEvents(canvas);
		}

		/**
		 * Draws the week and month day numbers for this week.
		 *
		 * @param canvas
		 *            The canvas to draw on
		 */
		private void drawWeekNumbersAndDates(Canvas canvas) {
			final float textHeight = mDrawPaint.getTextSize();

			final int y = (int) textHeight + mAllDayEventOffset; // (int)
																	// ((mHeight
																	// +
																	// textHeight)
																	// / 2) -
																	// mWeekSeperatorLineWidth;
			final int nDays = mNumCells;
			final int divisor = 2 * nDays;

			mDrawPaint.setTextAlign(Align.CENTER);
			mDrawPaint.setTextSize(DEFAULT_DATE_TEXT_SIZE);

			float dx = (float) mWidth / (float) nDays;
			int i = 0;

			if (mShowWeekNumber) {
				int x = mWidth / divisor;
				canvas.drawText(mDayNumbers[0], x, y, mDrawPaint);
				i++;
			}

			for (; i < nDays; i++) {
				if (mFocusDay[i]) {
					if (mHasSelectedDay && mSelectedDayIndex == i) {
						mDrawPaint.setColor(mUnfocusedMonthDateColor);
					} else {
						mDrawPaint.setColor(mFocusedMonthDateColor);
					}
					mMonthNumDrawPaint.setColor(mFocusedMonthNumberColor);

				} else {
					mDrawPaint.setColor(mUnfocusedMonthDateColor);
					mMonthNumDrawPaint.setColor(mUnfocusedMonthNumberColor);
				}

				mDrawPaint.setStrokeWidth(mWeekSeperatorLineWidth);

				// Draw day rectangle
				mTempRect.left = (int) (i * dx);
				mTempRect.top = 0;
				mTempRect.right = (int) ((i + 1) * dx);
				mTempRect.bottom = mHeight;
				canvas.drawRect(mTempRect, mDrawPaint);

				// Draw separator day left vertical line
				mDrawPaint.setColor(mWeekSeparatorLineColor);
				canvas.drawLine(mTempRect.left, mTempRect.top, mTempRect.left,
						mTempRect.bottom, mDrawPaint);

				if (i == (nDays - 1)) {
					mDrawPaint.setColor(mWeekSeparatorLineColor);
					canvas.drawLine(mTempRect.right, mTempRect.top,
							mTempRect.right, mTempRect.bottom, mDrawPaint);
				}

				// Draw day numner
				String number = mDayNumbers[i];
				int x = (int) ((i + 1) * dx) - mWeekSeperatorLineWidth;
				canvas.drawText(number, x, y, mMonthNumDrawPaint);
			}

		}

		private void drawEvents(Canvas canvas) {
			final int nDays = mNumCells;
			float dx = (float) mWidth / (float) nDays;
			int i = 0;

			final int top = (int) mDrawPaint.getTextSize() + mAllDayEventOffset;
			final int cellHeigth = mHeight - top;

			if (mShowWeekNumber) {
				i++;
			}

			for (; i < nDays; i++) {
				DayEvents events = mDayEvents[i];
				if (events == null) {
					 continue;
				 }

				boolean containsAllDayEvent = false;
				for (int j = 0; j < events.mEvents.size(); j++) {
					DayEvent e = events.mEvents.get(j);
					if (e.isAllDay()) {
						containsAllDayEvent = true;
						break;
					}
				}

				int left = (int) (i * dx) + mWeekSeperatorLineWidth;
				int cellWidth = (int) dx - mWeekSeperatorLineWidth;

				drawEvents(canvas, events, left, top, cellWidth, cellHeigth,
						containsAllDayEvent);
			}

		}

		private void drawEvents(Canvas canvas, DayEvents events, int left,
				int top, int width, int height, boolean containsAllDayEvent) {

			if (containsAllDayEvent) {
				mTempRect.left = left;
				 mTempRect.top = mWeekSeperatorLineWidth;
				 mTempRect.right = left + width;
				mTempRect.bottom = mAllDayEventOffset;

				mDrawPaint.setColor(mAllDayEventColor);
				canvas.drawRect(mTempRect, mDrawPaint);
			}

			ArrayList<DayEvent> dayEvents = events.mEvents;
			int size = dayEvents.size();

			float eventDx = (float) width / (float) size;
			float eventDy = height / 23.0f;

			for (int i = 0; i < size; i++) {
				 DayEvent e = dayEvents.get(i);

				if (e.isAllDay()) {
					 continue;
				 }

				int color = e.getColor();

				if (color != 0) {
					mDrawPaint.setColor(color);
				} else {
					mDrawPaint.setColor(mEventColor);
				}

				mTempRect.left = left + (i * mMarginEvent)
						+ (int) (i * eventDx);
				 mTempRect.top = top + (int) (e.getStartHour() * eventDy)
						+ (int) ((e.getStartMinute() / 60.0f) * eventDy);
				 mTempRect.right = left + (i * mMarginEvent)
						+ (int) ((i + 1) * eventDx) - 1;
				mTempRect.bottom = top + (int) (e.getEndHour() * eventDy)
						+ (int) ((e.getEndMinute() / 60.0f) * eventDy);

				canvas.drawRect(mTempRect, mDrawPaint);

			}
		}

		/**
		 * Draws a horizontal line for separating the weeks.
		 *
		 * @param canvas
		 *            The canvas to draw on.
		 */
		private void drawWeekSeparators(Canvas canvas) {
			// If it is the topmost fully visible child do not draw separator
			// line
			int firstFullyVisiblePosition = mListView.getFirstVisiblePosition();
			if (mListView.getChildAt(0).getTop() < 0) {
				firstFullyVisiblePosition++;
			}
			if (firstFullyVisiblePosition == mWeek) {
				return;
			}
			mDrawPaint.setColor(mWeekSeparatorLineColor);
			mDrawPaint.setStrokeWidth(mWeekSeperatorLineWidth);
			float startX;
			float stopX;

			startX = mShowWeekNumber ? mWidth / mNumCells : 0;
			stopX = mWidth;

			canvas.drawLine(startX, 0, stopX, 0, mDrawPaint);
		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mWidth = w;
			updateSelectionPositions();
		}

		/**
		 * This calculates the positions for the selected day lines.
		 */
		private void updateSelectionPositions() {
			if (mHasSelectedDay) {
				int selectedPosition = mSelectedDay - mFirstDayOfWeek;
				if (selectedPosition < 0) {
					selectedPosition += 7;
				}
				if (mShowWeekNumber) {
					selectedPosition++;
				}
			}
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
					.getPaddingBottom()) / mShownWeekCount;
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
		}
	}

	static class DayEvents {
		long mTimeInMiliseconds;
		ArrayList<DayEvent> mEvents;

		public DayEvents() {
			mEvents = new ArrayList<DayEvent>();
		 }
	}
}
