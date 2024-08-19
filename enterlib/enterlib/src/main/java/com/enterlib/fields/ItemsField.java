package com.enterlib.fields;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;

import com.enterlib.databinding.BindingProperty;
import com.enterlib.databinding.BindingResources;
import com.enterlib.databinding.ExpressionMember;
import com.enterlib.databinding.ReflectionResolver;

public abstract class ItemsField extends Field {

    public interface OnTemplateAppliedListener {
		void onTemplateApplied(ItemsField field, View template, Form form,
				Object item, int position);
	}

	public static final BindingProperty<ItemsField> AdapterProviderProperty = registerProperty(
			ItemsField.class,
			new BindingProperty<ItemsField>("AdapterProvider") {
				@Override
				public void set(ItemsField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.adapterProvider = (AdapterProvider) dc.get(member.getValueString());
					}
					
					if(object.adapterProvider == null && object.getViewModel()!=null){
						object.adapterProvider = (AdapterProvider)ReflectionResolver.getValue(member.getValueString(), object.getViewModel()); 
					}
				}

				@Override
				public Object get(com.enterlib.databinding.DependencyObject object) {
					return ((ItemsField) object).adapterProvider;
				};
			});

	public static final BindingProperty<ItemsField> ItemTemplateProperty = registerProperty(
			ItemsField.class, new BindingProperty<ItemsField>("ItemTemplate") {
				@Override
				public void set(ItemsField object, ExpressionMember member,
						BindingResources dc) {
					if (dc != null) {
						object.setTemplateResource(object.findResourceId(dc,member.getValueString(), "R.layout"));
					}
				}

				@Override
				public Object get(
						com.enterlib.databinding.DependencyObject object) {
					return ((ItemsField) object).templateResourceId;
				};
			});

    public static final BindingProperty<ItemsField> DropDownTemplateProperty = registerProperty(
            ItemsField.class, new BindingProperty<ItemsField>("DropDownTemplate") {
                @Override
                public void set(ItemsField object, ExpressionMember member,
                                BindingResources dc) {
                    if (dc != null) {
                        object.setDropDownTemplateResource(object.findResourceId(dc,member.getValueString(), "R.layout"));
                    }
                }

                @Override
                public Object get(
                        com.enterlib.databinding.DependencyObject object) {
                    return ((ItemsField) object).dropDownTemplateResourceId;
                };
            });



    protected Object items;
	protected AdapterProvider adapterProvider;
	protected OnTemplateAppliedListener onTemplateAppliedListener;
	protected int templateResourceId;
    protected int dropDownTemplateResourceId;

	public ItemsField() {
		setRestorable(false);
	}

	public ItemsField(View view, boolean required) {
		super(view, required);
		setRestorable(false);
	}

	public ItemsField(View view, String display, boolean required) {
		super(view, display, required);
		setRestorable(false);
	}

	public ItemsField(View view, String valueBinding, String display,
			boolean required) {
		super(view, valueBinding, display, required);
		setRestorable(false);
	}

	public ItemsField(View view, String valueBinding) {
		super(view, valueBinding);
		setRestorable(false);

	}

	public ItemsField(View view) {
		super(view);
		setRestorable(false);
	}

	public Object getItems() {
		return items;
	}

	public void setOnTemplateAppliedListener(
			OnTemplateAppliedListener onTemplateAppliedListener) {
		this.onTemplateAppliedListener = onTemplateAppliedListener;
	}


	public void callonTemplateApplied(Form form, View template, Object item,
			int position) {
		if (onTemplateAppliedListener != null) {
			onTemplateAppliedListener.onTemplateApplied(this, template, form,
					item, position);
		}
	}

	public void setTemplateResource(int resId) {
        templateResourceId = resId;
        if (adapterProvider != null) {
            adapterProvider.setAdapterLayoutRes(templateResourceId);
        } else
            adapterProvider = new AdapterProvider(this.getView().getContext(), templateResourceId);
    }

    public void setDropDownTemplateResource(int resourceId) {
        dropDownTemplateResourceId = resourceId;
        if (adapterProvider != null) {
            adapterProvider.setAdapterDropDownRes(dropDownTemplateResourceId);
        } else
            adapterProvider = new AdapterProvider(this.getView().getContext(), templateResourceId, dropDownTemplateResourceId);
    }


    public abstract void setAdapter(ListAdapter adapter);

	public abstract ListAdapter getAdapter();

}
