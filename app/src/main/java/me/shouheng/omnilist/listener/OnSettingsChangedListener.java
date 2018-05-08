package me.shouheng.omnilist.listener;

/**
 * Created by shouh on 2018/3/21.*/
public interface OnSettingsChangedListener {

    /**
     * Notify that given setting type state is changed.
     *
     * @param settingChangeType the type of changed setting */
    void onSettingChanged(SettingChangeType settingChangeType);
}
