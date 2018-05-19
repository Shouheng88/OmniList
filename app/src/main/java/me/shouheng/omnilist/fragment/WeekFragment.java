package me.shouheng.omnilist.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;

import java.util.Calendar;
import java.util.List;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.databinding.FragmentWeekBinding;
import me.shouheng.omnilist.fragment.base.CommonFragment;
import me.shouheng.omnilist.listener.OnDataChangeListener;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.PalmUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.enums.CalendarType;
import me.shouheng.omnilist.utils.preferences.ActionPreferences;

public class WeekFragment extends CommonFragment<FragmentWeekBinding> implements
        WeekView.EventClickListener,
        MonthLoader.MonthChangeListener,
        WeekView.EventLongPressListener,
        OnDataChangeListener {

    private static final int TYPE_DAY_VIEW = 1;
    private static final int TYPE_THREE_DAY_VIEW = 3;
    private static final int TYPE_WEEK_VIEW = 7;

    private final int REQUEST_EDIT_ASSIGNMENT = 0x3030;
    // Default 3 days
    private int mWeekViewType = TYPE_THREE_DAY_VIEW;

    private ActionPreferences actionPreferences;

    private ActionBar actionBar;

    private int curYear, curMonth;

    private AssignmentsStore assignmentsStore;

    public static WeekFragment newInstance() {
        Bundle args = new Bundle();
        WeekFragment fragment = new WeekFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_week;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        configToolbar();

        actionPreferences = ActionPreferences.getInstance();
        mWeekViewType = actionPreferences.getWeekViewType();

        assignmentsStore = AssignmentsStore.getInstance();

        getBinding().wv.setNumberOfVisibleDays(mWeekViewType);
        getBinding().wv.setOnEventClickListener(this);
        getBinding().wv.setMonthChangeListener(this);
        getBinding().wv.setEventLongPressListener(this);
        getBinding().wv.setIsDarkTheme(isDarkTheme());
        getBinding().wv.setPrimaryColor(primaryColor());
        getBinding().wv.goToHour(TimeUtils.calTimeToGo());
        getBinding().wv.setAccentColor(accentColor());
        getBinding().wv.setNowLineColor(primaryColor());
        getBinding().wv.setTodayHeaderTextColor(primaryColor());
        getBinding().wv.setAllDayEventHeight(100);
        getBinding().wv.setHourHeight(actionPreferences.getWeekViewHourHeight());
        getBinding().wv.setTodayBackgroundColor(Color.argb(24, Color.red(primaryColor()), Color.green(primaryColor()), Color.blue(primaryColor())));
        getBinding().wv.setScrollListener((newFirstVisibleDay, oldFirstVisibleDay) -> {
            if (actionBar != null && PalmUtils.isAlive(WeekFragment.this)) {
                actionBar.setSubtitle(TimeUtils.getWeekCalendarSubTitle(getContext(), newFirstVisibleDay, mWeekViewType));
            }
        });
    }

    private void configToolbar() {
        if (getActivity() != null) {
            actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.drawer_menu_calendar);
            }
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_show_week).setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.calendar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.item_today:
                getBinding().wv.goToToday();
                return true;
            case R.id.item_one_day:
                if (mWeekViewType != TYPE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_DAY_VIEW;
                    getBinding().wv.setNumberOfVisibleDays(1);
                }
                return true;
            case R.id.item_three_day:
                if (mWeekViewType != TYPE_THREE_DAY_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_THREE_DAY_VIEW;
                    getBinding().wv.setNumberOfVisibleDays(3);
                }
                return true;
            case R.id.item_seven_day:
                if (mWeekViewType != TYPE_WEEK_VIEW) {
                    item.setChecked(!item.isChecked());
                    mWeekViewType = TYPE_WEEK_VIEW;
                    getBinding().wv.setNumberOfVisibleDays(7);
                }
                return true;
            case R.id.action_show_month:
                Activity activity = getActivity();
                if (activity != null && activity instanceof OnWeekCalendarInteraction) {
                    ((OnWeekCalendarInteraction) activity).onShowMonthClicked();
                }
                actionPreferences.setCalendarType(CalendarType.MONTH);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        switch (event.getEventType()) {
            case ASSIGNMENT:
                Assignment assignment = assignmentsStore.get(event.getCode());
                ContentActivity.editAssignment(this, assignment, REQUEST_EDIT_ASSIGNMENT);
                break;
        }
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        curYear = newYear;
        curMonth = newMonth;
        LogUtils.d("onMonthChanged");
        return assignmentsStore.getWeek(newYear, newMonth);
    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) { }

    @Override
    public void onDestroy() {
        actionPreferences.setWeekViewType(mWeekViewType);
        actionPreferences.setWeekViewHourHeight(getBinding().wv.getHourHeight());
        super.onDestroy();
    }

    @Override
    public void onDataChanged() {
        getBinding().wv.notifyDatasetChanged();
    }

    public interface OnWeekCalendarInteraction {
        default void onScroll(Calendar newFirstVisibleDay, Calendar oldFirstVisibleDay) { }
        void onShowMonthClicked();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_EDIT_ASSIGNMENT:
                    getBinding().wv.notifyDatasetChanged();
                    break;
            }
        }
    }
}
