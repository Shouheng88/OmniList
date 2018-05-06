package me.shouheng.omnilist.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;
import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.apache.commons.io.FileUtils;
import org.polaric.colorful.BaseActivity;
import org.polaric.colorful.PermissionUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.adapter.AttachmentsAdapter;
import me.shouheng.omnilist.adapter.SubAssignmentsAdapter;
import me.shouheng.omnilist.async.CreateAttachmentTask;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.config.TextLength;
import me.shouheng.omnilist.databinding.FragmentAssignmentBinding;
import me.shouheng.omnilist.dialog.AttachmentPickerDialog;
import me.shouheng.omnilist.dialog.RatingsPickerDialog;
import me.shouheng.omnilist.dialog.ReminderPickerDialog;
import me.shouheng.omnilist.dialog.SimpleEditDialog;
import me.shouheng.omnilist.fragment.base.BaseModelFragment;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.manager.ModelHelper;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.enums.ModelType;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.model.enums.SubAssignmentType;
import me.shouheng.omnilist.model.tools.DaysOfWeek;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.provider.AlarmsStore;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.provider.AttachmentsStore;
import me.shouheng.omnilist.provider.LocationsStore;
import me.shouheng.omnilist.provider.SubAssignmentStore;
import me.shouheng.omnilist.provider.schema.AttachmentSchema;
import me.shouheng.omnilist.provider.schema.SubAssignmentSchema;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.IntentUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;
import me.shouheng.omnilist.viewmodel.AttachmentViewModel;
import me.shouheng.omnilist.viewmodel.BaseViewModel;
import me.shouheng.omnilist.viewmodel.LocationViewModel;
import me.shouheng.omnilist.widget.FlowLayout;
import me.shouheng.omnilist.widget.SlidingUpPanelLayout;
import me.shouheng.omnilist.widget.tools.CustomItemAnimator;
import me.shouheng.omnilist.widget.tools.CustomItemTouchHelper;
import me.shouheng.omnilist.widget.tools.IItemTouchHelperAdapter;
import me.shouheng.omnilist.widget.tools.SpaceItemDecoration;


public class AssignmentFragment extends BaseModelFragment<Assignment, FragmentAssignmentBinding> implements
        SubAssignmentsAdapter.OnItemRemovedListener, OnAttachingFileListener {

    private final static String EXTRA_IS_THIRD_PART = "extra_is_third_part";
    private final static String EXTRA_ACTION = "extra_action";

    private SubAssignmentsAdapter mAdapter;
    private AttachmentsAdapter attachmentsAdapter;

    private MaterialMenuDrawable materialMenu;

    private int playingPosition = -1;
    private boolean isSharingImage;

    private Assignment assignment;
    private Location location;
    private Alarm alarm;
    private List<SubAssignment> subAssignments = new LinkedList<>();
    private List<Attachment> attachments = new LinkedList<>();

    private AssignmentViewModel assignmentViewModel;
    private LocationViewModel locationViewModel;
    private AttachmentViewModel attachmentViewModel;

    public static AssignmentFragment newInstance(@NonNull Assignment assignment,
                                                 @Nullable Integer requestCode,
                                                 @Nullable String action,
                                                 boolean isThirdPart) {
        AssignmentFragment fragment = new AssignmentFragment();
        Bundle args = new Bundle();
        args.putBoolean(EXTRA_IS_THIRD_PART, isThirdPart);
        if (action != null) {
            args.putString(EXTRA_ACTION, action);
        }
        args.putSerializable(Constants.EXTRA_MODEL, assignment);
        if (requestCode != null) {
            args.putInt(Constants.EXTRA_REQUEST_CODE, requestCode);
        }
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_assignment;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        initViewModels();

        handleArguments();

        configToolbar();

        if (getArguments() != null && getArguments().getBoolean(EXTRA_IS_THIRD_PART)) {
            setContentChanged();
        }

        configMain(assignment);

        configDrawer(assignment);
    }

    private void initViewModels() {
        assignmentViewModel = ViewModelProviders.of(this).get(AssignmentViewModel.class);
        attachmentViewModel = ViewModelProviders.of(this).get(AttachmentViewModel.class);
        locationViewModel = ViewModelProviders.of(this).get(LocationViewModel.class);
    }

    // region handle arguments
    private void handleArguments() {
        Bundle arguments = getArguments();

        // Check arguments
        if (arguments == null
                || !arguments.containsKey(Constants.EXTRA_MODEL)
                || (assignment = (Assignment) arguments.getSerializable(Constants.EXTRA_MODEL)) == null) {
            ToastUtils.makeToast(R.string.text_no_such_assignment);
            if (getActivity() != null) getActivity().finish();
            return;
        }

        // Handle arguments for intent from third part
        if (arguments.getBoolean(EXTRA_IS_THIRD_PART)) {
            handleThirdPart();
        } else if(Constants.ACTION_ADD_SKETCH.equals(arguments.getString(EXTRA_ACTION))) {
            if (getActivity() != null) {
                PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () -> AttachmentHelper.sketch(this));
            }
        } else if (Constants.ACTION_TAKE_PHOTO.equals(arguments.getString(EXTRA_ACTION))) {
            if (getActivity() != null) {
                PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () -> AttachmentHelper.capture(this));
            }
        } else if (Constants.ACTION_ADD_FILES.equals(arguments.getString(EXTRA_ACTION))) {
            if (getActivity() != null) {
                PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () -> AttachmentHelper.pickFiles(this));
            }
        } else if (Constants.ACTION_ADD_RECORD.equals(arguments.getString(EXTRA_ACTION))) {
            if (getActivity() != null) {
                PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () ->
                        PermissionUtils.checkRecordPermission((BaseActivity) getActivity(), this::startRecording));
            }
        } else {
            // The cases above is new model, don't need to fetch data.
            fetchData(assignment);
        }
    }

    private void handleThirdPart() {
        if (!(getActivity() instanceof OnInteractionListener)) return;

        Intent intent = ((OnInteractionListener) getActivity()).getThirdPartIntent();

        String title = intent.getStringExtra(Intent.EXTRA_SUBJECT);
        assignment.setName(title);

        String comment = intent.getStringExtra(Intent.EXTRA_TEXT);
        int maxLength = TextLength.ASSIGNMENT_COMMENT_LENGTH.getLength();
        if (!TextUtils.isEmpty(comment) && comment.length() > maxLength) {
            comment = comment.substring(0, maxLength);
        }
        assignment.setComment(comment);

        // Single attachment data
        Uri uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);

        // Due to the fact that Google Now passes intent as text but with
        // audio recording attached the case must be handled in specific way
        if (uri != null && !Constants.INTENT_GOOGLE_NOW.equals(intent.getAction())) {
            new CreateAttachmentTask(this, uri, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

        // Multiple attachment data
        ArrayList<Uri> uris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (uris != null) {
            for (Uri uriSingle : uris) {
                new CreateAttachmentTask(this, uriSingle, this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        }
    }

    private void fetchData(Assignment assignment) {
        location = LocationsStore.getInstance().getLocation(assignment);
        subAssignments = SubAssignmentStore.getInstance().getSubAssignments(assignment, SubAssignmentSchema.SUB_ASSIGNMENT_ORDER);
        attachments = AttachmentsStore.getInstance().getAttachments(ModelType.ASSIGNMENT, assignment.getCode(), AttachmentSchema.ADDED_TIME);
        alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
    }

    private void updateOrders() {
        if (mAdapter.isPositionChanged()) {
            SubAssignmentStore.getInstance().updateOrders(mAdapter.getSubAssignments());
            mAdapter.setPositionChanged(false);
        }
    }

    private void updateAttachments() {
        if (attachmentsAdapter.isContentChanged()) {
            List<Attachment> attachments = attachmentsAdapter.getData();
            for (Attachment attachment : attachments){
                attachment.setModelCode(assignment.getCode());
                attachment.setModelType(ModelType.ASSIGNMENT);
            }
            attachmentViewModel.updateAttachments(assignment, attachments);
            attachmentsAdapter.clearContentChange();
        }
    }
    // endregion

    private void configToolbar() {
        if (getContext() == null || getActivity() == null) return;

        materialMenu = new MaterialMenuDrawable(getContext(), primaryColor(), MaterialMenuDrawable.Stroke.THIN);
        materialMenu.setIconState(MaterialMenuDrawable.IconState.ARROW);
        getBinding().main.toolbar.setNavigationIcon(materialMenu);
        ((AppCompatActivity) getActivity()).setSupportActionBar(getBinding().main.toolbar);
        ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle("");
            setStatusBarColor(getResources().getColor(isDarkTheme() ? R.color.dark_theme_foreground : R.color.md_grey_500));
        }
    }

    // region config main
    private void configMain(Assignment assignment) {
        getBinding().main.llRecord.setOnClickListener(v -> {
            if (isRecording()){
                stopRecording();
                ToastUtils.makeToast(R.string.recorded);
            }
        });

        mAdapter = new SubAssignmentsAdapter(SubAssignmentsAdapter.getMultiItems(subAssignments), assignment.getName(), assignment.getComment(), alarm);
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.tv_title:
                    showTitleEditor();
                    break;
                case R.id.ll_alarm:
                    showReminderPicker(alarm);
                    break;
                case R.id.ll_add_comment:
                    showCommentEditor();
                    break;
                case R.id.siv_clear:
                    showReminderNotice();
                    break;
                case R.id.ll_add_sub_assignment: {
                    SubAssignment subAssignment = ModelFactory.getSubAssignment();
                    subAssignment.setAssignmentCode(assignment.getCode());
                    showSubAssignmentEditor(subAssignment, null);
                    break;
                }
                case R.id.tv_sub_assignment_note:
                case R.id.tv_sub_assignment:
                    if (mAdapter.getItemViewType(position) == IItemTouchHelperAdapter.ViewType.NORMAL.id) {
                        showSubAssignmentEditor(Objects.requireNonNull(mAdapter.getItem(position)).subAssignment, position);
                    }
                    break;
                case R.id.iv_sub_assignment: {
                    SubAssignment subAssignment = Objects.requireNonNull(mAdapter.getItem(position)).subAssignment;
                    if (subAssignment.getSubAssignmentType() == SubAssignmentType.NOTE_WITH_PORTRAIT) {
                        // Do not handle click event for icon layout.
                        break;
                    }
                    if (subAssignment.isCompleted()) {
                        subAssignment.setCompleted(false);
                        subAssignment.setInCompletedThisTime(true);
                    } else {
                        subAssignment.setCompleted(true);
                        subAssignment.setCompleteThisTime(true);
                    }
                    subAssignment.setContentChanged(true);
                    setContentChanged();
                    mAdapter.notifyItemChanged(position);
                    break;
                }
            }
        });
        mAdapter.setOnItemClickListener((adapter, view, position) -> {
            LogUtils.e(position);
            if (mAdapter.getItemViewType(position) == IItemTouchHelperAdapter.ViewType.NORMAL.id) {
                showSubAssignmentEditor(Objects.requireNonNull(mAdapter.getItem(position)).subAssignment, position);
            }
        });
        mAdapter.setOnItemRemovedListener(this);

        getBinding().main.rvSubAssignments.setLayoutManager(new LinearLayoutManager(getActivity()));
        getBinding().main.rvSubAssignments.setItemAnimator(new CustomItemAnimator());
        getBinding().main.rvSubAssignments.setAdapter(mAdapter);

        ItemTouchHelper.Callback callback = new CustomItemTouchHelper(true, true, mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(getBinding().main.rvSubAssignments);

        attachmentsAdapter = new AttachmentsAdapter(attachments);
        attachmentsAdapter.setOnItemClickListener((adapter, view, position) -> {
            Attachment attachment = attachmentsAdapter.getData().get(position);
            switch (attachment.getMineType()) {
                case Constants.MIME_TYPE_AUDIO:
                    if (isPlaying()) {
                        if (position != playingPosition) {
                            stopPlaying();
                            playingPosition = position;
                            startPlaying(attachment);
                        } else {
                            stopPlaying();
                        }
                    } else {
                        playingPosition = position;
                        startPlaying(attachment);
                    }
                    break;
                default:
                    AttachmentHelper.resolveClickEvent(getContext(), attachment, attachments, assignment.getName());
            }
        });
        attachmentsAdapter.setOnContextMenuClickedListener((menuItem, attachment) -> {
            switch (menuItem) {
                case SHARE:
                    Intent shareIntent = new Intent(Intent.ACTION_SEND);
                    shareIntent.setType(FileHelper.getMimeType(PalmApp.getContext(), attachment.getUri()));
                    shareIntent.putExtra(Intent.EXTRA_STREAM, attachment.getUri());
                    if (IntentUtils.isAvailable(PalmApp.getContext(), shareIntent, null)) {
                        startActivity(shareIntent);
                    } else {
                        ToastUtils.makeToast(R.string.no_available_application_to_resolve_intent);
                    }
                    break;
                case DELETE:
                    int pos = attachmentsAdapter.getData().indexOf(attachment);
                    attachmentsAdapter.remove(pos);
                    break;
            }
        });

        int padding = ViewUtils.dp2Px(PalmApp.getContext(), 1);
        getBinding().main.rvFiles.setLayoutManager(new GridLayoutManager(getContext(), 3));
        getBinding().main.rvFiles.setItemAnimator(new CustomItemAnimator());
        getBinding().main.rvFiles.addItemDecoration(new SpaceItemDecoration(padding, padding, padding, padding));
        getBinding().main.rvFiles.setAttachmentsNumberTextView(getBinding().main.tvAttachmentNumber);
        getBinding().main.rvFiles.setEmptyView(getBinding().main.ivEmpty);
        getBinding().main.rvFiles.setAdapter(attachmentsAdapter);

        getBinding().main.sliding.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {}

            @Override
            public void onPanelCollapsed(View panel) {
                getBinding().main.ivDropUpDown.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp);
            }

            @Override
            public void onPanelExpanded(View panel) {
                getBinding().main.ivDropUpDown.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp);
            }

            @Override
            public void onPanelAnchored(View panel) {}

            @Override
            public void onPanelHidden(View panel) {}
        });
    }
    // endregion

    // region editor
    private void showTitleEditor() {
        new SimpleEditDialog.Builder()
                .setTitle(PalmApp.getStringCompact(R.string.edit_title))
                .setContent(TextUtils.isEmpty(assignment.getName()) ? "" : assignment.getName())
                .setSimpleAcceptListener(content -> {
                    if (TextUtils.isEmpty(content)){
                        return;
                    }
                    setContentChanged();
                    mAdapter.setTitle(content);
                    assignment.setName(content);
                })
                .setMaxLength(TextLength.ASSIGNMENT_TITLE_LENGTH.getLength())
                .build().show(Objects.requireNonNull(getFragmentManager()), "EDIT_ASSIGNMENT_TITLE");
    }

    private void showCommentEditor() {
        new SimpleEditDialog.Builder()
                .setTitle(PalmApp.getStringCompact(R.string.edit_comment))
                .setContent(TextUtils.isEmpty(assignment.getComment()) ? "" : assignment.getComment())
                .setSimpleAcceptListener(content -> {
                    if (TextUtils.isEmpty(content)){
                        mAdapter.setComment(getString(R.string.click_to_add_comments));
                        assignment.setComment("");
                        setContentChanged();
                        return;
                    }
                    setContentChanged();
                    mAdapter.setComment(content);
                    assignment.setComment(content);
                })
                .setMaxLength(TextLength.ASSIGNMENT_COMMENT_LENGTH.getLength())
                .build().show(Objects.requireNonNull(getFragmentManager()), "EDIT_ASSIGNMENT_COMMENTS");
    }

    private void showSubAssignmentEditor(SubAssignment subAssignment, @Nullable Integer position) {
        new SimpleEditDialog.Builder()
                .setTitle(getString(R.string.edit_sub_assignment))
                .setContent(subAssignment.getContent())
                .setSubAssignmentType(subAssignment.getSubAssignmentType())
                .setPortrait(subAssignment.getPortrait())
                .setMaxLength(TextLength.SUB_ASSIGNMENT_CONTENT_LENGTH.getLength())
                .setOnGetSubAssignmentListener((content, subAssignmentType, portrait) -> {
                    subAssignment.setContent(content);
                    subAssignment.setSubAssignmentType(subAssignmentType);
                    subAssignment.setPortrait(portrait);
                    subAssignment.setContentChanged(true);
                    if (position == null) {
                        subAssignment.setNewSubAssignment(true);
                        mAdapter.addData(mAdapter.getData().size() - 1, SubAssignmentsAdapter.getMultiItem(subAssignment));
                        getBinding().main.rvSubAssignments.smoothScrollToPosition(mAdapter.getData().size() - 1);
                    } else {
                        subAssignment.setNewSubAssignment(false);
                        mAdapter.notifyItemChanged(position);
                        getBinding().main.rvSubAssignments.smoothScrollToPosition(position);
                    }
                    setContentChanged();
                })
                .build().show(Objects.requireNonNull(getFragmentManager()), "EDIT_SUB_ASSIGNMENT");
    }

    private void showReminderPicker(Alarm paramAlarm) {
        paramAlarm = paramAlarm == null ? ModelFactory.getAlarm() : paramAlarm;
        paramAlarm.setModelCode(assignment.getCode());
        paramAlarm.setModelType(ModelType.ASSIGNMENT);

        new ReminderPickerDialog.Builder()
                .setAlarm(paramAlarm)
                .setOnReminderPickedListener(retAlarm -> {
                    if (AssignmentFragment.this.alarm == null){
                        AlarmsStore.getInstance().saveModel(retAlarm);
                    } else {
                        AlarmsStore.getInstance().update(retAlarm);
                        AlarmsManager.getsInstance().removeAlarm(retAlarm);
                    }
                    AlarmsManager.getsInstance().addAlarm(retAlarm);

                    AssignmentFragment.this.alarm = retAlarm;
                    assignment.setStartTime(retAlarm.getStartDate());
                    assignment.setEndTime(retAlarm.getEndDate());
                    assignment.setDaysOfWeek(retAlarm.getDaysOfWeek());
                    assignment.setNoticeTime(TimeUtils.getTimeInMillis(retAlarm.getHour(), retAlarm.getMinute()));
                    /*Here, the assignment may not be persisted*/
                    AssignmentsStore.getInstance().update(assignment);

                    mAdapter.setAlarm(alarm);

                    /*We should notify the user to persist assignment*/
                    setContentChanged();
                })
                .build().show(Objects.requireNonNull(getFragmentManager()), "REMINDER_PICKER");
    }

    private void showReminderNotice() {
        new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                .setTitle(R.string.text_tips)
                .setMessage(R.string.sure_to_remove_alarm)
                .setPositiveButton(R.string.text_confirm, (dialog, which) -> {
                    AlarmsStore.getInstance().update(alarm, Status.DELETED);
                    AlarmsManager.getsInstance().removeAlarm(alarm);
                    alarm = null;
                    mAdapter.setAlarm(null);

                    // Need to remove the alarm info from assignment
                    assignment.setNoticeTime(0);
                    assignment.setStartTime(TimeUtils.today());
                    assignment.setEndTime(TimeUtils.endToday());
                    assignment.setDaysOfWeek(DaysOfWeek.getInstance(0));

                    setContentChanged();

                    ToastUtils.makeToast(R.string.alarm_is_removed);
                })
                .setNegativeButton(R.string.text_cancel, null)
                .create()
                .show();
    }
    // endregion

    // region config drawer
    private void configDrawer(Assignment assignment) {
        if (isDarkTheme()) {
            getBinding().main.quickcontrolsContainer.setBackgroundResource(R.color.dark_theme_foreground);
            getBinding().drawer.drawerToolbar.setBackgroundColor(getResources().getColor(R.color.dark_theme_background));
            getBinding().drawer.drawerToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white_24dp);
            getBinding().drawer.getRoot().setBackgroundColor(getResources().getColor(R.color.dark_theme_background));
        }
        getBinding().drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        getBinding().drawer.drawerToolbar.setNavigationOnClickListener(v -> getBinding().drawerLayout.closeDrawer(GravityCompat.END));

        getBinding().drawer.mcs.setProgress(assignment.getProgress() / 5);
        getBinding().drawer.mcs.setOnProgressChangedListener((circularSeekBar, progress, fromUser) -> {
            if (assignment.getProgress() != progress) {
                assignment.setProgress(progress);
                setContentChanged();
            }
        });

        getBinding().drawer.tvTimeInfo.setText(ModelHelper.getTimeInfo(assignment));

        getBinding().drawer.flLabels.setOnClickListener(v -> showTagsEditDialog());
        getBinding().drawer.tvAddLabels.setOnClickListener(v -> showTagEditDialog());
        addTagsToLayout(assignment.getTags());

        getBinding().drawer.tvAddLocation.setOnClickListener(v -> tryToLocate());
        showLocationInfo();

        getBinding().drawer.tvCopyLink.setOnClickListener(v ->
                ModelHelper.copyLink(getActivity(), assignment));
        getBinding().drawer.tvCopyText.setOnClickListener(v -> {
            Activity activity = getActivity();
            if (activity != null) {
                ModelHelper.copyToClipboard(activity, getMarkdown());
                ToastUtils.makeToast(R.string.content_was_copied_to_clipboard);
            }
        });
        getBinding().drawer.tvAddToHomeScreen.setOnClickListener(v -> addShortcut());
        getBinding().drawer.tvStatistics.setOnClickListener(v -> ModelHelper.showStatistic(getContext(),
                assignment, mAdapter.getSubAssignments(), alarm, attachmentsAdapter.getData()));
        getBinding().drawer.tvSettings.setOnClickListener(null);
        getBinding().drawer.tvExport.setOnClickListener(v -> export());
    }

    private void share() {
        new BottomSheet.Builder(Objects.requireNonNull(getActivity()))
                .setStyle(isDarkTheme() ? R.style.BottomSheet_Dark : R.style.BottomSheet)
                .setMenu(ColorUtils.getThemedBottomSheetMenu(getContext(), R.menu.share))
                .setTitle(R.string.text_share)
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown(@NonNull BottomSheet bottomSheet, @Nullable Object o) {}

                    @Override
                    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem, @Nullable Object o) {
                        switch (menuItem.getItemId()) {
                            case R.id.action_share_text:
                                ModelHelper.share(getContext(), assignment.getName(), getMarkdown(), attachmentsAdapter.getData());
                                break;
                            case R.id.action_share_html:
                                viewHtml();
                                break;
                            case R.id.action_share_image:
                                isSharingImage = true;
                                createScreenCapture(getBinding().main.rvSubAssignments);
                                break;
                        }
                    }

                    @Override
                    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
                })
                .show();
    }

    private void export() {
        new BottomSheet.Builder(Objects.requireNonNull(getActivity()))
                .setStyle(isDarkTheme() ? R.style.BottomSheet_Dark : R.style.BottomSheet)
                .setMenu(ColorUtils.getThemedBottomSheetMenu(getContext(), R.menu.export))
                .setTitle(R.string.text_export)
                .setListener(new BottomSheetListener() {
                    @Override
                    public void onSheetShown(@NonNull BottomSheet bottomSheet, @Nullable Object o) {}

                    @Override
                    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem, @Nullable Object o) {
                        switch (menuItem.getItemId()) {
                            case R.id.export_html:
                                viewHtml();
                                break;
                            case R.id.capture:
                                isSharingImage = false;
                                createScreenCapture(getBinding().main.rvSubAssignments);
                                break;
                            case R.id.print:
                                viewHtml();
                                break;
                            case R.id.export_text:
                                outText();
                                break;
                        }
                    }

                    @Override
                    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
                })
                .show();
    }

    private void viewHtml() {
        ArrayList<Attachment> attachments = new ArrayList<>(attachmentsAdapter.getData());
        ContentActivity.viewAssignment(this, assignment, attachments, getMarkdown());
    }

    private void outText() {
        try {
            File exDir = FileHelper.getTextExportDir();
            File outFile = new File(exDir, FileHelper.getDefaultFileName(".md"));
            FileUtils.writeStringToFile(outFile, getMarkdown(), "utf-8");
            ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), outFile.getPath()));
        } catch (IOException e) {
            ToastUtils.makeToast(R.string.failed_to_create_file);
        }
    }

    private String getMarkdown() {
        return ModelHelper.getMarkdown(
                assignment,
                mAdapter.getSubAssignments(),
                location,
                alarm,
                attachmentsAdapter.getData()
        );
    }

    @Override
    protected void onGetScreenCutFile(File file) {
        if (isSharingImage) {
            ModelHelper.shareFile(getContext(), file, Constants.MIME_TYPE_IMAGE);
        }
    }
    // endregion

    // region tags
    protected String getTags() {
        return assignment.getTags();
    }

    @Override
    protected void onGetTags(String tags) {
        super.onGetTags(tags);
        assignment.setTags(tags);
        setContentChanged();
    }

    @Override
    protected FlowLayout getTagsLayout() {
        return getBinding().drawer.flLabels;
    }
    // endregion

    // region record
    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - audioRecordingTimeStart;
            getBinding().main.tvMinutes.setText(TimeUtils.getRecordTime(millis));
            if (isRecording()) handler.postDelayed(runnable, 1000);
        }
    };

    @Override
    protected void prepareBeforeRecord() {
        super.prepareBeforeRecord();
        getBinding().main.llRecord.setVisibility(View.VISIBLE);
        handler.postDelayed(runnable, 1000);
        getBinding().main.tvMinutes.setText("");
    }

    @Override
    protected void onPlayingStateChanged(boolean isPlaying) {
        super.onPlayingStateChanged(isPlaying);
        if (playingPosition != -1) {
            attachmentsAdapter.notifyPlayingStateChanged(playingPosition, isPlaying);
        }
        if (!isPlaying) {
            playingPosition = -1;
        }
    }

    @Override
    protected void onStopRecording(Attachment attachment) {
        super.onStopRecording(attachment);
        onGetAttachment(attachment);
        getBinding().main.llRecord.setVisibility(View.GONE);
    }
    // endregion

    // region location
    @Override
    protected void onGetLocation(Location location) {
        location.setModelCode(assignment.getCode());
        location.setModelType(ModelType.ASSIGNMENT);
        this.location = location;
        showLocationInfo();
        locationViewModel.saveModel(location);
    }

    private void showLocationInfo() {
        if (location == null) {
            getBinding().drawer.tvLocationInfo.setVisibility(View.GONE);
            return;
        }
        getBinding().drawer.tvLocationInfo.setVisibility(View.VISIBLE);
        getBinding().drawer.tvLocationInfo.setText(ModelHelper.getFormatLocation(location));
    }
    // endregion

    // region attachment
    private void showAttachmentPicker() {
        new AttachmentPickerDialog.Builder(this)
                .setAddLinkVisible(false)
                .setFilesVisible(true)
                .setOnPickAudioSelectedListener(() -> {
                    if (getActivity() != null) {
                        PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () ->
                                PermissionUtils.checkRecordPermission((BaseActivity) getActivity(), this::startRecording));
                    } else {
                        LogUtils.e("Activity is detached.");
                    }
                }).build()
                .show(Objects.requireNonNull(getFragmentManager()), "ATTACHMENT PICKER");
    }

    @Override
    protected void onGetAttachment(@NonNull Attachment attachment) {
        if (attachmentsAdapter.getData().size() >= TextLength.MAX_ATTACHMENT_NUMBER.getLength()) {
            ToastUtils.makeToast(R.string.arrive_max_attachments_number);
        } else {
            setContentChanged();
            attachmentsAdapter.addData(attachment);
            ToastUtils.makeToast(R.string.text_save_successfully);
        }
    }

    @Override
    protected void onFailedGetAttachment(Attachment attachment) {
        ToastUtils.makeToast(R.string.failed_to_save_attachment);
        int index;
        if ((index = attachmentsAdapter.getData().indexOf(attachment)) != -1) {
            attachmentsAdapter.remove(index);
        }
    }
    // endregion

    // region options menu
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (assignment.getPriority() != null) {
            if (isDarkTheme()){
                menu.findItem(R.id.action_more).setIcon(R.drawable.ic_more_vert_white);
                menu.findItem(R.id.action_files).setIcon(R.drawable.ic_attach_file_white);
                menu.findItem(R.id.action_share).setIcon(R.drawable.ic_share_white);
            }
            menu.findItem(R.id.action_rate).setIcon(assignment.getPriority().iconRes);
        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.assignment_edit, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getBinding().main.sliding.isPanelExpanded()) {
                    getBinding().main.sliding.collapsePanel();
                    return true;
                }
                if (getBinding().drawerLayout.isDrawerOpen(GravityCompat.END)) {
                    getBinding().drawerLayout.closeDrawer(GravityCompat.END);
                    return true;
                }
                if (isRecording()) {
                    stopRecording();
                    return true;
                }
                if (isContentChanged()) {
                    saveOrUpdateData(null);
                } else {
                    setResult();
                }
                break;
            case R.id.action_files:
                if (attachmentsAdapter.getData().size() >= TextLength.MAX_ATTACHMENT_NUMBER.getLength()) {
                    ToastUtils.makeToast(R.string.arrive_max_attachments_number);
                    break;
                }
                showAttachmentPicker();
                break;
            case R.id.action_rate:
                RatingsPickerDialog.newInstance(priority -> {
                    setContentChanged();
                    assignment.setPriority(priority);
                    if (getActivity() != null) {
                        getActivity().invalidateOptionsMenu();
                    }
                }).show(Objects.requireNonNull(getFragmentManager()), "PRIORITIES_PICKER");
                break;
            case R.id.action_more:
                getBinding().drawerLayout.openDrawer(GravityCompat.END, true);
                break;
            case R.id.action_share:
                share();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    // region editor structure
    @Override
    protected void setContentChanged() {
        if (!isContentChanged()) {
            super.setContentChanged();
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.CHECK);
        }
    }

    @Override
    protected boolean checkContent() {
        if (TextUtils.isEmpty(mAdapter.getTitle())) {
            assignment.setName(ModelHelper.getDefaultTitle());
        }
        return super.checkContent();
    }

    @Override
    protected Assignment getModel() {
        return assignment;
    }

    @Override
    protected BaseViewModel<Assignment> getViewModel() {
        return assignmentViewModel;
    }

    @Override
    protected void beforeSaveOrUpdate(BeforePersistEventHandler handler) {
        assignment.setSubAssignments(mAdapter.getSubAssignments());
        super.beforeSaveOrUpdate(handler);
    }

    @Override
    protected void afterSaveOrUpdate() {
        super.afterSaveOrUpdate();
        materialMenu.animateIconState(MaterialMenuDrawable.IconState.ARROW);
        /*This event is sent for third event handler, since we can't return result for caller*/
        Intent intent;
        if (getActivity() instanceof OnInteractionListener
                && (intent = ((OnInteractionListener) getActivity()).getThirdPartIntent()) != null
                && Constants.ACTION_ADD_FROM_THIRD_PART.equals(intent.getAction())) {
            sendDataSetChangeBroadcast();
        }
    }

    @Override
    public void onBackPressed() {
        if (getBinding().main.sliding.isPanelExpanded()) {
            getBinding().main.sliding.collapsePanel();
            return;
        }
        if (getBinding().drawerLayout.isDrawerOpen(GravityCompat.END)) {
            getBinding().drawerLayout.closeDrawer(GravityCompat.END);
            return;
        }
        if (isRecording()) {
            stopRecording();
            return;
        }
        onBack();
    }

    @Override
    public void onPause() {
        updateOrders();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        updateAttachments();
        if (isRecording()) {
            stopRecording();
        }
        if (isPlaying()) {
            stopPlaying();
        }
        super.onDestroy();
    }

    @Override
    public void onItemRemoved(SubAssignmentsAdapter.MultiItem item, int position) {
        SubAssignmentStore.getInstance().update(item.subAssignment, Status.DELETED);
        Snackbar.make(getBinding().drawerLayout, R.string.sub_assignment_delete_msg, Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.text_undo), v -> {
                    mAdapter.recoverItemToPosition(item.subAssignment, position);
                    SubAssignmentStore.getInstance().update(item.subAssignment, Status.NORMAL);
                }).show();
    }
    // endregion

    public interface OnInteractionListener {
        Intent getThirdPartIntent();
    }

    /**
     * Send a broadcast to notice the caller that the data set has been changed. You can refer to
     * the method {@link ContentActivity#resolveThirdPart(Activity, Intent, int)} that we used
     * {@link Intent#setClass(Context, Class)} to set the direction for intent, so we can't call
     * {@link Activity#startActivityForResult(Intent, int)}. That is the reason that we can't return
     * result to caller. So, I decided to use broadcast to notify callers that data set has changed,
     * update ui accordingly. */
    private void sendDataSetChangeBroadcast() {
        Intent intent = new Intent();
        intent.setAction(Constants.ACTION_DATA_SET_CHANGE_BROADCAST);
        if (getContext() != null) getContext().sendBroadcast(intent);
    }
}