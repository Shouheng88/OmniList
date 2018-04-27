package me.shouheng.omnilist.utils.preferences;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.tools.FabSortItem;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.base.BasePreferences;

/**
 * Created by shouh on 2018/4/9.*/
public class UserPreferences extends BasePreferences {

    public static List<FabSortItem> defaultFabOrders;

    private final String FAB_SORT_SPLIT = ":";

    static {
        defaultFabOrders = new LinkedList<>();
        defaultFabOrders.add(FabSortItem.ASSIGNMENT);
        defaultFabOrders.add(FabSortItem.CATEGORY);
        defaultFabOrders.add(FabSortItem.QUICK);
        defaultFabOrders.add(FabSortItem.CAPTURE);
        defaultFabOrders.add(FabSortItem.FILE);
    }

    private static UserPreferences preferences;

    public static UserPreferences getInstance() {
        if (preferences == null) {
            synchronized (UserPreferences.class) {
                if (preferences == null) {
                    preferences = new UserPreferences(PalmApp.getContext());
                }
            }
        }
        return preferences;
    }

    private UserPreferences(Context context) {
        super(context);
    }

    public void setFirstDayOfWeek(int firstDay){
        putInt(R.string.key_first_day_of_week, firstDay);
    }

    public int getFirstDayOfWeek(){
        return getInt(R.string.key_first_day_of_week, Calendar.SUNDAY);
    }

    public void setVideoSizeLimit(int limit){
        putInt(R.string.key_video_size_limit, limit);
    }

    public int getVideoSizeLimit(){
        return getInt(R.string.key_video_size_limit, 10);
    }

    public boolean isImageAutoCompress() {
        return getBoolean(R.string.key_auto_compress_image, true);
    }

    public boolean listAnimationEnabled() {
        return getBoolean(R.string.key_list_animation, true);
    }

    public boolean systemAnimationEnabled() {
        return getBoolean(R.string.key_system_animation, true);
    }

    public List<FabSortItem> getFabSortResult() {
        String fabStr = getString(R.string.key_fab_sort_result, null);
        if (!TextUtils.isEmpty(fabStr)) {
            String[] fabs = fabStr.split(FAB_SORT_SPLIT);
            List<FabSortItem> fabSortItems = new LinkedList<>();
            for (String fab : fabs) {
                fabSortItems.add(FabSortItem.valueOf(fab));
            }
            return fabSortItems;
        } else {
            return defaultFabOrders;
        }
    }

    public void setFabSortResult(List<FabSortItem> fabSortItems) {
        int size = fabSortItems.size();
        StringBuilder fabStr = new StringBuilder();
        for (int i=0;i<size;i++) {
            if (size == size - 1) {
                fabStr.append(fabSortItems.get(i).name());
            } else {
                fabStr.append(fabSortItems.get(i).name()).append(FAB_SORT_SPLIT);
            }
        }
        putString(R.string.key_fab_sort_result, fabStr.toString());
    }

    public boolean is24HourMode() {
        return getBoolean(R.string.key_is_24_hour_mode, true);
    }

    public int getTimeLineColor(Operation operation) {
        return getInt(getKey(R.string.key_operation_color_prefix) + operation.name(), defaultTimeLineColor(operation));
    }

    private int defaultTimeLineColor(Operation operation) {
        switch (operation) {
            case DELETE: return PalmApp.getContext().getResources().getColor(R.color.md_red_500);
            case TRASH: return PalmApp.getContext().getResources().getColor(R.color.md_deep_orange_500);
            case ARCHIVE: return PalmApp.getContext().getResources().getColor(R.color.md_pink_500);
            case COMPLETE: return PalmApp.getContext().getResources().getColor(R.color.md_purple_500);
            case SYNCED: return PalmApp.getContext().getResources().getColor(R.color.md_light_green_900);
            case ADD: return PalmApp.getContext().getResources().getColor(R.color.md_green_500);
            case UPDATE: return PalmApp.getContext().getResources().getColor(R.color.md_light_green_700);
            case INCOMPLETE: return PalmApp.getContext().getResources().getColor(R.color.md_blue_500);
            case RECOVER: return PalmApp.getContext().getResources().getColor(R.color.md_light_blue_600);
        }
        return ColorUtils.accentColor();
    }

    public void setUserInfoBG(@Nullable Uri uri) {
        putString(R.string.key_user_info_bg, uri == null ? "" : uri.toString());
    }

    public Uri getUserInfoBG() {
        String bgUri = getString(R.string.key_user_info_bg, null);
        if (!TextUtils.isEmpty(bgUri)) {
            return Uri.parse(bgUri);
        }
        return Uri.parse(Constants.DEFAULT_USER_INFO_BG);
    }

    public void setUserInfoBGVisible(boolean isVisible) {
        putBoolean(R.string.key_user_info_bg_visible, isVisible);
    }

    public boolean isUserInfoBgVisible() {
        return getBoolean(R.string.key_user_info_bg_visible, true);
    }

    public void setUserMotto(String motto) {
        putString(R.string.key_user_info_motto, motto);
    }

    public String getUserMotto() {
        return getString(R.string.key_user_info_motto, PalmApp.getStringCompact(R.string.setting_dashboard_user_motto_default));
    }
}
