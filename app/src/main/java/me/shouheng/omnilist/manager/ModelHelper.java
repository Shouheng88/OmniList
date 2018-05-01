package me.shouheng.omnilist.manager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.Model;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.Weather;
import me.shouheng.omnilist.model.enums.SubAssignmentType;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.ToastUtils;


/**
 * Created by wangshouheng on 2017/11/4.*/
public class ModelHelper {

    public static <T extends Model> String getTimeInfo(T model) {
        return PalmApp.getStringCompact(R.string.text_created_time) + " : "
                + TimeUtils.getPrettyTime(model.getAddedTime()) + "\n"
                + PalmApp.getStringCompact(R.string.text_last_modified_time) + " : "
                + TimeUtils.getPrettyTime(model.getLastModifiedTime()) + "\n"
                + PalmApp.getStringCompact(R.string.text_last_sync_time) + " : "
                + (model.getLastSyncTime().getTime() == 0 ? "--" : TimeUtils.getPrettyTime(model.getLastModifiedTime()));
    }

    public static void copyToClipboard(Activity ctx, String content) {
        ClipboardManager clipboardManager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setText(content);
    }

    public static String getMarkdown(Assignment assignment,
                                     List<SubAssignment> subAssignments,
                                     Location location,
                                     Alarm alarm,
                                     List<Attachment> attachments) {
        StringBuilder sb = new StringBuilder();

        // Append title
        sb.append("## ").append(assignment.getName()).append("\n\n");

        // Time info
        sb.append(PalmApp.getStringCompact(R.string.text_created_time))
                .append(": ")
                .append(TimeUtils.getPrettyTime(assignment.getAddedTime()))
                .append("\n");
        sb.append(PalmApp.getStringCompact(R.string.text_last_modified_time))
                .append(": ")
                .append(TimeUtils.getPrettyTime(assignment.getLastModifiedTime()))
                .append("\n");
        sb.append(PalmApp.getStringCompact(R.string.text_last_sync_time))
                .append(": ")
                .append(assignment.getLastSyncTime().getTime() == 0 ? "--" : TimeUtils.getPrettyTime(assignment.getLastModifiedTime()))
                .append("\n\n");

        // Append notification
        if (alarm != null) {
            sb.append(PalmApp.getStringCompact(R.string.text_notification))
                    .append(": ")
                    .append(alarm.getAlarmInfo(PalmApp.getContext()))
                    .append("\n\n");
        }

        // Append location
        if (location != null) {
            sb.append(PalmApp.getStringCompact(R.string.text_location))
                    .append(": ")
                    .append(getFormatLocation(location))
                    .append("\n\n");
        }

        // Append sub assignment
        for (SubAssignment subAssignment : subAssignments) {
            if (subAssignment.getSubAssignmentType() == SubAssignmentType.TODO) {
                sb.append(subAssignment.isCompleted() ? "- [x] " : "- [ ] ").append(subAssignment.getContent()).append("\n\n");
            } else {
                sb.append(subAssignment.getContent()).append("\n\n");
            }
        }

        // Append comment
        if (!TextUtils.isEmpty(assignment.getComment())) {
            sb.append(PalmApp.getStringCompact(R.string.text_comments))
                    .append(": ")
                    .append(assignment.getComment()).append("\n\n");
        }

        // Append attachment
        if (!attachments.isEmpty()) {
            sb.append("------")
                    .append("\n")
                    .append(PalmApp.getStringCompact(R.string.text_attachment))
                    .append(": ");
            for (Attachment attachment : attachments) {
                sb.append("![").append(attachment.getName()).append("]")
                        .append("(").append(attachment.getUri().toString()).append(")").append("\n");
            }
        }

        return sb.toString();
    }

    public static void share(Context context, String title, String content, List<Attachment> attachments) {
        Intent shareIntent = new Intent();
        if (attachments.size() == 0) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
        } else if (attachments.size() == 1) {
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType(attachments.get(0).getMineType());
            shareIntent.putExtra(Intent.EXTRA_STREAM, attachments.get(0).getUri());
        } else {
            shareIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
            ArrayList<Uri> uris = new ArrayList<>();
            Map<String, Boolean> mimeTypes = new HashMap<>();
            for (Attachment attachment : attachments) {
                uris.add(attachment.getUri());
                mimeTypes.put(attachment.getMineType(), true);
            }
            shareIntent.setType(mimeTypes.size() > 1 ? "*/*" : (String) mimeTypes.keySet().toArray()[0]);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        }
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, title);
        shareIntent.putExtra(Intent.EXTRA_TEXT, content);

        context.startActivity(Intent.createChooser(shareIntent, PalmApp.getStringCompact(R.string.share_message_chooser)));
    }

    public static void shareFile(Context context, File file, String mimeType) {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType(mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, FileHelper.getUriFromFile(context, file));
        context.startActivity(Intent.createChooser(shareIntent, PalmApp.getStringCompact(R.string.share_message_chooser)));
    }

    public static <T extends Model> void copyLink(Activity ctx, T model) {
        if (model.getLastSyncTime().getTime() == 0) {
            ToastUtils.makeToast(R.string.cannot_get_link_of_not_synced_item);
            return;
        }

        ClipboardManager clipboardManager = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setText(null);
    }

    public static String getFormatLocation(Location location) {
        return location.getCountry() + "|"
                + location.getProvince() + "|"
                + location.getCity() + "|"
                + location.getDistrict();
    }

    public static String getFormatWeather(Weather weather) {
        return PalmApp.getStringCompact(weather.getType().nameRes) + "|" + weather.getTemperature();
    }

    public static void showStatistic(Context context,
                                     Assignment assignment,
                                     List<SubAssignment> subAssignments,
                                     Alarm alarm,
                                     List<Attachment> attachments) {
        View root = LayoutInflater.from(context).inflate(R.layout.dialog_stats, null, false);
        LinearLayout llStats = root.findViewById(R.id.ll_stats);

        addStat(context, llStats, context.getString(R.string.text_created_time),
                TimeUtils.getPrettyTime(assignment.getAddedTime()));
        addStat(context, llStats, context.getString(R.string.text_last_modified_time),
                TimeUtils.getPrettyTime(assignment.getLastModifiedTime()));
        addStat(context, llStats, context.getString(R.string.text_last_sync_time),
                (assignment.getLastSyncTime().getTime() == 0 ? "--" : TimeUtils.getPrettyTime(assignment.getLastModifiedTime())));
        addStat(context, llStats, context.getString(R.string.text_alarm),
                alarm == null ? "--" : alarm.getAlarmInfo(context));

        addStat(context, llStats, context.getString(R.string.text_attachment), String.valueOf(attachments.size()));

        addStat(context, llStats, context.getString(R.string.text_sub_assignment_number), String.valueOf(subAssignments.size()));

        int len = 0;
        List<String> strings = new LinkedList<>();
        strings.add(assignment.getName());
        strings.add(assignment.getComment());
        for (SubAssignment subAssignment : subAssignments) {
            strings.add(subAssignment.getContent());
        }

        for (String string : strings) {
            if (!TextUtils.isEmpty(string)) {
                len+=string.length();
            }
        }

        addStat(context, llStats, context.getString(R.string.text_chars), String.valueOf(len));

        new AlertDialog.Builder(context)
                .setTitle(R.string.text_statistic)
                .setView(root)
                .setPositiveButton(R.string.text_confirm, null)
                .create()
                .show();
    }

    private static void addStat(Context context, LinearLayout llStats, String name, String result) {
        LinearLayout llStat = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.item_stat, null, false);
        ((TextView) llStat.findViewById(R.id.tv_name)).setText(name);
        ((TextView) llStat.findViewById(R.id.tv_result)).setText(result);
        llStats.addView(llStat);
    }
}