package me.shouheng.omnilist.model.enums;

/**
 * Created by wangshouheng on 2017/4/30. */
public enum AlarmType {
    DAILY(0), /*Alarm to show daily report message.*/
    SPECIFIED_DATE(1), /*Alarm to notice one time in given time.*/
    WEEK_REPEAT(2), /*Alarm to repeat according to specified week.*/
    MONTH_REPEAT(3); /*Alarm to repeat according to specified month.*/

    public final int id;

    AlarmType(int id) {
        this.id = id;
    }

    public static AlarmType getTypeById(int id) {
        for (AlarmType type : values()){
            if (type.id == id){
                return type;
            }
        }
        throw new IllegalArgumentException("Illegal alarm id!");
    }
}
