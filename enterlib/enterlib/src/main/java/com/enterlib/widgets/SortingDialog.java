package com.enterlib.widgets;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.enterlib.R;
import com.enterlib.app.UIUtils;
import com.enterlib.data.SortingElement;

/**
 * Created by hp on 11/24/2016.
 */
public class SortingDialog  extends AlertDialog {

    private final View rootView;
    private final SortingController controller;

    protected SortingDialog(Context context, final SortingController controller) {
        super(context);

        this.controller = controller;
        setIcon(0);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        rootView = inflater.inflate(R.layout.dialog_sorting, null);

        setView(rootView);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                context.getString(R.string.condition_filter_dialog_close),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        setButton(DialogInterface.BUTTON_POSITIVE,
                context.getString(R.string.accept),
                new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        controller.sort();
                    }
                });

        ListView listView = (ListView) rootView.findViewById(R.id.listView);
        listView.setAdapter(new SortElementAdapter());

    }

    class SortElementAdapter extends BaseAdapter {

        LayoutInflater inflater;

        public SortElementAdapter()
        {
            inflater = LayoutInflater.from(getContext());
        }
        /**
         * How many items are in the data set represented by this Adapter.
         *
         * @return Count of items.
         */
        @Override
        public int getCount() {
            return controller.getElements().size();
        }

        /**
         * Get the data item associated with the specified position in the data set.
         *
         * @param position Position of the item whose data we want within the adapter's
         *                 data set.
         * @return The data at the specified position.
         */
        @Override
        public Object getItem(int position) {
            return controller.getElements().get(position);
        }

        /**
         * Get the row id associated with the specified position in the list.
         *
         * @param position The position of the item within the adapter's data set whose row id we want.
         * @return The id of the item at the specified position.
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Get a View that displays the data at the specified position in the data set. You can either
         * create a View manually or inflate it from an XML layout file. When the View is inflated, the
         * parent View (GridView, ListView...) will apply default layout parameters unless you use
         * {@link LayoutInflater#inflate(int, ViewGroup, boolean)}
         * to specify a root view and to prevent attachment to the root.
         *
         * @param position    The position of the item within the adapter's data set of the item whose view
         *                    we want.
         * @param convertView The old view to reuse, if possible. Note: You should check that this view
         *                    is non-null and of an appropriate type before using. If it is not possible to convert
         *                    this view to display the correct data, this method can create a new view.
         *                    Heterogeneous lists can specify their number of view types, so that this View is
         *                    always of the right type (see {@link #getViewTypeCount()} and
         *                    {@link #getItemViewType(int)}).
         * @param parent      The parent that this view will eventually be attached to
         * @return A View corresponding to the data at the specified position.
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if(convertView !=null){
                view = convertView;
            }else {
                view = inflater.inflate(R.layout.adapter_sorting_element, parent, false);
            }

            final SortingElement e = controller.getElements().get(position);
            view.setTag(e);

            UIUtils.setTextViewText(view, R.id.textView, e.getHint());

            CheckBox active = (CheckBox) view.findViewById(R.id.checkBox);
            active.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    e.setActive(isChecked);
                }
            });
            active.setChecked(e.isActive());

            CheckBox descending = (CheckBox) view.findViewById(R.id.descending);
            descending.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    e.setDescending(isChecked);
                }
            });
            descending.setChecked(e.isDescending());

            return  view;
        }
    }
}
