package me.shouheng.omnilist.activity;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.databinding.ActivityMainBinding;
import me.shouheng.omnilist.databinding.ActivityMainNavHeaderBinding;
import me.shouheng.omnilist.intro.IntroActivity;
import me.shouheng.omnilist.listener.OnAttachingFileListener;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.utils.preferences.LockPreferences;

public class MainActivity extends CommonActivity<ActivityMainBinding> implements
        OnAttachingFileListener {

    private ActivityMainNavHeaderBinding headerBinding;

    private LockPreferences lockPreferences;

    private final int REQUEST_FAB_SORT = 0x0001;
    private final int REQUEST_ADD_NOTE = 0x0002;
    private final int REQUEST_ARCHIVE = 0x0003;
    private final int REQUEST_TRASH = 0x0004;
    private final int REQUEST_USER_INFO = 0x0005;
    private final int REQUEST_PASSWORD = 0x0006;
    private final int REQUEST_SEARCH = 0x0007;
    private final int REQUEST_NOTE_VIEW = 0x0008;
    private final int REQUEST_SETTING = 0x0009;
    private final int REQUEST_SETTING_BACKUP = 0x000A;

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

        IntroActivity.launchIfNecessary(this);

        checkPassword();
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

        // init float action buttons
        initFloatButtons();

        initFabSortItems();

        initDrawerMenu();

        toTodayFragment(true);
    }

    private void handleIntent(Intent intent) {
        String action = intent.getAction();
    }

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
        headerBinding.getRoot().setOnLongClickListener(v -> true);
//        headerBinding.getRoot().setOnClickListener(
//                view -> startActivityForResult(UserInfoActivity.class, REQUEST_USER_INFO));
    }

    private void setupHeader() {
//        headerBinding.userMotto.setText(preferencesUtils.getUserMotto());
//
//        boolean enabled = preferencesUtils.isUserInfoBgEnable();
//        headerBinding.userBg.setVisibility(enabled ? View.VISIBLE : View.GONE);
//        if (enabled) {
//            Uri customUri = preferencesUtils.getUserInfoBG();
//            if (customUri != null) {
//                Glide.with(PalmApp.getContext())
//                        .load(customUri)
//                        .centerCrop()
//                        .crossFade()
//                        .into(headerBinding.userBg);
//            } else {
//                Glide.with(PalmApp.getContext())
//                        .load(R.drawable.theme_bg_1)
//                        .centerCrop()
//                        .crossFade()
//                        .into(headerBinding.userBg);
//            }
//        }
    }

    private void setDrawerLayoutLocked(boolean lockDrawer){
        getBinding().drawerLayout.setDrawerLockMode(lockDrawer ? DrawerLayout.LOCK_MODE_LOCKED_CLOSED
                : DrawerLayout.LOCK_MODE_UNLOCKED);
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
                    break;
                case R.id.nav_categories:
                    break;
                case R.id.nav_calendar:
                    break;
//                case R.id.nav_sync:
//                    SynchronizeUtils.syncOneDrive(this, REQUEST_SETTING_BACKUP, true);
//                    break;
//                case R.id.nav_settings:
//                    SettingsActivity.start(this, REQUEST_SETTING);
//                    break;
//                case R.id.nav_archive:
//                    startActivityForResult(ArchiveActivity.class, REQUEST_ARCHIVE);
//                    break;
//                case R.id.nav_trash:
//                    startActivityForResult(TrashedActivity.class, REQUEST_TRASH);
//                    break;
            }
        }, 500);
    }

    // endregion

    private void initViewModels() {}

    private void initFloatButtons() {}

    private void initFabSortItems() {}

    private void toTodayFragment(boolean checkDuplicate) {

    }

    private Fragment getCurrentFragment(){
        return getCurrentFragment(R.id.fragment_container);
    }

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
//                SearchActivity.start(this, REQUEST_SEARCH);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        AttachmentHelper.resolveResult(this, requestCode, data);

        switch (requestCode) {
            case REQUEST_FAB_SORT:
                initFabSortItems();
                break;
            case REQUEST_NOTE_VIEW:
            case REQUEST_ADD_NOTE:
//                if (isNotesFragment()) ((NotesFragment) getCurrentFragment()).reload();
                break;
            case REQUEST_TRASH:
                updateListIfNecessary();
                break;
            case REQUEST_ARCHIVE:
                updateListIfNecessary();
                break;
            case REQUEST_SEARCH:
                updateListIfNecessary();
                break;
            case REQUEST_PASSWORD:
                init();
                break;
            case REQUEST_SETTING:
//                int[] changedTypes = data.getIntArrayExtra(SettingsActivity.KEY_CONTENT_CHANGE_TYPES);
//                boolean drawerUpdated = false, listUpdated = false;
//                for (int changedType : changedTypes) {
//                    if (changedType == OnSettingsChangedListener.ChangedType.DRAWER_CONTENT.id && !drawerUpdated) {
//                        setupHeader();
//                        drawerUpdated = true;
//                    }
//                    if (changedType == OnSettingsChangedListener.ChangedType.NOTE_LIST_TYPE.id && !listUpdated) {
//                        if (isNotesFragment()) {
//                            toNotesFragment(false);
//                        }
//                        listUpdated = true;
//                    }
//                }
                break;
        }
    }

    private void updateListIfNecessary() {

    }

    @Override
    public void onAttachingFileErrorOccurred(Attachment attachment) {}

    @Override
    public void onAttachingFileFinished(Attachment attachment) {}
}
