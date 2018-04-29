package me.shouheng.omnilist.model.enums;

import android.support.annotation.StringRes;

import me.shouheng.omnilist.R;

public enum SubAssignmentType {
    TODO(0, R.string.item_todo),
    NOTE(1, R.string.item_note),
    NOTE_WITH_PORTRAIT(2, R.string.item_note_with_portrait);

    public final int id;

    @StringRes
    public final int resName;

    SubAssignmentType(int id, @StringRes int resName) {
        this.id = id;
        this.resName = resName;
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
