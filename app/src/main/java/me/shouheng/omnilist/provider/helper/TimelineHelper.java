package me.shouheng.omnilist.provider.helper;

import android.support.annotation.Nullable;

import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.Model;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.model.Weather;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.provider.TimelineStore;

/**
 * Created by wangshouheng on 2017/11/3.*/
public class TimelineHelper {

    public static <T extends Model> void addTimeLine(T model, Operation operation) {
        if (!hasTimeLine(model, operation)) return;
        TimelineStore.getInstance().saveModel(ModelFactory.getTimeLine(model, operation));
    }

    @Nullable
    public static <T extends Model> TimeLine getTimeLine(T model, Operation operation) {
        if (!hasTimeLine(model, operation)) return null;
        return ModelFactory.getTimeLine(model, operation);
    }

    private static<T extends Model> boolean hasTimeLine(T model, Operation operation) {
        return model != null && (model instanceof Assignment
                || model instanceof SubAssignment
                || model instanceof Category
                || (model instanceof Alarm && Operation.ADD == operation)
                || (model instanceof Weather && Operation.ADD == operation)
                || (model instanceof Location && Operation.ADD == operation)
                || (model instanceof Attachment && Operation.ADD == operation));
    }
}
