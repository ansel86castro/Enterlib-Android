package com.enterlib.widgets;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;

import android.graphics.Paint;

public class DayEvent implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	long timeInMiliseconds;
	Calendar date;
	Calendar startTime;
	Calendar endTime;
	String name;
	int color;
	String[] lines;
	int columnIndex;
	String[] measureLines;
	int nameLinesEnd;
	private boolean mLinesBreaked;
	private ArrayList<String> newLines;

	private Object mTag;

	public DayEvent(String name) {
		this.name = name;
		date = Calendar.getInstance();
		startTime = Calendar.getInstance();
		endTime = Calendar.getInstance();
	}

	public DayEvent(String name, Calendar date, Calendar startTime,
			Calendar endDate) {
		this.name = name;
		this.date = date;
		this.startTime = startTime;
		this.endTime = endDate;

		timeInMiliseconds = date.getTimeInMillis();
	}

	public boolean isAllDay() {
		return getStartHour() == getEndHour()
				&& getStartMinute() == getEndMinute();
	}

	public Calendar getDate() {
		return date;
	}

	public int getDateCode() {
		return date.get(Calendar.YEAR) * 10000 + date.get(Calendar.MONTH) * 100
				+ date.get(Calendar.DAY_OF_MONTH);
	}

	public int getDay() {
		return date.get(Calendar.DAY_OF_MONTH);
	}

	public int getYear() {
		return date.get(Calendar.YEAR);
	}

	public int getMonth() {
		return date.get(Calendar.MONTH);
	}

	public int getDayOfWeek() {
		return date.get(Calendar.DAY_OF_WEEK);
	}

	/** Return the index of the day in the week starting form monday */
	public int getDayIndex() {
		return getDayIndex(getDayOfWeek());
	}

	/** Return the index of the day in the week starting form monday */
	public static int getDayIndex(int dayOfWeek) {
		switch (dayOfWeek) {
		case Calendar.MONDAY:
			return 0;
		case Calendar.TUESDAY:
			return 1;
		case Calendar.WEDNESDAY:
			return 2;
		case Calendar.THURSDAY:
			return 3;
		case Calendar.FRIDAY:
			return 4;
		case Calendar.SATURDAY:
			return 5;
		case Calendar.SUNDAY:
			return 6;
		}
		return -1;
	}

	public void setDate(Calendar date) {
		this.date = date;
	}

	public Calendar getStartTime() {
		return startTime;
	}

	public void setStartTime(Calendar startTime) {
		this.startTime = startTime;
	}

	public Calendar getEndTime() {
		return endTime;
	}

	public void setEndTime(Calendar endTime) {
		this.endTime = endTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public DayEvent setDay(Calendar date) {
		this.date = date;
		timeInMiliseconds = date.getTimeInMillis();
		return this;
	}

	public DayEvent setDay(int year, int month, int day) {
		date.set(year, month, day);
		timeInMiliseconds = date.getTimeInMillis();
		return this;
	}

	public DayEvent setStartTime(int hourOfDay, int minute) {
		startTime.set(0, 0, 0, hourOfDay, minute);
		return this;
	}

	public DayEvent setEndTime(int hourOfDay, int minute) {
		endTime.set(0, 0, 0, hourOfDay, minute);
		return this;
	}

	public int getStartHour() {
		return startTime.get(Calendar.HOUR_OF_DAY);
	}

	public int getEndHour() {
		return endTime.get(Calendar.HOUR_OF_DAY);
	}

	public int getStartMinute() {
		return startTime.get(Calendar.MINUTE);
	}

	public int getEndMinute() {
		return endTime.get(Calendar.MINUTE);
	}

	public int getColor() {
		return color;
	}

	public DayEvent setColor(int color) {
		this.color = color;
		return this;
	}

	public void setTag(Object tag) {
		this.mTag = tag;
	}

	public Object getTag() {
		return mTag;
	}

	public String[] getLines() {
		return lines;
	}

	public DayEvent setLines(String... lines) {
		this.lines = lines;
		mLinesBreaked = false;
		return this;
	}

	public DayEvent setLines(String lines) {
		this.lines = lines.split("\r\n|\n");
		mLinesBreaked = false;
		return this;
	}

	public ArrayList<String> getBreakedLines() {
		return newLines;
	}

	public int getNameLinesEnd() {
		return nameLinesEnd;
	}

	public void breakLines(int maxWidth, Paint paint) {
		if (mLinesBreaked) {
			return;
		}
		mLinesBreaked = true;

		int maxLenght = name != null ? name.length() : Integer.MIN_VALUE;
		if (lines != null) {
			for (int i = 0; i < lines.length; i++) {
				maxLenght = Math.max(maxLenght, lines[i].length());
			}
		}

		float[] widths = new float[maxLenght];
		newLines = new ArrayList<String>();

		if (name != null && !name.isEmpty()) {
			paint.getTextWidths(name, widths);

			float sumWidth = 0;
			int lastSplit = 0;
			for (int j = 0; j < name.length(); j++) {
				sumWidth += widths[j];

				if (sumWidth > maxWidth) {
					newLines.add(name.substring(lastSplit, j));
					lastSplit = j;
					sumWidth = widths[j];
				}
			}

			if (sumWidth > 0) {
				newLines.add(name.substring(lastSplit, name.length()));
			}

			nameLinesEnd = newLines.size();
		}

		if (lines != null) {
			for (int i = 0; i < lines.length; i++) {
				String line = lines[i];
				if (line == null || line.isEmpty()) {
					newLines.add("");
					continue;
				}

				paint.getTextWidths(line, widths);
				float sumWidth = 0;
				int lastSplit = 0;
				for (int j = 0; j < line.length(); j++) {
					sumWidth += widths[j];

					if (sumWidth > maxWidth) {
						newLines.add(line.substring(lastSplit, j));
						lastSplit = j;
						sumWidth = widths[j];
					}
				}

				if (sumWidth > 0) {
					newLines.add(line.substring(lastSplit, line.length()));
				}
			}
		}
	}

	public static int getDateCode(Calendar calendar) {
		return calendar.get(Calendar.YEAR) * 10000
				+ calendar.get(Calendar.MONTH) * 100
				+ calendar.get(Calendar.DAY_OF_MONTH);
	}

	public void clearTimes() {
		setStartTime(0, 0);
		setEndTime(0, 0);
	}

}