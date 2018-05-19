package com.alamkanak.weekview;

/**
 * Created by wangshouheng on 2017/5/4.*/
public enum EventType {
    CLASS(0),
    ASSIGNMENT(1),
    NOTE(2);

    public final int mId;

    EventType(int mId){
        this.mId = mId;
    }

    public static EventType getTypeById(int id){
        for (EventType type : values()){
            if (type.mId == id){
                return type;
            }
        }
        throw new IllegalArgumentException("Unrecognized id:" + id);
    }
}
