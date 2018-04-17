package me.shouheng.omnilist.utils.preferences;

import android.content.Context;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.base.BasePreferences;

public class SearchPreferences extends BasePreferences {

    private static SearchPreferences preferences;

    public static SearchPreferences getInstance() {
        if (preferences == null) {
            synchronized (LockPreferences.class) {
                if (preferences == null) {
                    preferences = new SearchPreferences(PalmApp.getContext());
                }
            }
        }
        return preferences;
    }

    private SearchPreferences(Context context) {
        super(context);
    }

    public boolean isTagsIncluded() {
        return getBoolean(getKey(R.string.key_search_include_tags), true);
    }

    public void setTagsIncluded(boolean isInclude) {
        putBoolean(getKey(R.string.key_search_include_tags), isInclude);
    }

    public boolean isArchivedIncluded() {
        return getBoolean(getKey(R.string.key_search_include_archived), true);
    }

    public void setArchivedIncluded(boolean isInclude) {
        putBoolean(getKey(R.string.key_search_include_archived), isInclude);
    }

    public boolean isTrashedIncluded() {
        return getBoolean(getKey(R.string.key_search_include_trashed), true);
    }

    public void setTrashedIncluded(boolean isInclude) {
        putBoolean(getKey(R.string.key_search_include_trashed), isInclude);
    }
}
