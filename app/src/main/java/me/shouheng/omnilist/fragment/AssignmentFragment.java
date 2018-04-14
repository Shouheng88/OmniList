package me.shouheng.omnilist.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.balysv.materialmenu.MaterialMenuDrawable;

import org.polaric.colorful.BaseActivity;
import org.polaric.colorful.PermissionUtils;

import java.util.List;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.adapter.AttachmentsAdapter;
import me.shouheng.omnilist.adapter.SubAssignmentsAdapter;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.config.TextLength;
import me.shouheng.omnilist.databinding.FragmentAssignmentBinding;
import me.shouheng.omnilist.dialog.AttachmentPickerDialog;
import me.shouheng.omnilist.dialog.RatingsPickerDialog;
import me.shouheng.omnilist.fragment.base.BaseModelFragment;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.manager.ModelHelper;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.enums.ModelType;
import me.shouheng.omnilist.provider.AlarmsStore;
import me.shouheng.omnilist.provider.AttachmentsStore;
import me.shouheng.omnilist.provider.LocationsStore;
import me.shouheng.omnilist.provider.SubAssignmentStore;
import me.shouheng.omnilist.provider.schema.AttachmentSchema;
import me.shouheng.omnilist.provider.schema.SubAssignmentSchema;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.IntentUtils;
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
import me.shouheng.omnilist.widget.tools.SpaceItemDecoration;


public class AssignmentFragment extends BaseModelFragment<Assignment, FragmentAssignmentBinding>
        implements OnAttachingFileListener {

    private final static String EXTRA_IS_THIRD_PART = "extra_is_third_part";
    private final static String EXTRA_ACTION = "extra_action";

    private SubAssignmentsAdapter mAdapter;
    private AttachmentsAdapter attachmentsAdapter;

    private MaterialMenuDrawable materialMenu;

    private int playingPosition = -1;

    private Assignment assignment;
    private List<SubAssignment> subAssignments;
    private List<Attachment> attachments;
    private Location location;
    private Alarm alarm;

    private AssignmentViewModel assignmentViewModel;
    private LocationViewModel locationViewModel;
    private AttachmentViewModel attachmentViewModel;

    public static AssignmentFragment newInstance(@NonNull Assignment assignment, @Nullable Integer requestCode){
        AssignmentFragment fragment = new AssignmentFragment();
        Bundle args = new Bundle();
        args.putSerializable(Constants.EXTRA_MODEL, assignment);
        if (requestCode != null) args.putInt(Constants.EXTRA_REQUEST_CODE, requestCode);
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
        } else {
            // The cases above is new model, don't need to fetch data.
            fetchData(assignment);
        }
    }

    private void handleThirdPart() {}

    private void fetchData(Assignment assignment) {
        location = LocationsStore.getInstance().getLocation(assignment);
        subAssignments = SubAssignmentStore.getInstance().getSubAssignments(assignment, SubAssignmentSchema.SUB_ASSIGNMENT_ORDER);
        attachments = AttachmentsStore.getInstance().getAttachments(ModelType.ASSIGNMENT, assignment.getCode(), AttachmentSchema.ADDED_TIME);
        alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
    }

    private void updateOrders() {

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
                    break;
                case R.id.ll_alarm:
                    break;
                case R.id.ll_add_comment:
                    break;
                case R.id.ll_add_sub_assignment:
                    break;
            }
        });

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

        // todo
        getBinding().drawer.tvCopyLink.setOnClickListener(null);
        getBinding().drawer.tvCopyText.setOnClickListener(null);
        getBinding().drawer.tvAddToHomeScreen.setOnClickListener(null);
        getBinding().drawer.tvStatistics.setOnClickListener(null);
        getBinding().drawer.tvSettings.setOnClickListener(null);
        getBinding().drawer.tvCapture.setOnClickListener(null);
    }
    // endregion

    private Handler handler = new Handler();

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - audioRecordingTimeStart;
            getBinding().main.tvMinutes.setText(TimeUtils.getRecordTime(millis));
            if (isRecording()) handler.postDelayed(runnable, 1000);
        }
    };

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

    private void showAttachmentPicker() {
        new AttachmentPickerDialog.Builder(this)
                .setAddLinkVisible(false)
                .setFilesVisible(true)
                .setOnPickAudioSelectedListener(() ->
                        PermissionUtils.checkStoragePermission((BaseActivity) getActivity(), () ->
                                PermissionUtils.checkRecordPermission((BaseActivity) getActivity(), this::startRecording)))
                .build()
                .show(getFragmentManager(), "ATTACHMENT PICKER");
    }

    @Override
    protected void prepareBeforeRecord() {
        super.prepareBeforeRecord();
        getBinding().main.llRecord.setVisibility(View.VISIBLE);
        handler.postDelayed(runnable, 1000);
        getBinding().main.tvMinutes.setText("");
    }

    @Override
    protected void onStopRecording(Attachment attachment) {
        super.onStopRecording(attachment);
        onGetAttachment(attachment);
        getBinding().main.llRecord.setVisibility(View.GONE);
    }

    @Override
    protected void onGetAttachment(@NonNull Attachment attachment) {
        setContentChanged();
        attachmentsAdapter.addData(attachment);
        ToastUtils.makeToast(R.string.text_save_successfully);
    }

    @Override
    protected void onFailedGetAttachment(Attachment attachment) {
        ToastUtils.makeToast(R.string.failed_to_save_attachment);
        int index;
        if ((index = attachmentsAdapter.getData().indexOf(attachment)) != -1) {
            attachmentsAdapter.remove(index);
        }
    }

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
                if (attachmentsAdapter.getData().size() >= TextLength.MAX_ATTACHMENT_NUMBER.length) {
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
                }).show(getFragmentManager(), "PRIORITIES_PICKER");
                break;
            case R.id.action_more:
                getBinding().drawerLayout.openDrawer(GravityCompat.END, true);
                break;
            case R.id.action_share:
                // todo 更多的分享途径
//                ModelHelper.share(getContext(), assignment.getName(),
//                        ModelHelper.getDescription(getContext(), assignment, subAssignments), attachments);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // region editor structure
    @Override
    protected void setContentChanged() {
        if (!isContentChanged()) {
            super.setContentChanged();
            materialMenu.animateIconState(MaterialMenuDrawable.IconState.CHECK);
        }
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
    // endregion
}