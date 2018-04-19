package me.shouheng.omnilist.manager;

import android.app.PendingIntent;

import me.shouheng.omnilist.model.Alarm;

/**
 * Created by wangshouheng on 2017/4/19. */
public interface AlarmSettingStrategy {
    void setRTCAlarm(Alarm alarm, PendingIntent sender);
}
