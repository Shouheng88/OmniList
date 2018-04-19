package me.shouheng.omnilist.manager;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.Build;

import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.PalmUtils;

public class AlarmSettingStrategyFactory {

    public static AlarmSettingStrategy getStrategy(AlarmManager alarmManager) {
        if (PalmUtils.isMarshmallow()){
            return new MarshmallowSetter(alarmManager);
        } else if (PalmUtils.isKitKat()) {
            return new KitKatSetter(alarmManager);
        } else {
            return new IceCreamSetter(alarmManager);
        }
    }

    public static final class IceCreamSetter implements AlarmSettingStrategy {

        private final AlarmManager alarmManager;

        IceCreamSetter(AlarmManager alarmManager) {
            this.alarmManager = alarmManager;
        }

        @Override
        public void setRTCAlarm(Alarm alarm, PendingIntent sender) {
            LogUtils.d("IceCream setRTCAlarm: \n" + alarm.toChinese());
            alarmManager.set(AlarmManager.RTC_WAKEUP, alarm.getNextTime().getTimeInMillis(), sender);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static final class KitKatSetter implements AlarmSettingStrategy {

        private final AlarmManager alarmManager;

        KitKatSetter(AlarmManager alarmManager) {
            this.alarmManager = alarmManager;
        }

        @Override
        public void setRTCAlarm(Alarm alarm, PendingIntent sender) {
            LogUtils.d("KitKat setRTCAlarm: \n" + alarm.toChinese());
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getNextTime().getTimeInMillis(), sender);
        }
    }

    @TargetApi(23)
    public static final class MarshmallowSetter implements AlarmSettingStrategy {

        private final AlarmManager alarmManager;

        MarshmallowSetter(AlarmManager alarmManager) {
            this.alarmManager = alarmManager;
        }

        @Override
        public void setRTCAlarm(Alarm alarm, PendingIntent sender) {
            LogUtils.d("Marshmallow setRTCAlarm: \n" + alarm.toChinese());
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarm.getNextTime().getTimeInMillis(), sender);
        }
    }
}
