package me.shouheng.omnilist.fragment.setting;

import android.app.Activity;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;

import com.afollestad.materialdialogs.MaterialDialog;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.listener.OnFragmentDestroyListener;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.NoticePreferences;

public class SettingsNotification extends BaseFragment {

    private final int RINGTONE_REQUEST_CODE = 0x000F;

    private NoticePreferences noticePreferences;

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

        noticePreferences = NoticePreferences.getInstance();

        addPreferencesFromResource(R.xml.preferences_notification);

        findPreference(R.string.key_notification_ringtone).setOnPreferenceClickListener(preference -> {
            showRingtonePicker();
            return true;
        });
        findPreference(R.string.key_notification_duration).setOnPreferenceClickListener(preference -> {
            showPostponeEditor();
            return true;
        });
    }

    private void configToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_notification);
        }
    }

    private void showRingtonePicker() {
        Intent intent = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, onRestoreRingtone());
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_DEFAULT_URI, onRestoreRingtone());
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, true);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_ALL);
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE, getString(R.string.pick_notification_ringtone));
        this.startActivityForResult(intent, RINGTONE_REQUEST_CODE);
    }

    private void showPostponeEditor() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.snooze_minutes)
                .content(R.string.input_the_snooze_minutes_in_minute)
                .inputType(InputType.TYPE_CLASS_NUMBER)
                .inputRange(0, 2)
                .negativeText(R.string.text_cancel)
                .input(getString(R.string.input_the_snooze_minutes_in_minute), String.valueOf(noticePreferences.getSnoozeDuration()),
                        (materialDialog, charSequence) -> {
                            try {
                                int minutes = Integer.parseInt(charSequence.toString());
                                if (minutes < 0) {
                                    ToastUtils.makeToast(R.string.illegal_number);
                                    return;
                                }
                                if (minutes > 30) {
                                    ToastUtils.makeToast(R.string.snooze_time_too_long);
                                    return;
                                }
                                noticePreferences.setSnoozeDuration(minutes);
                            } catch (Exception e) {
                                ToastUtils.makeToast(R.string.wrong_numeric_string);
                            }
                        }).show();
    }

    protected Uri onRestoreRingtone() {
        final String uriString = noticePreferences.getNotificationRingtone();
        return !TextUtils.isEmpty(uriString) ? Uri.parse(uriString) : null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case RINGTONE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK){
                    Uri uri = data.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    noticePreferences.setNotificationRingtone(uri == null ? null : uri.toString());
                }
                break;
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
