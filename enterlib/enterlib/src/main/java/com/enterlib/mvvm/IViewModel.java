package com.enterlib.mvvm;

import com.enterlib.IObservable;
import com.enterlib.databinding.INotifyPropertyChanged;

import java.util.Observer;

public interface IViewModel extends INotifyPropertyChanged {

    interface OnDeleteListener {
        /**@param data the item or list of items that were deleted
         * */
        void onDeleted(Object data);
    }

	IViewModel getParentViewModel();

	IView getView();

    void onDestroy();

    void load();

    boolean isLoaded();

    boolean isDestroyed();

    void addObserver(Observer observer);

    void deleteObserver(Observer observer);
}
