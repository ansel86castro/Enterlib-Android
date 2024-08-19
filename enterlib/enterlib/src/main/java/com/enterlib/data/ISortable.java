package com.enterlib.data;

import java.util.ArrayList;

/**
 * Created by hp on 11/26/2016.
 */
public interface ISortable {
    void sort(ArrayList<SortingElement> sorts, OnSortingListener listener);
}
