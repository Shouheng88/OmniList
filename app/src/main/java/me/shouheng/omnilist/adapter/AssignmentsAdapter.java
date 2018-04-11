package me.shouheng.omnilist.adapter;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.nineoldandroids.view.ViewHelper;

import java.util.ArrayList;
import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.widget.tools.IItemTouchHelperAdapter;

/**
 * Created by wangshouheng on 2017/3/13. */
public class AssignmentsAdapter extends BaseQuickAdapter<Assignment, BaseViewHolder> implements
        IItemTouchHelperAdapter {
    private boolean fadeWhenCompleted = true;
    private boolean strikeWhenCompleted = true;

    private List<Assignment> selectedAssignments = new ArrayList<>();
    private boolean isStateChanged;

    private Drawable cbFilled, cbOutline;

    public AssignmentsAdapter(@Nullable List<Assignment> data) {
        super(R.layout.item_assignment, data);
    }

    // todo remove to fragment
    private void switchCompletedState(BaseViewHolder helper, Assignment assignment) {
        isStateChanged = true;
        if (assignment.getProgress() == 100) {
            assignment.setProgress(0);
            assignment.setInCompletedThisTime(true);
        } else {
            assignment.setProgress(100);
            assignment.setCompleteThisTime(true);
        }
        updateUIByCompletedState(helper, assignment);
        assignment.setChanged(!assignment.isChanged());
    }

    // todo
//    private void onAssignmentSelected(ViewHolder holder, Assignment assignment, int position) {
//        if (onItemSelectedListener != null) {
//            onItemSelectedListener.onItemSelected(assignment, position);
//        }
//        if (isSelectionMode) {
//            if (assignment.isSelected()) {
//                assignment.setSelected(false);
//                selectedAssignments.remove(assignment);
//                notifyItemChanged(position);
//            } else {
//                assignment.setSelected(true);
//                selectedAssignments.add(assignment);
//                notifyItemChanged(position);
//            }
//        }
//    }

    public void addItemToPosition(Assignment item, int position) {
        addData(position, item);
        new Handler().postDelayed(this::notifyDataSetChanged, 500);
    }

    @Override
    protected void convert(BaseViewHolder helper, Assignment assignment) {
        helper.itemView.setBackgroundColor(PalmApp.getColorCompact(ColorUtils.isDarkTheme() ? R.color.dark_theme_background : R.color.light_theme_background));

        helper.setText(R.id.tv_title, assignment.getName());

        updateUIByCompletedState(helper, assignment);

        helper.addOnClickListener(R.id.iv_completed);
        helper.addOnClickListener(R.id.rl_item);

        helper.setImageResource(R.id.iv_priority, assignment.getPriority().iconRes);

        String strCreatedTime = mContext.getString(R.string.text_last_modified_time) + ": " + TimeUtils.getPrettyTime(assignment.getLastModifiedTime());
        helper.setText(R.id.tv_time_info, strCreatedTime);

        helper.getView(R.id.iv_files).setVisibility(assignment.getAttachments() != 0 ? View.VISIBLE : View.GONE);
        helper.getView(R.id.iv_alarm).setVisibility(assignment.getAlarms() != 0 ? View.VISIBLE : View.GONE);
    }

    private void updateUIByCompletedState(BaseViewHolder helper, Assignment assignment) {
        boolean completed = assignment.getProgress() == 100;

        helper.setImageDrawable(R.id.iv_completed, completed ? cbFilled() : cbOutline());
        if (fadeWhenCompleted) ViewHelper.setAlpha(helper.itemView,  completed ? 0.4F : 1F);

        TextView tvTitle = helper.getView(R.id.tv_title);
        TextView tvCreatedTime = helper.getView(R.id.tv_time_info);
        if (strikeWhenCompleted) {
            tvTitle.setPaintFlags(completed ? tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            tvCreatedTime.setPaintFlags(completed ? tvCreatedTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : tvCreatedTime.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }
    }

    private Drawable cbFilled() {
        if (cbFilled == null) {
            cbFilled = ColorUtils.tintDrawable(mContext.getResources().getDrawable(R.drawable.ic_check_box_black_24dp), ColorUtils.accentColor());
        }
        return cbFilled;
    }

    private Drawable cbOutline() {
        if (cbOutline == null) {
            cbOutline = ColorUtils.tintDrawable(mContext.getResources().getDrawable(R.drawable.ic_check_box_outline_blank_black_24dp), ColorUtils.accentColor());
        }
        return cbOutline;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {

    }

    @Override
    public void onItemRemoved(int position) {

    }

    @Override
    public void afterMoved() {

    }
}
