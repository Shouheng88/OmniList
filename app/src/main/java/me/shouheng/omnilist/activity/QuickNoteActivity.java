package me.shouheng.omnilist.activity;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import org.polaric.colorful.BaseActivity;

import java.util.Collections;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.dialog.AttachmentPickerDialog;
import me.shouheng.omnilist.dialog.QuickEditorDialog;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.LockPreferences;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;


public class QuickNoteActivity extends BaseActivity implements OnAttachingFileListener {

    private final static int REQUEST_PASSWORD = 0x0016;

    private QuickEditorDialog quickEditorDialog;

    private AssignmentViewModel assignmentViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (LockPreferences.getInstance().isPasswordRequired() && !PalmApp.isPasswordChecked()) {
            LockActivity.requireLaunch(this, REQUEST_PASSWORD);
        } else {
            init();
        }
    }

    private void init() {
        assignmentViewModel = ViewModelProviders.of(this).get(AssignmentViewModel.class);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        LogUtils.d("action:" + action);

        if (TextUtils.isEmpty(action)) {
            finish();
            return;
        }

        editMindSnagging();
    }

    private void editMindSnagging() {
        quickEditorDialog = new QuickEditorDialog.Builder()
                .setTitle(getString(R.string.quick_edit_assignment))
                .setContent("")
                .setOnAddAttachmentClickListener(this::showAttachmentPicker)
                .setOnLifeMethodCalledListener(new QuickEditorDialog.OnLifeMethodCalledListener() {
                    @Override
                    public void onCancel() {
                        finish();
                    }

                    @Override
                    public void onDismiss() {
                        finish();
                    }
                })
                .setOnAttachmentClickListener(this::resolveAttachmentClick)
                .setOnConfirmListener(this::saveMindSnagging)
                .build();
        quickEditorDialog.show(getSupportFragmentManager(), "MIND SNAGGING");
    }

    private void resolveAttachmentClick(Attachment attachment) {
        AttachmentHelper.resolveClickEvent(
                this,
                attachment,
                Collections.singletonList(attachment),
                attachment.getName());
    }

    private void saveMindSnagging(String content, Attachment attachment) {
        assignmentViewModel.saveAssignment(ModelFactory.getAssignment(), content, attachment)
                .observe(this, noteResource -> {
                    if (noteResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                        return;
                    }
                    switch (noteResource.status) {
                        case SUCCESS:
                            ToastUtils.makeToast(R.string.text_save_successfully);
                            AppWidgetUtils.notifyAppWidgets(QuickNoteActivity.this);
                            break;
                        case FAILED:
                            ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                            break;
                    }
                });
    }

    private void showAttachmentPicker() {
        new AttachmentPickerDialog.Builder()
                .setAddLinkVisible(false)
                .setRecordVisible(true)
                .setVideoVisible(true)
                .build().show(getSupportFragmentManager(), "ATTACHMENT PICKER");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            AttachmentHelper.resolveResult(this, requestCode, data);
        }
        switch (requestCode) {
            case REQUEST_PASSWORD:
                if (resultCode == RESULT_OK) {
                    init();
                } else {
                    finish();
                }
                break;
        }
    }

    @Override
    public void onAttachingFileErrorOccurred(Attachment attachment) {
        ToastUtils.makeToast(R.string.failed_to_save_attachment);
    }

    @Override
    public void onAttachingFileFinished(Attachment attachment) {
        if (AttachmentHelper.checkAttachment(attachment) && quickEditorDialog != null) {
            quickEditorDialog.setAttachment(attachment);
        }
    }
}
