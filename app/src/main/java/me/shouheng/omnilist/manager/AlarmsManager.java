package me.shouheng.omnilist.manager;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.provider.AlarmsStore;
import me.shouheng.omnilist.provider.schema.AlarmSchema;
import me.shouheng.omnilist.receiver.AlarmAlertReceiver;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.NoticePreferences;

/**
 * Created by wangshouheng on 2017/4/18. */
public class AlarmsManager {

    private Context context;

    private NoticePreferences noticePreferences;

    private SparseArray<Alarm> alarms;

    private static final long RETRY_TOTAL_TIME = 61 * 1000, RETRY_INTERVAL = 500;

    private final AlarmManager alarmManager;

    private final AlarmSettingStrategy alarmSettingStrategy;

    // region Initialize instance.
    @SuppressLint("StaticFieldLeak")
    private static AlarmsManager sInstance;

    public static void init(Context context) {
        sInstance = new AlarmsManager(context);
    }

    public static AlarmsManager getsInstance() {
        if (sInstance == null) {
            throw new NullPointerException("AlarmsManager not initialized yet");
        }
        return sInstance;
    }
    // endregion

    private AlarmsManager(Context context) {
        this.context = context;

        alarms = new SparseArray<>();
        noticePreferences = NoticePreferences.getInstance();
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmSettingStrategy = AlarmSettingStrategyFactory.getStrategy(alarmManager);

        initAlarmData();
    }

    // region Initialize alarm data.
    private void initAlarmData() {
        DBRetryCountDownTimer dbRetryCountDownTimer = new DBRetryCountDownTimer(RETRY_TOTAL_TIME, RETRY_INTERVAL);
        boolean hasInitialized = tryReadDb();
        if (!hasInitialized) {
            dbRetryCountDownTimer.start();
        }
    }

    private boolean tryReadDb() {
        List<Alarm> alarms = AlarmsStore.getInstance().get(null, AlarmSchema.ADDED_TIME);
        if (alarms == null) {
            return false;
        } else {
            for (Alarm alarm : alarms){
                this.alarms.put((int) alarm.getCode(), alarm);
            }
            return true;
        }
    }

    private final class DBRetryCountDownTimer extends CountDownTimer {

        private final Handler handler = new Handler();

        DBRetryCountDownTimer(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {}

        @Override
        public void onTick(long millisUntilFinished) {
            boolean hasInitialized = tryReadDb();
            if (hasInitialized) {
                handler.post(this::cancel);
            }
        }
    }
    // endregion

    // region Methods to get alarm.
    public SparseArray<Alarm> getAlarms() {
        return alarms;
    }

    public Alarm getAlarm(int code) throws AlarmNotFoundException {
        Alarm alarm = alarms.get(code);
        if (alarm == null) {
            throw new AlarmNotFoundException("code : " + code);
        }
        return alarm;
    }
    // endregion

    // region Methods to add/register alarm to queue.

    public void addAlarm(Alarm alarm) {
        LogUtils.d(alarm.toChinese());
        alarms.put((int) alarm.getCode(), alarm);
        registerAlarm(alarm);
    }

    public void registerAlarm(Alarm alarm) {
        Calendar nextTime = getNextTime(alarm);
        if (nextTime == null) {
            return;
        }
        alarm.setNextTime(nextTime);
        alarm.setLastModifiedTime(new Date());
        // Persist alarm information to database.
        AlarmsStore.getInstance().update(alarm);
        setUpRTCAlarm(alarm);
    }

    public void registerAllAlarms() {
        int count = alarms.size();
        LogUtils.d("registerAllAlarms: Add all alarms to system, the number of alarms added is " + count);
        for (int i=0; i<count; i++){
            Alarm alarm = alarms.valueAt(i);
            registerAlarm(alarm);
        }
    }

    public void setUpRTCAlarm(Alarm alarm) {
        Intent intent = new Intent(context, AlarmAlertReceiver.class);
        intent.putExtra(Constants.EXTRA_CODE, alarm.getCode());
        intent.setAction(Constants.ACTION_ALARM_ALERT);
        PendingIntent sender = PendingIntent.getBroadcast(
                context, (int) alarm.getCode(), intent, PendingIntent.FLAG_CANCEL_CURRENT);
        alarmSettingStrategy.setRTCAlarm(alarm, sender);
    }
    // endregion

    // region Methods to remove alarm from queue.
    public void removeAlarm(Alarm alarm) {
        LogUtils.d(alarm.toChinese());
        alarms.remove((int) alarm.getCode());
        removeRTCAlarm((int) alarm.getCode());
    }

    private void removeRTCAlarm(int alarmCode) {
        Intent intent = new Intent(context, AlarmAlertReceiver.class);
        intent.putExtra(Constants.EXTRA_CODE, alarmCode);
        PendingIntent sender = PendingIntent.getBroadcast(context, alarmCode, intent, 0);
        alarmManager.cancel(sender);
        sender.cancel();
    }
    // endregion

    // region Methods to snooze alarm.

    /**
     * Snooze alarm to given time. If the rescheduleTime is null, the default snooze minutes will be used.
     *
     * @param code code of alarm.
     * @param rescheduleTime  the next time to schedule alarm, in millis.
     * @throws AlarmNotFoundException no alarm found of given code. */
    public void snooze(int code, @Nullable Integer rescheduleTime) throws AlarmNotFoundException {
        Alarm alarm = alarms.get(code);
        if (alarm != null) {
            Calendar snoozeTime = Calendar.getInstance();
            if (rescheduleTime != null) {
                snoozeTime.setTimeInMillis(TimeUtils.getMillisTodayStart() + rescheduleTime);
                /*We set the next time, only if the specified time is after now.*/
                if (snoozeTime.after(Calendar.getInstance())) {
                    alarm.setNextTime(snoozeTime);
                    setUpRTCAlarm(alarm);
                }
            } else {
                /*Use default snooze time to calculate next time.*/
                int duration = noticePreferences.getSnoozeDuration();
                Calendar nextTime = alarm.getNextTime();
                nextTime.add(Calendar.MINUTE, duration);
                alarm.setNextTime(nextTime);
                setUpRTCAlarm(alarm);
            }
        } else {
            throw new AlarmNotFoundException("alarm not found:");
        }
        /*Broadcast alarm snooze event.*/
        broadcastAlarmState(alarm, Constants.ACTION_ALARM_SNOOZE);
    }
    // endregion

    // region Methods to dismiss alarm.
    public void dismiss(int code) throws AlarmNotFoundException {
        Alarm alarm = alarms.get(code);
        if (alarm != null) {
            /*Register alarm according to next time.*/
            registerAlarm(alarm);
        } else {
            throw new AlarmNotFoundException("code : " + code);
        }
        /*Broadcast alarm dismiss event.*/
        broadcastAlarmState(alarm, Constants.ACTION_ALARM_DISMISS);
    }
    // endregion

    // region Calculate next time of alarm.
    private Calendar getNextTime(Alarm alarm) {
        Calendar now = Calendar.getInstance();
        int hourNow = now.get(Calendar.HOUR_OF_DAY);
        int minuteNow = now.get(Calendar.MINUTE);
        int hour = alarm.getHour();
        int minute = alarm.getMinute();

        Calendar nextTime = Calendar.getInstance();
        switch (alarm.getAlarmType()){
            case DAILY:
                long tomorrow = TimeUtils.getStandardMillisTomorrow();
                nextTime.setTimeInMillis(tomorrow);
                break;
            case SPECIFIED_DATE:
                if (isAlarmOutOfDate(alarm)) return null;
                nextTime.setTimeInMillis(alarm.getEndDate().getTime() + TimeUtils.getTimeInMillis(alarm.getHour(), alarm.getMinute()));
                break;
            case WEEK_REPEAT:
                if (alarm.getEndDate().before(new Date())){ // 判断闹钟是否过期
                    return null;
                }
                if (!alarm.getDaysOfWeek().isRepeatSet()){ // 闹钟非重复：效果同一次性闹钟，这种闹钟应该是不存在的！
                    ToastUtils.makeToast("One Illegal alarm!!");
                    if (isAlarmOutOfDate(alarm)) return null;
                    nextTime.setTimeInMillis(alarm.getEndDate().getTime() + TimeUtils.getTimeInMillis(alarm.getHour(), alarm.getMinute()));
                } else { // 如果是今天的已经过期的闹钟，那么将时间设置为明天，然后按照明天计算需要增加几天，因为按照今天计算的话得到的还是过期的结果
                    if (hour < hourNow  || hour == hourNow && minute <= minuteNow) {
                        nextTime.add(Calendar.DAY_OF_YEAR, 1);
                    }
                    nextTime.set(Calendar.HOUR_OF_DAY, hour);
                    nextTime.set(Calendar.MINUTE, minute);
                    nextTime.set(Calendar.SECOND, 0);
                    nextTime.set(Calendar.MILLISECOND, 0);

                    int addDays = alarm.getDaysOfWeek().getNextAlarm(nextTime);
                    if (addDays > 0) {
                        nextTime.add(Calendar.DAY_OF_WEEK, addDays);
                    }
                }
                break;
            case MONTH_REPEAT:
                break;
        }

        return nextTime;
    }

    private boolean isAlarmOutOfDate(Alarm alarm) {
        long millisOfAlarm = alarm.getEndDate().getTime() + TimeUtils.getTimeInMillis(alarm.getHour(), alarm.getMinute());
        return millisOfAlarm < System.currentTimeMillis(); // 闹钟时间小于当前时间，闹钟过期
    }
    // endregion

    private void broadcastAlarmState(Alarm alarm, String action) {
        LogUtils.d("broadcastAlarmState: " + action);
        Intent intent = new Intent(action);
        intent.putExtra(Constants.EXTRA_CODE, alarm.getCode());
        context.sendBroadcast(intent);
    }
}
