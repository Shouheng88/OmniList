package me.shouheng.omnilist.model.enums;

public enum SubAssignmentType {
    TODO(0),
    NOTE(1);

    public final int id;

    SubAssignmentType(int id){
        this.id = id;
    }

    public static SubAssignmentType getTypeById(int id){
        for (SubAssignmentType type : values()){
            if (type.id == id){
                return type;
            }
        }
        throw new IllegalArgumentException("illegal sub assignment type id");
    }
}
