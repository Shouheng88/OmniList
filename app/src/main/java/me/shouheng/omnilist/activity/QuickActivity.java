package me.shouheng.omnilist.activity;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import org.polaric.colorful.BaseActivity;

import java.util.Collections;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.dialog.AttachmentPickerDialog;
import me.shouheng.omnilist.dialog.QuickEditorDialog;
import me.shouheng.omnilist.dialog.picker.CategoryPickerDialog;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.LockPreferences;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;
import me.shouheng.omnilist.viewmodel.CategoryViewModel;


public class QuickActivity extends BaseActivity implements OnAttachingFileListener {

    private final static int REQUEST_PASSWORD = 0x0016;

    private Category selectedCategory;

    private QuickEditorDialog quickEditorDialog;

    private AssignmentViewModel assignmentViewModel;
    private CategoryViewModel categoryViewModel;

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
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);

        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
        LogUtils.d("action:" + action);

        if (TextUtils.isEmpty(action)) {
            finish();
            return;
        }

        long categoryCode = 0;
        int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        Bundle extras = intent.getExtras();
        if (extras != null && extras.containsKey(Constants.INTENT_WIDGET)) {
            mAppWidgetId = extras.getInt(Constants.INTENT_WIDGET, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
                    Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
            categoryCode = sharedPreferences.getLong(
                    Constants.PREF_WIDGET_CATEGORY_CODE_PREFIX + String.valueOf(mAppWidgetId),
                    0);
        }

        if (categoryCode == 0) {
            /* Should show category picker dialog at first */
            ToastUtils.makeToast(R.string.widget_pick_category_at_first);
            showCategoryPicker();
        } else {
            /* Fetch category from database */
            fetchCategory(categoryCode);
        }
    }

    private void fetchCategory(long nbCode) {
        categoryViewModel.get(nbCode).observe(this, notebookResource -> {
            if (notebookResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                return;
            }
            switch (notebookResource.status) {
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_load_data);
                    break;
                case SUCCESS:
                    selectedCategory = notebookResource.data;
                    editMindSnagging();
                    break;
            }
        });
    }

    private void showCategoryPicker() {
        CategoryPickerDialog.newInstance()
                .setOnItemSelectedListener((dialog, category, position) -> {
                    selectedCategory = category;
                    editMindSnagging();
                    dialog.dismiss();
                })
                .show(getSupportFragmentManager(), "CATEGORY_PICKER");
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

                    private void finish() {
                        Intent intent = new Intent();
                        setResult(Activity.RESULT_OK, intent);
                        QuickActivity.this.finish();
                    }
                })
                .setOnAttachmentClickListener(this::resolveAttachmentClick)
                .setOnConfirmListener(this::saveAssignment)
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

    private void saveAssignment(String name, Attachment attachment) {
        // Fill the parent code
        Assignment assignment = ModelFactory.getAssignment();
        assignment.setCategoryCode(selectedCategory.getCode());
        // Save to database
        assignmentViewModel.saveAssignment(assignment, name, attachment)
                .observe(this, noteResource -> {
                    if (noteResource == null) {
                        ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                        return;
                    }
                    switch (noteResource.status) {
                        case SUCCESS:
                            ToastUtils.makeToast(R.string.text_save_successfully);
                            AppWidgetUtils.notifyAppWidgets(QuickActivity.this);
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
                .setRecordVisible(false)
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
