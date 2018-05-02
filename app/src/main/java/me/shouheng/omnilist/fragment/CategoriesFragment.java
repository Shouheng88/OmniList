package me.shouheng.omnilist.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.Collections;
import java.util.Objects;

import javax.annotation.Nonnull;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.adapter.CategoriesAdapter;
import me.shouheng.omnilist.databinding.FragmentCategoriesBinding;
import me.shouheng.omnilist.dialog.CategoryEditDialog;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.listener.OnDataChangeListener;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.utils.preferences.AssignmentPreferences;
import me.shouheng.omnilist.viewmodel.CategoryViewModel;
import me.shouheng.omnilist.widget.tools.CustomItemAnimator;
import me.shouheng.omnilist.widget.tools.CustomItemTouchHelper;
import me.shouheng.omnilist.widget.tools.DividerItemDecoration;


/**
 * Created by wangshouheng on 2017/3/29.*/
public class CategoriesFragment extends BaseFragment<FragmentCategoriesBinding> implements
        BaseQuickAdapter.OnItemClickListener, OnDataChangeListener {

    private final static String ARG_STATUS = "arg_status";

    private RecyclerView.OnScrollListener scrollListener;
    private CategoriesAdapter mAdapter;

    private CategoryEditDialog categoryEditDialog;

    private Status status;
    private CategoryViewModel categoryViewModel;

    private AssignmentPreferences assignmentPreferences;

    public static CategoriesFragment newInstance() {
        Bundle args = new Bundle();
        CategoriesFragment fragment = new CategoriesFragment();
        args.putSerializable(ARG_STATUS, Status.NORMAL);
        fragment.setArguments(args);
        return fragment;
    }

    public static CategoriesFragment newInstance(@Nonnull Status status) {
        Bundle args = new Bundle();
        CategoriesFragment fragment = new CategoriesFragment();
        args.putSerializable(ARG_STATUS, status);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_categories;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        if (getArguments() != null && getArguments().containsKey(ARG_STATUS))
            status = (Status) getArguments().get(ARG_STATUS);

        assignmentPreferences = AssignmentPreferences.getInstance();

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);

        configToolbar();

        configCategories();
    }

    private void configToolbar() {
        if (getActivity() != null) {
            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(R.string.drawer_menu_categories);
                actionBar.setSubtitle(null);
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
            }
        }
    }

    private void configCategories() {
        status = getArguments() == null || !getArguments().containsKey(ARG_STATUS) ? Status.NORMAL : (Status) getArguments().get(ARG_STATUS);

        mAdapter = new CategoriesAdapter(getContext(), Collections.emptyList());
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            switch (view.getId()) {
                case R.id.iv_more:
                    popCategoryMenu(view, position, mAdapter.getItem(position));
                    break;
            }
        });
        mAdapter.setOnItemClickListener(this);

        getBinding().ivEmpty.setSubTitle(getEmptySubTitle());

        getBinding().rvCategories.setEmptyView(getBinding().ivEmpty);
        getBinding().rvCategories.setHasFixedSize(true);
        getBinding().rvCategories.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL_LIST, isDarkTheme()));
        getBinding().rvCategories.setItemAnimator(new CustomItemAnimator());
        getBinding().rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        getBinding().rvCategories.setAdapter(mAdapter);
        if (scrollListener != null) getBinding().rvCategories.addOnScrollListener(scrollListener);

        ItemTouchHelper.Callback callback = new CustomItemTouchHelper(true, false, mAdapter);
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(getBinding().rvCategories);

        reload();
    }

    private String getEmptySubTitle() {
        if (status == null) return null;
        return PalmApp.getContext().getString(
                status == Status.NORMAL ? R.string.category_list_empty_sub_normal :
                        status == Status.TRASHED ? R.string.category_list_empty_sub_trashed :
                                status == Status.ARCHIVED ? R.string.category_list_empty_sub_archived :
                                        R.string.category_list_empty_sub_normal);
    }

    // region ViewModel
    public void reload() {
        notifyStatus(me.shouheng.omnilist.model.data.Status.LOADING);
        categoryViewModel.getCategories(status, assignmentPreferences.showCompleted()).observe(this, listResource -> {
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
        notifyDataChanged();
    }

    private void update(int position, Category category) {
        categoryViewModel.update(category).observe(this, categoryResource -> {
            if (categoryResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                return;
            }
            switch (categoryResource.status) {
                case SUCCESS:
                    mAdapter.notifyItemChanged(position);
                    notifyDataChanged();
                    ToastUtils.makeToast(R.string.text_save_successfully);
                    break;
                case LOADING:
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                    break;
            }
        });
    }

    private void update(int position, Category category, Status toStatus) {
        categoryViewModel.update(category, toStatus).observe(this, categoryResource -> {
            if (categoryResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                return;
            }
            switch (categoryResource.status) {
                case SUCCESS:
                    mAdapter.remove(position);
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                    break;
                case LOADING:
                    break;
            }
        });
    }

    private void updateOrders() {
        categoryViewModel.updateOrders(mAdapter.getData()).observe(this, listResource -> {
            if (listResource == null) {
                LogUtils.d("listResource is null");
                return;
            }
            LogUtils.d(listResource.message);
        });
    }

    private void notifyStatus(me.shouheng.omnilist.model.data.Status status) {
        if (getActivity() instanceof OnCategoriesInteractListener) {
            ((OnCategoriesInteractListener) getActivity()).onCategoryLoadStateChanged(status);
        }
    }
    // endregion

    private void notifyDataChanged() {
        AppWidgetUtils.notifyAppWidgets(getContext());

        if (getActivity() != null && getActivity() instanceof OnCategoriesInteractListener) {
            ((OnCategoriesInteractListener) getActivity()).onCategoryDataChanged();
        }
    }

    public void addCategory(Category category) {
        mAdapter.addData(0, category);
        getBinding().rvCategories.smoothScrollToPosition(0);
    }

    public void setScrollListener(RecyclerView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }

    // region pop menu
    private void popCategoryMenu(View v, int position, Category category) {
        PopupMenu popupM = new PopupMenu(getContext(), v);
        popupM.inflate(R.menu.category_pop_menu);
        configPopMenu(popupM);
        popupM.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.action_archive:
                    update(category, status, Status.ARCHIVED);
                    break;
                case R.id.action_trash:
                    update(category, status, Status.TRASHED);
                    break;
                case R.id.action_move_out:
                    update(category, status, Status.NORMAL);
                    break;
                case R.id.action_edit:
                    showEditor(position, mAdapter.getItem(position));
                    break;
                case R.id.action_delete:
                    showDeleteDialog(position, mAdapter.getItem(position));
                    break;
            }
            return true;
        });
        popupM.show();
    }

    private void configPopMenu(PopupMenu popupMenu) {
        popupMenu.getMenu().findItem(R.id.action_move_out).setVisible(status == Status.ARCHIVED || status == Status.TRASHED);
        popupMenu.getMenu().findItem(R.id.action_edit).setVisible(status == Status.NORMAL);
        popupMenu.getMenu().findItem(R.id.action_trash).setVisible(status == Status.NORMAL || status == Status.ARCHIVED);
        popupMenu.getMenu().findItem(R.id.action_archive).setVisible(status == Status.NORMAL);
        popupMenu.getMenu().findItem(R.id.action_delete).setVisible(status == Status.TRASHED);
    }

    private void showEditor(int position, Category param) {
        categoryEditDialog = CategoryEditDialog.newInstance(param, category -> update(position, category));
        categoryEditDialog.show(Objects.requireNonNull(getFragmentManager()), "CATEGORY_EDIT_DIALOG");
    }

    private void update(Category category, Status fromStatus, Status toStatus) {
        categoryViewModel.update(category, fromStatus, toStatus).observe(this, notebookResource -> {
            if (notebookResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                return;
            }
            switch (notebookResource.status) {
                case SUCCESS:
                    reload();
                    notifyDataChanged();
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_modify_data);
                    break;
            }
        });
    }

    private void showDeleteDialog(int position, Category category) {
        new MaterialDialog.Builder(getContext())
                .title(R.string.text_warning)
                .content(R.string.category_delete_message)
                .positiveText(R.string.text_confirm)
                .onPositive((materialDialog, dialogAction) -> update(position, category, Status.DELETED))
                .negativeText(R.string.text_cancel)
                .onNegative((materialDialog, dialogAction) -> materialDialog.dismiss())
                .show();
    }
    // endregion

    public void setSelectedColor(int color) {
        if (categoryEditDialog != null) categoryEditDialog.updateUIBySelectedColor(color);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    // region options menu
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(assignmentPreferences.showCompleted() ?
                R.id.action_show_completed : R.id.action_hide_completed).setChecked(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.capture, menu);
        inflater.inflate(R.menu.list_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_capture:
                createScreenCapture(getBinding().rvCategories, ViewUtils.dp2Px(PalmApp.getContext(), 60));
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
    public void onPause() {
        super.onPause();
        if (mAdapter.isPositionChanged()){
            updateOrders();
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        if (getActivity() != null && getActivity() instanceof OnCategoriesInteractListener) {
            ((OnCategoriesInteractListener) getActivity()).onCategorySelected(mAdapter.getItem(position));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getActivity() instanceof OnCategoriesInteractListener) {
            ((OnCategoriesInteractListener) getActivity()).onResumeToCategory();
        }
    }

    @Override
    public void onDataChanged() {
        reload();
    }

    public interface OnCategoriesInteractListener {
        default void onCategoryDataChanged() {}
        default void onResumeToCategory() {}
        default void onCategorySelected(Category category) {}
        default void onCategoryLoadStateChanged(me.shouheng.omnilist.model.data.Status status) {}
    }
}