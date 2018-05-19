package me.shouheng.omnilist.dialog;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

import org.joda.time.DateTime;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.DialogReminderPickerLayoutBinding;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.enums.AlarmType;
import me.shouheng.omnilist.model.tools.DaysOfWeek;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.utils.preferences.UserPreferences;

/**
 * Created by wangshouheng on 2017/4/22.*/
public class ReminderPickerDialog extends DialogFragment {

    private Alarm alarm;

    private OnReminderPickedListener onReminderPickedListener;

    private DialogReminderPickerLayoutBinding binding;

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_reminder_picker_layout, null, false);

        switchLayoutToRepeat(alarm.getAlarmType() != AlarmType.SPECIFIED_DATE);

        binding.spAlarmType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        alarm.setAlarmType(AlarmType.SPECIFIED_DATE);
                        break;
                    case 1:
                        alarm.setAlarmType(AlarmType.WEEK_REPEAT);
                        alarm.setDaysOfWeek(DaysOfWeek.getInstance(127));
                        break;
                    case 2:
                        alarm.setAlarmType(AlarmType.WEEK_REPEAT);
                        alarm.setDaysOfWeek(DaysOfWeek.getInstance(31));
                        break;
                    case 3:
                        alarm.setAlarmType(AlarmType.WEEK_REPEAT);
                        alarm.setDaysOfWeek(DaysOfWeek.getInstance(96));
                        break;
                    case 4:
                        alarm.setAlarmType(AlarmType.WEEK_REPEAT);
                        showWeekPicker();
                        break;
                    case 5:
                        alarm.setAlarmType(AlarmType.MONTH_REPEAT);
                        break;
                }
                displayAlarmInfo();
                switchLayoutToRepeat(position != 0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
        binding.spAlarmType.setSelection(getSelection());

        binding.oneShotLayout.tvOneShotDate.setOnClickListener(this::showDatePicker);
        binding.oneShotLayout.tvOneShotDate.setTextColor(ColorUtils.accentColor());
        binding.oneShotLayout.tvOneShotTime.setOnClickListener(this::showTimePicker);
        binding.oneShotLayout.tvOneShotTime.setTextColor(ColorUtils.accentColor());

        binding.repeatLayout.tvRepeatEndDate.setOnClickListener(this::showDatePicker);
        binding.repeatLayout.tvRepeatEndDate.setTextColor(ColorUtils.accentColor());
        binding.repeatLayout.tvRepeatAlarmSetTime.setOnClickListener(this::showTimePicker);
        binding.repeatLayout.tvRepeatAlarmSetTime.setTextColor(ColorUtils.accentColor());
        String startDateInfoStr = String.format(getString(R.string.today_date_time), TimeUtils.getLongDateWithWeekday(getContext(), new Date()));
        binding.repeatLayout.tvRepeatStartDate.setText(startDateInfoStr);

        displayAlarmInfo();

        return new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setView(binding.getRoot())
                .setNegativeButton(R.string.text_cancel, null)
                .setPositiveButton(R.string.text_confirm, (dialog, which) -> {
                    if (onReminderPickedListener != null){
                        onReminderPickedListener.onReminderPicked(alarm);
                    }
                })
                .create();
    }

    private void setBuilder(Builder builder) {
        this.alarm = builder.alarm;
        this.onReminderPickedListener = builder.onReminderPickedListener;
    }

    private int getSelection() {
        if (alarm.getAlarmType() == AlarmType.SPECIFIED_DATE) {
            return 0;
        }
        if (alarm.getAlarmType() == AlarmType.WEEK_REPEAT) {
            switch (alarm.getDaysOfWeek().getCoded()) {
                case 127: return 1;
                case 31: return 2;
                case 96: return 3;
                default: return 4;
            }
        }
        /*Default selection*/
        return 0;
    }

    private void switchLayoutToRepeat(boolean isRepeatMode) {
        binding.oneShotLayout.getRoot().setVisibility(isRepeatMode ? View.GONE : View.VISIBLE);
        binding.repeatLayout.getRoot().setVisibility(isRepeatMode ? View.VISIBLE : View.GONE);
    }

    private void showDatePicker(View v) {
        PopupMenu popupM = new PopupMenu(Objects.requireNonNull(getContext()), v);
        popupM.inflate(R.menu.date_picker_menu);

        popupM.getMenu().getItem(0).setTitle(String.format(getString(R.string.today_with_date), TimeUtils.getShortDate(getContext(), TimeUtils.today())));
        popupM.getMenu().getItem(1).setTitle(String.format(getString(R.string.tomorrow_with_date), TimeUtils.getShortDate(getContext(), TimeUtils.tomorrow())));
        popupM.getMenu().getItem(2).setTitle(String.format(getString(R.string.this_friday_with_date), TimeUtils.getShortDate(getContext(), TimeUtils.thisFriday())));
        popupM.getMenu().getItem(3).setTitle(String.format(getString(R.string.this_sunday_with_date), TimeUtils.getShortDate(getContext(), TimeUtils.thisSunday())));
        popupM.getMenu().getItem(4).setTitle(String.format(getString(R.string.next_monday_with_date), TimeUtils.getShortDate(getContext(), TimeUtils.nextMonday())));

        popupM.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.item_today:
                    onDatePicked(TimeUtils.today());
                    break;
                case R.id.item_tomorrow:
                    onDatePicked(TimeUtils.tomorrow());
                    break;
                case R.id.item_this_friday:
                    onDatePicked(TimeUtils.thisFriday());
                    break;
                case R.id.item_this_sunday:
                    onDatePicked(TimeUtils.thisSunday());
                    break;
                case R.id.item_next_monday:
                    onDatePicked(TimeUtils.nextMonday());
                    break;
                case R.id.item_custom:
                    showCustomDateDialog();
                    return true;
                case R.id.item_clear_date:break; // invisible
            }
            displayAlarmInfo();
            return true;
        });
        popupM.show();
    }

    private void showTimePicker(View v) {
        PopupMenu popupM = new PopupMenu(Objects.requireNonNull(getContext()), v);
        popupM.inflate(R.menu.time_picker_menu);
        ViewUtils.forceShowIcon(popupM);
        popupM.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()){
                case R.id.item_morning:
                    alarm.setHour(9);
                    alarm.setMinute(0);
                    break;
                case R.id.item_noon:
                    alarm.setHour(12);
                    alarm.setMinute(0);
                    break;
                case R.id.item_afternoon:
                    alarm.setHour(15);
                    alarm.setMinute(0);
                    break;
                case R.id.item_evening:
                    alarm.setHour(18);
                    alarm.setMinute(0);
                    break;
                case R.id.item_night:
                    alarm.setHour(21);
                    alarm.setMinute(0);
                    break;
                case R.id.item_custom:
                    showCustomTimeDialog();
                    return true;
                case R.id.item_clear_time: break; // invisible
            }
            displayAlarmInfo();
            return true;
        });
        popupM.show();
    }

    private void showCustomDateDialog() {
        int year, month, dayOfMonth;

        /*Get old end date information.*/
        Calendar oldCal = Calendar.getInstance();
        oldCal.setTime(alarm.getEndDate());
        year = oldCal.get(Calendar.YEAR);
        month = oldCal.get(Calendar.MONTH);
        dayOfMonth = oldCal.get(Calendar.DAY_OF_MONTH);

        /*Use current date.*/
        if (year > 2037){
            Calendar calendar = Calendar.getInstance();
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        }

        /*Show date picker dialog.*/
        DatePickerDialog.newInstance((datePickerDialog, year1, month1, day) ->
                onDatePicked(TimeUtils.date(year1, month1, day)),
                year, month, dayOfMonth, true
        ).show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "DATE_PICKER");
    }

    private void onDatePicked(Date endDate) {
        switch (alarm.getAlarmType()) {
            case WEEK_REPEAT:
            case DAILY_REPORT:
                // 默认的重复性质的闹钟的开始时间是今天的开始时间，结束时间是指定的日期
                alarm.setStartDate(TimeUtils.today());
                alarm.setEndDate(endDate);
                break;
            case SPECIFIED_DATE:
                // 用选定的日期的最大时间和最小时间作为闹钟的起止时间
                DateTime dateTime = new DateTime(endDate);
                alarm.setStartDate(TimeUtils.startTime(dateTime));
                alarm.setEndDate(TimeUtils.endTime(dateTime));
                break;
        }
        displayAlarmInfo();
    }

    private void showCustomTimeDialog() {
        TimePickerDialog.newInstance((view, hourOfDay, minute) -> {
                    alarm.setHour(hourOfDay);
                    alarm.setMinute(minute);
                    displayAlarmInfo(); }, alarm.getHour(), alarm.getMinute(), UserPreferences.getInstance().is24HourMode(), true
        ).show(Objects.requireNonNull(getActivity()).getSupportFragmentManager(), "TIME_PICKER");
    }

    private void showWeekPicker() {
        final boolean[] booleanArray = alarm.getDaysOfWeek().getBooleanArray();
        Dialog dlg = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                .setTitle(R.string.set_weeks)
                .setMultiChoiceItems(R.array.days_of_week, booleanArray, (dialog, which, isChecked) -> booleanArray[which] = isChecked)
                .setPositiveButton(R.string.text_accept, (dialog, which) -> {
                    DaysOfWeek daysOfWeek = DaysOfWeek.getInstance(booleanArray);
                    if (!daysOfWeek.isRepeatSet()){
                        ToastUtils.makeToast(R.string.week_required);
                        return;
                    }
                    alarm.setDaysOfWeek(DaysOfWeek.getInstance(booleanArray));
                    displayAlarmInfo();
                })
                .setNegativeButton(R.string.text_give_up, null)
                .create();
        dlg.show();
    }

    private void displayAlarmInfo() {
        switch (alarm.getAlarmType()){
            case SPECIFIED_DATE:
                binding.oneShotLayout.tvOneShotTime.setText(TimeUtils.shortTime(alarm.getHour(), alarm.getMinute()));
                binding.oneShotLayout.tvOneShotDate.setText(TimeUtils.getLongDateWithWeekday(getContext(), alarm.getEndDate()));
                binding.tvReminderInfo.setText(alarm.getAlarmInfo(getContext()));
                break;
            case WEEK_REPEAT:
                binding.repeatLayout.tvRepeatAlarmSetTime.setText(TimeUtils.shortTime(alarm.getHour(), alarm.getMinute()));
                binding.repeatLayout.tvRepeatEndDate.setText(TimeUtils.getLongDateWithWeekday(getContext(), alarm.getEndDate()));
                binding.tvReminderInfo.setText(alarm.getAlarmInfo(getContext()));
                break;
        }
    }

    public interface OnReminderPickedListener {
        void onReminderPicked(Alarm alarm);
    }

    public static class Builder {

        private Alarm alarm;

        private OnReminderPickedListener onReminderPickedListener;

        public Builder setAlarm(@NonNull Alarm alarm) {
            this.alarm = alarm;
            return this;
        }

        public Builder setOnReminderPickedListener(OnReminderPickedListener onReminderPickedListener) {
            this.onReminderPickedListener = onReminderPickedListener;
            return this;
        }

        public ReminderPickerDialog build() {
            ReminderPickerDialog reminderPickerDialog = new ReminderPickerDialog();
            reminderPickerDialog.setBuilder(this);
            return reminderPickerDialog;
        }
    }
}
