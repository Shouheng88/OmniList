package me.shouheng.omnilist.adapter;

import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.nineoldandroids.view.ViewHelper;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.widget.tools.IItemTouchHelperAdapter;

// todo 1. remove assignment when complete
public class TodayAssignmentAdapter extends BaseMultiItemQuickAdapter<TodayAssignmentAdapter.MultiItem, BaseViewHolder>
        implements IItemTouchHelperAdapter {

    private OnItemRemovedListener onItemRemovedListener;

    private Drawable cbFilled, cbOutline;

    public TodayAssignmentAdapter(List<MultiItem> data) {
        super(data);
        addItemType(ViewType.HEADER.id, R.layout.item_title);
        addItemType(ViewType.NORMAL.id, R.layout.item_assignment);
    }

    @Override
    protected void convert(BaseViewHolder helper, MultiItem item) {
        switch (ViewType.getTypeById(helper.getItemViewType())) {
            case HEADER:
                helper.itemView.setBackgroundColor(ColorUtils.fadeColor(ColorUtils.primaryColor(), 0.8f));
                convertHeader(helper, item);
                break;
            case NORMAL:
                helper.itemView.setBackgroundResource(ColorUtils.isDarkTheme() ?
                        R.color.dark_theme_background : R.color.light_theme_background);
                convertAssignment(helper, item.assignment);
                break;
        }
    }

    private void convertHeader(BaseViewHolder helper, MultiItem item) {
        helper.setText(R.id.tv_section_title, item.title);
        helper.setTextColor(R.id.tv_section_title, ColorUtils.primaryColor());
    }

    private void convertAssignment(BaseViewHolder helper, Assignment assignment) {
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

    @Override
    public void onItemMoved(int fromPosition, int toPosition) {
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
        Assignment mJustDeletedToDoItem = getData().remove(position).assignment;
        notifyItemRemoved(position);
        if (onItemRemovedListener != null) {
            if (direction == ItemTouchHelper.END) {
                onItemRemovedListener.onItemRemovedRight(mJustDeletedToDoItem, position);
            } else if (direction == ItemTouchHelper.START) {
                onItemRemovedListener.onItemRemovedLeft(mJustDeletedToDoItem, position);
            }
        }
    }

    @Override
    public void afterMoved() {
        notifyDataSetChanged();
    }

    public void addItemToPosition(Assignment item, int position) {
        addData(position, new MultiItem(item));
        new Handler().postDelayed(this::notifyDataSetChanged, 500);
    }

    public void setOnItemRemovedListener(OnItemRemovedListener onItemRemovedListener) {
        this.onItemRemovedListener = onItemRemovedListener;
    }

    public static class MultiItem implements MultiItemEntity {

        ViewType viewType;

        public Assignment assignment;

        public String title;

        public MultiItem(String title) {
            this.title = title;
            this.viewType = ViewType.HEADER;
        }

        public MultiItem(Assignment assignment) {
            this.assignment = assignment;
            this.viewType = ViewType.NORMAL;
        }

        @Override
        public int getItemType() {
            return viewType.id;
        }
    }

    public List<Assignment> getAssignments() {
        List<Assignment> ret = new LinkedList<>();
        for (MultiItem multiItem : mData) {
            if (multiItem.viewType == ViewType.NORMAL) {
                ret.add(multiItem.assignment);
            }
        }
        return ret;
    }

    public interface OnItemRemovedListener {
        void onItemRemovedLeft(Assignment item, int position);
        void onItemRemovedRight(Assignment item, int position);
    }
}
