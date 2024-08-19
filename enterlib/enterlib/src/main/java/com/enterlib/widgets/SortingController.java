package com.enterlib.widgets;

import android.content.Context;
import android.widget.Adapter;

import com.enterlib.StringUtils;
import com.enterlib.data.ISortable;
import com.enterlib.data.OnSortingListener;
import com.enterlib.data.SortingElement;
import com.enterlib.fields.ListField;

import java.util.ArrayList;

/**
 * Created by hp on 11/10/2016.
 */
public class SortingController implements OnSortingListener {

    ArrayList<SortingElement>elements = new ArrayList<>();
    ISortable sortingCallback;
    Context context;
    ListField listField;
    Adapter adapter;

    public SortingController(Context context ,ListField listField){
        this.context = context;
        this.listField = listField;
    }

    public SortingController(Context context, Adapter adapter){
        this.context =context;
        this.adapter = adapter;
    }

    public ArrayList<SortingElement> getElements() {
        return elements;
    }

    public void setSortingCallback(ISortable sortingCallback) {
        this.sortingCallback = sortingCallback;
    }

    public void add(String hint, String expression, boolean descending){
        elements.add(new SortingElement(hint, expression, descending));
    }

    public void add(SortingElement sort){
        elements.add(sort);
    }

    public void sort(){
        ArrayList<SortingElement>activeElements = new ArrayList<>();
        for (SortingElement s: elements) {
            if(s.isActive()){
                activeElements.add(s);
            }
        }

        if(listField!=null){
            Adapter adapter = listField.getAdapter();
            if(adapter instanceof ISortable){
                ((ISortable) adapter).sort(activeElements, this);
            }
        }else{
            if(adapter instanceof ISortable)
                ((ISortable) adapter).sort(activeElements, this);
        }

        if(sortingCallback != null) {
            sortingCallback.sort(activeElements, this);
        }
    }

    public static String toOrderByExpression(ArrayList<SortingElement>sorts){
        return StringUtils.aggregate(sorts, ", ", 0,sorts.size());
    }

    public void showDialog(){
        SortingDialog dialog = new SortingDialog(context, this);
        dialog.show();
    }

    public void reset(){
        for (SortingElement s: elements) {
            s.setActive(false);
            s.setDescending(false);
        }
    }

    @Override
    public void onSortingCompleted() {

    }
}
