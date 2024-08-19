package com.enterlib.data.sqlite;

import androidx.annotation.NonNull;

/**
 * Created by hp on 10/27/2016.
 */
public interface OnQueryListener {
    @NonNull
    SQLQuery onQuery(@NonNull SQLQuery query);
}
