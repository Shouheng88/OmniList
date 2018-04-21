package me.shouheng.omnilist.utils;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.v4.graphics.drawable.DrawableCompat;

import org.polaric.colorful.Colorful;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.utils.preferences.ColorPreferences;


/**
 * Created by wangshouheng on 2017/3/31.*/
public class ColorUtils {

    private static Boolean isDarkTheme;
    private static Integer primaryColor;
    private static Integer accentColor;

    private static final int DEFAULT_COLOR_ALPHA = 50;

    public static boolean isDarkTheme() {
        if (isDarkTheme == null) {
            isDarkTheme = ColorPreferences.getInstance().isDarkTheme();
        }
        return isDarkTheme;
    }

    public static @ColorInt int primaryColor() {
        if (primaryColor == null) {
            Colorful.ThemeColor themeColor = ColorPreferences.getInstance().getThemeColor();
            primaryColor = PalmApp.getColorCompact(themeColor.getColorRes());
        }
        return primaryColor;
    }

    public static @ColorInt int accentColor(){
        if (accentColor == null) {
            Colorful.AccentColor accentColor = ColorPreferences.getInstance().getAccentColor();
            ColorUtils.accentColor = PalmApp.getColorCompact(accentColor.getColorRes());
        }
        return accentColor;
    }

    public static Colorful.ThemeColor getThemeColor() {
        return ColorPreferences.getInstance().getThemeColor();
    }

    public static Colorful.AccentColor getAccentColor() {
        return ColorPreferences.getInstance().getAccentColor();
    }

    public static void forceUpdateThemeStatus() {
        Colorful.AccentColor accentColor = ColorPreferences.getInstance().getAccentColor();
        ColorUtils.accentColor = PalmApp.getColorCompact(accentColor.getColorRes());
        isDarkTheme = ColorPreferences.getInstance().isDarkTheme();
    }

    public static String getColorName(@ColorInt int color) {
        return "#" + getHexString(Color.red(color)) + getHexString(Color.green(color)) + getHexString( Color.blue(color));
    }

    private static String getHexString(@ColorInt int i){
        String s = Integer.toHexString(i).toUpperCase();
        return s.length() == 1 ? "0" + s : s;
    }

    public static Drawable tintDrawable(Drawable drawable, int color) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable.mutate());
        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(color));
        return wrappedDrawable;
    }

    public static @ColorInt int calStatusBarColor(@ColorInt int color) {
        return calStatusBarColor(color, DEFAULT_COLOR_ALPHA);
    }

    public static @ColorInt int calStatusBarColor(@ColorInt int color, int alpha) {
        float a = 1 - alpha / 255f;
        int red = color >> 16 & 0xff;
        int green = color >> 8 & 0xff;
        int blue = color & 0xff;
        red = (int) (red * a + 0.5);
        green = (int) (green * a + 0.5);
        blue = (int) (blue * a + 0.5);
        return 0xff << 24 | red << 16 | green << 8 | blue;
    }

    public static @ColorInt int parseColor(String colorHex, @ColorInt int defaultValue) {
        try {
            return Color.parseColor(colorHex);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static @ColorInt int fadeColor(@ColorInt int color, @FloatRange(from = 0, to = 1) float rate) {
        return (color & 0x00ffffff) | ((0xff - (int)(0xff * rate)) << 24);
    }
}
