package me.shouheng.omnilist.utils.preferences;

import android.content.Context;

import java.util.Calendar;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.base.BasePreferences;

/**
 * Created by shouh on 2018/4/9.*/
public class UserPreferences extends BasePreferences {

    private static UserPreferences preferences;

    public static UserPreferences getInstance() {
        if (preferences == null) {
            synchronized (UserPreferences.class) {
                if (preferences == null) {
                    preferences = new UserPreferences(PalmApp.getContext());
                }
            }
        }
        return preferences;
    }

    private UserPreferences(Context context) {
        super(context);
    }

    public void setFirstDayOfWeek(int firstDay){
        putInt(getKey(R.string.key_first_day_of_week), firstDay);
    }

    public int getFirstDayOfWeek(){
        return getInt(getKey(R.string.key_first_day_of_week), Calendar.SUNDAY);
    }

    public void setVideoSizeLimit(int limit){
        putInt(getKey(R.string.key_video_size_limit), limit);
    }

    public int getVideoSizeLimit(){
        return getInt(getKey(R.string.key_video_size_limit), 10);
    }

    public boolean isImageAutoCompress() {
        return getBoolean(getKey(R.string.key_auto_compress_image), true);
    }

    public boolean listAnimationEnabled() {
        return getBoolean(getKey(R.string.key_list_animation), true);
    }

    public boolean systemAnimationEnabled() {
        return getBoolean(getKey(R.string.key_system_animation), true);
    }
}
