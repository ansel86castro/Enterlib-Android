package com.enterlib.mvvm;

import android.view.View;
import android.widget.AdapterView;

import com.enterlib.IClosable;
import com.enterlib.data.IDataChangeNotify;
import com.enterlib.data.IDataChangeNotify.IDataChangeListener;
import com.enterlib.data.IEntityCursor;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.fields.Field;

public abstract class EntityCursorViewModel<T> extends ListViewModel implements IDataChangeListener {

	public static final int ENTITY_DETAILS = 0x42368697;

	protected IEntityCursor<T> cursor;

	public SelectionCommand Selection = new SelectionCommand() {
		@SuppressWarnings("unchecked")
		@Override
		public void invoke(Field field, AdapterView<?> adapterView,
				View itemView, int position, long id) {

			onItemSelected((T) adapterView.getItemAtPosition(position));
		}
	};

	public EntityCursorViewModel() {
	}

	public EntityCursorViewModel(IView view) {
		super(view);
	}


	protected void onItemSelected(T item) {
		INavigator navigator = getView();
		if (navigator != null) {
			navigator.navigateTo(ENTITY_DETAILS, null, item);
		}
	}

	public IEntityCursor<T> getCursor() {
		return cursor;
	}

	public Integer getCount() {
		if (cursor == null) {
			return null;
		}
		return cursor.getCount();
	}

	@Override
	public void onDestroy() {
		if(!isDestroyed()) {
			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
			super.onDestroy();
		}
	}

	@Override
	protected void onLoading() {
		if (cursor != null) {
			if(cursor instanceof IDataChangeNotify) {
				((IDataChangeNotify) cursor).removeDataChangeListener(this);
			}
			cursor.close();
			cursor = null;
			onPropertyChange("Cursor");
			onPropertyChange("Count");
		}
		super.onLoading();
	}

	@Override
	protected boolean loadAsync() throws Exception {
		cursor = createCursor();
		if(cursor instanceof IDataChangeNotify)
			((IDataChangeNotify)cursor).registerDataChangeListener(this);

		return true;
	}

	protected abstract IEntityCursor<T> createCursor() throws InvalidOperationException;

	@Override
	public void onDataChange(IDataChangeNotify sender) {
		onPropertyChange("Count");
	}

	@Override
	public void onDataInvalid(IDataChangeNotify sender) {
		onPropertyChange("Count");

	}

}
