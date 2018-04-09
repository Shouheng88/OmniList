package me.shouheng.omnilist.utils.preferences;

import android.content.Context;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.base.BasePreferences;

/**
 * Created by shouh on 2018/4/9.*/
public class LockPreferences extends BasePreferences {

    private static LockPreferences preferences;

    public static LockPreferences getInstance() {
        if (preferences == null) {
            synchronized (LockPreferences.class) {
                if (preferences == null) {
                    preferences = new LockPreferences(PalmApp.getContext());
                }
            }
        }
        return preferences;
    }

    private LockPreferences(Context context) {
        super(context);
    }

    public void setPasswordRequired(boolean isRequired) {
        putBoolean(getKey(R.string.key_is_password_required), isRequired);
    }

    public boolean isPasswordRequired() {
        return getBoolean(getKey(R.string.key_is_password_required), false);
    }

    public void setPassword(String password) {
        putString(getKey(R.string.key_password), password);
    }

    public String getPassword() {
        return getString(getKey(R.string.key_password), null);
    }

    public int getPasswordFreezeTime() {
        return getInt(getKey(R.string.key_password_freeze_time), 5);
    }

    public void setPasswordFreezeTime(int time) {
        putInt(getKey(R.string.key_password_freeze_time), time);
    }

    public void setPasswordQuestion(String question) {
        putString(getKey(R.string.key_password_question), question);
    }

    public String getPasswordQuestion() {
        return getString(getKey(R.string.key_password_question), null);
    }

    public void setPasswordAnswer(String answer) {
        putString(getKey(R.string.key_password_answer), answer);
    }

    public String getPasswordAnswer() {
        return getString(getKey(R.string.key_password_answer), null);
    }

    public void setLastInputErrorTime(long millis) {
        putLong(getKey(R.string.key_last_input_error_time), millis);
    }

    public long getLastInputErrorTime() {
        return getLong(getKey(R.string.key_last_input_error_time), 0);
    }
}
