package me.shouheng.omnilist.async;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.manager.AlarmNotFoundException;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.manager.PresentationToModelIntents;
import me.shouheng.omnilist.manager.WakeLockManager;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.preferences.NoticePreferences;

public class AlarmsService extends Service {

    private AlarmsManager alarmsManager;

    public static class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction() == null) return;

            switch (intent.getAction()) {
                case "android.intent.action.BOOT_COMPLETED":
                case "android.intent.action.TIMEZONE_CHANGED":
                case "android.intent.action.TIME_SET":
                case "android.intent.action.LOCALE_CHANGED":
                    intent.setClass(context, AlarmsService.class);
                    if (NoticePreferences.getInstance().getAllowWakeLock()){
                        WakeLockManager.getWakeLockManager().acquirePartialWakeLock(intent, "AlarmsService");
                    }
                    context.startService(intent);
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        alarmsManager = AlarmsManager.getsInstance();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (action == null) {
            throw new IllegalArgumentException("Illegal action");
        }

        switch (action){
            case Intent.ACTION_BOOT_COMPLETED:
                LogUtils.d("onStartCommand: " + "android.intent.action.BOOT_COMPLETED");
                alarmsManager.registerAllAlarms();
                break;
            case Intent.ACTION_TIMEZONE_CHANGED:
                LogUtils.d("onStartCommand: " + "android.intent.action.TIMEZONE_CHANGED");
                alarmsManager.registerAllAlarms();
                break;
            case Intent.ACTION_LOCALE_CHANGED:
                LogUtils.d("onStartCommand: " + "android.intent.action.LOCALE_CHANGED");
                alarmsManager.registerAllAlarms();
                break;
            case Intent.ACTION_TIME_CHANGED:
                LogUtils.d("onStartCommand: " + "android.intent.action.TIME_SET");
                alarmsManager.registerAllAlarms();
                break;
        }

        int code = -1;
        try {
            code = intent.getIntExtra(Constants.EXTRA_CODE, -1);
            switch (action) {
                case PresentationToModelIntents.ACTION_REQUEST_SNOOZE:
                    alarmsManager.snooze(code, null);
                    break;
                case PresentationToModelIntents.ACTION_REQUEST_DISMISS:
                    alarmsManager.dismiss(code);
                    break;
            }
        } catch (AlarmNotFoundException e) {
            LogUtils.d("onStartCommand: Alarm not found [ code:" + code + "]");
        }
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
