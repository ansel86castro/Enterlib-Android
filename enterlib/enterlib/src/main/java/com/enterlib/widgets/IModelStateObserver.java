package com.enterlib.widgets;

public interface IModelStateObserver {
	boolean onAdded(Object model);

	boolean onRemoved(Object model);
}
