package com.jeek.calendar.widget.calendar.week;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.SparseArray;

import com.jeek.calendar.library.R;
import com.jeek.calendar.widget.calendar.tools.OnCalendarClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jimmy on 2016/10/7 0007. */
public class WeekCalendarView extends ViewPager implements OnWeekClickListener {

    private List<OnCalendarClickListener> onCalendarClickListeners = new ArrayList<>();
    private OnLoadWeekTaskListener onLoadWeekTaskListener;
    private WeekAdapter mWeekAdapter;

    public WeekCalendarView(Context context) {
        this(context, null);
    }

    public WeekCalendarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(context, attrs);
        addOnPageChangeListener(mOnPageChangeListener);
    }

    private void initAttrs(Context context, AttributeSet attrs) {
        initWeekAdapter(context, context.obtainStyledAttributes(attrs, R.styleable.WeekCalendarView));
    }

    private void initWeekAdapter(Context context, TypedArray array) {
        mWeekAdapter = new WeekAdapter(context, array, this);
        setAdapter(mWeekAdapter);
        setCurrentItem(mWeekAdapter.getWeekCount() / 2, false);
    }

    @Override
    public void onClickDate(int year, int month, int day) {
        for (OnCalendarClickListener onCalendarClickListener : onCalendarClickListeners) {
            onCalendarClickListener.onClickDate(year, month, day);
        }
    }

    private OnPageChangeListener mOnPageChangeListener = new OnPageChangeListener() {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        @Override
        public void onPageSelected(int position) {
            WeekView weekView = mWeekAdapter.getViews().get(position);
            if (weekView != null) {
                weekView.clickThisWeek(weekView.getSelectYear(), weekView.getSelectMonth(), weekView.getSelectDay());
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {}
    };

    /**
     * 设置点击日期监听
     *
     * @param onCalendarClickListener 监听器 */
    public void addOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        onCalendarClickListeners.add(onCalendarClickListener);
    }

    public void removeOnCalendarClickListener(OnCalendarClickListener onCalendarClickListener) {
        onCalendarClickListeners.remove(onCalendarClickListener);
    }

    public SparseArray<WeekView> getWeekViews() {
        return mWeekAdapter.getViews();
    }

    public WeekAdapter getWeekAdapter() {
        return mWeekAdapter;
    }

    public WeekView getCurrentWeekView() {
        return getWeekViews().get(getCurrentItem());
    }

    public void setOnLoadWeekTaskListener(OnLoadWeekTaskListener onLoadWeekTaskListener) {
        this.onLoadWeekTaskListener = onLoadWeekTaskListener;
        int length = mWeekAdapter.getCount();
        SparseArray<WeekView> views = mWeekAdapter.getViews();
        for (int i=0;i<length;i++) {
            views.valueAt(i).setOnLoadWeekTaskListener(onLoadWeekTaskListener);
        }
    }

    public void setSelectedBGColor(int selectBGColor) {
        mWeekAdapter.setSelectBGColor(selectBGColor);
    }

    public void setSelectBGTodayColor(int selectBGTodayColor) {
        mWeekAdapter.setSelectBGTodayColor(selectBGTodayColor);
    }

    public void setCurrentDayColor(int currentDayColor) {
        mWeekAdapter.setCurrentDayColor(currentDayColor);
    }

    public void setNormalDayColor(int normalDayColor) {
        mWeekAdapter.setNormalDayColor(normalDayColor);
    }
}
