package me.shouheng.omnilist.model.tools;

import java.util.Calendar;
import java.util.Date;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.config.TextLength;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.Model;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.model.Weather;
import me.shouheng.omnilist.model.enums.AlarmType;
import me.shouheng.omnilist.model.enums.AssignmentType;
import me.shouheng.omnilist.model.enums.ModelType;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.enums.Portrait;
import me.shouheng.omnilist.model.enums.Priority;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.model.enums.SubAssignmentType;
import me.shouheng.omnilist.model.enums.WeatherType;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.UserUtil;

/**
 * Created by wangshouheng on 2017/11/17. */
public class ModelFactory {

    private static long getLongCode() {
        return System.currentTimeMillis();
    }

    private static int getIntegerCode() {
        Calendar now = Calendar.getInstance();
        int appendix = now.get(Calendar.HOUR_OF_DAY) * 3600000
                + now.get(Calendar.MINUTE) * 60000
                + now.get(Calendar.SECOND) * 1000
                + now.get(Calendar.MILLISECOND);
        int prefix = (int)(Math.random() * 21); // 生成一个小于[0,20]的整数
        return prefix * 100000000 + appendix;
    }

    private static <T extends Model> T getModel(Class<T> itemType) {
        try {
            T newItem = itemType.newInstance();
            newItem.setCode(getLongCode());
            newItem.setUserId(UserUtil.getInstance(PalmApp.getContext()).getUserIdKept());
            newItem.setAddedTime(new Date());
            newItem.setLastModifiedTime(new Date());
            newItem.setLastSyncTime(new Date(0));
            newItem.setStatus(Status.NORMAL);
            return newItem;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Alarm getAlarm() {
        Calendar c = Calendar.getInstance();
        Alarm alarm = getModel(Alarm.class);

        assert alarm != null;
        alarm.setCode(getIntegerCode());

        alarm.setModelType(ModelType.NONE);
        alarm.setModelCode(0);

        alarm.setEnabled(true);
        alarm.setAlarmType(AlarmType.SPECIFIED_DATE);
        alarm.setNextTime(c);

        alarm.setDaysOfWeek(DaysOfWeek.getInstance(0));
        alarm.setDaysOfMonth(DaysOfMonth.getInstance(0));

        alarm.setStartDate(new Date());
        alarm.setEndDate(TimeUtils.getTomorrowDate().getTime());

        alarm.setHour(c.get(Calendar.HOUR_OF_DAY));
        alarm.setMinute(c.get(Calendar.MINUTE));

        return alarm;
    }

    public static Attachment getAttachment() {
        Attachment attachment = getModel(Attachment.class);
        assert attachment != null;
        attachment.setModelType(ModelType.NONE);
        attachment.setModelCode(0);
        return attachment;
    }

    public static Location getLocation() {
        Location location = getModel(Location.class);
        assert location != null;
        location.setModelType(ModelType.NONE);
        return location;
    }

    public static <T extends Model> TimeLine getTimeLine(T model, Operation operation) {
        TimeLine timeLine = new TimeLine();

        timeLine.setCode(ModelFactory.getLongCode());
        timeLine.setUserId(model.getUserId());
        timeLine.setAddedTime(new Date());
        timeLine.setLastModifiedTime(new Date());
        timeLine.setLastSyncTime(new Date(0));
        timeLine.setStatus(Status.NORMAL);

        timeLine.setOperation(operation);
        timeLine.setModelName(getModelName(model));
        timeLine.setModelCode(model.getCode());
        timeLine.setModelType(ModelType.getTypeByName(model.getClass()));
        return timeLine;
    }

    public static Feedback getFeedback() {
        return getModel(Feedback.class);
    }

    public static Category getCategory() {
        Category category = getModel(Category.class);
        assert category != null;
        category.setPortrait(Portrait.FOLDER);
        category.setCategoryOrder(0);
        // use the primary color as the category color
        category.setColor(ColorUtils.primaryColor());
        return category;
    }

    public static Weather getWeather(WeatherType type, int temperature) {
        Weather weather = getModel(Weather.class);
        assert weather != null;
        weather.setType(type);
        weather.setTemperature(temperature);
        return weather;
    }

    public static Assignment getAssignment(){
        Assignment assignment = getModel(Assignment.class);
        assert assignment != null;
        assignment.setPriority(Priority.LEVEL_03);
        assignment.setProgress(0);
        assignment.setAssignmentType(AssignmentType.NORMAL);
        assignment.setStartTime(new Date());
        assignment.setEndTime(new Date());
        assignment.setCompleteTime(new Date(0));
        assignment.setAssignmentOrder(0);
        return assignment;
    }

    public static SubAssignment getSubAssignment(){
        SubAssignment subAssignment = getModel(SubAssignment.class);
        assert subAssignment != null;
        subAssignment.setCompleted(false);
        subAssignment.setSubAssignmentType(SubAssignmentType.TODO);
        return subAssignment;
    }

    private static <M extends Model> String getModelName(M model) {
        String modelName = null;
        if (model instanceof Attachment) return ((Attachment) model).getUri().toString();
        else if (model instanceof Location) {
            Location location = ((Location) model);
            modelName = location.getCountry() + "|" + location.getCity() + "|" + location.getDistrict();
        }
        else if (model instanceof Weather) {
            Weather weather = ((Weather) model);
            modelName = PalmApp.getStringCompact(weather.getType().nameRes) + "|" + weather.getTemperature();
        }
        if (modelName != null && modelName.length() > TextLength.TIMELINE_TITLE_LENGTH.length) {
            return modelName.substring(0, TextLength.TIMELINE_TITLE_LENGTH.length);
        }
        return modelName;
    }
}
