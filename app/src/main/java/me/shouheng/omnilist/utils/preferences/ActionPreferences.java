package me.shouheng.omnilist.utils.preferences;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.base.BasePreferences;

/**
 * Created by shouh on 2018/4/9.*/
public class ActionPreferences extends BasePreferences {

    private static ActionPreferences preferences;

    public static ActionPreferences getInstance() {
        if (preferences == null) {
            synchronized (ActionPreferences.class) {
                if (preferences == null) {
                    preferences = new ActionPreferences(PalmApp.getContext());
                }
            }
        }
        return preferences;
    }

    private ActionPreferences(Context context) {
        super(context);
    }

    public void setTourActivityShowed() {
        putBoolean(R.string.key_is_tour_activity_showed, true);
    }

    public boolean isTourActivityShowed() {
        return getBoolean(R.string.key_is_tour_activity_showed, false);
    }

    public void setAttachmentUri(@NonNull Uri uri) {
        putString(R.string.key_attachment_uri, uri.toString());
    }

    public String getAttachmentUri() {
        return getString(R.string.key_attachment_uri, "");
    }

    public void setAttachmentFilePath(String filePath) {
        putString(R.string.key_attachment_file_path, filePath);
    }

    public String getAttachmentFilePath() {
        return getString(R.string.key_attachment_file_path, "");
    }
}
