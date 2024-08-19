package com.enterlib.data;

public interface IDataChangeNotify {

	interface IDataChangeListener {
		/** The data has changed */
		void onDataChange(IDataChangeNotify sender);

		/**
		 * The data is invalid ,that is the provider won't report more data
		 * changes
		 * */
		void onDataInvalid(IDataChangeNotify sender);
	}

	void registerDataChangeListener(IDataChangeListener listener);

	void removeDataChangeListener(IDataChangeListener listener);

	void notifyDataChange();

	void notifyDataInvalid();
}
