package me.shouheng.omnilist.fragment.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import me.shouheng.omnilist.R;

public class SettingsNotification extends BaseFragment {

    public static SettingsNotification newInstance() {
        Bundle args = new Bundle();
        SettingsNotification fragment = new SettingsNotification();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configToolbar();

        addPreferencesFromResource(R.xml.preferences_notification);
    }

    private void configToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_notification);
        }
    }
}
