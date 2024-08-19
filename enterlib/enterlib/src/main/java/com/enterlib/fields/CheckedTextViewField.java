package com.enterlib.fields;

import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.TextView;

/**
 * Created by Ansel on 2/8/2018.
 */

public class CheckedTextViewField extends TextViewField implements Checkable {
    public CheckedTextViewField() {
    }

    public CheckedTextViewField(CheckedTextView view, String valueBinding, String display, boolean required) {
        super(view, valueBinding, display, required);
    }

    public CheckedTextViewField(CheckedTextView view, String display, boolean required) {
        super(view, display, required);
    }

    public CheckedTextViewField(CheckedTextView view, String valueBinding) {
        super(view, valueBinding);
    }

    public CheckedTextViewField(CheckedTextView view) {
        super(view);
    }

    public CheckedTextViewField(CheckedTextView view, boolean inRequired) {
        super(view, inRequired);
    }

    @Override
    public void setChecked(boolean checked) {
        CheckedTextView view = (CheckedTextView) getView();
        view.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        CheckedTextView view = (CheckedTextView) getView();
        return view.isChecked();
    }

    @Override
    public void toggle() {
        CheckedTextView view = (CheckedTextView) getView();
        view.toggle();
    }
}
