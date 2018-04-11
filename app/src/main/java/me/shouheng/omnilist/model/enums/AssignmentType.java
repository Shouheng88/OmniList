package me.shouheng.omnilist.model.enums;

/**
 * Created by wangshouheng on 2017/5/8.*/
public enum AssignmentType {
    NORMAL(0);

    public final int id;

    AssignmentType(int id){
        this.id = id;
    }

    public static AssignmentType getTypeById(int id){
        for (AssignmentType type : values()){
            if (type.id == id){
                return type;
            }
        }
        throw new IllegalArgumentException("illegal assignment type id");
    }
}
