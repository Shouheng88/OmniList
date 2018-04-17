package me.shouheng.omnilist.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.enums.Portrait;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.helper.StoreHelper;
import me.shouheng.omnilist.provider.helper.TimelineHelper;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.provider.schema.BaseSchema;
import me.shouheng.omnilist.provider.schema.CategorySchema;


/**
 * Created by wangshouheng on 2017/8/19. */
public class CategoryStore extends BaseStore<Category> {

    private static CategoryStore sInstance = null;

    public static CategoryStore getInstance(){
        if (sInstance == null){
            synchronized (CategoryStore.class) {
                if (sInstance == null) {
                    sInstance = new CategoryStore(PalmApp.getContext());
                }
            }
        }
        return sInstance;
    }
    
    private CategoryStore(Context context) {
        super(context);
    }

    @Override
    protected void afterDBCreated(SQLiteDatabase db) {}

    @Override
    protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    @Override
    public void fillModel(Category model, Cursor cursor) {
        model.setName(cursor.getString(cursor.getColumnIndex(CategorySchema.NAME)));
        model.setColor(cursor.getInt(cursor.getColumnIndex(CategorySchema.COLOR)));
        model.setPortrait(Portrait.getPortraitById(cursor.getInt(cursor.getColumnIndex(CategorySchema.PORTRAIT))));
        model.setCategoryOrder(cursor.getInt(cursor.getColumnIndex(CategorySchema.CATEGORY_ORDER)));

        int cntIndex = cursor.getColumnIndex(CategorySchema.COUNT);
        if (cntIndex != -1) model.setCount(cursor.getInt(cntIndex));
    }

    @Override
    protected void fillContentValues(ContentValues values, Category model) {
        values.put(CategorySchema.NAME, model.getName());
        values.put(CategorySchema.COLOR, model.getColor());
        values.put(CategorySchema.PORTRAIT, model.getPortrait().id);
        values.put(CategorySchema.CATEGORY_ORDER, model.getCategoryOrder());
    }

    public synchronized List<Category> getCategories(String whereSQL, String orderSQL, Status status, boolean showCompleted) {
        Cursor cursor = null;
        List<Category> models;
        SQLiteDatabase database = getWritableDatabase();
        try {
            cursor = database.rawQuery(" SELECT *, " + getAssignmentsCount(status, showCompleted)
                            + " FROM " + tableName
                            + " WHERE " + BaseSchema.USER_ID + " = " + userId
                            + " AND ( " + CategorySchema.STATUS + " = " + status.id + " OR " + CategorySchema.COUNT + " > 0 ) "
                            + (TextUtils.isEmpty(whereSQL) ? "" : " AND " + whereSQL)
                            + " GROUP BY " + CategorySchema.CODE
                            + (TextUtils.isEmpty(orderSQL) ? "" : " ORDER BY " + orderSQL),
                    new String[]{});
            models = getList(cursor);
        } finally {
            closeCursor(cursor);
            closeDatabase(database);
        }
        return models;
    }

    public synchronized void update(Category model, Status fromStatus, Status toStatus) {
        if (model == null || toStatus == null) return;
        TimelineHelper.addTimeLine(model, StoreHelper.getStatusOperation(toStatus));
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {

            /*
             * Update current category itself OF GIVEN STATUS. */
            database.execSQL(" UPDATE " + tableName
                            + " SET " + BaseSchema.STATUS + " = " + toStatus.id + " , " + BaseSchema.LAST_MODIFIED_TIME + " = ? "
                            + " WHERE " + BaseSchema.CODE + " = " + model.getCode()
                            + " AND " + BaseSchema.USER_ID + " = " + userId,
                    new String[]{String.valueOf(System.currentTimeMillis())});

            /*
             * Update the status of all associated assignment OF GIVEN STATUS. */
            database.execSQL(" UPDATE " + AssignmentSchema.TABLE_NAME
                            + " SET " + BaseSchema.STATUS + " = " + toStatus.id + " , " + BaseSchema.LAST_MODIFIED_TIME + " = ? "
                            + " WHERE " + AssignmentSchema.CATEGORY_CODE + " = " + model.getCode()
                            + " AND " + BaseSchema.USER_ID + " = " + userId
                            + " AND " + BaseSchema.STATUS + " = " + fromStatus.id,
                    new String[]{String.valueOf(System.currentTimeMillis())});

            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    public synchronized void updateOrders(List<Category> categories){
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            int size = categories.size();
            for (int i = 0; i < size; i++){
                database.execSQL(" UPDATE " + tableName +
                        " SET " + CategorySchema.CATEGORY_ORDER + " = " + i +
                        " WHERE " + CategorySchema.CODE + " = " + categories.get(i).getCode());
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    private String getAssignmentsCount(Status status, boolean showCompleted) {
        return " (SELECT COUNT(*) FROM " + AssignmentSchema.TABLE_NAME + " AS t1 "
                + " WHERE t1." + AssignmentSchema.CATEGORY_CODE + " = " + tableName + "." + CategorySchema.CODE
                + " AND t1." + AssignmentSchema.USER_ID + " = " + userId
                + (showCompleted ? "" : " AND t1." + AssignmentSchema.PROGRESS  + " != 100 ")
                + " AND t1." + AssignmentSchema.STATUS + " = " + (status == null ? Status.NORMAL.id : status.id) + " ) "
                + " AS " + CategorySchema.COUNT;
    }
}
