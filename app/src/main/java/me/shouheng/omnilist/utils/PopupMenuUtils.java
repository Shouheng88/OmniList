package me.shouheng.omnilist.utils;

import android.annotation.SuppressLint;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.PopupMenu;

import java.lang.reflect.Field;

public class PopupMenuUtils {

    @SuppressLint("RestrictedApi")
    public static void forceShowIcon(PopupMenu popupM){
        try {
            Field field = popupM.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper menuPopupHelper = (MenuPopupHelper) field.get(popupM);
            menuPopupHelper.setForceShowIcon(true);
        } catch (NoSuchFieldException e) {
            LogUtils.d("showDatePicker: NoSuchFieldException");
        } catch (IllegalAccessException e) {
            LogUtils.d("showDatePicker: IllegalAccessException");
        }
    }
}
