package me.shouheng.omnilist.activity;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.bumptech.glide.Glide;
import com.github.clans.fab.FloatingActionButton;

import org.polaric.colorful.PermissionUtils;

import java.util.List;

import me.shouheng.omnilist.BuildConfig;
import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.config.Config;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.ActivityMainBinding;
import me.shouheng.omnilist.databinding.ActivityMainNavHeaderBinding;
import me.shouheng.omnilist.dialog.CategoryEditDialog;
import me.shouheng.omnilist.dialog.picker.BasePickerDialog;
import me.shouheng.omnilist.dialog.picker.CategoryPickerDialog;
import me.shouheng.omnilist.fragment.AssignmentsFragment;
import me.shouheng.omnilist.fragment.CategoriesFragment;
import me.shouheng.omnilist.fragment.FragmentDebug;
import me.shouheng.omnilist.fragment.MonthFragment;
import me.shouheng.omnilist.fragment.TodayFragment;
import me.shouheng.omnilist.fragment.WeekFragment;
import me.shouheng.omnilist.intro.IntroActivity;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.listener.OnDataChangeListener;
import me.shouheng.omnilist.listener.SettingChangeType;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.manager.FragmentHelper;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Status;
import me.shouheng.omnilist.model.tools.FabSortItem;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.IntentUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.SynchronizeUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.enums.CalendarType;
import me.shouheng.omnilist.utils.preferences.ActionPreferences;
import me.shouheng.omnilist.utils.preferences.LockPreferences;
import me.shouheng.omnilist.utils.preferences.UserPreferences;
import me.shouheng.omnilist.viewmodel.CategoryViewModel;
import me.shouheng.omnilist.widget.tools.CustomRecyclerScrollViewListener;

import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;

public class MainActivity extends CommonActivity<ActivityMainBinding> implements
        OnAttachingFileListener,
        CategoriesFragment.OnCategoriesInteractListener,
        AssignmentsFragment.AssignmentsFragmentInteraction,
        TodayFragment.TodayFragmentInteraction,
        MonthFragment.OnMonthCalendarInteraction,
        WeekFragment.OnWeekCalendarInteraction{

    // region request codes
    private final int REQUEST_FAB_SORT = 0x0001;
    private final int REQUEST_ARCHIVE = 0x0003;
    private final int REQUEST_TRASH = 0x0004;
    private final int REQUEST_USER_INFO = 0x0005;
    private final int REQUEST_PASSWORD = 0x0006;
    private final int REQUEST_SEARCH = 0x0007;
    private final int REQUEST_EDIT_ASSIGNMENT = 0x0008;
    private final int REQUEST_SETTING = 0x0009;
    private final int REQUEST_SETTING_BACKUP = 0x000A;
    // endregion

    private ActivityMainNavHeaderBinding headerBinding;

    private LockPreferences lockPreferences;
    private UserPreferences userPreferences;

    private RecyclerView.OnScrollListener onScrollListener;
    private FloatingActionButton[] floatingActionButtons;

    private CategoryEditDialog categoryEditDialog;

    private CategoryViewModel categoryViewModel;

    private DataSetChangedReceiver dataSetChangedReceiver;

    private long onBackPressed;

    @Override
    protected void beforeSetContentView() {
        setTranslucentStatusBar();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_main;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        lockPreferences = LockPreferences.getInstance();
        userPreferences = UserPreferences.getInstance();

        if (savedInstanceState == null) {
            IntroActivity.launchIfNecessary(this);
        }

        checkPassword();

        regNoteChangeReceiver();
    }

    private void checkPassword() {
        if (lockPreferences.isPasswordRequired()
                && !PalmApp.isPasswordChecked()
                && !TextUtils.isEmpty(lockPreferences.getPassword())) {
            LockActivity.requireLaunch(this, REQUEST_PASSWORD);
        } else {
            init();
        }
    }

    private void init() {
        handleIntent(getIntent());

        configToolbar();

        initViewModels();

        initHeaderView();

        initFloatButtons();

        initFabSortItems();

        initDrawerMenu();

        toTodayFragment(false);
    }

    // region handle intent
    private void handleIntent(Intent intent) {
        String action = intent.getAction();

        // if the action is empty or the activity is recreated for theme change, don;t handle intent
        if (TextUtils.isEmpty(action) || recreateForThemeChange) return;

        switch (action) {
            case Constants.ACTION_SHORTCUT:
                intent.setClass(this, ContentActivity.class);
                startActivity(intent);
                break;
            case Constants.ACTION_WIDGET_LIST:
                Assignment assignment;
                if (intent.hasExtra(Constants.EXTRA_MODEL)
                        && (assignment = (Assignment) intent.getSerializableExtra(Constants.EXTRA_MODEL)) != null) {
                    editAssignment(assignment);
                }
                break;
            case Constants.ACTION_WIDGET_LAUNCH_APP:
                // do nothing just open the app.
                break;
            case Constants.ACTION_ADD_RECORD:
            case Constants.ACTION_TAKE_PHOTO:
            case Constants.ACTION_ADD_SKETCH:
                startAction(action);
                break;
            case Intent.ACTION_SEND:
            case Intent.ACTION_SEND_MULTIPLE:
            case Constants.INTENT_GOOGLE_NOW:
                PermissionUtils.checkStoragePermission(this, this::handleThirdPart);
                break;
            case Constants.ACTION_RESTART_APP:
                // Recreate
                recreate();
        }
    }

    private void startAction(String action) {
        ToastUtils.makeToast(R.string.widget_pick_category_at_first);
        showCategoryPicker((dialog, category, position) -> {
            Assignment assignment = ModelFactory.getAssignment();
            assignment.setCategoryCode(category.getCode());
            PermissionUtils.checkStoragePermission(MainActivity.this, () ->
                    ContentActivity.resolveAction(MainActivity.this,
                            assignment, action, REQUEST_EDIT_ASSIGNMENT));
            dialog.dismiss();
        });
    }

    private void handleThirdPart() {
        Intent i = getIntent();
        if (IntentUtils.checkAction(i,
                Intent.ACTION_SEND,
                Intent.ACTION_SEND_MULTIPLE,
                Constants.INTENT_GOOGLE_NOW) && i.getType() != null) {
            ToastUtils.makeToast(R.string.widget_pick_category_at_first);
            showCategoryPicker((dialog, category, position) -> {
                Assignment assignment = ModelFactory.getAssignment();
                assignment.setCategoryCode(category.getCode());
                PermissionUtils.checkStoragePermission(this, () ->
                        ContentActivity.resolveThirdPart(this, i, assignment));
                dialog.dismiss();
            });
        }
    }

    private void showCategoryPicker(BasePickerDialog.OnItemSelectedListener<Category> onItemSelectedListener) {
        CategoryPickerDialog.newInstance()
                .setOnCreateClickListener(() -> editCategory(onItemSelectedListener))
                .setOnItemSelectedListener(onItemSelectedListener)
                .show(getSupportFragmentManager(), "CATEGORY_PICKER");
    }
    // endregion

    private void configToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_white);
        }
        if (!isDarkTheme()) toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
    }

    // region drawer
    private void initHeaderView() {
        if (headerBinding == null) {
            View header = getBinding().nav.inflateHeaderView(R.layout.activity_main_nav_header);
            headerBinding = DataBindingUtil.bind(header);
        }
        setupHeader();
        headerBinding.getRoot().setOnLongClickListener(v -> {
            if (BuildConfig.DEBUG) {
                toFragment(new FragmentDebug());
            }
            return true;
        });
        headerBinding.getRoot().setOnClickListener(view -> startActivityForResult(UserInfoActivity.class, REQUEST_USER_INFO));
    }

    private void setupHeader() {
        headerBinding.userMotto.setText(userPreferences.getUserMotto());

        boolean enabled = userPreferences.isUserInfoBgVisible();
        headerBinding.userBg.setVisibility(enabled ? View.VISIBLE : View.GONE);
        if (enabled) {
            Uri customUri = userPreferences.getUserInfoBG();
            Glide.with(PalmApp.getContext())
                    .load(customUri)
                    .centerCrop()
                    .crossFade()
                    .into(headerBinding.userBg);
        }
    }

    private void setDrawerLayoutLocked(boolean lockDrawer){
        getBinding().drawerLayout.setDrawerLockMode(lockDrawer ?
                DrawerLayout.LOCK_MODE_LOCKED_CLOSED : DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    private void initDrawerMenu() {
        getBinding().nav.getMenu().findItem(R.id.nav_today).setChecked(true);
        getBinding().nav.setNavigationItemSelectedListener(menuItem -> {
            getBinding().drawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_today:
                case R.id.nav_categories:
                case R.id.nav_calendar:
                    menuItem.setChecked(true);
                    break;
            }
            execute(menuItem);
            return true;
        });
    }

    private void execute(final MenuItem menuItem) {
        new Handler().postDelayed(() -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_today:
                    toTodayFragment(true);
                    break;
                case R.id.nav_categories:
                    toCategoriesFragment();
                    break;
                case R.id.nav_calendar:
                    if (ActionPreferences.getInstance().getCalendarType() == CalendarType.MONTH) {
                        toMonthFragment();
                    } else {
                        toWeekFragment();
                    }
                    break;
                case R.id.nav_sync:
                    SynchronizeUtils.syncOneDrive(this, REQUEST_SETTING_BACKUP, true);
                    break;
                case R.id.nav_settings:
                    SettingsActivity.start(this, REQUEST_SETTING);
                    break;
                case R.id.nav_archive:
                    startActivityForResult(ArchiveActivity.class, REQUEST_ARCHIVE);
                    break;
                case R.id.nav_trash:
                    startActivityForResult(TrashedActivity.class, REQUEST_TRASH);
                    break;
            }
        }, 500);
    }
    // endregion

    private void initViewModels() {
        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);
    }

    // region float action buttons
    private void initFloatButtons() {
        getBinding().menu.setMenuButtonColorNormal(accentColor());
        getBinding().menu.setMenuButtonColorPressed(accentColor());
        getBinding().menu.setOnMenuButtonLongClickListener(v -> {
            FabSortActivity.start(this, REQUEST_FAB_SORT);
            return false;
        });
        getBinding().menu.setOnMenuToggleListener(opened -> getBinding().rlMenuContainer.setVisibility(opened ? View.VISIBLE : View.GONE));
        getBinding().rlMenuContainer.setOnClickListener(view -> getBinding().menu.close(true));
        getBinding().rlMenuContainer.setBackgroundResource(isDarkTheme() ? R.color.dark_menu_container_background_color : R.color.light_menu_container_background_color);

        floatingActionButtons = new FloatingActionButton[]{getBinding().fab1, getBinding().fab2, getBinding().fab3, getBinding().fab4, getBinding().fab5};

        for (int i = 0; i< floatingActionButtons.length; i++) {
            floatingActionButtons[i].setColorNormal(accentColor());
            floatingActionButtons[i].setColorPressed(accentColor());
            int finalI = i;
            floatingActionButtons[i].setOnClickListener(view -> resolveFabClick(finalI));
        }

        onScrollListener = new CustomRecyclerScrollViewListener() {
            @Override
            public void show() {
                getBinding().menu.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
            }

            @Override
            public void hide() {
                RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) getBinding().menu.getLayoutParams();
                int fabMargin = lp.bottomMargin;
                getBinding().menu.animate().translationY(getBinding().menu.getHeight() + fabMargin).setInterpolator(new AccelerateInterpolator(2.0f)).start();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == SCROLL_STATE_IDLE){
                    LogUtils.d("onScrollStateChanged: SCROLL_STATE_IDLE");
                }
            }
        };
    }

    private void initFabSortItems() {
        try {
            List<FabSortItem> fabSortItems = userPreferences.getFabSortResult();
            for (int i = 0; i< floatingActionButtons.length; i++) {
                floatingActionButtons[i].setImageDrawable(ColorUtils.tintDrawable(getResources().getDrawable(fabSortItems.get(i).iconRes), Color.WHITE));
                floatingActionButtons[i].setLabelText(getString(fabSortItems.get(i).nameRes));
            }
        } catch (Exception e) {
            LogUtils.d("initFabSortItems, error occurred : " + e);
            userPreferences.setFabSortResult(UserPreferences.defaultFabOrders);
        }
    }

    private void resolveFabClick(int index) {
        getBinding().menu.close(true);
        FabSortItem fabSortItem = userPreferences.getFabSortResult().get(index);
        switch (fabSortItem) {
            case CATEGORY:
                editCategory(null);
                break;
            case ASSIGNMENT:
                startAction(Constants.ACTION_ADD_ASSIGNMENT);
                break;
            case FILE:
                startAction(Constants.ACTION_ADD_FILES);
                break;
            case CAPTURE:
                startAction(Constants.ACTION_TAKE_PHOTO);
                break;
            case DRAFT:
                startAction(Constants.ACTION_ADD_SKETCH);
                break;
            case QUICK:
                Intent i = new Intent(this, QuickActivity.class);
                i.setAction(Constants.ACTION_ADD_QUICK_ASSIGNMENT);
                startActivityForResult(i, REQUEST_EDIT_ASSIGNMENT);
                break;
            case RECORD:
                startAction(Constants.ACTION_ADD_RECORD);
                break;
        }
    }

    private void editCategory(BasePickerDialog.OnItemSelectedListener<Category> onItemSelectedListener) {
        categoryEditDialog = CategoryEditDialog.newInstance(ModelFactory.getCategory(), category ->
                categoryViewModel.saveModel(category).observe(this, categoryResource -> {
                    if (categoryResource == null) {
                        ToastUtils.makeToast(R.string.text_error_when_save);
                        return;
                    }
                    switch (categoryResource.status) {
                        case SUCCESS:
                            ToastUtils.makeToast(R.string.text_save_successfully);
                            Fragment fragment = getCurrentFragment();
                            if (fragment != null && fragment instanceof CategoriesFragment) {
                                ((CategoriesFragment) fragment).addCategory(category);
                            }
                            if (onItemSelectedListener != null) {
                                showCategoryPicker(onItemSelectedListener);
                            }
                            break;
                        case FAILED:
                            ToastUtils.makeToast(R.string.text_error_when_save);
                            break;
                    }
                }));
        categoryEditDialog.show(getSupportFragmentManager(), "CATEGORY_EDIT_DIALOG");
    }

    private void editAssignment(Assignment assignment) {
        ContentActivity.editAssignment(this, assignment, REQUEST_EDIT_ASSIGNMENT);
    }
    // endregion

    // region switch fragment
    private void toMonthFragment() {
        if (getCurrentFragment() instanceof MonthFragment) return;
        MonthFragment monthFragment = MonthFragment.newInstance();
        monthFragment.setOnScrollListener(onScrollListener);
        toFragment(monthFragment);
        new Handler().postDelayed(() -> getBinding().nav.getMenu().findItem(R.id.nav_calendar).setChecked(true), 300);
    }

    private void toWeekFragment() {
        if (getCurrentFragment() instanceof WeekFragment) return;
        WeekFragment weekFragment = WeekFragment.newInstance();
        toFragment(weekFragment);
        new Handler().postDelayed(() -> getBinding().nav.getMenu().findItem(R.id.nav_calendar).setChecked(true), 300);
    }

    private void toTodayFragment(boolean checkDuplicate) {
        if (getCurrentFragment() instanceof TodayFragment && checkDuplicate) return;
        TodayFragment todayFragment = TodayFragment.newInstance();
        todayFragment.setScrollListener(onScrollListener);
        toFragment(todayFragment);
        new Handler().postDelayed(() -> getBinding().nav.getMenu().findItem(R.id.nav_today).setChecked(true), 300);
    }

    private void toCategoriesFragment() {
        if (getCurrentFragment() instanceof CategoriesFragment) return;
        CategoriesFragment categoriesFragment = CategoriesFragment.newInstance();
        categoriesFragment.setScrollListener(onScrollListener);
        toFragment(categoriesFragment);
        new Handler().postDelayed(() -> getBinding().nav.getMenu().findItem(R.id.nav_categories).setChecked(true), 300);
    }

    private void toFragment(Fragment fragment) {
        FragmentHelper.replace(this, fragment, R.id.fragment_container);
    }

    private Fragment getCurrentFragment(){
        return getCurrentFragment(R.id.fragment_container);
    }

    private boolean isTodayFragment() {
        Fragment f = getCurrentFragment();
        return f != null && f instanceof TodayFragment;
    }

    private boolean isDashboard() {
        Fragment f = getCurrentFragment();
        return f != null && (f instanceof CategoriesFragment
                || f instanceof TodayFragment
                || f instanceof WeekFragment
                || f instanceof MonthFragment);
    }
    // endregion

    // region back pressed event
    @Override
    public void onBackPressed() {
        if (isDashboard()){
            if (getBinding().drawerLayout.isDrawerOpen(GravityCompat.START)){
                getBinding().drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                if (getBinding().menu.isOpened()) {
                    getBinding().menu.close(true);
                    return;
                }
                if (isTodayFragment()) {
                    againExit();
                } else {
                    toTodayFragment(true);
                }
            }
        } else {
            super.onBackPressed();
        }
    }

    private void againExit() {
        if (onBackPressed + Config.BACK_TIME_INTERVAL > System.currentTimeMillis()) {
            SynchronizeUtils.syncOneDrive(this);
            super.onBackPressed();
            return;
        } else {
            ToastUtils.makeToast(R.string.text_tab_again_exit);
        }
        onBackPressed = System.currentTimeMillis();
    }
    // endregion

    // region options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                Fragment fragment = getCurrentFragment();
                if (!fragment.onOptionsItemSelected(item)) {
                    getBinding().drawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            }
            case R.id.action_search:
                SearchActivity.start(this, REQUEST_SEARCH);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    // endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        AttachmentHelper.resolveResult(this, requestCode, data);

        switch (requestCode) {
            case REQUEST_FAB_SORT:
                initFabSortItems();
                break;
            case REQUEST_EDIT_ASSIGNMENT:
            case REQUEST_TRASH:
            case REQUEST_ARCHIVE:
            case REQUEST_SEARCH:
                updateListIfNecessary();
                break;
            case REQUEST_PASSWORD:
                init();
                break;
            case REQUEST_SETTING:
                int[] changedTypes = data.getIntArrayExtra(SettingsActivity.KEY_CONTENT_CHANGE_TYPES);
                boolean drawerUpdated = false, fabSortUpdated = false;
                for (int changedType : changedTypes) {
                    if (changedType == SettingChangeType.DRAWER.id && !drawerUpdated) {
                        setupHeader();
                        drawerUpdated = true;
                    }
                    if (changedType == SettingChangeType.FAB.id && !fabSortUpdated) {
                        initFabSortItems();
                        fabSortUpdated = true;
                    }
                }
                break;
        }
    }

    @Override
    public void onColorSelection(@NonNull ColorChooserDialog dialog, int selectedColor) {
        if (categoryEditDialog != null) {
            categoryEditDialog.updateUIBySelectedColor(selectedColor);
        }
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof CategoriesFragment) {
            ((CategoriesFragment) currentFragment).setSelectedColor(selectedColor);
        }
    }

    // region register and unregister data set changed broadcast
    private void regNoteChangeReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constants.ACTION_DATA_SET_CHANGE_BROADCAST);
        dataSetChangedReceiver = new DataSetChangedReceiver();
        registerReceiver(dataSetChangedReceiver, filter);
    }

    private class DataSetChangedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateListIfNecessary();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(dataSetChangedReceiver);
    }
    // endregion

    // region category fragment interaction
    @Override
    public void onCategorySelected(Category category) {
        AssignmentsFragment assignmentsFragment = AssignmentsFragment.newInstance(
                category, me.shouheng.omnilist.model.enums.Status.NORMAL);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.addToBackStack(null);
        transaction.replace(R.id.fragment_container, assignmentsFragment).commit();
    }

    @Override
    public void onResumeToCategory() {
        setDrawerLayoutLocked(false);
        getBinding().menu.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCategoryLoadStateChanged(Status status) {
        onLoadStateChanged(status);
    }
    // endregion

    // region assignments fragment interaction
    @Override
    public void onActivityAttached() {
        setDrawerLayoutLocked(true);
        getBinding().menu.setVisibility(View.GONE);
    }

    @Override
    public void onAssignmentsLoadStateChanged(Status status) {
        onLoadStateChanged(status);
    }
    // endregion

    // region calendar fragment interaction
    @Override
    public void onShowWeekClicked() {
        toWeekFragment();
    }

    @Override
    public void onShowMonthClicked() {
        toMonthFragment();
    }
    // endregion

    private void updateListIfNecessary() {
        LogUtils.d("updateListIfNecessary");
        Fragment f = getCurrentFragment();
        if (f instanceof OnDataChangeListener) {
            ((OnDataChangeListener) f).onDataChanged();
        }
    }

    // region attachment handler
    @Override
    public void onAttachingFileErrorOccurred(Attachment attachment) {}

    @Override
    public void onAttachingFileFinished(Attachment attachment) {}
    // endregion

    private void onLoadStateChanged(Status status) {
        switch (status) {
            case SUCCESS:
            case FAILED:
                getBinding().sl.setVisibility(View.GONE);
                break;
            case LOADING:
                getBinding().sl.setVisibility(View.VISIBLE);
                break;
        }
    }
}
