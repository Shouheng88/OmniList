package me.shouheng.omnilist.model.enums;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.Model;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.model.Weather;


/**
 * Created by wangshouheng on 2017/8/12. */
public enum ModelType {
    NONE(0, Model.class, R.string.model_name_none, R.drawable.circle),
    CATEGORY(17, Category.class, R.string.model_name_category, R.drawable.ic_folder_special_black_24dp),
    ASSIGNMENT(1, Assignment.class, R.string.model_name_assignment, R.drawable.ic_assignment_turned_in_black_24dp),
    SUB_ASSIGNMENT(5, SubAssignment.class, R.string.model_name_sub_assignment, R.drawable.ic_storage_black_24dp),
    ALARM(10, Alarm.class, R.string.model_name_alarm, R.drawable.ic_access_alarm_grey),
    ATTACHMENT(11, Attachment.class, R.string.model_name_attachment, R.drawable.ic_attach_file_black),
    LOCATION(13, Location.class, R.string.model_name_location, R.drawable.ic_location1_grey_24dp),
    TIME_LINE(15, TimeLine.class, R.string.model_name_timeline, R.drawable.ic_timeline),
    WEATHER(16, Weather.class, R.string.model_name_weather, R.drawable.ic_wb_sunny_black_24dp);

    public final int id;

    public final Class<?> cls;

    @StringRes
    public final int typeName;

    @DrawableRes
    public final int drawableRes;

    ModelType(int id, Class<?> cls, @StringRes int typeName, @DrawableRes int drawableRes) {
        this.id = id;
        this.cls = cls;
        this.typeName = typeName;
        this.drawableRes = drawableRes;
    }

    public static ModelType getTypeById(int id){
        for (ModelType type : values()){
            if (type.id == id){
                return type;
            }
        }
        return NONE;
    }

    public static ModelType getTypeByName(Class<?> cls) {
        for (ModelType type : values()){
            if (type.cls.getName().equals(cls.getName())){
                return type;
            }
        }
        return NONE;
    }
}
