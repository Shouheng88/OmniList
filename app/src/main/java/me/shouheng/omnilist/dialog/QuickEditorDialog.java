package me.shouheng.omnilist.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;

import com.bumptech.glide.Glide;

import java.io.IOException;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.DialogQuickEditorLayoutBinding;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.ToastUtils;


/**
 * Created by wangshouheng on 2017/8/19. */
public class QuickEditorDialog extends DialogFragment {

    private String title;
    private String content;
    private Attachment attachment;

    private MediaPlayer mPlayer;

    private OnConfirmListener onConfirmListener;
    private OnAddAttachmentClickListener onAddAttachmentClickListener;
    private OnAttachmentClickListener onAttachmentClickListener;
    private OnLifeMethodCalledListener onLifeMethodCalledListener;

    private DialogQuickEditorLayoutBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()),
                R.layout.dialog_quick_editor_layout, null, false);

        binding.wtv.bindEditText(binding.et);
        binding.et.setText(content);
        binding.et.addTextChangedListener(new EtTextWatcher());
        binding.iv.setOnClickListener(v -> {
            if (onAddAttachmentClickListener != null) {
                onAddAttachmentClickListener.onAddAttachmentClick();
            }
        });

        setAttachment(attachment);
        initButtons();

        binding.bottom.btnNegative.setOnClickListener(v -> dismiss());
        binding.bottom.btnPositive.setOnClickListener(v -> {
            if (onConfirmListener != null){
                onConfirmListener.onConfirm(binding.et.getText().toString(), attachment);
            }
            dismiss();
        });
        binding.bottom.btnNeutral.setOnClickListener(v -> {
            if (onAddAttachmentClickListener != null) {
                onAddAttachmentClickListener.onAddAttachmentClick();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setTitle(title)
                .setView(binding.getRoot())
                .create();
    }

    public void setBuilder(Builder builder) {
        this.title = builder.title;
        this.content = builder.content;
        this.attachment = builder.attachment;
        this.onConfirmListener = builder.onConfirmListener;
        this.onAddAttachmentClickListener = builder.onAddAttachmentClickListener;
        this.onAttachmentClickListener = builder.onAttachmentClickListener;
        this.onLifeMethodCalledListener = builder.onLifeMethodCalledListener;
    }

    private void initButtons() {
        binding.bottom.btnNegative.setTextColor(ColorUtils.accentColor());

        binding.bottom.btnPositive.setTextColor(Color.GRAY);
        binding.bottom.btnPositive.setEnabled(false);

        if (attachment == null) {
            binding.bottom.btnNeutral.setTextColor(ColorUtils.accentColor());
        } else {
            binding.bottom.btnNeutral.setEnabled(false);
            binding.bottom.btnNeutral.setTextColor(Color.GRAY);
        }
    }

    private class EtTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            setCancelable(false);
            if (s.length() == 0 && attachment == null) {
                binding.bottom.btnPositive.setEnabled(false);
                binding.bottom.btnPositive.setTextColor(Color.GRAY);
            } else {
                if (!binding.bottom.btnPositive.isEnabled()) {
                    binding.bottom.btnPositive.setEnabled(true);
                    binding.bottom.btnPositive.setTextColor(ColorUtils.accentColor());
                }
            }
        }
    }

    public void setAttachment(final Attachment attachment) {
        if (attachment == null) return;

        // This means the user added a new attachment
        if (this.attachment == null) setCancelable(false);
        this.attachment = attachment;

        binding.bottom.btnNeutral.setEnabled(false);
        binding.bottom.btnNeutral.setTextColor(Color.GRAY);
        binding.bottom.btnPositive.setEnabled(true);
        binding.bottom.btnPositive.setTextColor(ColorUtils.accentColor());

        binding.iv.setVisibility(View.GONE);
        binding.siv.setVisibility(View.VISIBLE);

        if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMineType())){
            binding.siv.setImageResource(attachment.isAudioPlaying() ? R.drawable.stop : R.drawable.play);
        } else {
            Uri thumbnailUri = FileHelper.getThumbnailUri(getContext(), attachment.getUri());
            Glide.with(PalmApp.getContext())
                    .load(thumbnailUri)
                    .centerCrop()
                    .crossFade()
                    .into(binding.siv);
        }

        binding.siv.setOnClickListener(v -> {
            if (Constants.MIME_TYPE_AUDIO.equals(attachment.getMineType())) {
                if (isPlaying()) {
                    stopPlaying();
                } else {
                    startPlaying(attachment);
                }
            }
            if (onAttachmentClickListener != null) {
                onAttachmentClickListener.onClick(attachment);
            }
        });
    }

    private boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    private void startPlaying(Attachment attachment) {
        if (mPlayer == null) mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource(getContext(), attachment.getUri());
            mPlayer.prepare();
            mPlayer.start();
            notifyPlayingStateChanged(true);
            mPlayer.setOnCompletionListener(mp -> {
                mPlayer = null;
                notifyPlayingStateChanged(false);
            });
        } catch (IOException e) {
            ToastUtils.makeToast(R.string.failed_when_play_audio);
        }
    }

    private void stopPlaying() {
        if (mPlayer != null) {
            notifyPlayingStateChanged(false);
            mPlayer.release();
            mPlayer = null;
        }
    }

    private void notifyPlayingStateChanged(boolean playing) {
        if (attachment != null){
            attachment.setAudioPlaying(playing);
            binding.siv.setImageResource(attachment.isAudioPlaying() ? R.drawable.stop : R.drawable.play);
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        if (isPlaying()) stopPlaying();
        if (onLifeMethodCalledListener != null) {
            onLifeMethodCalledListener.onCancel();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (isPlaying()) stopPlaying();
        if (onLifeMethodCalledListener != null) {
            onLifeMethodCalledListener.onDismiss();
        }
    }

    public static class Builder {
        private String title;
        private String content;
        private Attachment attachment;

        private OnConfirmListener onConfirmListener;
        private OnAddAttachmentClickListener onAddAttachmentClickListener;
        private OnAttachmentClickListener onAttachmentClickListener;
        private OnLifeMethodCalledListener onLifeMethodCalledListener;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setAttachment(Attachment attachment) {
            this.attachment = attachment;
            return this;
        }

        public Builder setOnAddAttachmentClickListener(OnAddAttachmentClickListener onAddAttachmentClickListener) {
            this.onAddAttachmentClickListener = onAddAttachmentClickListener;
            return this;
        }

        public Builder setOnConfirmListener(OnConfirmListener onConfirmListener) {
            this.onConfirmListener = onConfirmListener;
            return this;
        }

        public Builder setOnAttachmentClickListener(OnAttachmentClickListener onAttachmentClickListener) {
            this.onAttachmentClickListener = onAttachmentClickListener;
            return this;
        }

        public Builder setOnLifeMethodCalledListener(OnLifeMethodCalledListener onLifeMethodCalledListener) {
            this.onLifeMethodCalledListener = onLifeMethodCalledListener;
            return this;
        }

        public QuickEditorDialog build() {
            QuickEditorDialog dialog = new QuickEditorDialog();
            dialog.setBuilder(this);
            return dialog;
        }
    }

    public interface OnConfirmListener {
        void onConfirm(String content, Attachment attachment);
    }

    public interface OnAddAttachmentClickListener {
        void onAddAttachmentClick();
    }

    public interface OnAttachmentClickListener {
        void onClick(Attachment attachment);
    }

    public interface OnLifeMethodCalledListener {
        void onCancel();
        void onDismiss();
    }
}
