package me.shouheng.omnilist.fragment.setting;

import android.os.Bundle;
import android.preference.Preference;

import com.afollestad.materialdialogs.MaterialDialog;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.activity.base.ThemedActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.dialog.FeedbackDialog;
import me.shouheng.omnilist.dialog.NoticeDialog;
import me.shouheng.omnilist.intro.IntroActivity;
import me.shouheng.omnilist.model.tools.Feedback;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.IntentUtils;
import me.shouheng.omnilist.widget.ColorPreference;


/**
 * Created by wang shouheng on 2017/12/21.*/
public class SettingsFragment extends BaseFragment {

    private ColorPreference primaryColor, accentColor;

    private Preference.OnPreferenceClickListener listener = preference -> {
        if (getActivity() != null && getActivity() instanceof OnPreferenceClickListener) {
            ((OnPreferenceClickListener) getActivity()).onPreferenceClick(preference.getKey());
        }
        return true;
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        primaryColor = (ColorPreference) findPreference(R.string.key_primary_color);
        accentColor = (ColorPreference) findPreference(R.string.key_accent_color);

        primaryColor.setValue(ColorUtils.primaryColor());
        accentColor.setValue(ColorUtils.accentColor());

        addPreferenceClickListeners();
    }

    private void addPreferenceClickListeners() {
        findPreference(R.string.key_preferences).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_assignment).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_notification).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_data_backup).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_data_security).setOnPreferenceClickListener(listener);

        findPreference(R.string.key_is_dark_theme).setOnPreferenceClickListener(preference -> {
            updateThemeSettings();
            return true;
        });
        findPreference(R.string.key_primary_color).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_accent_color).setOnPreferenceClickListener(listener);
        findPreference(R.string.key_colored_navigation_bar).setOnPreferenceClickListener(preference -> {
            ((ThemedActivity) getActivity()).updateTheme();
            return true;
        });
        findPreference(R.string.key_dashboard).setOnPreferenceClickListener(listener);

        findPreference(R.string.key_user_guide).setOnPreferenceClickListener(preference -> {
            IntentUtils.openWiki(getActivity());
            return true;
        });
        findPreference(R.string.key_user_intro).setOnPreferenceClickListener(preference -> {
            showIntroduction();
            return true;
        });

        findPreference(R.string.key_support_development).setOnPreferenceClickListener(preference -> {
            NoticeDialog.newInstance().show(((CommonActivity) getActivity()).getSupportFragmentManager(), "Notice");
            return true;
        });
        findPreference(R.string.key_feedback).setOnPreferenceClickListener(preference -> {
            showFeedbackEditor();
            return true;
        });
        findPreference(R.string.key_about).setOnPreferenceClickListener(listener);
    }

    private void showFeedbackEditor() {
        FeedbackDialog.newInstance((dialog, feedback) -> sendFeedback(feedback))
                .show(((CommonActivity) getActivity()).getSupportFragmentManager(), "Feedback Editor");
    }

    private void sendFeedback(Feedback feedback) {
        String subject = String.format(Constants.DEVELOPER_EMAIL_PREFIX, feedback.getFeedbackType().name());
        String body = feedback.getQuestion() + Constants.DEVELOPER_EMAIL_EMAIL_PREFIX + feedback.getEmail();
        IntentUtils.sendEmail(getActivity(), subject, body);
    }

    private void showIntroduction() {
        new MaterialDialog.Builder(getActivity())
                .title(R.string.text_tips)
                .content(R.string.show_introduction_again)
                .positiveText(R.string.text_ok)
                .negativeText(R.string.text_cancel)
                .onPositive((materialDialog, dialogAction) -> IntroActivity.launch(getActivity()))
                .show();
    }

    private void updateThemeSettings() {
        ColorUtils.forceUpdateThemeStatus();
        ((ThemedActivity) getActivity()).reUpdateTheme();
    }

    public void notifyAccentColorChanged(int accentColor) {
        this.accentColor.setValue(accentColor);
    }

    public void notifyPrimaryColorChanged(int primaryColor) {
        this.primaryColor.setValue(primaryColor);
    }

    public interface OnPreferenceClickListener {
        void onPreferenceClick(String key);
    }
}
