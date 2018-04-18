package me.shouheng.omnilist.fragment;

import android.os.Bundle;

import com.jeek.calendar.widget.calendar.week.WeekCalendarView;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.FragmentMonthCalendarBinding;
import me.shouheng.omnilist.fragment.base.BaseFragment;

public class MonthFragment extends BaseFragment<FragmentMonthCalendarBinding> {

    public static MonthFragment newInstance() {
        Bundle args = new Bundle();
        MonthFragment fragment = new MonthFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_month_calendar;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        WeekCalendarView weekCalendarView;
    }
}
