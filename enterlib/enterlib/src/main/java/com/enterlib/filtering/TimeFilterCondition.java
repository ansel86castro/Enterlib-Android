package com.enterlib.filtering;

import android.content.Context;
import android.view.ViewGroup;

import com.enterlib.converters.Converters;
import com.enterlib.converters.DateConverter;
import com.enterlib.converters.IValueConverter;
import com.enterlib.fields.DatePickerButtonField;
import com.enterlib.widgets.DatePickerButton;
import com.enterlib.widgets.TimePickerButton;

import java.util.Date;

/**
 * Created by ansel on 10/09/2016.
 */
public class TimeFilterCondition<T> extends FilterCondition {

    private DateConverter filterDateConverter = new DateConverter("yyyy-MM-dd HH:mm:ss");

    public TimeFilterCondition(String queryName, String queryHint,
                               DatePickerButtonField field) {
        super(queryName, queryHint, field);
    }

    public TimeFilterCondition(Context context, String queryName,
                               String queryHint, IValueConverter dateConverter) {
        super(queryName, queryHint, null);

        TimePickerButton datePicker = new TimePickerButton(context);
        datePicker.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));

        setField(new DatePickerButtonField(datePicker, "TimeFilterCondition" + queryHint));

        if (dateConverter == null) {
            dateConverter = new com.enterlib.converters.DateToStringConverter("HH:mm");
        }
        this.converter = dateConverter;
    }

    public TimeFilterCondition(Context context, String queryHint,
                               IValueConverter dateConverter) {
        this(context, null, queryHint, dateConverter);
    }

    public TimeFilterCondition(Context context, String queryHint) {
        this(context, null, queryHint, new com.enterlib.converters.DateToStringConverter("HH:mm"));
    }

    public TimeFilterCondition(Context context, String queryName, String queryHint, int operation) {
        this(context, queryName, queryHint, null);
        this.filterOp = operation;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final boolean eval(Object item) {
        Date date = (Date) queryValue;
        if (date == null) {
            return true;
        }
        return eval(date, (T) item);
    }

    protected boolean eval(Date date, T item){return false;}

    @Override
    public String getFilterExpression() {
        if(queryValue == null)
            return null;
        return String.format("%s %s '%s'", queryName,
                getOpString(),
                filterDateConverter.getString((Date) queryValue));
    }
}
