package com.enterlib.app;

import java.io.InputStream;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enterlib.R;
import com.enterlib.StringUtils;
import com.enterlib.converters.Converters;
import com.enterlib.exceptions.ConversionFailException;
import com.enterlib.widgets.DatePickerButton;
import com.enterlib.widgets.TimePickerButton;

/**
 * Contains utilities for working with {@link View}
 * */
public final class UIUtils {

	public static void showMessage(Context context, String message) {
		Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
		toast.show();
	}

	public static void setTextViewText(View view, int id, String text) {
		TextView v = (TextView) view.findViewById(id);
		v.setText(text);
	}

	public static void setTextViewText(View view, String label, int id,
			String text) {
		TextView v = (TextView) view.findViewById(id);
		if (label == null) {
			v.setText(text);
		} else if (text != null) {
			v.setText(label + text);
		} else {
			v.setText(label);
		}
	}

	public static void setTextViewText(View view, String label, int id,
			Date value) {
		setTextViewText(view, label, id,
				Converters.DateToStringConverter.getString(value));
	}

	public static void setTextViewText(View view, String label, int id,
			int value) {
		setTextViewText(view, label, id, String.valueOf(value));
	}

	public static void setTextViewText(View view, String label, int id,
			double value) {
		setTextViewText(view, label, id, String.valueOf(value));
	}

	public static void setTextViewText(View view, int id, Date value) {
		setTextViewText(view, null, id,
				Converters.DateToStringConverter.getString(value));
	}

	public static void setTextViewText(View view, int id, int value) {
		setTextViewText(view, null, id, String.valueOf(value));
	}

	public static void setTextViewText(View view, int id, double value) {
		setTextViewText(view, null, id, String.valueOf(value));
	}

	public static void setCheckBoxValue(View view, int id, boolean value) {

		CheckBox v = (CheckBox) view.findViewById(id);
		v.setChecked(value);
	}

	public static Date GetDateTime(View view, int iddate, int idtime) {
		return GetDateTime((DatePickerButton) view.findViewById(iddate),
				(TimePickerButton) view.findViewById(idtime));
	}

	public static Date GetDateTime(DatePickerButton date, TimePickerButton time) {
		try {
			return com.enterlib.DateUtils.getDateTime(date.getDate(),
					time.getDate());
		} catch (ConversionFailException e) {
			Log.d("PresentUtils", e.getMessage());
			return null;
		}
		// String dateStr = date .getText().toSql();
		// String timeStr = time.getText().toSql();
		// if(dateStr == null || dateStr.isEmpty() || timeStr == null ||
		// timeStr.isEmpty())
		// return null;
		// try {
		// // String[]values = dateStr.split("/");
		// // int d = Integer.parseInt(values[0]);// - 1;
		// // int m = Integer.parseInt(values[1]) - 1;
		// // int y = Integer.parseInt(values[2]);// - 1;
		// // return dfTime.parse(d +"/" + m + "/" + y + " " + timeStr);
		// return dfTime.parse(dateStr + " " + timeStr);
		// } catch (Exception e) {
		// e.printStackTrace();
		// return null;
		// }
	}

	public static void SetEditViewText(Activity act, int viewId, String text) {
		EditText v = (EditText) act.findViewById(viewId);
		if (v != null) {
			v.setText(text);
		}
	}

	public static void SetEditViewText(View rootView, int viewId, String text) {
		EditText v = (EditText) rootView.findViewById(viewId);
		if (v != null) {
			v.setText(text);
		}
	}

	public static String GetEditViewText(Activity view, int viewId) {
		EditText v = (EditText) view.findViewById(viewId);
		if (v != null) {
			return v.getText().toString();
		}
		return null;
	}

	public static String GetEditViewText(View view, int viewId) {
		EditText v = (EditText) view.findViewById(viewId);
		if (v != null) {
			return v.getText().toString();
		}
		return null;
	}

	public static int GetEditViewInteger(View view, int viewId) {
		String str = GetEditViewText(view, viewId);
		if (str != null && !str.isEmpty()) {
			try {
				return Integer.parseInt(str);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return -1;
	}

	public static double GetEditViewDouble(View view, int viewId) {
		String str = GetEditViewText(view, viewId);
		if (str != null && !str.isEmpty()) {
			try {
				return Double.parseDouble(str);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return -1;
	}

	public static void setErrorBackground(View view, int viewID) {
		View t = view.findViewById(viewID);
		t.setBackgroundResource(R.drawable.custom_edit_text_error);
	}

	public static void setDefaultBackground(View view, int viewID) {
		View t = view.findViewById(viewID);
		t.setBackgroundColor(Color.WHITE);
	}

	public static void hideView(View container, int viewId) {
		View view = container.findViewById(viewId);
		view.setVisibility(View.INVISIBLE);
	}

	public static void showView(View container, int viewId) {
		View view = container.findViewById(viewId);
		view.setVisibility(View.VISIBLE);
	}

	public static void collapseView(View container, int viewId) {
		View view = container.findViewById(viewId);
		view.setVisibility(View.GONE);
	}

	public static void setTextViewTextOrCollapse(View root, int viewId,
			String value) {
		if (StringUtils.isNullOrWhitespace(value)) {
			collapseView(root, viewId);
		} else {
			setTextViewText(root, viewId, value);
		}
	}

	public static void setTextViewTextOrHide(View root, int viewId, String value) {
		if (StringUtils.isNullOrWhitespace(value)) {
			hideView(root, viewId);
		} else {
			setTextViewText(root, viewId, value);
		}
	}

	public static void setOnClick(View root, int id, OnClickListener listener) {
		View v = root.findViewById(id);
		v.setOnClickListener(listener);
	}
	
	public static int findResourceId(Context context, String idName, String type) {
		Resources res = context.getResources();				
		int resID = res.getIdentifier(idName, type, context.getPackageName());									
		return resID;
	}
	
	public static int calculateInSampleSize(int width, int height, int reqWidth, int reqHeight) {  
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}
	
	public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
	        int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeResource(res, resId, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeResource(res, resId, options);
	}

	public static Bitmap decodeSampledBitmapFromStream(InputStream is, int reqWidth, int reqHeight) {

	    // First decode with inJustDecodeBounds=true to check dimensions
	    final BitmapFactory.Options options = new BitmapFactory.Options();
	    options.inJustDecodeBounds = true;
	    BitmapFactory.decodeStream(is, null, options);

	    // Calculate inSampleSize
	    options.inSampleSize = calculateInSampleSize(options.outWidth, options.outHeight, reqWidth, reqHeight);

	    // Decode bitmap with inSampleSize set
	    options.inJustDecodeBounds = false;
	    return BitmapFactory.decodeStream(is, null, options);
	}

    public static BitmapDrawable viewToBitmap(Context c, View view) {
        view.measure(View.MeasureSpec.getSize(view.getMeasuredWidth()),
                View.MeasureSpec.getSize(view.getMeasuredHeight()));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        BitmapDrawable drawable = new BitmapDrawable(c.getResources(),android.graphics.Bitmap.createBitmap(view.getDrawingCache()));
        view.setDrawingCacheEnabled(false);
        return drawable;
    }

    public static BitmapDrawable viewToBitmap(Context c, View view, int width, int height) {
        view.measure(View.MeasureSpec.getSize(width),
                View.MeasureSpec.getSize(height));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        BitmapDrawable drawable = new BitmapDrawable(c.getResources(),
                android.graphics.Bitmap.createBitmap(view.getDrawingCache()));
        view.setDrawingCacheEnabled(false);
        return drawable;
    }

	
}
