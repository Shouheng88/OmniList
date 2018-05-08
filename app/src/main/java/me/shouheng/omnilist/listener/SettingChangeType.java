package me.shouheng.omnilist.listener;

public enum SettingChangeType {
    DRAWER(0),
    FAB(1);

    public final int id;

    SettingChangeType(int id) {
        this.id = id;
    }
}
