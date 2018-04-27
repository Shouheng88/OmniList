package me.shouheng.omnilist.fragment.setting;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.listener.OnFragmentDestroyListener;


/**
 * Created by shouh on 2018/3/21.*/
public class SettingsAssignment extends BaseFragment {

    public static SettingsAssignment newInstance() {
        Bundle args = new Bundle();
        SettingsAssignment fragment = new SettingsAssignment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configToolbar();

        addPreferencesFromResource(R.xml.preferences_assignment);
    }

    private void configToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_assignment);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof OnFragmentDestroyListener) {
            ((OnFragmentDestroyListener) getActivity()).onFragmentDestroy();
        }
    }
}
