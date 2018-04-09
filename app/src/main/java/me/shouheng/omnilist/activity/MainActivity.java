package me.shouheng.omnilist.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.databinding.ActivityMainBinding;
import me.shouheng.omnilist.utils.ColorUtils;

public class MainActivity extends CommonActivity<ActivityMainBinding> {

    private Drawer drawer;

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        configToolbar();

        configDrawer();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setStatusBarColor(ColorUtils.primaryDarkColor());
    }

    private void configToolbar() {
        setSupportActionBar(getBinding().barLayout.toolbar);
    }

    private void configDrawer() {
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(getBinding().barLayout.toolbar)
                .build();
    }
}
