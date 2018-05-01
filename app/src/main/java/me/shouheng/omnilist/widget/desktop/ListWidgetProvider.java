package me.shouheng.omnilist.widget.desktop;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.SparseArray;
import android.view.View;
import android.widget.RemoteViews;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.MainActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.utils.ColorUtils;


public class ListWidgetProvider extends WidgetProvider {

    @Override
    protected RemoteViews getRemoteViews(Context context,
                                         int widgetId,
                                         boolean isSmall,
                                         boolean isSingleLine,
                                         SparseArray<PendingIntent> map) {
        if (isSmall) {
            return configSmall(context, map);
        } else if (isSingleLine) {
            return configSingleLine(context, map);
        } else {
            return configList(context, widgetId, map);
        }
    }

    private RemoteViews configSmall(Context context, SparseArray<PendingIntent> pendingIntentsMap) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_small);
        views.setOnClickPendingIntent(R.id.iv_launch_app, pendingIntentsMap.get(R.id.iv_launch_app));
        return views;
    }

    private RemoteViews configSingleLine(Context context, SparseArray<PendingIntent> pendingIntentsMap) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
        configToolbar(views, pendingIntentsMap);
        views.setViewVisibility(R.id.iv_setting, View.GONE);
        return views;
    }

    private RemoteViews configList(Context context, int widgetId, SparseArray<PendingIntent> pendingIntentsMap) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout_list);
        configToolbar(views, pendingIntentsMap);

        views.setViewVisibility(R.id.tv_app_name, View.GONE);

        Intent intent = new Intent(context, ListWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
        views.setRemoteAdapter(R.id.widget_list, intent);

        views.setPendingIntentTemplate(R.id.widget_list, listClickPendingIntent(context));

        return views;
    }

    private PendingIntent listClickPendingIntent(Context context) {
        Intent clickIntent = new Intent(context, MainActivity.class);
        clickIntent.setAction(Constants.ACTION_WIDGET_LIST);

        return PendingIntent.getActivity(context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void configToolbar(RemoteViews views, SparseArray<PendingIntent> pendingIntentsMap) {
        views.setOnClickPendingIntent(R.id.iv_launch_app, pendingIntentsMap.get(R.id.iv_launch_app));
        views.setOnClickPendingIntent(R.id.iv_add_mind, pendingIntentsMap.get(R.id.iv_add_mind));
        views.setOnClickPendingIntent(R.id.iv_add_record, pendingIntentsMap.get(R.id.iv_add_record));
        views.setOnClickPendingIntent(R.id.iv_add_photo, pendingIntentsMap.get(R.id.iv_add_photo));
        views.setOnClickPendingIntent(R.id.iv_add_sketch, pendingIntentsMap.get(R.id.iv_add_sketch));
        views.setOnClickPendingIntent(R.id.iv_setting, pendingIntentsMap.get(R.id.iv_setting));
        views.setInt(R.id.toolbar, "setBackgroundColor", ColorUtils.fadeColor(ColorUtils.primaryColor(), 0.2f));
    }
}

