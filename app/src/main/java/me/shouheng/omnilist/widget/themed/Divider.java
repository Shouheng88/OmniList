package me.shouheng.omnilist.widget.themed;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.ColorUtils;


/**
 * Created by wangshouheng on 2017/12/5.*/
public class Divider extends View {

    public Divider(Context context) {
        super(context);
        initTheme();
    }

    public Divider(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initTheme();
    }

    public Divider(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initTheme();
    }

    private void initTheme() {
        setBackgroundResource(ColorUtils.isDarkTheme() ? R.color.white_divider_color : R.color.black_divider_color);
    }

    public void setTheme(boolean isDarkTheme) {
        setBackgroundResource(isDarkTheme ? R.color.white_divider_color : R.color.black_divider_color);
    }
}
