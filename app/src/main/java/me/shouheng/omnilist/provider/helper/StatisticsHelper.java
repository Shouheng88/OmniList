package me.shouheng.omnilist.provider.helper;

import android.support.annotation.MainThread;

import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.enums.ModelType;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.model.tools.Stats;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.provider.AttachmentsStore;
import me.shouheng.omnilist.provider.CategoryStore;
import me.shouheng.omnilist.provider.LocationsStore;
import me.shouheng.omnilist.provider.TimelineStore;
import me.shouheng.omnilist.provider.schema.TimelineSchema;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.viewmodel.StatisticViewModel;

/**
 * Created by wang shouheng on 2018/1/19.*/
public class StatisticsHelper {

    /**
     * Get all the stats prepared to show.
     *
     * todo add more stats according to Omni List
     *
     * @return the Stats object contains the actions result. */
    @MainThread
    public static Stats getStats() {
        Stats stats = new Stats();

        AssignmentsStore notesStore = AssignmentsStore.getInstance();
        stats.setTotalNotes(notesStore.getCount(null, Status.DELETED, true));
        stats.setArchivedNotes(notesStore.getCount(null, Status.ARCHIVED, false));
        stats.setTrashedNotes(notesStore.getCount(null, Status.TRASHED, false));

        CategoryStore categoryStore = CategoryStore.getInstance();
        stats.setTotalMinds(categoryStore.getCount(null, Status.DELETED, true));
        stats.setArchivedMinds(categoryStore.getCount(null, Status.ARCHIVED, false));
        stats.setTrashedMinds(categoryStore.getCount(null, Status.TRASHED, false));

        LocationsStore locationsStore = LocationsStore.getInstance();
        List<Location> locations = locationsStore.getDistinct(null, null);
        stats.setLocCnt(locations.size());
        stats.setLocations(locations);
        stats.setTotalLocations(locationsStore.getCount(null, Status.DELETED, true));

//        NotebookStore notebookStore = NotebookStore.getInstance(context);
//        stats.setTotalNotebooks(notebookStore.getCount(null, Status.TRASHED, false));

        AttachmentsStore attachmentsStore = AttachmentsStore.getInstance();
        List<Attachment> attachments = attachmentsStore.get(null, null);
        int images = 0, videos = 0, audioRecordings = 0, sketches = 0, files = 0;
        for (Attachment attachment : attachments) {
            if (Constants.MIME_TYPE_IMAGE.equals(attachment.getMineType())) {
                images++;
            } else if (Constants.MIME_TYPE_VIDEO.equals(attachment.getMineType())) {
                videos++;
            } else if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMineType())) {
                audioRecordings++;
            } else if (Constants.MIME_TYPE_SKETCH.equals(attachment.getMineType())) {
                sketches++;
            } else if (Constants.MIME_TYPE_FILES.equals(attachment.getMineType())) {
                files++;
            }
        }
        stats.setTotalAttachments(attachments.size());
        stats.setImages(images);
        stats.setVideos(videos);
        stats.setAudioRecordings(audioRecordings);
        stats.setSketches(sketches);
        stats.setFiles(files);

        stats.setNotesStats(getAddedStatistics(ModelType.ASSIGNMENT, StatisticViewModel.DAYS_OF_ADDED_MODEL));

        LogUtils.d(stats);
        return stats;
    }

    /**
     * Get the added statistic of model, this method can be refined by query one time and filter in program.
     *
     * @param modelType model type
     * @param days days of model statistic
     * @return the added count of every day */
    public static List<Integer> getAddedStatistics(ModelType modelType, int days) {
        Calendar sevenDaysAgo = TimeUtils.sevenDaysAgo();
        List<Integer> states = new LinkedList<>();
        for (int i=0; i<days; i++) {
            long startMillis = sevenDaysAgo.getTimeInMillis();
            sevenDaysAgo.add(Calendar.DAY_OF_YEAR, 1);
            long endMillis = sevenDaysAgo.getTimeInMillis();
            String whereSQL = TimelineSchema.ADDED_TIME + " >= " + startMillis
                    + " AND " + TimelineSchema.ADDED_TIME + " < " + endMillis
                    + " AND " + TimelineSchema.MODEL_TYPE + " = " + modelType.id
                    + " AND " + TimelineSchema.OPERATION + " = " + Operation.ADD.id;
            int count = TimelineStore.getInstance().getCount(whereSQL, Status.DELETED, true);
            states.add(count);
        }
        return states;
    }
}
