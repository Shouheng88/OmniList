package me.shouheng.omnilist.model.enums;

/**
 * Created by wangshouheng on 2017/4/30. */
public enum AlarmType {
    SPECIFIED_DATE(1),
    WEEK_REPEAT(2),
    MONTH_REPEAT(3);

    public final int id;

    AlarmType(int id){
        this.id = id;
    }

    public static AlarmType getTypeById(int id){
        for (AlarmType type : values()){
            if (type.id == id){
                return type;
            }
        }
        return SPECIFIED_DATE;
    }
}
