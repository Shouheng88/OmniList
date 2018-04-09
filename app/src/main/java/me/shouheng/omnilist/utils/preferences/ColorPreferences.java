package me.shouheng.omnilist.utils.preferences;

import android.content.Context;

import org.polaric.colorful.Colorful;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.utils.base.BasePreferences;

/**
 * Created by shouh on 2018/4/8.*/
public class ColorPreferences extends BasePreferences {

    private static ColorPreferences sInstance;

    public static ColorPreferences getInstance() {
        if (sInstance == null) {
            synchronized (ColorPreferences.class) {
                if (sInstance == null){
                    sInstance = new ColorPreferences(PalmApp.getContext());
                }
            }
        }
        return sInstance;
    }

    private ColorPreferences(Context context) {
        super(context);
    }

    public void setDarkTheme(boolean isDarkTheme) {
        putBoolean(getKey(R.string.key_is_dark_theme), isDarkTheme);
    }

    public boolean isDarkTheme() {
        return getBoolean(getKey(R.string.key_is_dark_theme), false);
    }

    public void setThemeColor(Colorful.ThemeColor themeColor){
        putString(getKey(R.string.key_primary_color), themeColor.getIdentifyName());
    }

    public Colorful.ThemeColor getThemeColor(){
        return Colorful.ThemeColor.getByPrimaryName(getString(getKey(R.string.key_primary_color), Colorful.Defaults.primaryColor.getIdentifyName()));
    }

    public Colorful.AccentColor getAccentColor() {
        return Colorful.AccentColor.getByAccentName(getString(getKey(R.string.key_accent_color), Colorful.Defaults.accentColor.getColorName()));
    }

    public void setAccentColor(Colorful.AccentColor accentColor) {
        putString(getKey(R.string.key_accent_color), accentColor.getAccentName());
    }

    public void setColoredNavigationBar(boolean coloredNavigationBar) {
        putBoolean(getKey(R.string.key_is_colored_navigation_bar), coloredNavigationBar);
    }

    public boolean isColoredNavigationBar() {
        return getBoolean(getKey(R.string.key_is_colored_navigation_bar), false);
    }
}
