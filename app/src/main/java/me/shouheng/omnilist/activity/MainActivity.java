package me.shouheng.omnilist.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.databinding.ActivityMainBinding;
import me.shouheng.omnilist.databinding.ActivityMainNavHeaderBinding;

public class MainActivity extends CommonActivity<ActivityMainBinding> {

    private ActivityMainNavHeaderBinding headerBinding;

    @Override
    protected void beforeSetContentView() {
        setTranslucentStatusBar();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        configToolbar();

        initHeaderView();
    }

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        }
        if (!isDarkTheme()) toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
    }

    private void initHeaderView() {
        if (headerBinding == null) {
            View header = getBinding().nav.inflateHeaderView(R.layout.activity_main_nav_header);
            headerBinding = DataBindingUtil.bind(header);
        }
        setupHeader();
        headerBinding.getRoot().setOnLongClickListener(v -> true);
//        headerBinding.getRoot().setOnClickListener(
//                view -> startActivityForResult(UserInfoActivity.class, REQUEST_USER_INFO));
    }

    private void setupHeader() {
//        headerBinding.userMotto.setText(preferencesUtils.getUserMotto());
//
//        boolean enabled = preferencesUtils.isUserInfoBgEnable();
//        headerBinding.userBg.setVisibility(enabled ? View.VISIBLE : View.GONE);
//        if (enabled) {
//            Uri customUri = preferencesUtils.getUserInfoBG();
//            if (customUri != null) {
//                Glide.with(PalmApp.getContext())
//                        .load(customUri)
//                        .centerCrop()
//                        .crossFade()
//                        .into(headerBinding.userBg);
//            } else {
//                Glide.with(PalmApp.getContext())
//                        .load(R.drawable.theme_bg_1)
//                        .centerCrop()
//                        .crossFade()
//                        .into(headerBinding.userBg);
//            }
//        }
    }
}
