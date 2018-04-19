package me.shouheng.omnilist.utils.preferences;

import android.content.Context;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.base.BasePreferences;

public class NoticePreferences extends BasePreferences {

    private static NoticePreferences preferences;

    public static NoticePreferences getInstance() {
        if (preferences == null) {
            synchronized (NoticePreferences.class) {
                if (preferences == null) {
                    preferences = new NoticePreferences(PalmApp.getContext());
                }
            }
        }
        return preferences;
    }

    private NoticePreferences(Context context) {
        super(context);
    }

    public void setAllowWakeLock(boolean allowWakeLock) {
        putBoolean(getKey(R.string.key_allow_wake_lock), allowWakeLock);
    }

    public boolean getAllowWakeLock() {
        return getBoolean(getKey(R.string.key_allow_wake_lock), false);
    }

    public void setLightColor(int lightColor) {
        putInt(getKey(R.string.key_notification_light_color), lightColor);
    }

    public int getLightColor() {
        return getInt(getKey(R.string.key_notification_light_color), 0);
    }

    public void setAllowVibrate(boolean allowVibrate) {
        putBoolean(getKey(R.string.key_allow_vibrate), allowVibrate);
    }

    public boolean isVibrateAllowed() {
        return getBoolean(getKey(R.string.key_allow_vibrate), true);
    }

    public void setSnoozeDuration(int duration) {
        putInt(getKey(R.string.key_notification_duration), duration);
    }

    public int getSnoozeDuration() {
        return getInt(getKey(R.string.key_notification_duration), 5);
    }

    public void setNotificationRingtone(String notificationRingtone) {
        putString(getKey(R.string.key_notification_ringtone), notificationRingtone);
    }

    public String getNotificationRingtone() {
        return getString(getKey(R.string.key_notification_ringtone), null);
    }
}
