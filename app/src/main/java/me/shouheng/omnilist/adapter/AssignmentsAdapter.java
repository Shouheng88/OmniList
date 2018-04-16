package me.shouheng.omnilist.adapter;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.nineoldandroids.view.ViewHelper;

import java.util.Collections;
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

    private OnItemRemovedListener onItemRemovedListener;

    private Drawable cbFilled, cbOutline;

    private Assignment mJustDeletedToDoItem;
    private int mIndexOfDeletedToDoItem;

    private boolean isPositionChanged;

    private boolean isStateChanged;

    public AssignmentsAdapter(@Nullable List<Assignment> data) {
        super(R.layout.item_assignment, data);
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
        ViewHelper.setAlpha(helper.itemView,  completed ? 0.4F : 1F);

        TextView tvTitle = helper.getView(R.id.tv_title);
        TextView tvCreatedTime = helper.getView(R.id.tv_time_info);
        tvTitle.setPaintFlags(completed ? tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        tvCreatedTime.setPaintFlags(completed ? tvCreatedTime.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG : tvCreatedTime.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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

    public void setOnItemRemovedListener(OnItemRemovedListener onItemRemovedListener) {
        this.onItemRemovedListener = onItemRemovedListener;
    }

    public void addItemToPosition(Assignment item, int position) {
        addData(position, item);
        new Handler().postDelayed(this::notifyDataSetChanged, 500);
    }

    @Override
    public void addData(int position, @NonNull Assignment data) {
        super.addData(position, data);
        isPositionChanged = true;
    }

    @Override
    public void addData(@NonNull Assignment data) {
        super.addData(data);
        isPositionChanged = true;
    }

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
        isPositionChanged = true;
        if(fromPosition < toPosition){
            for(int i=fromPosition; i<toPosition; i++){
                Collections.swap(getData(), i, i+1);
            }
        } else{
            for(int i=fromPosition; i > toPosition; i--){
                Collections.swap(getData(), i, i-1);
            }
        }
        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onItemRemoved(int position, int direction) {
        isPositionChanged = true;
        mJustDeletedToDoItem =  getData().remove(position);
        mIndexOfDeletedToDoItem = position;
        notifyItemRemoved(position);
        if (onItemRemovedListener != null) {
            if (direction == ItemTouchHelper.END) {
                onItemRemovedListener.onItemRemovedRight(mJustDeletedToDoItem, mIndexOfDeletedToDoItem);
            } else if (direction == ItemTouchHelper.START) {
                onItemRemovedListener.onItemRemovedLeft(mJustDeletedToDoItem, mIndexOfDeletedToDoItem);
            }
        }
    }

    @Override
    public void afterMoved() {
        notifyDataSetChanged();
    }

    public boolean isStateChanged() {
        return isStateChanged;
    }

    public void setStateChanged(boolean stateChanged) {
        isStateChanged = stateChanged;
    }

    public boolean isPositionChanged() {
        return isPositionChanged;
    }

    public void setPositionChanged(boolean positionChanged) {
        isPositionChanged = positionChanged;
    }

    public void recoverItemToPosition(Assignment item, int position) {
        getData().add(position, item);
        notifyItemInserted(position);
    }

    public interface OnItemRemovedListener {
        void onItemRemovedLeft(Assignment item, int position);
        void onItemRemovedRight(Assignment item, int position);
    }
}
