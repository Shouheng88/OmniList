package me.shouheng.omnilist.fragment.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.polaric.colorful.BaseActivity;
import org.polaric.colorful.PermissionUtils;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.config.TextLength;
import me.shouheng.omnilist.databinding.DialogDrawerBgOptionsBinding;
import me.shouheng.omnilist.dialog.SimpleEditDialog;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.listener.OnFragmentDestroyListener;
import me.shouheng.omnilist.listener.OnSettingsChangedListener;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.UserPreferences;


/**
 * Created by shouh on 2018/3/18. */
public class SettingsDashboard extends BaseFragment implements OnAttachingFileListener {

    private UserPreferences userPreferences;

    public static SettingsDashboard newInstance() {
        Bundle args = new Bundle();
        SettingsDashboard fragment = new SettingsDashboard();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userPreferences = UserPreferences.getInstance();

        configToolbar();

        addPreferencesFromResource(R.xml.preferences_dashboard);

        setPreferenceClickListeners();
    }

    private void configToolbar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.setting_personalize_dashboard);
        }
    }

    private void setPreferenceClickListeners() {
        findPreference(R.string.key_user_info_bg_visible).setOnPreferenceClickListener(preference -> {
            notifyDashboardChanged();
            return true;
        });
        findPreference(R.string.key_user_info_bg).setOnPreferenceClickListener(preference -> {
            showBgOptions();
            return true;
        });
        findPreference(R.string.key_user_info_motto).setOnPreferenceClickListener(preference -> {
            showMottoEditor();
            return true;
        });
    }

    private void showMottoEditor() {
        new SimpleEditDialog.Builder()
                .setContent(userPreferences.getUserMotto())
                .setSimpleAcceptListener(content -> {
                    notifyDashboardChanged();
                    userPreferences.setUserMotto(content);
                }).setMaxLength(TextLength.MOTTO_TEXT_LENGTH.length)
                .build().show(((AppCompatActivity) getActivity()).getSupportFragmentManager(), "MOTTO_EDITOR");
    }

    private void showBgOptions() {
        DialogDrawerBgOptionsBinding bgOptionsBinding = DataBindingUtil.inflate(getActivity().getLayoutInflater(),
                R.layout.dialog_drawer_bg_options, null, false);

        Dialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.setting_dashboard_user_info_bg)
                .setView(bgOptionsBinding.getRoot())
                .create();
        dialog.show();

        bgOptionsBinding.civBg1.setFillingCircleColor(
                Constants.DEFAULT_USER_INFO_BG.equals(userPreferences.getUserInfoBG().toString()) ?
                        ColorUtils.accentColor() : Color.parseColor("#cccccc"));

        Glide.with(PalmApp.getContext())
                .load(Constants.DEFAULT_USER_INFO_BG)
                .centerCrop()
                .crossFade()
                .into(bgOptionsBinding.civDefault);

        bgOptionsBinding.rlBg1.setOnClickListener(view -> {
            userPreferences.setUserInfoBG(null);
            notifyDashboardChanged();
            dialog.dismiss();
        });
        bgOptionsBinding.tvPick.setOnClickListener(view -> {
            PermissionUtils.checkStoragePermission((BaseActivity) getActivity(),
                    () -> AttachmentHelper.pickFromAlbum(SettingsDashboard.this));
            dialog.dismiss();
        });
        bgOptionsBinding.tvPick.setTextColor(ColorUtils.accentColor());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AttachmentHelper.resolveResult(SettingsDashboard.this, requestCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onGetBackgroundImage(@NonNull Attachment attachment) {
        userPreferences.setUserInfoBG(attachment.getUri());
        notifyDashboardChanged();
    }

    private void notifyDashboardChanged() {
        if (getActivity() != null && getActivity() instanceof OnSettingsChangedListener) {
            ((OnSettingsChangedListener) getActivity()).onDashboardSettingChanged(OnSettingsChangedListener.ChangedType.DRAWER_CONTENT);
        }
    }

    @Override
    public void onAttachingFileErrorOccurred(Attachment attachment) {
        ToastUtils.makeToast(R.string.failed_to_save_attachment);
    }

    @Override
    public void onAttachingFileFinished(Attachment attachment) {
        onGetBackgroundImage(attachment);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof OnFragmentDestroyListener) {
            ((OnFragmentDestroyListener) getActivity()).onFragmentDestroy();
        }
    }
}
