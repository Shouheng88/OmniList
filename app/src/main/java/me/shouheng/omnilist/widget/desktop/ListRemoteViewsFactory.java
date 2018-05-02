package me.shouheng.omnilist.widget.desktop;

import android.app.Application;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;

import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.TimeUtils;


public class ListRemoteViewsFactory implements RemoteViewsFactory, SharedPreferences.OnSharedPreferenceChangeListener {

    private PalmApp app;
    private int appWidgetId;
    private List<Assignment> assignmentList;

    private SharedPreferences sharedPreferences;

    ListRemoteViewsFactory(RemoteViewsService remoteViewsService, Application app, Intent intent) {
        this.app = (PalmApp) app;
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        sharedPreferences = app.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate() {
        LogUtils.d("Created widget " + appWidgetId);
        setupModels();
    }

    private void setupModels() {
        String condition = sharedPreferences.getString(
                Constants.PREF_WIDGET_SQL_PREFIX + String.valueOf(appWidgetId),
                "");
        boolean isOverdue = sharedPreferences.getBoolean(
                Constants.PREF_WIDGET_OVERDUE_PREFIX + String.valueOf(appWidgetId),
                false);
        AssignmentsStore store = AssignmentsStore.getInstance();
        if (isOverdue) {
            long todayEnd = TimeUtils.endToday().getTime();
            assignmentList = store.get(
                    AssignmentSchema.START_TIME + " <= " + todayEnd +
                            " AND " + AssignmentSchema.PROGRESS + " != " + Constants.MAX_ASSIGNMENT_PROGRESS,
                    AssignmentSchema.START_TIME + " DESC ", Status.NORMAL, false);
        } else {
            assignmentList = store.get(condition, AssignmentSchema.ASSIGNMENT_ORDER);
        }
    }

    @Override
    public void onDataSetChanged() {
        setupModels();
    }

    @Override
    public void onDestroy() {
        sharedPreferences.edit().remove(Constants.PREF_WIDGET_SQL_PREFIX + String.valueOf(appWidgetId)).apply();
        sharedPreferences.edit().remove(Constants.PREF_WIDGET_TYPE_PREFIX + String.valueOf(appWidgetId)).apply();
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public int getCount() {
        return assignmentList.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row = new RemoteViews(app.getPackageName(), R.layout.widget_item_assignment);

        Assignment assignment = assignmentList.get(position);

        row.setTextViewText(R.id.tv_title, assignment.getName());

        row.setImageViewResource(R.id.iv_priority, assignment.getPriority().iconRes);

        row.setImageViewResource(R.id.iv_completed, assignment.getProgress() == Constants.MAX_ASSIGNMENT_PROGRESS ?
                R.drawable.ic_check_box_black_24dp :
                R.drawable.ic_check_box_outline_blank_black_24dp);

        row.setTextViewText(R.id.tv_time_info,
                PalmApp.getStringCompact(R.string.text_last_modified_time) + ":"
                        + TimeUtils.getLongDateTime(app.getApplicationContext(), assignment.getLastModifiedTime()));
        row.setInt(R.id.root, "setBackgroundColor", app.getResources().getColor(R.color.white_translucent));
        row.setViewVisibility(R.id.iv_files, assignment.getAttachments() != 0 ? View.VISIBLE : View.GONE);
        row.setViewVisibility(R.id.iv_alarm, assignment.getAlarms() != 0 ? View.VISIBLE : View.GONE);

        Bundle extras = new Bundle();
        extras.putParcelable(Constants.EXTRA_MODEL, assignment);
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        row.setOnClickFillInIntent(R.id.root, fillInIntent);

        return row;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return assignmentList.get(position).getCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static void updateConfiguration(
            Context mContext, int mAppWidgetId, Category category, boolean isOverdue, boolean includeCompleted) {
        Editor editor = mContext.getSharedPreferences(Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS).edit();
        String sqlCondition;
        if (isOverdue) {
            editor.putBoolean(Constants.PREF_WIDGET_OVERDUE_PREFIX + String.valueOf(mAppWidgetId), true);
            editor.putLong(Constants.PREF_WIDGET_CATEGORY_CODE_PREFIX + String.valueOf(mAppWidgetId), 0L);
        } else {
            sqlCondition = AssignmentSchema.CATEGORY_CODE + " = " + category.getCode() +
                    (includeCompleted ? "" : " AND " + AssignmentSchema.PROGRESS + " != " + Constants.MAX_ASSIGNMENT_PROGRESS);
            editor.putLong(Constants.PREF_WIDGET_CATEGORY_CODE_PREFIX + String.valueOf(mAppWidgetId), category.getCode());
            editor.putString(Constants.PREF_WIDGET_SQL_PREFIX + String.valueOf(mAppWidgetId), sqlCondition);
        }
        editor.apply();
        AppWidgetUtils.notifyAppWidgets(mContext);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {}
}
