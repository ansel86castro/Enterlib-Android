package com.enterlib.widgets;

import android.view.View;
import android.widget.PopupWindow;

import com.enterlib.R;

public class ErrorPopup extends PopupWindow {
	private boolean mAbove = false;
	private final View mView;
	private int mPopupInlineErrorBackgroundId = 0;
	private int mPopupInlineErrorAboveBackgroundId = 0;

	public ErrorPopup(View v, int width, int height) {
		super(v, width, height);
		mView = v;
		// Make sure the TextView has a background set as it will be used the
		// first time it is
		// shown and positioned. Initialized with below background, which should
		// have
		// dimensions identical to the above version for this to work (and is
		// more likely).
		mPopupInlineErrorBackgroundId = R.drawable.popup_inline_error_holo_light;
		mView.setBackgroundResource(mPopupInlineErrorBackgroundId);
	}

	public void fixDirection(boolean above) {
		mAbove = above;

		if (above) {
			mPopupInlineErrorAboveBackgroundId = R.drawable.popup_inline_error_above_holo_light;
		} else {
			mPopupInlineErrorBackgroundId = R.drawable.popup_inline_error_holo_light;
		}

		mView.setBackgroundResource(above ? mPopupInlineErrorAboveBackgroundId
				: mPopupInlineErrorBackgroundId);
	}

	// private int getResourceId(int currentId, int index) {
	// if (currentId == 0) {
	// TypedArray styledAttributes = mView.getContext().obtainStyledAttributes(
	// R.styleable.Theme);
	// currentId = styledAttributes.getResourceId(index, 0);
	// styledAttributes.recycle();
	// }
	// return currentId;
	// }

	@Override
	public void update(int x, int y, int w, int h, boolean force) {
		super.update(x, y, w, h, force);

		boolean above = isAboveAnchor();
		if (above != mAbove) {
			fixDirection(above);
		}
	}
}