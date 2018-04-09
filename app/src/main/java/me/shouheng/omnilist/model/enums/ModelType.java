package me.shouheng.omnilist.model.enums;

import android.location.Location;
import android.support.annotation.StringRes;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Model;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.model.Weather;


/**
 * Created by wangshouheng on 2017/8/12. */
public enum ModelType {
    NONE(0, Model.class, R.string.model_name_none),
    ALARM(10, Alarm.class, R.string.model_name_alarm),
    ATTACHMENT(11, Attachment.class, R.string.model_name_attachment),
    LOCATION(13, Location.class, R.string.model_name_location),
    TIME_LINE(15, TimeLine.class, R.string.model_name_timeline),
    WEATHER(16, Weather.class, R.string.model_name_weather);

    public final int id;

    public final Class<?> cls;

    @StringRes
    public final int typeName;

    ModelType(int id, Class<?> cls, int typeName) {
        this.id = id;
        this.cls = cls;
        this.typeName = typeName;
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
