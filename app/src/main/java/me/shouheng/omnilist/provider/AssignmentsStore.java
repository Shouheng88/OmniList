package me.shouheng.omnilist.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.Date;
import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.model.enums.AssignmentType;
import me.shouheng.omnilist.model.enums.ModelType;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.enums.Priority;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.model.tools.DaysOfWeek;
import me.shouheng.omnilist.provider.helper.StoreHelper;
import me.shouheng.omnilist.provider.helper.TimelineHelper;
import me.shouheng.omnilist.provider.schema.AlarmSchema;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.provider.schema.AttachmentSchema;
import me.shouheng.omnilist.provider.schema.SubAssignmentSchema;
import me.shouheng.omnilist.provider.schema.TimelineSchema;

/**
 * Created by wangshouheng on 2017/3/13. */
public class AssignmentsStore extends BaseStore<Assignment> {

    private static AssignmentsStore sInstance = null;

    public static AssignmentsStore getInstance() {
        if (sInstance == null){
            synchronized (AssignmentsStore.class) {
                if (sInstance == null) {
                    sInstance = new AssignmentsStore(PalmApp.getContext());
                }
            }
        }
        return sInstance;
    }

    private AssignmentsStore(final Context context) {
        super(context);
    }

    // region sql fragments
    private final String GET_ATTACHMENTS_COUNT = " (SELECT COUNT(*) FROM " + AttachmentSchema.TABLE_NAME + " AS t1 "
            + " WHERE t1." + AttachmentSchema.MODEL_CODE + " = " + tableName + "." + AssignmentSchema.CODE
            + " AND t1." + AttachmentSchema.USER_ID + " = " + userId
            + " AND t1." + AttachmentSchema.MODEL_TYPE + " = " + ModelType.ASSIGNMENT.id
            + " AND t1." + AttachmentSchema.STATUS + " = " + Status.NORMAL.id + " ) "
            + " AS " + AssignmentSchema.ATTACHMENT_NUMBER;

    private final String GET_ALARMS_COUNT = " (SELECT COUNT(*) FROM " + AlarmSchema.TABLE_NAME + " AS t2 "
            + " WHERE t2." + AlarmSchema.MODEL_CODE + " = " + tableName + "." + AssignmentSchema.CODE
            + " AND t2." + AlarmSchema.USER_ID + " = " + userId
            + " AND t2." + AlarmSchema.MODEL_TYPE + " = " + ModelType.ASSIGNMENT.id
            + " AND t2." + AttachmentSchema.STATUS + " = " + Status.NORMAL.id + " ) "
            + " AS " + AssignmentSchema.ALARM_NUMBER;
    // endregion

    @Override
    protected void afterDBCreated(SQLiteDatabase db) {}

    @Override
    public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion){}

    @Override
    public void fillModel(Assignment assignment, Cursor cursor) {
        assignment.setCategoryCode(cursor.getLong(cursor.getColumnIndex(AssignmentSchema.CATEGORY_CODE)));
        assignment.setName(cursor.getString(cursor.getColumnIndex(AssignmentSchema.NAME)));
        assignment.setComment(cursor.getString(cursor.getColumnIndex(AssignmentSchema.COMMENT)));
        assignment.setTags(cursor.getString(cursor.getColumnIndex(AssignmentSchema.TAGS)));
        assignment.setStartTime(new Date(cursor.getLong(cursor.getColumnIndex(AssignmentSchema.START_TIME))));
        assignment.setEndTime(new Date(cursor.getLong(cursor.getColumnIndex(AssignmentSchema.END_TIME))));
        assignment.setDaysOfWeek(DaysOfWeek.getInstance(cursor.getInt(cursor.getColumnIndex(AssignmentSchema.DAYS_OF_WEEK))));
        assignment.setNoticeTime(cursor.getInt(cursor.getColumnIndex(AssignmentSchema.NOTICE_TIME)));
        assignment.setCompleteTime(new Date(cursor.getLong(cursor.getColumnIndex(AssignmentSchema.COMPLETED_TIME))));
        assignment.setProgress(cursor.getInt(cursor.getColumnIndex(AssignmentSchema.PROGRESS)));
        assignment.setPriority(Priority.getTypeById(cursor.getInt(cursor.getColumnIndex(AssignmentSchema.PRIORITY))));
        assignment.setAssignmentOrder(cursor.getInt(cursor.getColumnIndex(AssignmentSchema.ASSIGNMENT_ORDER)));
        assignment.setAssignmentType(AssignmentType.getTypeById(cursor.getInt(cursor.getColumnIndex(AssignmentSchema.ASSIGNMENT_TYPE))));

        int alarmCntIndex, attachCntIndex;
        if ((alarmCntIndex = cursor.getColumnIndex(AssignmentSchema.ALARM_NUMBER)) != -1) {
            assignment.setAlarms(cursor.getInt(alarmCntIndex));
        }
        if ((attachCntIndex = cursor.getColumnIndex(AssignmentSchema.ATTACHMENT_NUMBER)) != -1) {
            assignment.setAttachments(cursor.getInt(attachCntIndex));
        }
    }

    @Override
    protected void fillContentValues(ContentValues values, Assignment assignment) {
        values.put(AssignmentSchema.CATEGORY_CODE, assignment.getCategoryCode());
        values.put(AssignmentSchema.NAME, assignment.getName());
        values.put(AssignmentSchema.COMMENT, assignment.getComment());
        values.put(AssignmentSchema.TAGS, assignment.getTags());
        values.put(AssignmentSchema.START_TIME, assignment.getStartTime() == null ? 0 : assignment.getStartTime().getTime());
        values.put(AssignmentSchema.END_TIME, assignment.getEndTime() == null ? 0 : assignment.getEndTime().getTime());
        values.put(AssignmentSchema.DAYS_OF_WEEK, assignment.getDaysOfWeek() == null ? 0 : assignment.getDaysOfWeek().getCoded());
        values.put(AssignmentSchema.NOTICE_TIME, assignment.getNoticeTime());
        values.put(AssignmentSchema.COMPLETED_TIME, assignment.getCompleteTime() == null ? 0 : assignment.getCompleteTime().getTime());
        values.put(AssignmentSchema.PROGRESS, assignment.getProgress());
        values.put(AssignmentSchema.PRIORITY, assignment.getPriority().id);
        values.put(AssignmentSchema.ASSIGNMENT_ORDER, assignment.getAssignmentOrder());
        values.put(AssignmentSchema.ASSIGNMENT_TYPE, assignment.getAssignmentType().id);
    }

    public synchronized List<Assignment> get(String whereSQL, String orderSQL, Status status, boolean exclude) {
        Cursor cursor = null;
        List<Assignment> assignments;
        final SQLiteDatabase database = getWritableDatabase();
        try {
            String sql = " SELECT *," + GET_ATTACHMENTS_COUNT + ", " + GET_ALARMS_COUNT
                    + " FROM " + tableName
                    + " WHERE " + AssignmentSchema.USER_ID + " = " + userId
                    + (TextUtils.isEmpty(whereSQL) ? "" : " AND " + whereSQL)
                    + (status == null ? "" : " AND " + AssignmentSchema.STATUS + (exclude ? " != " : " = ") + status.id)
                    + (TextUtils.isEmpty(orderSQL) ? "" : " ORDER BY " + orderSQL);
            cursor = database.rawQuery(sql, new String[]{});
            assignments = getList(cursor);
        } finally {
            closeCursor(cursor);
            closeDatabase(database);
        }
        return assignments;
    }

    @Override
    public synchronized void saveOrUpdate(Assignment model) {
        if (model == null) return;

        if (isNewModel(model.getCode())) {
            saveAssignment(model, model.getSubAssignments());
        } else {
            updateAssignment(model, model.getSubAssignments());
        }
    }

    private synchronized void saveAssignment(Assignment assignment, List<SubAssignment> subAssignments) {
        TimelineHelper.addTimeLine(assignment, Operation.ADD);
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.insert(tableName, null, getContentValues(assignment));
            for (SubAssignment subAssignment : subAssignments) {
                /*All the sub assignment is new.*/
                TimeLine timeLine = TimelineHelper.getTimeLine(subAssignment, Operation.ADD);
                if (timeLine != null) {
                    database.insert(TimelineSchema.TABLE_NAME, null, StoreHelper.getContentValues(timeLine));
                }
                database.insert(SubAssignmentSchema.TABLE_NAME, null, StoreHelper.getContentValues(subAssignment));
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    private synchronized void updateAssignment(Assignment assignment, List<SubAssignment> subAssignments) {
        StoreHelper.setLastModifiedInfo(assignment);
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            /*Update assignment.*/
            database.update(tableName, getContentValues(assignment),
                    AssignmentSchema.CODE + " = ? " + " AND " + AssignmentSchema.USER_ID + " = ? ",
                    new String[]{String.valueOf(assignment.getCode()), String.valueOf(userId)});
            /*Delete associated sub assignments.*/
            database.delete(SubAssignmentSchema.TABLE_NAME,
                    SubAssignmentSchema.PARENT_CODE + " = ? " + " AND " + SubAssignmentSchema.USER_ID + " = ? ",
                    new String[]{String.valueOf(assignment.getCode()), String.valueOf(userId)});
            /*Insert sub assignment.*/
            for (SubAssignment sub : subAssignments) {
                /*Insert new sub assignment.*/
                StoreHelper.setLastModifiedInfo(sub);
                database.insert(SubAssignmentSchema.TABLE_NAME, null, StoreHelper.getContentValues(sub));
                /*Get timeline for sub assignment.*/
                if (sub.isContentChanged()) {
                    TimeLine timeLine;
                    if (sub.isNewSubAssignment()) {
                        timeLine = TimelineHelper.getTimeLine(sub, Operation.ADD);
                    } else {
                        if (sub.isCompleteThisTime()) {
                            timeLine = TimelineHelper.getTimeLine(sub, Operation.COMPLETE);
                        } else if (sub.isInCompletedThisTime()) {
                            timeLine = TimelineHelper.getTimeLine(sub, Operation.INCOMPLETE);
                        } else {
                            timeLine = TimelineHelper.getTimeLine(sub, Operation.UPDATE);
                        }
                    }
                    if (timeLine != null) {
                        database.insert(TimelineSchema.TABLE_NAME, null, StoreHelper.getContentValues(timeLine));
                    }
                }
            }
            /*Commit transaction.*/
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    public synchronized void updateAssignments(List<Assignment> assignments) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            for (Assignment assignment : assignments){
                if (assignment.isChanged()){
                    StoreHelper.setLastModifiedInfo(assignment);
                    database.update(AssignmentSchema.TABLE_NAME, getContentValues(assignment),
                            AssignmentSchema.CODE + " = ? " + " AND " + AssignmentSchema.USER_ID + " = ? ",
                            new String[]{String.valueOf(assignment.getCode()), String.valueOf(userId)});
                    TimeLine timeLine = null;
                    if (assignment.isCompleteThisTime()) {
                        timeLine = TimelineHelper.getTimeLine(assignment, Operation.COMPLETE);
                    } else if (assignment.isInCompletedThisTime()) {
                        timeLine = TimelineHelper.getTimeLine(assignment, Operation.INCOMPLETE);
                    }
                    if (timeLine != null) {
                        database.insert(TimelineSchema.TABLE_NAME, null, StoreHelper.getContentValues(timeLine));
                    }
                    assignment.setChanged(false);
                }
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    public synchronized void updateOrders(List<Assignment> assignments) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            int size = assignments.size();
            for (int i = 0; i < size; i++) {
                database.execSQL(" UPDATE " + AssignmentSchema.TABLE_NAME +
                        " SET " + AssignmentSchema.ASSIGNMENT_ORDER + " = " + i +
                        " WHERE " + AssignmentSchema.CODE + " = " + assignments.get(i).getCode());
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    public synchronized void updateAssignment(long code, boolean completed) {
        TimelineHelper.addTimeLine(get(code), completed ? Operation.COMPLETE : Operation.INCOMPLETE);
        final SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.execSQL(" UPDATE " + AssignmentSchema.TABLE_NAME
                            + " SET " + AssignmentSchema.PROGRESS + " = " + (completed ? 100 : 0) + " , "
                            + AssignmentSchema.LAST_MODIFIED_TIME + " = ? "
                            + " WHERE " + AssignmentSchema.CODE + " = " + code
                            + " AND " + AssignmentSchema.USER_ID + " = " + userId,
                    new String[]{String.valueOf(System.currentTimeMillis())});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    public synchronized void deleteAssignments(Category category) {
        SQLiteDatabase database = getWritableDatabase();
        database.beginTransaction();
        try {
            database.execSQL(" UPDATE " + tableName
                            + " SET " + AssignmentSchema.STATUS + " = " + Status.DELETED.id + ", "
                            + AssignmentSchema.LAST_MODIFIED_TIME + " = ? "
                            + " WHERE " + AssignmentSchema.CATEGORY_CODE + " = " + category.getCode()
                            + " AND " + AssignmentSchema.USER_ID + " = " + userId,
                    new String[]{String.valueOf(System.currentTimeMillis())});
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
            closeDatabase(database);
        }
    }

    /**
     * Get the assignment according to alarm type. Note that this method will only return the assignments
     * based on the start and end time of alarm. Need to re-filter the return values by the alarm type
     * and repeat value.
     *
     * @param startMillis start millis
     * @param endMillis and millis
     * @return the queried list */
    public synchronized List<Assignment> getAssignments(long startMillis, long endMillis, String whereSQL) {
        Cursor cursor = null;
        List<Assignment> assignments;
        final SQLiteDatabase database = getWritableDatabase();
        try {
            String sql = " SELECT " + tableName + ".*, " + GET_ATTACHMENTS_COUNT + ", " + GET_ALARMS_COUNT
                    + " FROM " + tableName
                    + " WHERE " + tableName + "." + AssignmentSchema.USER_ID + " = " + userId
                    + " AND " + tableName + "." + AssignmentSchema.START_TIME + " <= " + endMillis
                    + " AND " + tableName + "." + AssignmentSchema.END_TIME + " >= " + startMillis
                    + " AND " + tableName + "." + AssignmentSchema.STATUS + " = " + Status.NORMAL.id
                    + (TextUtils.isEmpty(whereSQL) ? "" : " AND " + whereSQL);
            cursor = database.rawQuery(sql, new String[]{});
            assignments = getList(cursor);
        } finally {
            closeCursor(cursor);
            closeDatabase(database);
        }
        return assignments;
    }
}
