package me.shouheng.omnilist.fragment.setting;

import android.os.Bundle;
import android.preference.Preference;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.afollestad.materialdialogs.MaterialDialog;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.listener.OnFragmentDestroyListener;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.utils.preferences.AssignmentPreferences;


/**
 * Created by shouh on 2018/3/21.*/
public class SettingsAssignment extends BaseFragment {

    private AssignmentPreferences assignmentPreferences;

    private Preference prefLeft, prefRight;

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

        assignmentPreferences = AssignmentPreferences.getInstance();

        addPreferencesFromResource(R.xml.preferences_assignment);

        prefLeft = findPreference(R.string.key_assignment_slide_left);
        prefLeft.setOnPreferenceClickListener(slideListener);
        prefRight = findPreference(R.string.key_assignment_slide_right);
        prefRight.setOnPreferenceClickListener(slideListener);

        updateSlidePreferences();
    }

    private void configToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_assignment);
        }
    }

    private void updateSlidePreferences() {
        prefLeft.setSummary(assignmentPreferences.getSlideLeftOperation().operationName);
        prefRight.setSummary(assignmentPreferences.getSlideRightOperation().operationName);
    }

    private Preference.OnPreferenceClickListener slideListener = preference -> {
        if (getString(R.string.key_assignment_slide_left).equals(preference.getKey())) {
            showSlideOptionsChooser(true);
        } else if (getString(R.string.key_assignment_slide_right).equals(preference.getKey())) {
            showSlideOptionsChooser(false);
        }
        return true;
    };

    private void showSlideOptionsChooser(boolean isSlideLeft) {
        String[] items = new String[]{
                PalmApp.getStringCompact(Operation.ARCHIVE.operationName),
                PalmApp.getStringCompact(Operation.TRASH.operationName)
        };

        new MaterialDialog.Builder(getActivity())
                .items(items)
                .itemsCallback((dialog, itemView, position, text) -> {
                    Operation operation = position == 0 ? Operation.ARCHIVE : Operation.TRASH;
                    if (isSlideLeft) {
                        assignmentPreferences.setSlideLeftOperation(operation);
                    } else {
                        assignmentPreferences.setSlideRightOperation(operation);
                    }
                    updateSlidePreferences();
                }).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof OnFragmentDestroyListener) {
            ((OnFragmentDestroyListener) getActivity()).onFragmentDestroy();
        }
    }
}
