package me.shouheng.omnilist.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.enums.SubAssignmentType;
import me.shouheng.omnilist.provider.schema.SubAssignmentSchema;


/**
 * Created by wangshouheng on 2017/8/19. */
public class SubAssignmentStore extends BaseStore<SubAssignment> {

    private static SubAssignmentStore sInstance = null;

    public static SubAssignmentStore getInstance(){
        if (sInstance == null){
            synchronized (SubAssignmentStore.class) {
                if (sInstance == null) {
                    sInstance = new SubAssignmentStore(PalmApp.getContext());
                }
            }
        }
        return sInstance;
    }

    private SubAssignmentStore(Context context) {
        super(context);
    }

    @Override
    protected void afterDBCreated(SQLiteDatabase db) {}

    @Override
    protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    @Override
    public void fillModel(SubAssignment subAssignment, Cursor cursor) {
        subAssignment.setAssignmentCode(cursor.getLong(cursor.getColumnIndex(SubAssignmentSchema.PARENT_CODE)));
        subAssignment.setCompleted(cursor.getInt(cursor.getColumnIndex(SubAssignmentSchema.COMPLETED)) == 1);
        subAssignment.setContent(cursor.getString(cursor.getColumnIndex(SubAssignmentSchema.CONTENT)));
        subAssignment.setSubAssignmentOrder(cursor.getInt(cursor.getColumnIndex(SubAssignmentSchema.SUB_ASSIGNMENT_ORDER)));
        subAssignment.setSubAssignmentType(SubAssignmentType.getTypeById(cursor.getInt(cursor.getColumnIndex(SubAssignmentSchema.SUB_ASSIGNMENT_TYPE))));
    }

    @Override
    protected void fillContentValues(ContentValues values, SubAssignment subAssignment) {
        values.put(SubAssignmentSchema.CONTENT, subAssignment.getContent());
        values.put(SubAssignmentSchema.PARENT_CODE, subAssignment.getAssignmentCode());
        values.put(SubAssignmentSchema.COMPLETED, subAssignment.isCompleted() ? 1 : 0);
        values.put(SubAssignmentSchema.SUB_ASSIGNMENT_ORDER, subAssignment.getSubAssignmentOrder());
        values.put(SubAssignmentSchema.SUB_ASSIGNMENT_TYPE, subAssignment.getSubAssignmentType().id);
    }

    public synchronized void updateOrders(List<SubAssignment> subAssignments) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            int size = subAssignments.size();
            for (int i = 0; i < size; i++){
                database.execSQL(" UPDATE " + tableName
                        + " SET " + SubAssignmentSchema.SUB_ASSIGNMENT_ORDER + " = " + i
                        + " WHERE " + SubAssignmentSchema.CODE + " = " + subAssignments.get(i).getCode());
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }
}
