package me.shouheng.omnilist.adapter;

import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.LogUtils;

/**
 * Created by wangshouheng on 2017/4/9.*/
public class AttachmentsAdapter extends BaseQuickAdapter<Attachment, BaseViewHolder> {

    private OnContextMenuClickedListener onContextMenuClickedListener;

    private boolean isContentChanged;

    public AttachmentsAdapter(@Nullable List<Attachment> data) {
        super(R.layout.item_attachment, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, Attachment attachment) {
        Uri thumbnailUri = FileHelper.getThumbnailUri(PalmApp.getContext(), attachment.getUri());
        Glide.with(PalmApp.getContext())
                .load(thumbnailUri)
                .centerCrop()
                .crossFade()
                .into((ImageView) helper.getView(R.id.iv_thumbnail));

        helper.setText(R.id.tv_title, FileHelper.getNameFromUri(PalmApp.getContext(), thumbnailUri));

        if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMineType())){
            helper.setImageResource(R.id.iv_thumbnail, attachment.isAudioPlaying() ? R.drawable.stop : R.drawable.play);
        }

        MenuItem.OnMenuItemClickListener listener = item -> {
            LogUtils.d(item.getItemId());
            switch (OnContextMenuClickedListener.AttachmentMenuItem.getItemById(item.getItemId())){
                case SHARE:
                    if (onContextMenuClickedListener != null) {
                        onContextMenuClickedListener.onItemClicked(OnContextMenuClickedListener.AttachmentMenuItem.SHARE, attachment);
                    }
                    break;
                case DELETE:
                    if (onContextMenuClickedListener != null) {
                        onContextMenuClickedListener.onItemClicked(OnContextMenuClickedListener.AttachmentMenuItem.DELETE, attachment);
                    }
                    break;
            }
            return true;
        };

        helper.itemView.setOnCreateContextMenuListener((menu, v, menuInfo) -> {
            menu.add(0, OnContextMenuClickedListener.AttachmentMenuItem.SHARE.itemId, 0, R.string.text_share).setOnMenuItemClickListener(listener);
            menu.add(0, OnContextMenuClickedListener.AttachmentMenuItem.DELETE.itemId, 1, R.string.text_delete).setOnMenuItemClickListener(listener);
        });
    }

    public void notifyPlayingStateChanged(int position, boolean playing){
        Attachment attachment = getData().get(position);
        if (attachment != null){
            attachment.setAudioPlaying(playing);
            notifyItemChanged(position);
        }
    }

    @Override
    public void addData(int position, @NonNull Attachment data) {
        super.addData(position, data);
        isContentChanged = true;
    }

    @Override
    public void addData(@NonNull Attachment data) {
        super.addData(data);
        isContentChanged = true;
    }

    @Override
    public void remove(int position) {
        super.remove(position);
        isContentChanged = true;
        new Handler().postDelayed(this::notifyDataSetChanged, 500);
    }

    public boolean isContentChanged() {
        return isContentChanged;
    }

    public void clearContentChange() {
        isContentChanged = false;
    }

    public void setOnContextMenuClickedListener(OnContextMenuClickedListener onContextMenuClickedListener) {
        this.onContextMenuClickedListener = onContextMenuClickedListener;
    }

    public interface OnContextMenuClickedListener {

        enum AttachmentMenuItem {
            DELETE(0),
            SHARE(1);

            public final int itemId;

            AttachmentMenuItem(int itemId) {
                this.itemId = itemId;
            }

            public static AttachmentMenuItem getItemById(int itemId) {
                for (AttachmentMenuItem type : values()){
                    if (type.itemId == itemId){
                        return type;
                    }
                }
                throw new IllegalArgumentException("illegal itemId");
            }
        }

        void onItemClicked(AttachmentMenuItem menuItem, Attachment data);
    }
}
