package me.shouheng.omnilist.manager;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import me.shouheng.omnilist.BuildConfig;
import me.shouheng.omnilist.async.AlarmsService;
import me.shouheng.omnilist.config.Constants;


public class PresentationToModelIntents {

    public static final String ACTION_REQUEST_SNOOZE = BuildConfig.APPLICATION_ID
            + ".model.interfaces.ServiceIntents.ACTION_REQUEST_SNOOZE";

    public static final String ACTION_REQUEST_DISMISS = BuildConfig.APPLICATION_ID
            + ".model.interfaces.ServiceIntents.ACTION_REQUEST_DISMISS";

    public static PendingIntent createPendingIntent(Context context, String action, int code) {
        Intent intent = new Intent(action);
        intent.putExtra(Constants.EXTRA_CODE, code);
        intent.setClass(context, AlarmsService.class);
        return PendingIntent.getService(context, code, intent, 0);
    }
}
