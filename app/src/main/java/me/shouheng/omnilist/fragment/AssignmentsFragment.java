package me.shouheng.omnilist.fragment;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import java.io.File;
import java.util.Collections;
import java.util.Objects;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.adapter.AssignmentsAdapter;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.FragmentAssignmentsBinding;
import me.shouheng.omnilist.dialog.RatingsPickerDialog;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.listener.OnDataChangeListener;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.enums.Priority;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.provider.AlarmsStore;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.utils.preferences.AssignmentPreferences;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;
import me.shouheng.omnilist.widget.tools.CustomItemAnimator;
import me.shouheng.omnilist.widget.tools.CustomItemTouchHelper;
import me.shouheng.omnilist.widget.tools.CustomRecyclerScrollViewListener;
import me.shouheng.omnilist.widget.tools.DividerItemDecoration;

public class AssignmentsFragment extends BaseFragment<FragmentAssignmentsBinding> implements
        TextView.OnEditorActionListener, AssignmentsAdapter.OnItemRemovedListener, OnDataChangeListener {

    private static final String ARG_CATEGORY = "argument_category";
    private static final String ARG_STATUS = "argument_status";

    private final int REQUEST_FOR_EDIT = 10;

    private Category category;
    private Status status;

    private AssignmentViewModel assignmentViewModel;

    private AssignmentsAdapter mAdapter;

    private boolean isInEditMode;

    private AssignmentPreferences assignmentPreferences;

    public static AssignmentsFragment newInstance(Category category, Status status) {
        AssignmentsFragment fragment = new AssignmentsFragment();
        Bundle arguments = new Bundle();
        arguments.putSerializable(ARG_CATEGORY, category);
        arguments.putSerializable(ARG_STATUS, status);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_assignments;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey(ARG_STATUS)) {
            status = (Status) arguments.get(ARG_STATUS);
        }
        if (arguments != null && arguments.containsKey(ARG_CATEGORY)) {
            category = (Category) arguments.get(ARG_CATEGORY);
        }

        assignmentPreferences = AssignmentPreferences.getInstance();

        assignmentViewModel = ViewModelProviders.of(this).get(AssignmentViewModel.class);

        configToolbar();

        configViews();

        configList();
    }

    private void configToolbar() {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.drawer_menu_category);
                actionBar.setDisplayHomeAsUpEnabled(true);
                String subTitle = category != null ? category.getName() : null;
                actionBar.setSubtitle(subTitle);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back_white_24dp);
            }
        }
    }

    private void configViews() {
        getBinding().cdAddAssignment.setVisibility(status == Status.NORMAL ? View.VISIBLE : View.GONE);
        getBinding().fab.setVisibility(status == Status.NORMAL ? View.VISIBLE : View.GONE);

        getBinding().fab.setOnClickListener(v -> {
            ContentActivity.editAssignment(this, getNewAssignment(), REQUEST_FOR_EDIT);
        });
        getBinding().fab.setColorNormal(accentColor());
        getBinding().fab.setColorPressed(accentColor());

        getBinding().etAssignmentTitle.addTextChangedListener(titleWatcher);
        getBinding().etAssignmentTitle.setOnEditorActionListener(this);

        getBinding().ivRate.setImageResource(assignmentPreferences.getDefaultPriority().iconRes);
        getBinding().ivRate.setTag(assignmentPreferences.getDefaultPriority());
        getBinding().ivRate.setOnClickListener(v ->
                RatingsPickerDialog.newInstance(priority -> {
                    getBinding().ivRate.setTag(priority);
                    getBinding().ivRate.setImageResource(priority.iconRes);
                }).show(Objects.requireNonNull(getFragmentManager()), "PRIORITIES_PICKER")
        );
    }

    private TextWatcher titleWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() == 0){
                if (isInEditMode){
                    isInEditMode = false;
                    if (getActivity() != null) getActivity().invalidateOptionsMenu();
                }
            } else {
                if (!isInEditMode){
                    isInEditMode = true;
                    if (getActivity() != null) getActivity().invalidateOptionsMenu();
                }
            }
        }
    };

    // region assignment list
    private void configList() {
        mAdapter = new AssignmentsAdapter(Collections.emptyList());
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_completed:
                    /* Modify assignment value. */
                    Assignment assignment = mAdapter.getItem(position);
                    assert assignment != null;
                    if (assignment.getProgress() == Constants.MAX_ASSIGNMENT_PROGRESS) {
                        assignment.setProgress(0);
                        assignment.setInCompletedThisTime(true);
                    } else {
                        assignment.setProgress(Constants.MAX_ASSIGNMENT_PROGRESS);
                        assignment.setCompleteThisTime(true);
                    }
                    assignment.setChanged(!assignment.isChanged());
                    mAdapter.setStateChanged(true);
                    /* Update assignment state in database. */
                    updateState(position);
                    break;
                case R.id.rl_item:
                    ContentActivity.editAssignment(this,
                            Objects.requireNonNull(mAdapter.getItem(position)),
                            REQUEST_FOR_EDIT);
                    break;
            }
        });
        mAdapter.setOnItemRemovedListener(this);

        getBinding().ivEmpty.setSubTitle(getEmptySubTitle());

        getBinding().recyclerview.setEmptyView(getBinding().ivEmpty);
        getBinding().recyclerview.setHasFixedSize(true);
        getBinding().recyclerview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, isDarkTheme()));
        getBinding().recyclerview.setItemAnimator(new CustomItemAnimator());
        getBinding().recyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        getBinding().recyclerview.setAdapter(mAdapter);
        RecyclerView.OnScrollListener scrollListener = new CustomRecyclerScrollViewListener() {
            @Override
            public void show() {
                getBinding().fab.animate()
                        .translationY(0)
                        .setInterpolator(new DecelerateInterpolator(2))
                        .start();
            }

            @Override
            public void hide() {
                CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) getBinding().fab.getLayoutParams();
                int fabMargin = lp.bottomMargin;
                getBinding().fab.animate()
                        .translationY(getBinding().fab.getHeight() + fabMargin)
                        .setInterpolator(new AccelerateInterpolator(2.0f))
                        .start();
            }
        };
        getBinding().recyclerview.addOnScrollListener(scrollListener);

        ItemTouchHelper.Callback callback = new CustomItemTouchHelper(true, assignmentPreferences.isAssignmentSlideEnable(), mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(getBinding().recyclerview);

        reload();
    }

    private String getEmptySubTitle() {
        if (status == null) return null;
        return PalmApp.getContext().getString(
                status == Status.NORMAL ? R.string.assignments_empty_list_sub_normal :
                        status == Status.TRASHED ? R.string.assignments_empty_list_sub_trashed :
                                status == Status.ARCHIVED ? R.string.assignments_empty_list_sub_archived :
                                        R.string.assignments_empty_list_sub_normal);
    }

    public void reload() {
        notifyStatus(me.shouheng.omnilist.model.data.Status.LOADING);
        assignmentViewModel.getAssignments(category, status, assignmentPreferences.showCompleted()).observe(this, listResource -> {
            if (listResource == null) {
                notifyStatus(me.shouheng.omnilist.model.data.Status.FAILED);
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                return;
            }
            notifyStatus(listResource.status);
            switch (listResource.status) {
                case SUCCESS:
                    mAdapter.setNewData(listResource.data);
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_load_data);
                    break;
            }
        });
    }

    private void notifyDataChanged() {
        AppWidgetUtils.notifyAppWidgets(getContext());

        if (getActivity() != null && getActivity() instanceof AssignmentsFragmentInteraction) {
            ((AssignmentsFragmentInteraction) getActivity()).onAssignmentDataChanged();
        }
    }

    private void updateOrders() {
        if (mAdapter.isPositionChanged()) {
            assignmentViewModel.updateOrders(mAdapter.getData()).observe(this, listResource -> {
                // do nothing
            });
            mAdapter.setPositionChanged(false);
        }
    }

    private void updateState(Integer position) {
        assignmentViewModel.updateAssignments(mAdapter.getData()).observe(this, listResource -> {
            if (listResource == null) {
                ToastUtils.makeToast(R.string.text_error_when_save);
                return;
            }
            switch (listResource.status) {
                case FAILED:
                    ToastUtils.makeToast(R.string.text_error_when_save);
                    break;
                case SUCCESS:
                    if (position != null) {
                        /* Remove or update item. */
                        Assignment assignment = mAdapter.getItem(position);
                        assert assignment != null;
                        if (assignmentPreferences.showCompleted()) {
                            mAdapter.notifyItemChanged(position);
                        } else if (assignment.getProgress() == Constants.MAX_ASSIGNMENT_PROGRESS){
                            mAdapter.remove(position);
                        }
                    }
                    ToastUtils.makeToast(R.string.text_update_successfully);
                    mAdapter.setStateChanged(false);
                    break;
            }
        });
    }

    private void notifyStatus(me.shouheng.omnilist.model.data.Status status) {
        if (getActivity() instanceof AssignmentsFragmentInteraction) {
            ((AssignmentsFragmentInteraction) getActivity()).onAssignmentsLoadStateChanged(status);
        }
    }
    // endregion

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
        if (getActivity() != null && getActivity() instanceof AssignmentsFragmentInteraction) {
            ((AssignmentsFragmentInteraction) getActivity()).onActivityAttached();
        }
    }

    // region options menu
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isInEditMode) {
            menu.findItem(R.id.action_search).setVisible(false);
        } else {
            MenuItem menuItem = menu.findItem(R.id.action_search);
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
            /*Set current selection.*/
            menu.findItem(assignmentPreferences.showCompleted() ?
                    R.id.action_show_completed : R.id.action_hide_completed).setChecked(true);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isInEditMode){
            inflater.inflate(R.menu.edit_mode, menu);
        } else {
            inflater.inflate(R.menu.capture, menu);
            inflater.inflate(R.menu.list_filter, menu);
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                if (getActivity() != null) {
                    getActivity().onBackPressed();
                }
                return true;
            case R.id.action_save:
                createAssignment();
                break;
            case R.id.action_clear:
                isInEditMode = false;
                Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                getBinding().etAssignmentTitle.setText("");
                break;
            case R.id.action_capture:
                createScreenCapture(getBinding().recyclerview, ViewUtils.dp2Px(PalmApp.getContext(), 60));
                break;
            case R.id.action_show_completed:
                assignmentPreferences.setShowCompleted(true);
                Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                reload();
                break;
            case R.id.action_hide_completed:
                assignmentPreferences.setShowCompleted(false);
                Objects.requireNonNull(getActivity()).invalidateOptionsMenu();
                reload();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    @Override
    protected void onGetScreenCutFile(File file) {
        super.onGetScreenCutFile(file);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            hideInputLayout();
            createAssignment();
            return true;
        }
        return false;
    }

    // region create assignment
    private Assignment getNewAssignment() {
        Assignment assignment = ModelFactory.getAssignment();
        assignment.setCategoryCode(category.getCode());
        return assignment;
    }

    private void hideInputLayout() {
        if (getActivity() == null) {
            LogUtils.e("activity is null");
            return;
        }
        InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void createAssignment() {
        if (TextUtils.isEmpty(getBinding().etAssignmentTitle.getText())){
            ToastUtils.makeToast(R.string.title_required);
            return;
        }

        Assignment assignment = getNewAssignment();
        assignment.setName(getBinding().etAssignmentTitle.getText().toString());
        assignment.setPriority((Priority) getBinding().ivRate.getTag());
        createAssignment(assignment);

        getBinding().etAssignmentTitle.setText("");
        getActivity().invalidateOptionsMenu();
        isInEditMode = false;
    }

    private void createAssignment(Assignment assignment) {
          assignmentViewModel.saveModel(assignment).observe(this, assignmentResource -> {
            if (assignmentResource == null) {
                ToastUtils.makeToast(R.string.text_error_when_save);
                return;
            }
            switch (assignmentResource.status) {
                case FAILED:
                    ToastUtils.makeToast(R.string.text_error_when_save);
                    break;
                case SUCCESS:
                    mAdapter.addItemToPosition(assignment, 0);
                    getBinding().recyclerview.smoothScrollToPosition(0);
                    ToastUtils.makeToast(R.string.text_save_successfully);
                    break;
            }
        });
    }
    // endregion

    @Override
    public void onPause() {
        super.onPause();
        updateOrders();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case REQUEST_FOR_EDIT:
                if (resultCode == Activity.RESULT_OK) {
                    reload();
                    notifyDataChanged();
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (mAdapter.isStateChanged()) {
            updateState(null);
        }
        super.onDestroy();
    }

    // region Swipe event
    private void trashModel(Assignment assignment) {
        AssignmentsStore.getInstance().update(assignment, Status.TRASHED);
        final Alarm alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
        if (alarm != null){
            AlarmsStore.getInstance().update(alarm, Status.DELETED);
//            AlarmsManager.getsInstance().removeAlarm(alarm);
        }
    }

    private void archiveModel(Assignment assignment) {
        AssignmentsStore.getInstance().update(assignment, Status.ARCHIVED);
        final Alarm alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
        if (alarm != null) {
            AlarmsStore.getInstance().update(alarm, Status.DELETED);
//            AlarmsManager.getsInstance().removeAlarm(alarm);
        }
    }

    private void moveOutModel(Assignment assignment) {
        AssignmentsStore.getInstance().update(assignment, Status.NORMAL);
        final Alarm alarm = AlarmsStore.getInstance().getAlarm(assignment, null);
        if (alarm != null) {
            AlarmsStore.getInstance().update(alarm, Status.NORMAL);
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
        AssignmentsStore.getInstance().update(assignment, status);
        if (alarm != null) {
            AlarmsStore.getInstance().update(alarm, status);
            AlarmsManager.getsInstance().addAlarm(alarm);
        }
    }

    @Override
    public void onItemRemovedLeft(Assignment item, int position) {
        notifyDataChanged();

        if (status == Status.ARCHIVED || status == Status.TRASHED) {
            moveOutModel(item);
            Snackbar.make(getBinding().coordinatorLayout, R.string.assignment_move_out_msg, Snackbar.LENGTH_SHORT)
                    .setAction(getResources().getString(R.string.text_undo), v -> recoverModel(item, position))
                    .show();
            return;
        }

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

        Snackbar.make(getBinding().coordinatorLayout, titleRes, Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.text_undo), v -> recoverModel(item, position))
                .show();
    }

    @Override
    public void onItemRemovedRight(Assignment item, int position) {
        notifyDataChanged();

        if (status == Status.ARCHIVED || status == Status.TRASHED) {
            moveOutModel(item);
            Snackbar.make(getBinding().coordinatorLayout, R.string.assignment_move_out_msg, Snackbar.LENGTH_SHORT)
                    .setAction(getResources().getString(R.string.text_undo), v -> recoverModel(item, position))
                    .show();
            return;
        }

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

        Snackbar.make(getBinding().coordinatorLayout, titleRes, Snackbar.LENGTH_SHORT)
                .setAction(getResources().getString(R.string.text_undo), v -> recoverModel(item, position))
                .show();
    }

    @Override
    public void onDataChanged() {
        reload();
    }
    // endregion

    public interface AssignmentsFragmentInteraction {
        default void onAssignmentDataChanged() {}
        void onActivityAttached();
        void onAssignmentsLoadStateChanged(me.shouheng.omnilist.model.data.Status status);
    }
}