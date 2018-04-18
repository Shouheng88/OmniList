package com.jeek.calendar.widget.calendar.month;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;

import com.jeek.calendar.widget.calendar.tools.Default;

import org.joda.time.DateTime;

/**
 * Created by Jimmy on 2016/10/6 0006. */
public class MonthAdapter extends PagerAdapter {

    private SparseArray<MonthView> mViews;
    private Context mContext;
    private TypedArray mArray;
    private MonthCalendarView mMonthCalendarView;
    private int mMonthCount = 48;

    private int selectBGColor = Default.mSelectBGColor;
    private int selectBGTodayColor = Default.mSelectBGTodayColor;
    private int currentDayColor = Default.mCurrentDayColor;
    private int normalDayColor = Default.mNormalDayColor;
    private int holidayTextColor = Default.mHolidayTextColor;

    MonthAdapter(Context context, TypedArray array, MonthCalendarView monthCalendarView) {
        mContext = context;
        mArray = array;
        mMonthCalendarView = monthCalendarView;
        mViews = new SparseArray<>();
    }

    @Override
    public int getCount() {
        return mMonthCount;
    }

    @Override
    @NonNull
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        if (mViews.get(position) == null) {
            int date[] = getYearAndMonth(position);
            MonthView monthView = new MonthView(mContext, mArray, date[0], date[1]);
            monthView.setId(position);
            monthView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
            monthView.setSelectBGColor(selectBGColor);
            monthView.setSelectBGTodayColor(selectBGTodayColor);
            monthView.setCurrentDayColor(currentDayColor);
            monthView.setNormalDayColor(normalDayColor);
            monthView.setHolidayTextColor(holidayTextColor);
            monthView.invalidate();
            monthView.setOnDateClickListener(mMonthCalendarView);
            mViews.put(position, monthView);
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    private int[] getYearAndMonth(int position) {
        int date[] = new int[2];
        DateTime time = new DateTime();
        time = time.plusMonths(position - mMonthCount / 2);
        date[0] = time.getYear();
        date[1] = time.getMonthOfYear() - 1;
        return date;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }

    public SparseArray<MonthView> getViews() {
        return mViews;
    }

    public int getMonthCount() {
        return mMonthCount;
    }

    public void setSelectBGColor(int selectBGColor) {
        this.selectBGColor = selectBGColor;
        int size = mViews.size();
        for (int i=0; i<size; i++) {
            mViews.valueAt(i).setSelectBGColor(selectBGColor);
        }
    }

    public void setSelectBGTodayColor(int selectBGTodayColor) {
        this.selectBGTodayColor = selectBGTodayColor;
        int size = mViews.size();
        for (int i=0; i<size; i++) {
            mViews.valueAt(i).setSelectBGTodayColor(selectBGColor);
        }
    }

    public void setCurrentDayColor(int currentDayColor) {
        this.currentDayColor = currentDayColor;
        int size = mViews.size();
        for (int i=0; i<size; i++) {
            mViews.valueAt(i).setCurrentDayColor(currentDayColor);
        }
    }

    public void setNormalDayColor(int normalDayColor) {
        this.normalDayColor = normalDayColor;
        int size = mViews.size();
        for (int i=0; i<size; i++) {
            mViews.valueAt(i).setNormalDayColor(normalDayColor);
        }
    }

    public void setHolidayTextColor(int holidayTextColor) {
        this.holidayTextColor = holidayTextColor;
        int size = mViews.size();
        for (int i=0; i<size; i++) {
            mViews.valueAt(i).setHolidayTextColor(normalDayColor);
        }
    }
}
