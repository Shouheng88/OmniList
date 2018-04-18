package me.shouheng.omnilist.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.jeek.calendar.widget.calendar.tools.OnCalendarClickListener;

import java.util.Calendar;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.FragmentMonthCalendarBinding;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.utils.TimeUtils;

public class MonthFragment extends BaseFragment<FragmentMonthCalendarBinding> implements OnCalendarClickListener {

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
        configToolbar();

        configCalendarTheme();

        configEvents();

        showDateSubTitle(Calendar.getInstance());
    }

    private void configToolbar() {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.drawer_menu_calendar);
            }
        }
    }

    private void configCalendarTheme() {
        if (isDarkTheme()) {
            getBinding().weekBar.setBackgroundResource(R.color.dark_theme_background);

            getBinding().mcvCalendar.setNormalDayColor(Color.WHITE);
            getBinding().mcvCalendar.setBackgroundResource(R.color.dark_theme_background);

            getBinding().wcvCalendar.setNormalDayColor(Color.WHITE);
            getBinding().wcvCalendar.setBackgroundResource(R.color.dark_theme_background);

            getBinding().rlScheduleList.setBackgroundResource(R.color.dark_theme_background);
            getBinding().rvScheduleList.setBackgroundResource(R.color.dark_theme_background);
            getBinding().rlNoTask.setBackgroundResource(R.color.dark_theme_background);
        }

        getBinding().wcvCalendar.setSelectedBGColor(primaryColor());
        getBinding().wcvCalendar.setSelectBGTodayColor(primaryColor());
        getBinding().wcvCalendar.setCurrentDayColor(primaryColor());
        getBinding().wcvCalendar.setHolidayTextColor(primaryColor());

        getBinding().mcvCalendar.setSelectedBGColor(primaryColor());
        getBinding().mcvCalendar.setSelectBGTodayColor(primaryColor());
        getBinding().mcvCalendar.setCurrentDayColor(primaryColor());
        getBinding().mcvCalendar.setHolidayTextColor(primaryColor());
    }

    private void configEvents() {
        getBinding().wcvCalendar.addOnCalendarClickListener(this);
        getBinding().mcvCalendar.addOnCalendarClickListener(this);
    }

    private void showDateSubTitle(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        showDateSubTitle(calendar);
    }

    private void showDateSubTitle(Calendar calendar) {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setSubtitle(TimeUtils.getNoMonthDay(getContext(), calendar.getTime()));
            }
        }
    }

    @Override
    public void onClickDate(int year, int month, int day) {
        showDateSubTitle(year, month, day);
    }
}
