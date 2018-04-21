package com.jeek.calendar.widget.calendar.month;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.jeek.calendar.library.R;
import com.jeek.calendar.widget.calendar.tools.OnCalendarClickListener;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Jimmy on 2016/10/6 0006. */
public class MonthCalendarView extends ViewPager implements OnMonthClickListener {

    private MonthAdapter mMonthAdapter;

    private List<OnCalendarClickListener> onCalendarClickListeners = new LinkedList<>();

    public MonthCalendarView(Context context) {
        this(context, null);
    }

    public MonthCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        mMonthAdapter = new MonthAdapter(context, context.obtainStyledAttributes(attrs, R.styleable.MonthCalendarView), this);
        setAdapter(mMonthAdapter);
        setCurrentItem(mMonthAdapter.getMonthCount() / 2, false);

        addOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) { }

            @Override
            public void onPageSelected(int position) {}

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        MonthView monthView = mMonthAdapter.getViews().get(getCurrentItem());
                        if (monthView != null) {
                            monthView.clickThisMonth(monthView.getSelectYear(), monthView.getSelectMonth(), monthView.getSelectDay());
                        }
                        break;
                }
            }
        });
    }

    @Override
    public void onClickThisMonth(int year, int month, int day) {
        for (OnCalendarClickListener onCalendarClickListener : onCalendarClickListeners) {
            onCalendarClickListener.onClickDate(year, month, day);
        }
    }

    @Override
    public void onClickLastMonth(int year, int month, int day) {
        MonthView monthDateView = mMonthAdapter.getViews().get(getCurrentItem() - 1);
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
        }
        setCurrentItem(getCurrentItem() - 1, true);
    }

    @Override
    public void onClickNextMonth(int year, int month, int day) {
        MonthView monthDateView = mMonthAdapter.getViews().get(getCurrentItem() + 1);
        if (monthDateView != null) {
            monthDateView.setSelectYearMonth(year, month, day);
            monthDateView.invalidate();
        }
        onClickThisMonth(year, month, day);
        setCurrentItem(getCurrentItem() + 1, true);
    }

    /**
     * To today */
    public void focusToday() {
        setCurrentItem(mMonthAdapter.getMonthCount() / 2, true);
        MonthView monthView = mMonthAdapter.getViews().get(mMonthAdapter.getMonthCount() / 2);
        if (monthView != null) {
            Calendar calendar = Calendar.getInstance();
            monthView.clickThisMonth(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
        }
    }

    public void addOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        onCalendarClickListeners.add(onCalendarClickListener);
    }

    public void removeOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        onCalendarClickListeners.remove(onCalendarClickListener);
    }

    public SparseArray<MonthView> getMonthViews() {
        return mMonthAdapter.getViews();
    }

    public MonthView getCurrentMonthView() {
        return getMonthViews().get(getCurrentItem());
    }

    public void setOnLoadMonthTaskListener(OnLoadMonthTaskListener onLoadMonthTaskListener) {
        int length = mMonthAdapter.getCount();
        SparseArray<MonthView> views = mMonthAdapter.getViews();
        for (int i=0;i<length;i++) {
            views.valueAt(i).setOnLoadMonthTaskListener(onLoadMonthTaskListener);
        }
    }

    // region custom methods to add color
    public void setSelectedBGColor(int selectBGColor) {
        mMonthAdapter.setSelectBGColor(selectBGColor);
    }

    public void setSelectBGTodayColor(int selectBGTodayColor) {
        mMonthAdapter.setSelectBGTodayColor(selectBGTodayColor);
    }

    public void setCurrentDayColor(int currentDayColor) {
        mMonthAdapter.setCurrentDayColor(currentDayColor);
    }

    public void setNormalDayColor(int normalDayColor) {
        mMonthAdapter.setNormalDayColor(normalDayColor);
    }

    public void setHolidayTextColor(int holidayTextColor) {
        mMonthAdapter.setHolidayTextColor(holidayTextColor);
    }
    // endregion
}
