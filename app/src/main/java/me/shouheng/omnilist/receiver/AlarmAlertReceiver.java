package me.shouheng.omnilist.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.NotificationCompat.Builder;

import java.util.Calendar;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.manager.AlarmNotFoundException;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.manager.PresentationToModelIntents;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.NoticePreferences;

public class AlarmAlertReceiver extends BroadcastReceiver {

    private final static int NOTIFICATION_OFFSET = 1000;

    private Context context;

    private AlarmsManager alarmsManager;

    private NotificationManager notificationManager;

    @Override
    public void onReceive(final Context ctx, final Intent intent) {
        context = ctx;
        alarmsManager = AlarmsManager.getsInstance();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        handleIntent(intent);
    }

    private void handleIntent(final Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            throw new IllegalArgumentException("Illegal action received");
        }

        /*Notice the extra code is long value.*/
        long code = intent.getLongExtra(Constants.EXTRA_CODE, -1);
        try {
            switch (action) {
                case Constants.ACTION_ALARM_ALERT: {
                    /*Alert and set next time.*/
                    Alarm alarm = alarmsManager.getAlarm((int) code);
                    onAlert(alarm);
                    alarmsManager.registerAlarm(alarm);
                    break;
                }
                case Constants.ACTION_ALARM_DISMISS: {
                    /*Dismiss current notification.*/
                    notificationManager.cancel((int) code);
                    notificationManager.cancel((int) (code + NOTIFICATION_OFFSET));
                    break;
                }
                case Constants.ACTION_CANCEL_SNOOZE: {
                    /*Cancel snooze notification.*/
                    notificationManager.cancel((int) code);
                    notificationManager.cancel((int) (code + NOTIFICATION_OFFSET));
                    break;
                }
                case Constants.ACTION_ALARM_SNOOZE: {
                    /*Cancel current notification and snooze the alarm.*/
                    notificationManager.cancel((int) code);
                    Alarm alarm = alarmsManager.getAlarm((int) code);
                    onSnoozed(alarm);
                    break;
                }
                case Constants.ACTION_CANCEL_NOTIFICATION: {
                    /*Cancel current notification.*/
                    int notificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1);
                    notificationManager.cancel(notificationId);
                    break;
                }
                case Constants.ACTION_MARK_ASSIGNMENT_AS_DONE: {
                    /*Done assignment and cancel current notification.*/
                    AssignmentsStore.getInstance().updateAssignment(code, true);
                    int notificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1);
                    notificationManager.cancel(notificationId);
                    ToastUtils.makeToast(R.string.one_assignment_marked_as_done);
                    break;
                }
                case Constants.ACTION_POSTPONE_ALARM: {
                    /*Postpone alarm for given minutes.*/
                    Alarm alarm = alarmsManager.getAlarm((int) code);
                    int snoozeMinutes = NoticePreferences.getInstance().getSnoozeDuration();
                    Calendar nextTime = Calendar.getInstance();
                    nextTime.add(Calendar.MINUTE, snoozeMinutes);
                    alarm.setNextTime(nextTime);
                    alarmsManager.setUpRTCAlarm(alarm);
                    int notificationId = intent.getIntExtra(Constants.EXTRA_NOTIFICATION_ID, -1);
                    notificationManager.cancel(notificationId);
                    ToastUtils.makeToast(String.format(PalmApp.getStringCompact(R.string.will_be_reminded_in_minutes), snoozeMinutes));
                    break;
                }
            }
        } catch (AlarmNotFoundException e) {
            notificationManager.cancel((int) code);
        }
    }

    private void onAlert(Alarm alarm) {
        int alarmCode = (int) alarm.getCode();
        Assignment assignment = AssignmentsStore.getInstance().get(alarm.getModelCode());
        /*If the assignment is null, we don't show notification for it*/
        if (assignment == null) {
            alarmsManager.removeAlarm(alarm);
            return;
        }

        Builder mBuilder = new Builder(context)
                .setContentTitle(assignment.getName())
                .setContentText(assignment.getComment())
                .setAutoCancel(true)
                .setSmallIcon(R.drawable.ic_assignment_turned_in_black_24dp)
                .setColor(ColorUtils.accentColor());

        /*Add action: mark as done.*/
        Intent mdIntent = new Intent(context, AlarmAlertReceiver.class);
        mdIntent.setAction(Constants.ACTION_MARK_ASSIGNMENT_AS_DONE);
        mdIntent.putExtra(Constants.EXTRA_CODE, assignment.getCode());
        mdIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, (int) alarm.getCode());
        PendingIntent piMD = PendingIntent.getBroadcast(
                context, (int) alarm.getCode(), mdIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        mBuilder.addAction(R.drawable.ic_check_circle_black_24dp, context.getString(R.string.mark_as_done), piMD);

        /*Add action: postpone.*/
        Intent pIntent = new Intent(context, AlarmAlertReceiver.class);
        pIntent.setAction(Constants.ACTION_POSTPONE_ALARM);
        pIntent.putExtra(Constants.EXTRA_CODE, alarm.getCode());
        pIntent.putExtra(Constants.EXTRA_NOTIFICATION_ID, (int) alarm.getCode());
        PendingIntent piP = PendingIntent.getBroadcast(
                context, (int) alarm.getCode(), pIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        int snoozeMinutes = NoticePreferences.getInstance().getSnoozeDuration();
        mBuilder.addAction(R.drawable.ic_snooze_black_24dp, String.format(context.getString(R.string.remind_in_minutes), snoozeMinutes), piP);

        /*Add action: click.*/
        Intent clickIntent = new Intent(context, ContentActivity.class);
        clickIntent.setAction(Constants.ACTION_NOTIFICATION);
        clickIntent.putExtra(Constants.EXTRA_CODE, assignment.getCode());
        clickIntent.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_ASSIGNMENT);
        clickIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(
                context, (int) assignment.getCode(), clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(contentIntent);

        /*Vibrate.*/
        if (NoticePreferences.getInstance().isVibrateAllowed()) {
            mBuilder.setVibrate(getVibrate());
        }

        /*Ringtone.*/
        String ringtone = NoticePreferences.getInstance().getNotificationRingtone(); // 铃声
        if (ringtone != null){
            mBuilder.setSound(Uri.parse(ringtone));
        }

        Notification notification = mBuilder.build();

        /*Light.*/
        setNotificationLight(notification);

        notificationManager.notify(alarmCode, notification);
    }

    private void onSnoozed(Alarm alarm) {
        LogUtils.d("onSnoozed: " + alarm);
        int alarmCode = (int) alarm.getCode();
        Assignment assignment = AssignmentsStore.getInstance().get(alarm.getModelCode());

        /*Add action cancel.*/
        Intent cancelIntent = new Intent(context, AlarmAlertReceiver.class);
        cancelIntent.setAction(Constants.ACTION_CANCEL_NOTIFICATION);
        cancelIntent.putExtra(Constants.EXTRA_CODE, alarmCode);
        PendingIntent piCancel = PendingIntent.getBroadcast(context, alarmCode, cancelIntent, 0);

        /*Action dismiss.*/
        PendingIntent piDismiss = PresentationToModelIntents.createPendingIntent(
                context, PresentationToModelIntents.ACTION_REQUEST_DISMISS, alarmCode);

        Notification status = new Builder(context)
                .setContentTitle(assignment.getName())
                .setContentText(assignment.getComment())
                .setSmallIcon(R.drawable.ic_assignment_turned_in_black_24dp)
                .setContentIntent(piCancel)
                .setOngoing(true)
                .addAction(R.drawable.ic_highlight_off_black_24dp, PalmApp.getStringCompact(R.string.text_dismiss), piDismiss)
                .setDefaults(Notification.DEFAULT_LIGHTS)
                .build();

        notificationManager.notify(alarmCode, status);
    }

    private long[] getVibrate() {
        return new long[]{500, 500};
    }

    private void setNotificationLight(Notification notification) {
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        switch (NoticePreferences.getInstance().getLightColor()) {
            case 0:
                notification.ledARGB = Color.GREEN;
                break;
            case 1:
                notification.ledARGB = Color.RED;
                break;
            case 2:
                notification.ledARGB = Color.YELLOW;
                break;
            case 3:
                notification.ledARGB = Color.BLUE;
                break;
            case 4:
                break;
        }
        notification.ledOnMS = 1000;
        notification.ledOffMS = 1000;
    }
}
