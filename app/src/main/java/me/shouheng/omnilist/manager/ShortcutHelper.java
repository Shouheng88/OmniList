package me.shouheng.omnilist.manager;

import android.content.Context;
import android.content.Intent;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.MainActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Model;


public class ShortcutHelper {

    public static <T extends Model> void addShortcut(Context context, T model) {
        Context mContext = context.getApplicationContext();
        Intent shortcutIntent = new Intent(mContext, MainActivity.class);
        shortcutIntent.putExtra(Constants.EXTRA_CODE, model.getCode());
        shortcutIntent.putExtra(Constants.EXTRA_FRAGMENT, getFragmentToDispatch(model));
        shortcutIntent.setAction(Constants.ACTION_SHORTCUT);

        Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getShortcutName(model));
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                // todo replace
                Intent.ShortcutIconResource.fromContext(mContext, R.drawable.ic_add_a_photo_white));
        addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");

        context.sendBroadcast(addIntent);
    }

    private static <T extends Model> String getShortcutName(T model) {
         if (model instanceof Assignment) {
            return ((Assignment) model).getName();
        }
        return PalmApp.getStringCompact(R.string.app_name);
    }

    private static <T extends Model> String getFragmentToDispatch(T model) {
        if (model instanceof Assignment) {
            return Constants.VALUE_FRAGMENT_ASSIGNMENT;
        }
        return PalmApp.getStringCompact(R.string.app_name);
    }
}
