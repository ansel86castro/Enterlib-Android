package com.enterlib.fields;

import android.view.View;
import android.widget.RatingBar;

/**
 * Created by ansel on 14/09/2016.
 */
public class RatingBarField extends Field {

    Class<?>bindType;

    public RatingBarField(RatingBar view) {
        super(view);
    }

    public RatingBar getRatingBar(){
        RatingBar rb = (RatingBar) getView();
        return rb;
    }


    @Override
    protected void onSetErrorMessage(String errorMessage) {
    }

    @Override
    protected Object getViewValue() {
        if(bindType == Integer.class){
            return  (int)getRatingBar().getRating();
        }else if(bindType == Double.class) {
            return (double) getRatingBar().getRating();
        }else if(bindType == Short.class) {
            return (short) getRatingBar().getRating();
        }

        return getRatingBar().getRating();

    }

    @Override
    protected void setViewValue(Object value) {
        RatingBar rb = getRatingBar();
        if(value == null) {
            bindType = null;
            rb.setRating(0);
        }
        else if(value instanceof  Integer){
            bindType = Integer.class;
            rb.setRating((Integer)value);
        }else if(value instanceof  Double){
            bindType = Double.class;
            rb.setRating((float)(double)(Double)value);
        }else if(value instanceof  Float){
            bindType = Float.class;
            rb.setRating((Float)value);
        }else if(value instanceof  Short){
            bindType = Short.class;
            rb.setRating((Short)value);
        }
    }
}
