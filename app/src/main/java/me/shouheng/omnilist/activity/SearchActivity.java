package me.shouheng.omnilist.activity;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.util.Collections;
import java.util.Objects;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.adapter.AssignmentsAdapter;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.ActivitySearchBinding;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.preferences.SearchPreferences;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;
import me.shouheng.omnilist.widget.tools.CustomItemAnimator;
import me.shouheng.omnilist.widget.tools.DividerItemDecoration;


// todo 所有的列表为空的时候展示的图片换成大图
public class SearchActivity extends CommonActivity<ActivitySearchBinding> implements OnQueryTextListener {

    private final static String EXTRA_NAME_REQUEST_CODE = "extra.request.code";

    private final static int REQUEST_EDIT = 0x0FF1;

    private AssignmentsAdapter mAdapter;

    private SearchView mSearchView;

    private String queryString;

    private AssignmentViewModel assignmentViewModel;

    private SearchPreferences searchPreferences;

    private boolean isContentChanged = false;

    public static void start(Activity mContext, int requestCode){
        Intent intent = new Intent(mContext, SearchActivity.class);
        intent.putExtra(EXTRA_NAME_REQUEST_CODE, requestCode);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivityForResult(intent, requestCode);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_search;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        setSupportActionBar(getBinding().toolbarLayout.toolbar);
        if (!isDarkTheme()){
            getBinding().toolbarLayout.toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAdapter = new AssignmentsAdapter(Collections.emptyList());
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_completed:
                    Assignment assignment = mAdapter.getItem(position);
                    if (assignment.getProgress() == Constants.MAX_ASSIGNMENT_PROGRESS) {
                        assignment.setProgress(0);
                        assignment.setInCompletedThisTime(true);
                    } else {
                        assignment.setProgress(Constants.MAX_ASSIGNMENT_PROGRESS);
                        assignment.setCompleteThisTime(true);
                    }
                    assignment.setChanged(!assignment.isChanged());
                    mAdapter.setStateChanged(true);
                    mAdapter.notifyItemChanged(position);
                    updateState();
                    break;
                case R.id.rl_item:
                    ContentActivity.editAssignment(SearchActivity.this, Objects.requireNonNull(mAdapter.getItem(position)), REQUEST_EDIT);
                    break;
            }
        });

        searchPreferences = SearchPreferences.getInstance();

        getBinding().rvResult.setEmptyView(getBinding().ivEmpty);
        getBinding().rvResult.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL_LIST, isDarkTheme()));
        getBinding().rvResult.setItemAnimator(new CustomItemAnimator());
        getBinding().rvResult.setLayoutManager(new LinearLayoutManager(this));
        getBinding().rvResult.setAdapter(mAdapter);

        assignmentViewModel = ViewModelProviders.of(this).get(AssignmentViewModel.class);
    }

    private void updateState() {
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
                    ToastUtils.makeToast(R.string.text_update_successfully);
                    isContentChanged = true;
                    break;
            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.item_include_tags).setChecked(searchPreferences.isTagsIncluded());
        menu.findItem(R.id.item_include_archived).setChecked(searchPreferences.isArchivedIncluded());
        menu.findItem(R.id.item_include_trashed).setChecked(searchPreferences.isTrashedIncluded());
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filter_search_condition, menu);
        getMenuInflater().inflate(R.menu.search, menu);

        mSearchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        mSearchView.setOnQueryTextListener(this);
        mSearchView.setQueryHint(getString(R.string.search_by_conditions));
        mSearchView.setIconifiedByDefault(false);
        mSearchView.setIconified(false);

        MenuItemCompat.setOnActionExpandListener(menu.findItem(R.id.action_search), new MenuItemCompat.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }
            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                finish();
                return false;
            }
        });

        menu.findItem(R.id.action_search).expandActionView();

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            case R.id.item_include_tags:
                searchPreferences.setTagsIncluded(!searchPreferences.isTagsIncluded());
                invalidateOptionsMenu();
                break;
            case R.id.item_include_archived:
                searchPreferences.setArchivedIncluded(!searchPreferences.isArchivedIncluded());
                invalidateOptionsMenu();
                break;
            case R.id.item_include_trashed:
                searchPreferences.setTrashedIncluded(!searchPreferences.isTrashedIncluded());
                invalidateOptionsMenu();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        onQueryTextChange(query);
        hideInputManager();
        return true;
    }

    private void hideInputManager() {
        if (mSearchView != null) {
            mSearchView.clearFocus();
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (newText.equals(queryString)) {
            return true;
        }

        queryString = newText;

        if (!TextUtils.isEmpty(queryString)) {
            queryAll(queryString);
        } else {
            mAdapter.setNewData(Collections.emptyList());
        }

        return true;
    }

    private void queryAll(String queryText) {
        getBinding().topMpb.setVisibility(View.VISIBLE);
        assignmentViewModel.getAssignments(queryText, AssignmentSchema.ADDED_TIME).observe(this, searchResultResource -> {
            getBinding().topMpb.setVisibility(View.GONE);
            if (searchResultResource == null) {
                ToastUtils.makeToast(R.string.text_error_when_save);
                return;
            }
            switch (searchResultResource.status) {
                case SUCCESS:
                    if (searchResultResource.data != null) {
                        mAdapter.setNewData(searchResultResource.data);
                    }
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_error_when_save);
                    break;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_EDIT:
                    queryAll(queryString);
                    isContentChanged = true;
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (isContentChanged) {
            Intent intent = new Intent();
            setResult(Activity.RESULT_OK, intent);
            finish();
        } else {
            super.onBackPressed();
        }
    }
}
