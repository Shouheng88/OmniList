package me.shouheng.omnilist.widget.themed;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;

import me.shouheng.omnilist.utils.ColorUtils;

/**
 * Created by shouh on 2018/4/9.*/
public class SupportToolbar extends Toolbar {

    public SupportToolbar(Context context) {
        super(context);
        init();
    }

    public SupportToolbar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SupportToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setBackgroundColor(ColorUtils.primaryColor());
    }
}
