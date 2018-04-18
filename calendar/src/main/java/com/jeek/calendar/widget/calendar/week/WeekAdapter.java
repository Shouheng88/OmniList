package com.jeek.calendar.widget.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.PagerAdapter;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;

import com.jeek.calendar.widget.calendar.tools.Default;

import org.joda.time.DateTime;

/**
 * Created by Jimmy on 2016/10/7 0007. */
public class WeekAdapter extends PagerAdapter {

    private SparseArray<WeekView> mViews;
    private Context mContext;
    private TypedArray mArray;
    private WeekCalendarView mWeekCalendarView;
    private DateTime mStartDate;
    private int mWeekCount = 220;

    private int selectBGColor = Default.mSelectBGColor;
    private int selectBGTodayColor = Default.mSelectBGTodayColor;
    private int currentDayColor = Default.mCurrentDayColor;
    private int normalDayColor = Default.mNormalDayColor;

    private OnLoadWeekTaskListener onLoadWeekTaskListener;

    public WeekAdapter(Context context, TypedArray array, WeekCalendarView weekCalendarView) {
        mContext = context;
        mArray = array;
        mWeekCalendarView = weekCalendarView;
        mViews = new SparseArray<>();
        initStartDate();
    }

    private void initStartDate() {
        mStartDate = new DateTime();
        mStartDate = mStartDate.plusDays(-mStartDate.getDayOfWeek() % 7);
    }

    @Override
    public int getCount() {
        return mWeekCount;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (mViews.get(position) == null) {
            instanceWeekView(position);
        }
        container.addView(mViews.get(position));
        return mViews.get(position);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((View) object);
    }

    public SparseArray<WeekView> getViews() {
        return mViews;
    }

    public int getWeekCount() {
        return mWeekCount;
    }

    public WeekView instanceWeekView(int position) {
        WeekView weekView = new WeekView(mContext, mArray,
                mStartDate.plusWeeks(position - mWeekCount / 2));
        weekView.setId(position);
        weekView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        weekView.setOnWeekClickListener(mWeekCalendarView);
        weekView.setOnLoadWeekTaskListener(onLoadWeekTaskListener);
        weekView.setSelectBGColor(selectBGColor);
        weekView.setSelectBGTodayColor(selectBGTodayColor);
        weekView.setCurrentDayColor(currentDayColor);
        weekView.setNormalDayColor(normalDayColor);
        weekView.invalidate();
        mViews.put(position, weekView);
        return weekView;
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
}
