package me.shouheng.omnilist.model.tools;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import me.shouheng.omnilist.R;


/**
 * Created by wangshouheng on 2017/8/8. */
public enum FabSortItem {
    ASSIGNMENT(R.string.fab_opt_assignment, R.drawable.ic_assignment_turned_in_black_24dp),
    CATEGORY(R.string.fab_opt_category, R.drawable.ic_folder_special_black_24dp),
    FILE(R.string.fab_opt_file, R.drawable.ic_attach_file_white),
    CAPTURE(R.string.fab_opt_capture, R.drawable.ic_add_a_photo_white),
    DRAFT(R.string.fab_opt_draft, R.drawable.ic_gesture_grey_24dp),
    RECORD(R.string.fab_opt_record, R.drawable.ic_mic_white_24dp),
//    NOTICE(R.string.fab_opt_notice, R.drawable.ic_access_alarm_white),
    QUICK(R.string.fab_quick_assignment, R.drawable.ic_lightbulb_outline_black_24dp);

    @StringRes
    public final int nameRes;

    @DrawableRes
    public final int iconRes;

    FabSortItem(int nameRes, @DrawableRes int iconRes) {
        this.nameRes = nameRes;
        this.iconRes = iconRes;
    }
}
