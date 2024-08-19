package com.enterlib.mvvm;

import com.enterlib.generics.ObservableValue;
import com.enterlib.threading.AsyncManager;

public class BaseCommand {

	private final ObservableValue<Boolean> isEnable = new ObservableValue<Boolean>(true);

	public boolean isEnabled(){
		return isEnable.getValue();
	}
	
	public void setEnabled(boolean value){
		isEnable.setValue(value);
	}
	
	public ObservableValue<Boolean>  getEnabledObservable(){
		return isEnable;
	}
	
	public BaseCommand() {
		super();
	}

	public void setEnableOnUIThread(final boolean value) {
		AsyncManager.postUISync(new Runnable() {
			@Override
			public void run() {
				isEnable.setValue(value);
			}
		});
	}

}