package com.enterlib.widgets;

import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.enterlib.R;

public class TextViewErrorController {
	ErrorPopup mErrorPopup;
	TextView view;
	CharSequence error;

	public TextViewErrorController(TextView view) {
		this.view = view;
	}

	public void showError(CharSequence error) {
		this.error = error;
		if (error == null || error.length() == 0) {
			hideError();
		} else {
			showError();
		}
	}

	private void showError() {

		Drawable dr = view.getContext().getResources()
				.getDrawable(R.drawable.indicator_input_error);
		dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
		view.setCompoundDrawables(null, null, dr, null);

		if (mErrorPopup == null) {
			LayoutInflater inflater = LayoutInflater.from(view.getContext());
			final TextView err = (TextView) inflater.inflate(
					R.layout.textview_hint, null);

			final float scale = view.getResources().getDisplayMetrics().density;
			mErrorPopup = new ErrorPopup(err, (int) (200 * scale + 0.5f),
					(int) (50 * scale + 0.5f));
			mErrorPopup.setFocusable(false);
			// The user is entering text, so the input method is needed. We
			// don't want the popup to be displayed on top of it.
			//mErrorPopup.setInputMethodMode(PopupWindow.INPUT_METHOD_NEEDED);
		}

		TextView tv = (TextView) mErrorPopup.getContentView();
		chooseSize(mErrorPopup, error, tv);
		tv.setText(error);

		mErrorPopup.showAsDropDown(view, getErrorX(), getErrorY());
		mErrorPopup.fixDirection(mErrorPopup.isAboveAnchor());
	}

	private void chooseSize(PopupWindow pop, CharSequence text, TextView tv) {
		int wid = tv.getPaddingLeft() + tv.getPaddingRight();
		int ht = tv.getPaddingTop() + tv.getPaddingBottom();

		int defaultWidthInPixels = view.getResources().getDimensionPixelSize(
				R.dimen.textview_error_popup_default_width);
		Layout l = new StaticLayout(text, tv.getPaint(), defaultWidthInPixels,
				Layout.Alignment.ALIGN_NORMAL, 1, 0, true);
		float max = 0;
		for (int i = 0; i < l.getLineCount(); i++) {
			max = Math.max(max, l.getLineWidth(i));
		}

		/*
		 * Now set the popup size to be big enough for the text plus the border
		 * capped to DEFAULT_MAX_POPUP_WIDTH
		 */
		pop.setWidth(wid + (int) Math.ceil(max));
		pop.setHeight(ht + l.getHeight());
	}

	private void hideError() {
		view.setCompoundDrawables(null, null, null, null);
		if (mErrorPopup != null) {
			if (mErrorPopup.isShowing()) {
				mErrorPopup.dismiss();
			}
		}
	}

	/**
	 * Returns the X offset to make the pointy top of the error point at the
	 * middle of the error icon.
	 */
	private int getErrorX() {

		/*
		 * The "25" is the distance between the point and the right edge of the
		 * background
		 */
		final float scale = view.getResources().getDisplayMetrics().density;

		// final Drawables dr = view.;
		Drawable left;
		Drawable right;
		Drawable[] dr = view.getCompoundDrawables();
		int mDrawableSizeRight = 0;
		int mDrawableSizeLeft = 0;
		if (dr != null) {
			left = dr[0];
			right = dr[2];
			mDrawableSizeLeft = left != null ? left.getIntrinsicWidth() : 0;
			mDrawableSizeRight = right != null ? right.getIntrinsicWidth() : 0;
		}

		final int layoutDirection = View.LAYOUT_DIRECTION_LTR;// view.getLayoutDirection();
		int errorX;
		int offset;
		switch (layoutDirection) {
		default:
		case View.LAYOUT_DIRECTION_LTR:
			offset = -(dr != null ? mDrawableSizeRight : 0) / 2
			+ (int) (25 * scale + 0.5f);
			errorX = view.getWidth() - mErrorPopup.getWidth()
					- view.getPaddingRight() + offset;
			break;
		case View.LAYOUT_DIRECTION_RTL:
			offset = (dr != null ? mDrawableSizeLeft : 0) / 2
			- (int) (25 * scale + 0.5f);
			errorX = view.getPaddingLeft() + offset;
			break;
		}
		return errorX;
	}

	/**
	 * Returns the Y offset to make the pointy top of the error point at the
	 * bottom of the error icon.
	 */
	private int getErrorY() {
		/*
		 * Compound, not extended, because the icon is not clipped if the text
		 * height is smaller.
		 */
		final int compoundPaddingTop = view.getCompoundPaddingTop();
		int vspace = view.getBottom() - view.getTop()
				- view.getCompoundPaddingBottom() - compoundPaddingTop;

		Drawable left = null;
		Drawable right = null;
		Drawable[] dr = view.getCompoundDrawables();
		int mDrawableHeightRight = 0;
		int mDrawableHeightLeft = 0;
		if (dr != null) {
			left = dr[0];
			right = dr[2];
			mDrawableHeightLeft = (left != null) ? left.getIntrinsicHeight()
					: 0;
			mDrawableHeightRight = (right != null) ? right.getIntrinsicHeight()
					: 0;
		}

		final int layoutDirection = View.LAYOUT_DIRECTION_LTR;// view.getLayoutDirection();
		int height;
		switch (layoutDirection) {
		default:
		case View.LAYOUT_DIRECTION_LTR:
			height = (right != null ? mDrawableHeightRight : 0);
			break;
		case View.LAYOUT_DIRECTION_RTL:
			height = (left != null ? mDrawableHeightLeft : 0);
			break;
		}

		int icontop = compoundPaddingTop + (vspace - height) / 2;

		/*
		 * The "2" is the distance between the point and the top edge of the
		 * background.
		 */
		final float scale = view.getResources().getDisplayMetrics().density;
		return icontop + height - view.getHeight() - (int) (2 * scale + 0.5f)
				+ 5;
	}
}
