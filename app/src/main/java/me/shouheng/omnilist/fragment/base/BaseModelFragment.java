package me.shouheng.omnilist.fragment.base;

import android.app.Activity;
import android.arch.lifecycle.LiveData;
import android.content.Intent;
import android.databinding.ViewDataBinding;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog;

import org.polaric.colorful.PermissionUtils;

import java.io.File;
import java.io.IOException;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.config.TextLength;
import me.shouheng.omnilist.dialog.SimpleEditDialog;
import me.shouheng.omnilist.manager.LocationManager;
import me.shouheng.omnilist.manager.ShortcutHelper;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.Model;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.NetworkUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.viewmodel.BaseViewModel;
import me.shouheng.omnilist.widget.FlowLayout;


/**
 * Created by wangshouheng on 2017/9/3.*/
public abstract class BaseModelFragment<T extends Model, V extends ViewDataBinding> extends BaseFragment<V> {

    // region structure
    private boolean contentChanged = false;

    private boolean savedOrUpdated = false;

    protected void setContentChanged() {
        this.contentChanged = true;
    }

    protected boolean isContentChanged() {
        return contentChanged;
    }

    protected abstract T getModel();

    protected abstract BaseViewModel<T> getViewModel();

    protected boolean checkContent() {
        return true;
    }

    protected void beforeSaveOrUpdate(BeforePersistEventHandler handler) {
        if (handler != null) {
            handler.onGetEventResult(true);
        }
    }

    protected void doPersist(PersistEventHandler handler) {
        getViewModel().saveOrUpdate(getModel()).observe(this, tResource -> {
            if (tResource == null) {
                ToastUtils.makeToast(R.string.text_error_when_save);
                return;
            }
            switch (tResource.status) {
                case SUCCESS:
                    ToastUtils.makeToast(R.string.text_save_successfully);
                    updateState();
                    afterSaveOrUpdate();
                    if (handler != null) handler.onGetEventResult(true);
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_error_when_save);
                    if (handler != null) handler.onGetEventResult(false);
                    break;
            }
        });
    }

    protected void afterSaveOrUpdate() {
        AppWidgetUtils.notifyAppWidgets(getContext());
    }

    protected LiveData<Resource<Boolean>> isNewModel() {
        return getViewModel().isNewModel(getModel().getCode());
    }

    protected final void saveOrUpdateData(PersistEventHandler handler) {
        if (!checkContent()) {
            if (handler != null) {
                handler.onGetEventResult(false);
            }
            return;
        }

        beforeSaveOrUpdate(succeed -> {
            if (succeed) {
                doPersist(handler);
            }
        });
    }

    private void updateState() {
        contentChanged = false;
        savedOrUpdated = true;
    }

    protected final void onBack() {
        if (getActivity() == null) {
            // the activity is not attached
            LogUtils.e("Error! Activity is not attached when go back!");
            return;
        }

        CommonActivity activity = (CommonActivity) getActivity();
        if (isContentChanged()){
            new MaterialDialog.Builder(getContext())
                    .title(R.string.text_tips)
                    .content(R.string.text_save_or_discard)
                    .positiveText(R.string.text_save)
                    .negativeText(R.string.text_give_up)
                    .onPositive((materialDialog, dialogAction) -> {
                        if (!checkContent()){
                            return;
                        }
                        saveOrUpdateData(succeed -> setResult());
                    })
                    .onNegative((materialDialog, dialogAction) -> activity.superOnBackPressed())
                    .show();
        } else {
            setResult();
        }
    }

    protected final void setResult() {
        // If the activity is null, do nothing
        if (getActivity() == null) return;

        CommonActivity activity = (CommonActivity) getActivity();

        // The model didn't change.
        if (!savedOrUpdated) {
            activity.superOnBackPressed();
        }

        // If the argument has request code, return it, otherwise just go back
        Bundle args = getArguments();
        if (args != null && args.containsKey(Constants.EXTRA_REQUEST_CODE)){
            Intent intent = new Intent();
            intent.putExtra(Constants.EXTRA_MODEL, getModel());
            getActivity().setResult(Activity.RESULT_OK, intent);
            activity.superOnBackPressed();
        } else {
            activity.superOnBackPressed();
        }
    }

    public interface BeforePersistEventHandler {
        void onGetEventResult(boolean succeed);
    }

    public interface PersistEventHandler {
        void onGetEventResult(boolean succeed);
    }
    // endregion

    // region drawer
    protected void addShortcut() {
        if (getActivity() == null) return;

        isNewModel().observe(this, booleanResource -> {
            if (booleanResource == null) {
                LogUtils.e("Error! booleanResource is null when query is new model!");
                return;
            }
            LogUtils.d(booleanResource);
            switch (booleanResource.status) {
                case SUCCESS:
                    if (booleanResource.data != null && !booleanResource.data) {
                        ShortcutHelper.addShortcut(getActivity().getApplicationContext(), getModel());
                        ToastUtils.makeToast(R.string.successfully_add_shortcut);
                    } else {
                        new MaterialDialog.Builder(getContext())
                                .title(R.string.text_tips)
                                .content(R.string.text_save_and_retry_to_add_shortcut)
                                .positiveText(R.string.text_save_and_retry)
                                .negativeText(R.string.text_give_up)
                                .onPositive((materialDialog, dialogAction) -> saveOrUpdateData(succeed -> {
                                    if (succeed) {
                                        ShortcutHelper.addShortcut(getContext(), getModel());
                                        ToastUtils.makeToast(R.string.successfully_add_shortcut);
                                    }
                                })).show();
                    }
                    break;
            }
        });
    }

    protected void showColorPickerDialog(int titleRes) {
        if (!(getActivity() instanceof ContentActivity)) {
            throw new IllegalArgumentException("The associated activity must be content!");
        }
        new ColorChooserDialog.Builder((ContentActivity) getActivity(), titleRes)
                .preselect(primaryColor())
                .accentMode(false)
                .titleSub(titleRes)
                .backButton(R.string.text_back)
                .doneButton(R.string.done_label)
                .cancelButton(R.string.text_cancel)
                .show();
    }
    // endregion

    // region tags
    protected void showTagEditDialog() {
        SimpleEditDialog.newInstance("", tag -> {
            if (TextUtils.isEmpty(tag)){
                return;
            }
            if (tag.indexOf(';') != -1){
                ToastUtils.makeToast(R.string.illegal_label);
                return;
            }

            String tags = getTags();

            tags = tags == null ? "" : tags;
            tags = tags + tag + ";";
            if (tags.length() > TextLength.LABELS_TOTAL_LENGTH.length) {
                ToastUtils.makeToast(R.string.total_labels_too_long);
                return;
            }

            onGetTags(tags);

            addTagToLayout(tag);
        }).setMaxLength(TextLength.LABEL_TEXT_LENGTH.length).show(getFragmentManager(), "SHOW_ADD_LABELS_DIALOG");
    }

    protected void showTagsEditDialog() {
        SimpleEditDialog.newInstance(getTags() == null ? "" : getTags(),
                content -> {
                    content = content == null ? "" : content;
                    if (!content.endsWith(";")){
                        content = content + ";";
                    }

                    onGetTags(content);

                    addTagsToLayout(content);
                })
                .setMaxLength(TextLength.LABELS_TOTAL_LENGTH.length)
                .show(getFragmentManager(), "SHOW_LABELS_LAYOUT");
    }

    protected FlowLayout getTagsLayout(){
        return null;
    }

    protected String getTags() {
        return "";
    }

    protected void onGetTags(String tags) {}

    protected void addTagsToLayout(String stringTags) {
        if (getTagsLayout() == null) {
            return;
        }

        getTagsLayout().removeAllViews();
        if (stringTags == null){
            return;
        }
        String[] tags = stringTags.split(";");
        for (String tag : tags){
            addTagToLayout(tag);
        }
    }

    protected void addTagToLayout(String tag) {
        if (getTagsLayout() == null) {
            return;
        }

        TextView tvLabel = new TextView(getContext());
        int margin = ViewUtils.dp2Px(PalmApp.getContext(), 2f);
        tvLabel.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        ViewGroup.MarginLayoutParams params = new ViewGroup.MarginLayoutParams(tvLabel.getLayoutParams());
        params.setMargins(margin, margin, margin, margin);
        tvLabel.setLayoutParams(params);
        tvLabel.setPadding(ViewUtils.dp2Px(PalmApp.getContext(), 5f), 0, ViewUtils.dp2Px(PalmApp.getContext(), 5f), 0);
        tvLabel.setBackgroundResource(R.drawable.label_background);
        tvLabel.setText(tag);

        getTagsLayout().addView(tvLabel);
    }
    // endregion

    // region recording
    private MediaRecorder mRecorder;
    private MediaPlayer mPlayer = null;
    private String recordName;
    private boolean isRecording;
    protected long audioRecordingTimeStart;

    protected boolean isRecording() {
        return isRecording;
    }

    protected void prepareBeforeRecord() { }

    protected void startRecording() {
        prepareBeforeRecord();
        File file = FileHelper.createNewAttachmentFile(getContext(), Constants.MIME_TYPE_AUDIO_EXTENSION);
        if (file == null) {
            ToastUtils.makeToast(R.string.failed_to_create_file);
            return;
        }
        if (mRecorder == null) {
            mRecorder = new MediaRecorder();
            mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mRecorder.setAudioEncodingBitRate(96000);
            mRecorder.setAudioSamplingRate(44100);
        }
        recordName = file.getAbsolutePath();
        mRecorder.setOutputFile(recordName);
        try {
            audioRecordingTimeStart = System.currentTimeMillis();
            mRecorder.prepare();
            mRecorder.start();
            isRecording = true;
            ToastUtils.makeToast(R.string.recording);
        } catch (IOException | IllegalStateException e) {
            ToastUtils.makeToast(R.string.failed_to_record);
        }
    }

    protected void stopRecording() {
        if (mRecorder != null) {
            mRecorder.stop();
            long audioRecordingTime = System.currentTimeMillis() - audioRecordingTimeStart;
            mRecorder.release();
            mRecorder = null;
            isRecording = false;

            Attachment attachment = ModelFactory.getAttachment();
            attachment.setUri(Uri.fromFile(new File(recordName)));
            attachment.setMineType(Constants.MIME_TYPE_AUDIO);
            attachment.setLength(audioRecordingTime);

            onStopRecording(attachment);
        }
    }

    protected void onStopRecording(Attachment attachment) {}


    protected boolean isPlaying() {
        return mPlayer != null && mPlayer.isPlaying();
    }

    protected void startPlaying(Attachment attachment) {
        if (mPlayer == null) {
            mPlayer = new MediaPlayer();
        }
        try {
            mPlayer.setDataSource(getActivity(), attachment.getUri());
            mPlayer.prepare();
            mPlayer.start();
            onPlayingStateChanged(true);
            mPlayer.setOnCompletionListener(mp -> {
                mPlayer = null;
                onPlayingStateChanged(false);
            });
        } catch (IOException e) {
            ToastUtils.makeToast(R.string.failed_when_play_audio);
        }
    }

    protected void stopPlaying() {
        if (mPlayer != null) {
            onPlayingStateChanged(false);
            mPlayer.release();
            mPlayer = null;
        }
    }

    protected void onPlayingStateChanged(boolean isPlaying) {}
    // endregion

    // region locate
    protected void tryToLocate() {
        if (!NetworkUtils.isNetworkAvailable(getActivity())){
            ToastUtils.makeToast(R.string.check_network_availability);
            return;
        }
        if (getActivity() != null) {
            PermissionUtils.checkLocationPermission((CommonActivity) getActivity(), this::baiduLocate);
        }
    }

    private void baiduLocate() {
        ToastUtils.makeToast(R.string.trying_to_get_location);
        LocationManager.getInstance(getContext()).locate(bdLocation -> {
            if (bdLocation != null && !TextUtils.isEmpty(bdLocation.getCity())){
                Location location = ModelFactory.getLocation();
                location.setLongitude(bdLocation.getLongitude());
                location.setLatitude(bdLocation.getLatitude());
                location.setCountry(bdLocation.getCountry());
                location.setProvince(bdLocation.getProvince());
                location.setCity(bdLocation.getCity());
                location.setDistrict(bdLocation.getDistrict());
                onGetLocation(location);
            } else {
                ToastUtils.makeToast(R.string.failed_to_get_location);
            }
        });
    }

    protected void onGetLocation(Location location) {}
    // endregion
}
