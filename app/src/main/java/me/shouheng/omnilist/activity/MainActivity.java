package me.shouheng.omnilist.activity;

import android.os.Bundle;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.databinding.ActivityMainBinding;

public class MainActivity extends CommonActivity<ActivityMainBinding> {

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {

    }
}
