package com.enterlib.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.content.res.Resources;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.enterlib.R;

public class EventDayView extends FrameLayout {

	private static final int DEFAULT_HOUR_TEXT_SIZE = 14;

	private static final int UNSCALED_HOUR_MIN_VISIBLE_HEIGHT = 12;

	private static final int UNSCALED_DAY_SEPARATOR_LINE_WIDTH = 1;

	private static final int UNSCALED_HOUR_WIDTH = 24;

	private static final int DEFAULT_SHOWN_HOUR_COUNT = 7;

	private static final int UNSCALED_EVENT_MARGIN = 1;

	private static final int UNSCALED_ALL_DAY_MAX_HEIGHT = 75;

	private int mHourMinVisibleHeight;

	private int mDaySeperatorLineWidth;

	private int mMarginEvent;

	private int mHourWidth;

	private ListView mListView;

	private HourAdapter mAdapter;

	private int mNewEvenColor;

	private int mEventColor;

	private int mAllDayEventColor;

	private int mHourLabelColor;

	private int mEmptyCellColor;

	private int mSeparatorLineColor;

	private int mTextColor;

	// private SparseArray<ArrayList<DayEvent>> mEvents = new
	// SparseArray<ArrayList<DayEvent>>();

	private ArrayList<DayEvent> mDayEvents = new ArrayList<DayEvent>();

	private HourEvents[] mHourEvents = new HourEvents[24];

	private int[] mHourChunks = new int[24];

	private boolean mChunksComputed;

	public static interface OnEventSelectedListener {
		void onEventSelected(DayEvent e, int selectedHour);
	}

	public static interface OnCreateEventListener {
		void onCreateEvent(int hourOfDay);
	}

	class AllDayClickListener implements OnClickListener {
		DayEvent e;

		public AllDayClickListener(DayEvent e) {
			this.e = e;
		}

		@Override
		public void onClick(View v) {
			if (mEventSelectedListener != null) {
				mEventSelectedListener.onEventSelected(e, -1);
			}
		}

	}

	private OnEventSelectedListener mEventSelectedListener;

	private OnCreateEventListener mCreateEventListener;

	private LinearLayout mAllDayPanel;

	private ScrollView mAllDayScrollContainer;

	private int mAllDayPanelMaxHeight;

	static class HourEvents {
		ArrayList<DayEvent> mEvents;

		public HourEvents() {
			mEvents = new ArrayList<DayEvent>();
		}
	}

	public EventDayView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public EventDayView(Context context) {
		this(context, null, 0);
	}

	public EventDayView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		DisplayMetrics displayMetrics = getResources().getDisplayMetrics();

		mHourMinVisibleHeight = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_HOUR_MIN_VISIBLE_HEIGHT,
				displayMetrics);

		mDaySeperatorLineWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_DAY_SEPARATOR_LINE_WIDTH,
				displayMetrics);

		mHourWidth = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_HOUR_WIDTH,
				displayMetrics);

		mMarginEvent = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_EVENT_MARGIN,
				displayMetrics);
		mAllDayPanelMaxHeight = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, UNSCALED_ALL_DAY_MAX_HEIGHT,
				displayMetrics);

		Resources res = context.getResources();
		mHourLabelColor = res.getColor(R.color.day_event_hour);
		mEventColor = res.getColor(R.color.day_event);
		mEmptyCellColor = res.getColor(R.color.day_event_empty);
		mSeparatorLineColor = res.getColor(R.color.day_event_line_separator);
		mTextColor = res.getColor(R.color.day_event_text_color);
		mNewEvenColor = res.getColor(R.color.calendar_new_event);
		mAllDayEventColor = res.getColor(R.color.gray);

		LayoutInflater layoutInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View content = layoutInflater.inflate(R.layout.event_day_view, null,
				false);
		mAllDayPanel = (LinearLayout) content.findViewById(R.id.AllDayPanel);
		mAllDayScrollContainer = (ScrollView) content
				.findViewById(R.id.AllDayScrollContainer);

		addView(content);

		mListView = (ListView) findViewById(R.id.hourList);
		setUpListView();
		setUpAdapter();
	}

	public void setOnEventSelectedListener(OnEventSelectedListener listener) {
		this.mEventSelectedListener = listener;
	}

	public void setOnCreateEventListener(OnCreateEventListener listener) {
		this.mCreateEventListener = listener;
	}

	/**
	 * Sets all the required fields for the list view.
	 */
	private void setUpListView() {
		// Configure the listview
		mListView.setDivider(null);
		mListView.setItemsCanFocus(true);
		mListView.setVerticalScrollBarEnabled(false);
		// mListView.setFriction(mFriction);
		// mListView.setVelocityScale(mVelocityScale);
	}

	/**
	 * Creates a new adapter if necessary and sets up its parameters.
	 */
	private void setUpAdapter() {
		if (mAdapter == null) {
			mAdapter = new HourAdapter();
			mAdapter.registerDataSetObserver(new DataSetObserver() {
				@Override
				public void onChanged() {
					// if (mOnDateChangeListener != null) {
					// Calendar selectedDay = mAdapter.getSelectedDay();
					// mOnDateChangeListener.onSelectedDayChange(EventCalendarView.this,
					// selectedDay.get(Calendar.YEAR),
					// selectedDay.get(Calendar.MONTH),
					// selectedDay.get(Calendar.DAY_OF_MONTH));
					// }
				}
			});
			mListView.setAdapter(mAdapter);
		}

		// refresh the view with the new parameters
		mAdapter.notifyDataSetChanged();
	}

	public void clearEvents() {
		mAllDayScrollContainer.setVisibility(GONE);
		mAllDayPanel.removeAllViews();

		mDayEvents.clear();
		for (int i = 0; i < mHourEvents.length; i++) {
			 mHourEvents[i] = null;
		 }
		mChunksComputed = false;
	}

	public void addEvent(DayEvent event) {
		mChunksComputed = false;
		if (event.isAllDay()) {

			int visibility = mAllDayScrollContainer.getVisibility();
			if (visibility == GONE) {
				mAllDayScrollContainer.setVisibility(VISIBLE);
			}

			LinearLayout layout = new LinearLayout(getContext());
			layout.setOnClickListener(new AllDayClickListener(event));

			android.widget.LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

			layoutParams.topMargin = 5;
			if (event.getColor() == 0) {
				 layout.setBackgroundColor(mAllDayEventColor);
			 } else {
				 layout.setBackgroundColor(event.getColor());
			 }

			layout.setOrientation(LinearLayout.VERTICAL);
			layout.setLayoutParams(layoutParams);

			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					android.view.ViewGroup.LayoutParams.MATCH_PARENT,
					android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
			TextView tvSubject = new TextView(getContext());
			tvSubject.setLayoutParams(params);
			tvSubject.setTypeface(Typeface.create(tvSubject.getTypeface(),
					Typeface.BOLD));
			tvSubject.setTextSize(TypedValue.COMPLEX_UNIT_SP,
					DEFAULT_HOUR_TEXT_SIZE);
			tvSubject.setText(event.name);

			layout.addView(tvSubject);

			if (event.getLines() != null) {
				for (int i = 0; i < event.lines.length; i++) {
					TextView tvDescription = new TextView(getContext());
					tvDescription.setTextSize(TypedValue.COMPLEX_UNIT_SP,
							DEFAULT_HOUR_TEXT_SIZE);
					tvDescription
							.setLayoutParams(new LinearLayout.LayoutParams(
									android.view.ViewGroup.LayoutParams.MATCH_PARENT,
									android.view.ViewGroup.LayoutParams.WRAP_CONTENT));

					tvDescription.setText(event.lines[i]);
					layout.addView(tvDescription);
				 }
			}

			mAllDayPanel.addView(layout);
			int width = getWidth();

			mAllDayScrollContainer.measure(MeasureSpec.makeMeasureSpec(width,
					MeasureSpec.AT_MOST), MeasureSpec.makeMeasureSpec(
					mAllDayPanelMaxHeight, MeasureSpec.AT_MOST));

			mAllDayScrollContainer.requestLayout();

		} else {
			mDayEvents.add(event);
		}

	}

	private void computeChunks() {
		if (mChunksComputed) {
			 return;
		 }
		mChunksComputed = true;

		Arrays.fill(mHourChunks, 0);

		Collections.sort(mDayEvents, new Comparator<DayEvent>() {

			 @Override
			 public int compare(DayEvent lhs, DayEvent rhs) {
				 int lStartHour = lhs.getStartHour();
				 int rStartHour = rhs.getStartHour();
				 return lStartHour == rStartHour ? 0
						: lStartHour < rStartHour ? -1 : 1;
			 }

		});

		for (int i = 0; i < mDayEvents.size(); i++) {
			 DayEvent e = mDayEvents.get(i);
			 e.columnIndex = -1;
			 addEventToHours(e);
		 }

		for (int hour = 0; hour < 24; hour++) {
			 ArrayList<DayEvent> events = getEvents(hour);
			 mHourChunks[hour] = 0;
			 if (events == null) {
				 continue;
			 }

			int eventSize = events.size();
			 int chunkCount = Math.max(findMaxChunksBellow(hour),
					findMaxChunksAbove(hour, events));
			 mHourChunks[hour] = chunkCount;

			for (int i = 0; i < eventSize; i++) {
				 DayEvent e = events.get(i);
				 if (e.getStartHour() == hour) {
					 e.columnIndex = findEmptyChunk(chunkCount, events);
				}
			 }
		 }
	}

	private void addEventToHours(DayEvent event) {
		int startHour = event.getStartHour();
		int endHour = event.getEndHour();
		int endMinute = event.getEndMinute();

		for (int i = startHour; i < endHour; i++) {
			HourEvents hourEvents = mHourEvents[i];
			if (hourEvents == null) {
				 hourEvents = new HourEvents();
				 mHourEvents[i] = hourEvents;
			 }
			 hourEvents.mEvents.add(event);
		}

		if (endMinute > 0) {
			HourEvents hourEvents = mHourEvents[endHour];
			if (hourEvents == null) {
				 hourEvents = new HourEvents();
				 mHourEvents[endHour] = hourEvents;
			 }
			 hourEvents.mEvents.add(event);
		}
	}

	private int findMaxChunksBellow(int hour) {
		int maxChunks = 0;
		for (int ihour = hour; ihour < 24; ihour++) {
			 ArrayList<DayEvent> events = getEvents(ihour);
			 if (events == null) {
				 return maxChunks;
			 }
			 maxChunks = Math.max(maxChunks, events.size());
		 }
		return maxChunks;
	}

	private int findMaxChunksAbove(int hour, ArrayList<DayEvent> events) {
		int maxChunks = 0;
		int eventSize = events.size();
		for (int ievent = 0; ievent < eventSize; ievent++) {
			 DayEvent e = events.get(ievent);
			 int startHour = e.getStartHour();
			 if (startHour < hour) {
				 maxChunks = Math.max(maxChunks, mHourChunks[startHour]);
			 }
		 }
		return maxChunks;
	}

	private int findEmptyChunk(int chunks, ArrayList<DayEvent> events) {
		int size = events.size();
		for (int i = 0; i < chunks; i++) {
			boolean empty = true;
			 for (int j = 0; j < size; j++) {
				 if (events.get(j).columnIndex == i) {
					 empty = false;
					 break;
				 }
			 }
			 if (empty) {
				 return i;
			 }
		 }
		return -1;
	}

	private ArrayList<DayEvent> getEvents(int hourOfDay) {
		HourEvents hourEvents = mHourEvents[hourOfDay];
		if (hourEvents == null) {
			 return null;
		 }
		return hourEvents.mEvents;
	 }

	public void refreshEvents() {
		mChunksComputed = false;
		mAdapter.notifyDataSetChanged();
	}

	public class HourAdapter extends BaseAdapter implements OnTouchListener {
		private final GestureDetector mGestureDetector;

		int mSelectedHour = -1;
		int mFocusedHour;

		public HourAdapter() {
			mGestureDetector = new GestureDetector(getContext(),
					new CalendarGestureListener());
		 }

		@Override
		 public int getCount() {
			 return 24;
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
			 HourView view;
			 if (convertView != null) {
				 view = (HourView) convertView;
			 } else {
				 view = new HourView(getContext());
				android.widget.AbsListView.LayoutParams params = new android.widget.AbsListView.LayoutParams(
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
						android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
				view.setLayoutParams(params);
				view.setClickable(true);
				view.setOnTouchListener(this);
			 }

			view.init(position, position == mSelectedHour);
			 return view;
		 }

		 @Override
		 public boolean onTouch(View v, MotionEvent event) {
			if (mListView.isEnabled() && mGestureDetector.onTouchEvent(event)) {
				HourView view = (HourView) v;
				if (view.mTaped) {
					mSelectedHour = -1;
					if (mCreateEventListener != null) {
						mCreateEventListener.onCreateEvent(view.mHour);
					}
				} else if (!view.hasEvents()) {
					mSelectedHour = view.mHour;
				} else {
					// call avent selected
					DayEvent e = view.findEventFromPosition((int) event.getX(),
							(int) event.getY());
					if (e != null && mEventSelectedListener != null) {
						mEventSelectedListener.onEventSelected(e, view.mHour);
					}
				}
				notifyDataSetChanged();
				return true;
			 }
			 return false;
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

	class HourView extends View {
		private int mHour;
		private Paint mLabelPaint = new Paint();
		private Paint mCellPaint = new Paint();
		private Paint mRowPaint = new Paint();
		private Paint mTextPaint = new Paint();

		// Quick reference to the width of this view, matches parent
		private int mWidth;

		// The height this view should draw at in pixels, set by height param
		private int mHeight;

		private String mHourString;

		private Rect mTempRect = new Rect();

		private Rect mTempTextRect = new Rect();

		ArrayList<DayEvent> mEvents;

		private boolean mTaped;

		public HourView(Context context) {
			 super(context);

			 initilaizePaints();
		 }

		/**
		 * Initialize the paint instances.
		 */
		private void initilaizePaints() {
			mCellPaint.setFakeBoldText(false);
			mCellPaint.setAntiAlias(true);
			mCellPaint.setStyle(Style.FILL);

			mLabelPaint.setFakeBoldText(true);
			mLabelPaint.setAntiAlias(true);
			mLabelPaint.setStyle(Style.FILL);
			mLabelPaint.setTextAlign(Align.CENTER);
			mLabelPaint.setTextSize(DEFAULT_HOUR_TEXT_SIZE);

			mRowPaint.setFakeBoldText(false);
			mRowPaint.setAntiAlias(true);
			mRowPaint.setStrokeWidth(mDaySeperatorLineWidth);
			mRowPaint.setStyle(Style.STROKE);

			mTextPaint.setAntiAlias(true);
			mTextPaint.setStyle(Style.FILL);
			mTextPaint.setTextAlign(Align.LEFT);
			mTextPaint.setTextSize(DEFAULT_HOUR_TEXT_SIZE);
			mTextPaint.setColor(mTextColor);

		}

		@Override
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {
			mWidth = w;
		}

		public void init(int hour, boolean tapped) {
			mHour = hour;
			mTaped = tapped;
			mHourString = String.valueOf(mHour);

			computeChunks();
			mEvents = getEvents(mHour);

		}

		private DayEvent findEventFromPosition(int x, int y) {
			if (x <= mHourWidth) {
				return null;
			}

			x -= mHourWidth;
			int chunks = mHourChunks[mHour];
			int cellWidth = mWidth - mHourWidth;
			int dx = cellWidth / chunks;

			int eventIndex = x / dx;

			for (int i = 0; i < mEvents.size(); i++) {
				 DayEvent e = mEvents.get(i);
				if (e.columnIndex == eventIndex) {
					 computeRect(e, mTempRect);
					 if (y >= mTempRect.top && y <= mTempRect.bottom) {
						 return e;
					 }
				 }
			 }
			return null;
		}

		boolean hasEvents() {
			return mEvents != null && mEvents.size() > 0;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			mHeight = (mListView.getHeight() - mListView.getPaddingTop() - mListView
					.getPaddingBottom()) / DEFAULT_SHOWN_HOUR_COUNT;
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mHeight);
		}

		@Override
		protected void onDraw(Canvas canvas) {
			drawRow(canvas);
			drawHour(canvas);
			drawCell(canvas);
			drawEvents(canvas);
		}

		private void drawEvents(Canvas canvas) {
			ArrayList<DayEvent> events = mEvents;
			if (events == null || events.size() == 0) {
				 return;
			 }

			int chunks = mHourChunks[mHour];
			int cellWidth = mWidth - mHourWidth - 3 * mDaySeperatorLineWidth
					- ((chunks + 1) * mMarginEvent);
			float dx = (float) cellWidth / (float) chunks;

			int offset = (mHourWidth + mDaySeperatorLineWidth) + mMarginEvent;

			mCellPaint.setStrokeWidth(0);

			mLabelPaint.setTextAlign(Align.LEFT);
			 mLabelPaint.setColor(mTextColor);

			for (int i = 0; i < events.size(); i++) {

				DayEvent e = events.get(i);
				 int startHour = e.getStartHour();
				 int endHour = e.getEndHour();
				 int column = e.columnIndex;

				mTempRect.top = (startHour < mHour) ? mDaySeperatorLineWidth
						: getOffset(e.getStartMinute());
				 mTempRect.bottom = endHour > mHour ? (mHeight - mDaySeperatorLineWidth)
						: getOffset(e.getEndMinute());

				mTempRect.left = offset + column * mMarginEvent
						+ (int) (column * dx);
				 mTempRect.right = offset + column * mMarginEvent
						+ (int) ((column + 1) * dx);

				if (e.color == 0) {
					 mCellPaint.setColor(mEventColor);
				 } else {
					 mCellPaint.setColor(e.color);
				 }

				canvas.drawRect(mTempRect, mCellPaint);

				drawEventText(canvas, e);
			 }

		}

		private void drawEventText(Canvas canvas, DayEvent e) {
			e.breakLines(mTempRect.width(), mTextPaint);

			ArrayList<String> lines = e.getBreakedLines();
			if (lines == null) {
				return;
			}

			final float textHeight = mTextPaint.getTextSize();
			mTempRect.height();
			int nameEnd = e.getNameLinesEnd();
			int startLine = findStartLineIndex(e, textHeight, lines.size());

			for (int i = startLine, length = lines.size(); i < length; i++) {
				String line = lines.get(i);
				int y = mTempRect.top
						+ (int) ((i - startLine + 1) * textHeight);
				if (y > mTempRect.bottom) {
					 break;
				 }

				if (i < nameEnd) {
					 canvas.drawText(line, mTempRect.left, y, mLabelPaint);
				 } else {
					canvas.drawText(line, mTempRect.left, y, mTextPaint);
				}
			 }
		}

		private int findStartLineIndex(DayEvent e, float textHeight, int lines) {
			int startHour = e.getStartHour();
			if (startHour < mHour) {
				mTempTextRect.left = mTempRect.left;
				mTempTextRect.right = mTempRect.right;
				computeTopBottom(e, mTempTextRect);

				int middleHours = mHour - startHour - 1;

				int middleHeight = mHeight - 2 * mDaySeperatorLineWidth;

				int startHourLines = (int) (mTempTextRect.height() / textHeight);
				int startLine = startHourLines
						+ (int) ((middleHeight / textHeight)) * middleHours;
				return Math.min(startLine, lines);

			}
			return 0;
		}

		private void computeRect(DayEvent e, Rect rect) {
			int startHour = e.getStartHour();
			 int endHour = e.getEndHour();
			 int column = e.columnIndex;

			int chunks = mHourChunks[mHour];
			int cellWidth = mWidth - mHourWidth - 3 * mDaySeperatorLineWidth
					- ((chunks + 1) * mMarginEvent);
			float dx = (float) cellWidth / (float) chunks;
			int offset = (mHourWidth + mDaySeperatorLineWidth) + mMarginEvent;

			mTempRect.top = (startHour < mHour) ? mDaySeperatorLineWidth
					: getOffset(e.getStartMinute());
			 mTempRect.bottom = endHour > mHour ? (mHeight - mDaySeperatorLineWidth)
					: getOffset(e.getEndMinute());

			mTempRect.left = offset + column * mMarginEvent
					+ (int) (column * dx);
			 mTempRect.right = offset + column * mMarginEvent
					+ (int) ((column + 1) * dx);
		}

		private void computeTopBottom(DayEvent e, Rect bounds) {
			int startHour = e.getStartHour();
			int endHour = e.getEndHour();

			bounds.top = getOffset(e.getStartMinute());
			bounds.bottom = endHour > startHour ? (mHeight - mDaySeperatorLineWidth)
					: getOffset(e.getEndMinute());
		}

		private int getOffset(int minute) {
			float s = minute / 60.0f;
			return (int) (mDaySeperatorLineWidth * (1 - s) + (mHeight - mDaySeperatorLineWidth)
					* s);
		}

		// Draws a white rectangle and the border
		 private void drawRow(Canvas canvas) {
			 // Draw Row Rectangle
			 mTempRect.left = 0;
			 mTempRect.right = mWidth;
			 mTempRect.top = 0;
			 mTempRect.bottom = mHeight;

			mCellPaint.setColor(mEmptyCellColor);

			canvas.drawRect(mTempRect, mCellPaint);
			 canvas.drawRect(mTempRect, mRowPaint);
		}

		private void drawHour(Canvas canvas) {
			 final float textHeight = mLabelPaint.getTextSize();

			mTempRect.left = mDaySeperatorLineWidth;
			 mTempRect.right = mHourWidth - mDaySeperatorLineWidth;
			 mTempRect.top = mDaySeperatorLineWidth;
			 mTempRect.bottom = mHeight - mDaySeperatorLineWidth;
			 mCellPaint.setColor(mHourLabelColor);
			 canvas.drawRect(mTempRect, mCellPaint);

			final int y = (int) textHeight;
			int x = mHourWidth / 2;

			mLabelPaint.setColor(mTextColor);
			mLabelPaint.setTextAlign(Align.CENTER);
			canvas.drawText(mHourString, x, y, mLabelPaint);

			mCellPaint.setStrokeWidth(mDaySeperatorLineWidth);
			mCellPaint.setColor(mSeparatorLineColor);
			canvas.drawLine(mHourWidth, 0, mHourWidth, mHeight, mCellPaint);
		 }

		private void drawCell(Canvas canvas) {
			 // Draw Event Rect
			 mTempRect.left = mHourWidth + mDaySeperatorLineWidth;
			 mTempRect.right = mWidth - mDaySeperatorLineWidth;
			 mTempRect.top = mDaySeperatorLineWidth;
			 mTempRect.bottom = mHeight - mDaySeperatorLineWidth;

			if (mTaped) {
				 mCellPaint.setColor(mNewEvenColor);
				 canvas.drawRect(mTempRect, mCellPaint);

				mLabelPaint.setTextAlign(Align.LEFT);
				 mLabelPaint.setColor(mTextColor);

				final float textHeight = mLabelPaint.getTextSize();
				final int y = (int) textHeight;
				int x = mHourWidth + mDaySeperatorLineWidth;
				canvas.drawText(getContext().getString(R.string.add_event) , x, y, mLabelPaint);
			 } else {

				mCellPaint.setColor(mEmptyCellColor);
				 canvas.drawRect(mTempRect, mCellPaint);
			 }
		 }

	}

}
