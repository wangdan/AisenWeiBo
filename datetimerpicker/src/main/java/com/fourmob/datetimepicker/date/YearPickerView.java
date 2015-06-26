package com.fourmob.datetimepicker.date;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.StateListDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.fourmob.datetimepicker.R;

import java.util.ArrayList;
import java.util.List;

public class YearPickerView extends ListView implements AdapterView.OnItemClickListener, DatePickerDialog.OnDateChangedListener {
	
    private YearAdapter mAdapter;
	private int mChildSize;
	private final DatePickerController mController;
	private TextViewWithCircularIndicator mSelectedView;
	private int mViewSize;

	public YearPickerView(Context context, DatePickerController datePickerController) {
		super(context);
		mController = datePickerController;
		mController.registerOnDateChangedListener(this);

		setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

		Resources resources = context.getResources();
		mViewSize = resources.getDimensionPixelOffset(R.dimen.date_picker_view_animator_height);
		mChildSize = resources.getDimensionPixelOffset(R.dimen.year_label_height);

		setVerticalFadingEdgeEnabled(true);
		setFadingEdgeLength(mChildSize / 3);
		init(context);
		setOnItemClickListener(this);
		setSelector(new StateListDrawable());
		setDividerHeight(0);
		onDateChanged();
	}

    private static int getYearFromTextView(TextView view) {
        return Integer.valueOf(view.getText().toString());
    }

	private void init(Context context) {
		ArrayList<String> years = new ArrayList<String>();
		for (int year = mController.getMinYear(); year <= mController.getMaxYear(); year++) {
			years.add(String.format("%d", year));
		}
		mAdapter = new YearAdapter(context, R.layout.year_label_text_view, years);
		setAdapter(mAdapter);
	}

	public int getFirstPositionOffset() {
        final View firstChild = getChildAt(0);
        if (firstChild == null) {
            return 0;
        }
        return firstChild.getTop();
	}

	public void onDateChanged() {
		mAdapter.notifyDataSetChanged();
		postSetSelectionCentered(mController.getSelectedDay().year - mController.getMinYear());
	}


	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mController.tryVibrate();
        TextViewWithCircularIndicator clickedView = (TextViewWithCircularIndicator) view;
        if (clickedView != null) {
            if (clickedView != mSelectedView) {
                if (mSelectedView != null) {
                    mSelectedView.drawIndicator(false);
                    mSelectedView.requestLayout();
                }
                clickedView.drawIndicator(true);
                clickedView.requestLayout();
                mSelectedView = clickedView;
            }
            mController.onYearSelected(getYearFromTextView(clickedView));
            mAdapter.notifyDataSetChanged();
        }
	}

	public void postSetSelectionCentered(int position) {
		postSetSelectionFromTop(position, mViewSize / 2 - mChildSize / 2);
	}

	public void postSetSelectionFromTop(final int position, final int y) {
		post(new Runnable() {
			public void run() {
				setSelectionFromTop(position, y);
				requestLayout();
			}
		});
	}

	private class YearAdapter extends ArrayAdapter<String> {

        public YearAdapter(Context context, int resource, List<String> years) {
            super(context, resource, years);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextViewWithCircularIndicator v = (TextViewWithCircularIndicator) super.getView(position, convertView, parent);
            v.requestLayout();
            int year = getYearFromTextView(v);
            boolean selected = mController.getSelectedDay().year == year;
            v.drawIndicator(selected);
            if (selected) {
                mSelectedView = v;
            }
            return v;
        }
	}
}