package me.shouheng.colorful;

import android.content.Context;
import android.support.annotation.StyleRes;
import android.util.Log;

public class ThemeDelegate {

    private Colorful.AccentColor accentColor;

    private boolean translucent;

    private boolean dark;

    @StyleRes
    private int styleResAccent;

    @StyleRes
    private int styleResBase;

    ThemeDelegate(Context context, Colorful.AccentColor accent, boolean translucent, boolean dark) {
        this.accentColor = accent;
        this.translucent = translucent;
        this.dark = dark;

        long curTime = System.currentTimeMillis();

        styleResAccent = context.getResources().getIdentifier(accentColor.getAccentName(), "style", context.getPackageName());
        styleResBase = dark ? R.style.Colorful_Dark : R.style.Colorful_Light;

        Log.d(Util.LOG_TAG, "ThemeDelegate fetched theme in " + (System.currentTimeMillis() - curTime) + " ms");
    }

    @StyleRes int getStyleResAccent() {
        return styleResAccent;
    }

    @StyleRes int getStyleResBase() {
        return styleResBase;
    }

    public Colorful.AccentColor getAccentColor() {
        return accentColor;
    }

    boolean isTranslucent() {
        return translucent;
    }

    public boolean isDark() {
        return dark;
    }
}
