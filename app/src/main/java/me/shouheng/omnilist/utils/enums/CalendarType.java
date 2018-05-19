package me.shouheng.omnilist.utils.enums;

public enum CalendarType {
    MONTH(0),
    WEEK(1);

    public final int id;

    CalendarType(int id) {
        this.id = id;
    }

    public static CalendarType getCalendarTypeById(int id) {
        for (CalendarType calendarType : values()) {
            if (calendarType.id == id) {
                return calendarType;
            }
        }
        throw new IllegalArgumentException("illegal id " + id);
    }
}
