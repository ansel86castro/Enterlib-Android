package com.enterlib.mvvm;

import com.enterlib.R;
import com.enterlib.exceptions.ValidationException;
import com.enterlib.threading.AsyncManager;
import com.enterlib.threading.IWorkPost;

/**
 * This Class extends the {@link BaseViewModel} and defines operations for
 * saving data and storing data asynchronous
 * */
public abstract class EditViewModel extends BaseViewModel {

	protected EditViewModel(IEditableView view) {
		super(view);
	}

	/**
	 * Saves the data in another {@link Thread} and notifies the
	 * {@link IEditableView} when the data was saved or an error occurred.
	 * */
	public void save() {
		onSaving();

		AsyncManager.postAsync(new IWorkPost() {

			@Override
			public boolean runWork() throws Exception {
				return saveAsync();
			}

			@Override
			public void onWorkFinish(Exception workException) {
				onSaved(workException);
			}
		});
	}

	/** Implement the saving operation */
	protected abstract boolean saveAsync() throws Exception;

	/** Called prior saving and the invocation of {@code saveAsync} */
	protected void onSaving() {
		IEditableView view = (IEditableView) getView();
		if (view != null && view.isValid()) {
			view.onAsyncOperationBegin(R.string.salvando);
		}

	}

	/**
	 * Called after the saved operation finished, passing any exception thrown
	 * in {@code saveAsync}
	 */
	protected void onSaved(Exception exception) {
		IEditableView view = (IEditableView) getView();
		if (view != null && view.isValid()) {
			view.onAsyncOperationEnd();

			if (exception != null) {
				if (exception instanceof ValidationException) {
					view.onEditEnd(((ValidationException) exception).getError());
				} else {
					view.onFailure(exception);
				}
			} else {
				view.onEditEnd(null);
			}
		}
	}

}
