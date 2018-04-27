package me.shouheng.omnilist.fragment.setting;

import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.StringRes;

import me.shouheng.omnilist.PalmApp;


/**
 * Created by shouh on 2018/4/4.*/
public abstract class BaseFragment extends PreferenceFragment {

    @Override
    public Preference findPreference(CharSequence key) {
        return super.findPreference(key);
    }

    public Preference findPreference(@StringRes int keyRes) {
        return super.findPreference(PalmApp.getStringCompact(keyRes));
    }
}
