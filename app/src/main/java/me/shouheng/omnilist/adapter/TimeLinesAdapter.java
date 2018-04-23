package me.shouheng.omnilist.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.preferences.UserPreferences;
import me.shouheng.omnilist.widget.CircleImageView;
import me.shouheng.omnilist.widget.Timeline;


/**
 * Created by wangshouheng on 2017/8/19. */
public class TimeLinesAdapter extends BaseQuickAdapter<TimeLine, BaseViewHolder> {

    private Context context;

    private Drawable atomDrawable;

    public TimeLinesAdapter(Context context, @Nullable List<TimeLine> data) {
        super(R.layout.item_time_line, data);
        this.context = context;
    }

    @Override
    protected void convert(BaseViewHolder helper, TimeLine timeLine) {
        helper.setText(R.id.tv, getOperation(timeLine));
        helper.setImageResource(R.id.iv_operation, timeLine.getModelType().drawableRes);
        ((CircleImageView) helper.getView(R.id.civ)).setFillingCircleColor(
                UserPreferences.getInstance().getTimeLineColor(timeLine.getOperation()));
        helper.setText(R.id.tv_date, TimeUtils.getShortDate(context, timeLine.getAddedTime()));
        helper.setText(R.id.tv_time, TimeUtils.shortTime(timeLine.getAddedTime()));
        helper.setText(R.id.tv_sub, timeLine.getModelName());
        helper.setTextColor(R.id.tv_sub, ColorUtils.accentColor());
        ((Timeline) helper.getView(R.id.timeLine)).setAtomDrawable(atomDrawable());
    }

    private Drawable atomDrawable() {
        if (atomDrawable == null) {
            atomDrawable = ColorUtils.tintDrawable(
                    PalmApp.getDrawableCompact(R.drawable.solid_circle_green),
                    ColorUtils.primaryColor());
        }
        return atomDrawable;
    }

    private String getOperation(TimeLine timeLine) {
        return context.getString(timeLine.getOperation().operationName)
                + " " + context.getString(timeLine.getModelType().typeName) + " : ";
    }
}
