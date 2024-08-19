package com.enterlib.fields;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.viewpager.widget.ViewPager;

import com.enterlib.R;
import com.enterlib.databinding.BindingExpression;
import com.enterlib.databinding.BindingResources;
import com.enterlib.exceptions.InvalidOperationException;
import com.enterlib.serialization.JSonSerializer;
import com.enterlib.threading.AsyncResultTask;
import com.enterlib.threading.IResultNotifyCallback;
import com.enterlib.validations.ErrorInfo;
import com.enterlib.validations.FormValidator;
import com.enterlib.validations.IValidator;
import com.enterlib.validations.ValidationResult;
import com.enterlib.widgets.DateTimePickerButton;
import com.enterlib.widgets.FilterableSpinner;
import com.enterlib.widgets.HorizontalNumPicker;
import com.enterlib.widgets.PickListLayout;
import com.enterlib.widgets.PickListView;

public class Form {

	public static class SaveStore implements Serializable {
		public SaveStore(String json, Class<?> type) {
			Json = json;
			Type = type;
		}

		private static final long serialVersionUID = 1L;

		public String Json;
		public Class<?> Type;
	}

	/**Interface for providing a Field for a custom View*/
	public static interface FieldFactory {
		Field createField(Class<?> viewClass, View view);
	}

	private static final ArrayList<FieldFactory> fieldFactories = new ArrayList<Form.FieldFactory>();

	private static final FieldFactory defaultFactory = new FieldFactory() {
		@SuppressWarnings("unchecked")
		@Override
		public Field createField(Class<?> viewClass, View view) {

			if (view instanceof CompoundButton) return new CompoundButtonField((CompoundButton) view);
            if (view instanceof DateTimePickerButton) return new DatePickerButtonField((DateTimePickerButton) view);
			if (view instanceof FilterableSpinner) return new FilterableSpinnerField((FilterableSpinner) view);
			if (view instanceof CheckedTextView) return new CheckedTextViewField((CheckedTextView)view);
			if (view instanceof TextView) return new TextViewField((TextView) view);
			if (view instanceof ImageView) return new ImageField((ImageView) view);
			if (view instanceof Spinner) return new SpinnerField((Spinner) view);
			if (viewClass == PickListView.class) return new PickListField((PickListView) view);
			if (viewClass == PickListLayout.class) return new PickListLayoutField((PickListLayout) view);
			if (view instanceof AdapterView<?>) return new ListField((AdapterView) view);
			if( view instanceof ViewPager) return new ViewPagerField((ViewPager) view);
			if(viewClass == HorizontalNumPicker.class) return new HorizontalNumPickerField((HorizontalNumPicker) view);
			if(view instanceof RatingBar) return new RatingBarField((RatingBar) view);
			if(view instanceof ViewGroup) return new ContainerField((ViewGroup) view);

			return new GenericField(view);
		}
	};

	/**Register a user {@link FieldFactory}. It's recommend to use
	 * this method in the Applicacion's onCreate method*/
	public static void addFactories(FieldFactory... factories) {
		for (int i = 0; i < factories.length; i++) {
			if (!fieldFactories.contains(factories[i])) {
				fieldFactories.add(factories[i]);
			}
		}
	}
	
	public static void removeFactories(FieldFactory... factories){
		for (int i = 0; i < factories.length; i++) {			
				fieldFactories.remove(factories[i]);			
		}
	}

	// A set of fields or validators to call validation on
	private ArrayList<IValidator> ruleSet;
	private Handler handler;
	private SparseArray<Field> fieldsById;
	private ArrayList<Field> fields;
	private Object viewModel;
	private BindingResources bindingResources;

	public Form() {
		ruleSet = new ArrayList<IValidator>();
		handler = new Handler();
		fieldsById = new SparseArray<Field>();
		fields = new ArrayList<Field>();
	}

	public void clear() {
		ruleSet.clear();
		fieldsById.clear();
		fields.clear();
	}

	public Object getViewModel() {
		return viewModel;
	}

	public void setViewModel(Object viewModel) {
		this.viewModel = viewModel;
	}

	// Add a field which is also a validator
	public Form addValidator(IValidator v) {
		if (v instanceof Field) {
			addField((Field) v);
		} else {
			ruleSet.add(v);
		}
		return this;
	}

	public Form addFormValidator(FormValidator validator) {
		validator.setForm(this);
		ruleSet.add(validator);
		return this;
	}

	public Form addField(Field field) {
		field.setForm(this);
		View view = field.getView();

		int id;
		if (view != null && view.getId() != View.NO_ID) {
			id = view.getId();
		} else {
			id = fields.size() + 1;
			field.setId(id);
		}

		fields.add(field);
		fieldsById.put(id, field);
		return this;
	}

	public Field getFieldById(int id) {
		return fieldsById.get(id);
	}
	
	public Field getFieldByBinding(String valueBinding){
		for (int i = 0; i < fields.size(); i++) {
			Field f = fields.get(i);
			if(f.getValueBinding()!=null && f.getValueBinding().equals(valueBinding))
				return f;
		}
		return null;
	}

	public Field getField(int index) {
		return fields.get(index);
	}

	public int getFieldsCount() {
		return fields.size();
	}

	public int getValidatorCount() {
		return ruleSet.size();
	}

	public IValidator getValidator(int index) {
		return ruleSet.get(index);
	}

    public boolean validate(Activity activity) {
        if (!validate()) {
            showErrorDialog(activity, null);
            return false;
        }

        updateSource();
        return true;
    }

	/**Returns true of all the Fields, and {@link IValidator} 
	 * pass the validation.*/
	public boolean validate() {
		boolean finalResult = true;

		// validate fields
		for (Field f : fields) {
			boolean result = f.validate();
			if (result == false) {
				finalResult = false;
			}
			// if true go around
			// if all true it should stay true
		}

		// validates general validators
		for (IValidator v : ruleSet) {
			boolean result = v.validate();
			if (result == false) {
				finalResult = false;
			}
			// if true go around
			// if all true it should stay true
		}

		return finalResult;
	}

	public void validateAsync(IResultNotifyCallback<Boolean> callback) {
		new AsyncResultTask<Boolean>(callback) {
			@Override
			protected Boolean doInBackground() throws InvalidOperationException {
				boolean finalResult = true;

				for (IValidator f : fields) {
					boolean result = f.validateAsync(handler);
					if (result == false) {
						finalResult = false;
					}
					// if true go around
					// if all true it should stay true
				}

				for (IValidator v : ruleSet) {
					boolean result = v.validateAsync(handler);
					if (result == false) {
						finalResult = false;
					}
					// if true go around
					// if all true it should stay true
				}
				return finalResult;
			}

		}.run();
	}

	public String getErrorMesssage() {
		StringBuilder sb = new StringBuilder();

		for (Field field : fields) {
			if (field.getErrorMessage() != null) {
				String display = field.getDisplay();
				if (display != null) {
					sb.append(field.getDisplay());
					sb.append(":");
				}
				sb.append(field.getErrorMessage());
				sb.append("\n");
			}
		}

		for (IValidator v : ruleSet) {
			if (v.getErrorMessage() != null) {
				sb.append(v.getErrorMessage());
				sb.append("\n");
			}
		}

		return sb.toString();
	}

	public void setValue(int fieldId, Object value) {
		Field field = getFieldById(fieldId);
		if (field == null) {
			return;
		}
		field.setValue(value);
	}

	public Object getValue(int fieldId) {
		Field field = getFieldById(fieldId);
		if (field == null) {
			return null;
		}
		return field.getValue();
	}

	/** Restore the Field's value and state from the savedInstanceState */
	public void restoreState(Bundle savedInstanceState) {
		int count = fields.size();
		JSonSerializer serializer = new JSonSerializer();

		for (int i = 0; i < count; i++) {
			Field field = fields.get(i);
			field.restoreState(savedInstanceState, serializer);
		}
	}

	/** Save the Field's value and state in outstate*/
	public void saveState(Bundle outState) {
		int count = fields.size();
		JSonSerializer serializer = new JSonSerializer();

		for (int i = 0; i < count; i++) {
			Field field = fields.get(i);
			field.saveState(outState, serializer);
		}
	}

	/**Update the target properties
	 * Use the viewModel as the sourceObject*/
	public void updateTargets() {
		updateTargets(viewModel);
	}

	/**Update the target properties
	 * @param sourceObject The source object of the binding hierarchy */
	public void updateTargets(Object sourceObject) {
		int length = fields.size();
		for (int i = 0; i < length; i++) {
			Field field = fields.get(i);
			field.updateTarget(sourceObject);
		}
	}

	/**Updates the source properties
	 * Use the viewModel as the sourceObject  of the binding hierarchy*/
	public void updateSource() {
		updateSource(viewModel);
	}

	/**Updates the source properties
	 * @param sourceObject The source object */
	public void updateSource(Object sourceObject) {

		int length = fields.size();
		for (int i = 0; i < length; i++) {
			Field field = fields.get(i);
			field.updateSource(sourceObject);
		}
	}

	/**Set the field error messages
	 * @param ei Contains a {@link ValidationResult} collection where
	 * 	      the {@code ValidationResult.getField()} returns the name of the invalid source property
	 * */
	public void setFieldErrors(ErrorInfo ei) {
		if (!ei.containsErrors()) {
			return;
		}

		HashMap<String, Field> fieldsMap = new HashMap<String, Field>(fieldsById.size());
		
		for (int i = 0; i < fields.size(); i++) {
			Field f = fields.get(i);
			fieldsMap.put(f.getValueBinding(), f);
		}

		for (ValidationResult validationResult : ei.getValidationResults()) {
			Field field = fieldsMap.get(validationResult.getField());
			if (field != null) {
				String error = validationResult.getError(field.getContext());
				if (error != null && !error.isEmpty()) {
					field.setErrorMessage(error);
				}
			}
		}
	}

	public void showErrorDialog(Context context, String title) {
		AlertDialog.Builder ab = new AlertDialog.Builder(context);
		ab.setTitle(title != null ? title : context.getString(R.string.validation_error));
		ab.setPositiveButton(R.string.cerrar, null);

		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rootView = inflater.inflate(R.layout.dialog_notify, null);
		TableLayout table = (TableLayout) rootView.findViewById(R.id.panel);

		for (Field field : fields) {
			String error = field.getErrorMessage();
			if (error != null) {
				String display = field.getDisplay();
				if (display != null) {
					TableRow row = new TableRow(context);
					row.setLayoutParams(new TableLayout.LayoutParams(
							LayoutParams.MATCH_PARENT,
							LayoutParams.WRAP_CONTENT));
					table.addView(row);
					row.setPadding(0, 0, 0, 10);

					// add field name at column 0
					TextView label = new TextView(context);
					label.setLayoutParams(new TableRow.LayoutParams(
							LayoutParams.WRAP_CONTENT,
							LayoutParams.WRAP_CONTENT));
					label.setText(display + ": ");
					label.setGravity(Gravity.RIGHT);
					label.setCompoundDrawablesWithIntrinsicBounds(
							R.drawable.gtk_dialog_error, 0, 0, 0);
					label.setCompoundDrawablePadding(2);
					row.addView(label);

					// add field error at column 1
					label = new TextView(context);
					label.setText(error);
					label.setTextColor(0xFFFF0000);
					label.setGravity(Gravity.LEFT);
					TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
							0, LayoutParams.WRAP_CONTENT);
					layoutParams.gravity = Gravity.FILL_HORIZONTAL;
					label.setLayoutParams(layoutParams);
					row.addView(label);
				} 
//				else {
//					TableRow row = new TableRow(context);
//					row.setLayoutParams(new TableLayout.LayoutParams(
//							LayoutParams.MATCH_PARENT,
//							LayoutParams.WRAP_CONTENT));
//					table.addView(row);
//					row.setPadding(0, 0, 0, 10);
//					TextView label = (TextView) inflater.inflate(
//							R.layout.error_text_notify, null);
//					label.setText(error);
//					row.addView(label);
//				}
			}
		}

		for (IValidator v : ruleSet) {
			String error = v.getErrorMessage();
			if (error != null) {
				TableRow row = new TableRow(context);
				row.setLayoutParams(new TableLayout.LayoutParams(
						LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
				table.addView(row);
				row.setPadding(0, 0, 0, 10);
				TextView label = (TextView) inflater.inflate(
						R.layout.error_text_notify, null);
				label.setText(error);
				row.addView(label);
			}
		}

		rootView.requestLayout();
		rootView.refreshDrawableState();
		ab.setView(rootView);
		ab.show();
	}

	/** Creates the Form from the view hierarchy
	 * @param bindingResources A dictionary like object 
	 * 						    containing references for the bindings
	 * @param rootView The root of the view hierarchy
	 * @param viewModel The default source object for the bindings
	 * */
	public static Form build(BindingResources bindingResources, View rootView,
			Object viewModel) {
		Form form = new Form();
		form.bindingResources = bindingResources;
		form.viewModel = viewModel;

		build(bindingResources, rootView, form, null);
		return form;
	}

	private static Field createField(Class<?> viewClass, View view) {

		int factoryLenght = fieldFactories.size();
		for (int i = 0; i < factoryLenght; i++) {
			Field field = fieldFactories.get(i).createField(viewClass, view);
			if (field != null) {
				return field;
			}
		}

		return defaultFactory.createField(viewClass, view);
	}

	private static void build(BindingResources bindingResources, View view,
			Form form, Field parentField) {
		Object tag = view.getTag();
		Class<?> viewClass = view.getClass();

		String tagString;

		if (tag instanceof String) {
			tagString = (String) tag;
			if (tagString != null && !tagString.isEmpty()
					&& isBinding(tagString.trim())) {
				Field field = createField(viewClass, view);
				if (field == null) {
					return;
				}

				field.setForm(form);
				field.setParentField(parentField);
				if(parentField instanceof ContainerField){
					((ContainerField) parentField).addField(field);
				}
				
				try {
					BindingExpression binding = BindingExpression.parse(tagString);
					field.initBinding(binding, bindingResources);
				} catch (Exception e) {
					Log.e("Form", e.getMessage(), e);
					throw new RuntimeException(e.getMessage(), e);
				}

				form.addField(field);
				parentField = field;
			}
		}

		if (view instanceof ViewGroup) {
			ViewGroup parentView = (ViewGroup) view;
			int count = parentView.getChildCount();
			for (int j = 0; j < count; j++) {
				View child = parentView.getChildAt(j);
				build(bindingResources, child, form, parentField);
			}
		}

	}

	private static boolean isBinding(String tagString) {
		return tagString != null && tagString.charAt(0) == '{'
				&& tagString.charAt(tagString.length() - 1) == '}';
	}

	public BindingResources getBindingResources() {
		return bindingResources;
	}

	/**Link a view hierarchy to its corresponding
	 * Fields in the Form. Use this method when 
	 * the view heirarchy is destroyed and recreated and
	 * you want to maintain the Fields states */
	public void bindView(View view) {
		bindView(view, 0);
	}

	private int bindView(View rootView, int index) {
		Object tag = rootView.getTag();

		if (tag instanceof String) {
			String tagString = (String) tag;
			if (tagString != null && !tagString.isEmpty()
					&& isBinding(tagString.trim())) {
				Field field = fieldsById.get(rootView.getId());
				if (field == null && index < fields.size()) {
					field = fields.get(index);
				}

				if (field != null) {
					field.setView(rootView);
					index++;
				}
			}
		}

		if (rootView instanceof ViewGroup) {
			ViewGroup parentView = (ViewGroup) rootView;
			int count = parentView.getChildCount();
			for (int j = 0; j < count; j++) {
				View child = parentView.getChildAt(j);
				index = bindView(child, index);
			}
		}

		return index;

	}

}
