package me.shouheng.omnilist.fragment;


import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.adapter.TodayAdapter;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.FragmentTodayBinding;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.data.Status;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.provider.AlarmsStore;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.utils.preferences.AssignmentPreferences;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;
import me.shouheng.omnilist.widget.tools.CustomItemAnimator;
import me.shouheng.omnilist.widget.tools.CustomItemTouchHelper;
import me.shouheng.omnilist.widget.tools.DividerItemDecoration;

public class TodayFragment extends BaseFragment<FragmentTodayBinding> implements
        TodayAdapter.OnItemRemovedListener {

    private final int REQUEST_FOR_EDIT = 10;

    private RecyclerView.OnScrollListener scrollListener;

    private AssignmentViewModel assignmentViewModel;

    private TodayAdapter mAdapter;

    private AssignmentPreferences assignmentPreferences;

    public static TodayFragment newInstance() {
        Bundle args = new Bundle();
        TodayFragment fragment = new TodayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_today;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        assignmentViewModel = ViewModelProviders.of(this).get(AssignmentViewModel.class);
        assignmentPreferences = AssignmentPreferences.getInstance();

        configToolbar();

        configList();
    }

    private void configToolbar() {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.drawer_menu_today);
                actionBar.setSubtitle(null);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            }
        }
    }

    private void configList() {
        mAdapter = new TodayAdapter(Collections.emptyList());
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_completed:
                    Assignment assignment = Objects.requireNonNull(mAdapter.getItem(position)).assignment;
                    assert assignment != null;
                    if (assignment.getProgress() == Constants.MAX_ASSIGNMENT_PROGRESS) {
                        assignment.setProgress(0);
                        assignment.setInCompletedThisTime(true);
                    } else {
                        assignment.setProgress(Constants.MAX_ASSIGNMENT_PROGRESS);
                        assignment.setCompleteThisTime(true);
                    }
                    assignment.setChanged(!assignment.isChanged());
                    mAdapter.notifyItemChanged(position);
                    /* Update assignment state in database. */
                    updateState();
                    break;
                case R.id.rl_item:
                    ContentActivity.editAssignment(this,
                            Objects.requireNonNull(mAdapter.getItem(position)).assignment,
                            REQUEST_FOR_EDIT);
                    break;
            }
        });
        mAdapter.setOnItemRemovedListener(this);

        getBinding().rvAssignments.setEmptyView(getBinding().ivEmpty);
        getBinding().rvAssignments.setHasFixedSize(true);
        getBinding().rvAssignments.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, isDarkTheme()));
        getBinding().rvAssignments.setItemAnimator(new CustomItemAnimator());
        getBinding().rvAssignments.setLayoutManager(new LinearLayoutManager(getActivity()));
        getBinding().rvAssignments.setAdapter(mAdapter);
        getBinding().rvAssignments.addOnScrollListener(scrollListener);

        ItemTouchHelper.Callback callback = new CustomItemTouchHelper(false, assignmentPreferences.isAssignmentSlideEnable(), mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(getBinding().rvAssignments);

        reload(true);
    }

    // region Date options
    public void reload(boolean showShader) {
        if (showShader) notifyStatus(Status.LOADING);
        assignmentViewModel.getToday().observe(this, listResource -> {
            if (listResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                if (showShader)  notifyStatus(Status.FAILED);
                return;
            }
            if (showShader) notifyStatus(listResource.status);
            switch (listResource.status) {
                case SUCCESS:
                    assert listResource.data != null;
                    mAdapter.setNewData(setupAssignments(listResource.data));
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_load_data);
                    break;
            }
        });
    }

    private List<TodayAdapter.MultiItem> setupAssignments(List<Assignment> assignments) {
        List<TodayAdapter.MultiItem> multiItems = new LinkedList<>();
        List<Assignment> today = new LinkedList<>();
        List<Assignment> overdue = new LinkedList<>();

        Date todayDate = TimeUtils.today();
        for (Assignment assignment : assignments) {
            if (assignment.getEndTime().after(todayDate)) {
                today.add(assignment);
            } else {
                overdue.add(assignment);
            }
        }
        if (!today.isEmpty()) {
            multiItems.add(new TodayAdapter.MultiItem(PalmApp.getStringCompact(R.string.text_today)));
            for (Assignment assignment : today) {
                multiItems.add(new TodayAdapter.MultiItem(assignment));
            }
        }
        if (!overdue.isEmpty()) {
            multiItems.add(new TodayAdapter.MultiItem(PalmApp.getStringCompact(R.string.text_overdue)));
            for (Assignment assignment : overdue) {
                multiItems.add(new TodayAdapter.MultiItem(assignment));
            }
        }

        return multiItems;
    }

    private void updateState() {
        assignmentViewModel.updateAssignments(mAdapter.getAssignments()).observe(this, listResource -> {
            if (listResource == null) {
                ToastUtils.makeToast(R.string.text_error_when_save);
                return;
            }
            switch (listResource.status) {
                case FAILED:
                    ToastUtils.makeToast(R.string.text_error_when_save);
                    break;
                case SUCCESS:
                    reload(false);
                    ToastUtils.makeToast(R.string.text_update_successfully);
                    break;
            }
        });
    }

    private void notifyStatus(Status status) {
        if (getActivity() instanceof TodayFragmentInteraction) {
            ((TodayFragmentInteraction) getActivity()).onAssignmentsLoadStateChanged(status);
        }
    }
    // endregion

    private void notifyDataChanged() {
        AppWidgetUtils.notifyAppWidgets(getContext());
    }

    public void setScrollListener(RecyclerView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    // region Swipe event
    private void trashModel(Assignment assignment) {
        AssignmentsStore.getInstance().update(assignment, me.shouheng.omnilist.model.enums.Status.TRASHED);
        final Alarm alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
        if (alarm != null){
            AlarmsStore.getInstance().update(alarm, me.shouheng.omnilist.model.enums.Status.DELETED);
            AlarmsManager.getsInstance().removeAlarm(alarm);
        }
    }

    private void archiveModel(Assignment assignment) {
        AssignmentsStore.getInstance().update(assignment, me.shouheng.omnilist.model.enums.Status.ARCHIVED);
        final Alarm alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
        if (alarm != null) {
            AlarmsStore.getInstance().update(alarm, me.shouheng.omnilist.model.enums.Status.DELETED);
            AlarmsManager.getsInstance().removeAlarm(alarm);
        }
    }

    /**
     * {@link #trashModel(Assignment)} and {@link #archiveModel(Assignment)}
     *
     * @param assignment assignment
     * @param position position */
    private void recoverModel(Assignment assignment, int position) {
        final Alarm alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
        mAdapter.addItemToPosition(assignment, position);
        AssignmentsStore.getInstance().update(assignment, me.shouheng.omnilist.model.enums.Status.NORMAL);
        if (alarm != null) {
            AlarmsStore.getInstance().update(alarm, me.shouheng.omnilist.model.enums.Status.NORMAL);
            AlarmsManager.getsInstance().addAlarm(alarm);
        }
    }

    @Override
    public void onItemRemovedLeft(Assignment item, int position) {
        notifyDataChanged();

        Operation operation = assignmentPreferences.getSlideLeftOperation();
        int titleRes = -1;
        if (operation == Operation.ARCHIVE) {
            archiveModel(item);
            titleRes = R.string.assignment_archive_msg;
        } else if (operation == Operation.TRASH) {
            trashModel(item);
            titleRes = R.string.assignment_trash_msg;
        }
        if (titleRes == -1) throw new IllegalArgumentException("Left slide option illegal!");

        Snackbar.make(getBinding().rlContainer, titleRes, Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.text_undo), v -> recoverModel(item, position))
                .show();
    }

    @Override
    public void onItemRemovedRight(Assignment item, int position) {
        notifyDataChanged();

        Operation operation = assignmentPreferences.getSlideRightOperation();
        int titleRes = -1;
        if (operation == Operation.ARCHIVE) {
            archiveModel(item);
            titleRes = R.string.assignment_archive_msg;
        } else if (operation == Operation.TRASH) {
            trashModel(item);
            titleRes = R.string.assignment_trash_msg;
        }
        if (titleRes == -1) throw new IllegalArgumentException("Right slide option illegal!");

        Snackbar.make(getBinding().rlContainer, titleRes, Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.text_undo), v -> recoverModel(item, position))
                .show();
    }
    // endregion

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*For current version, we don't allow to capture the list for the section title height.*/
        setHasOptionsMenu(true);
    }

    // region Options menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.capture, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_capture:
                createScreenCapture(getBinding().rvAssignments, ViewUtils.dp2Px(PalmApp.getContext(), 60));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_FOR_EDIT:
                if (resultCode == Activity.RESULT_OK) {
                    reload(true);
                    notifyDataChanged();
                }
                break;
        }
    }

    public interface TodayFragmentInteraction {
        void onAssignmentsLoadStateChanged(Status status);
    }
}
