package me.shouheng.omnilist;

import android.app.Application;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.stetho.Stetho;

import org.polaric.colorful.Colorful;

import io.fabric.sdk.android.Fabric;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.manager.WakeLockManager;

/**
 * todo 1. replace images in intro page.
 * todo 2. translate text.
 *
 * Created by shouh on 2018/4/8.*/
public class PalmApp extends Application {

    private static PalmApp mInstance;

    private static boolean passwordChecked;

    public static synchronized PalmApp getContext() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        mInstance = this;

        MultiDex.install(this);

        Colorful.init(this);

        /*
         * Enable stetho only in debug mode. */
        if (BuildConfig.DEBUG) {
            Stetho.initializeWithDefaults(this);
        }

        AlarmsManager.init(getApplicationContext());

        WakeLockManager.init(getApplicationContext(), false);
    }

    public static boolean isPasswordChecked() {
        return passwordChecked;
    }

    public static void setPasswordChecked() {
        PalmApp.passwordChecked = true;
    }

    public static String getStringCompact(@StringRes int resId) {
        return PalmApp.getContext().getString(resId);
    }

    public static @ColorInt int getColorCompact(@ColorRes int colorRes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return PalmApp.getContext().getColor(colorRes);
        } else {
            return PalmApp.getContext().getResources().getColor(colorRes);
        }
    }

    public static Drawable getDrawableCompact(@DrawableRes int resId) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            return getContext().getDrawable(resId);
        } else {
            return getContext().getResources().getDrawable(resId);
        }
    }
}
