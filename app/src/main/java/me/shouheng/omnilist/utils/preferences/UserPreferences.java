package me.shouheng.omnilist.utils.preferences;

import android.content.Context;
import android.text.TextUtils;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.tools.FabSortItem;
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
        putInt(getKey(R.string.key_first_day_of_week), firstDay);
    }

    public int getFirstDayOfWeek(){
        return getInt(getKey(R.string.key_first_day_of_week), Calendar.SUNDAY);
    }

    public void setVideoSizeLimit(int limit){
        putInt(getKey(R.string.key_video_size_limit), limit);
    }

    public int getVideoSizeLimit(){
        return getInt(getKey(R.string.key_video_size_limit), 10);
    }

    public boolean isImageAutoCompress() {
        return getBoolean(getKey(R.string.key_auto_compress_image), true);
    }

    public boolean listAnimationEnabled() {
        return getBoolean(getKey(R.string.key_list_animation), true);
    }

    public boolean systemAnimationEnabled() {
        return getBoolean(getKey(R.string.key_system_animation), true);
    }

    public List<FabSortItem> getFabSortResult() {
        String fabStr = getString(getKey(R.string.key_fab_sort_result), null);
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
        putString(getKey(R.string.key_fab_sort_result), fabStr.toString());
    }

    public boolean is24HourMode() {
        return getBoolean(getKey(R.string.key_is_24_hour_mode), true);
    }
}
