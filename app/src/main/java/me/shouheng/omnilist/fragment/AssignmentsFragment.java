package me.shouheng.omnilist.fragment;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.SpeechRecognizer;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.baidu.speech.VoiceRecognitionService;

import org.json.JSONObject;
import org.polaric.colorful.BaseActivity;
import org.polaric.colorful.PermissionUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.ContentActivity;
import me.shouheng.omnilist.adapter.AssignmentsAdapter;
import me.shouheng.omnilist.config.BaiduConstants;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.FragmentAssignmentsBinding;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.listener.PalmAnimationListener;
import me.shouheng.omnilist.listener.PalmAnimatorListener;
import me.shouheng.omnilist.listener.SpeechRecognitionListener;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.enums.Operation;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.provider.AlarmsStore;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.utils.AppWidgetUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.NetworkUtils;
import me.shouheng.omnilist.utils.SpeechRecognizorUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.utils.ViewUtils;
import me.shouheng.omnilist.utils.preferences.AssignmentPreferences;
import me.shouheng.omnilist.viewmodel.AssignmentViewModel;
import me.shouheng.omnilist.widget.tools.CustomItemAnimator;
import me.shouheng.omnilist.widget.tools.CustomItemTouchHelper;
import me.shouheng.omnilist.widget.tools.CustomRecyclerScrollViewListener;
import me.shouheng.omnilist.widget.tools.DividerItemDecoration;

// todo 限制输入的标题的长度
public class AssignmentsFragment extends BaseFragment<FragmentAssignmentsBinding> implements
        TextView.OnEditorActionListener,
        AssignmentsAdapter.OnItemRemovedListener {

    private static final String ARG_CATEGORY = "argument_category";
    private static final String ARG_STATUS = "argument_status";

    private final int REQUEST_FOR_EDIT = 10;

    private Category category;
    private Status status;

    private AssignmentViewModel assignmentViewModel;

    private AssignmentsAdapter mAdapter;

    private boolean isInEditMode, isInRecognitionMode, firstTimeRecognition = true;
    private float fabStartPos = 0, actionDownY;

    private SpeechRecognizer speechRecognizer;
    private long speechEndTime = -1, touchStartTime;
    private AudioManager mAudioManager;
    private Animation outerScaleAnim;

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
            if (isInRecognitionMode){
                ToastUtils.makeToast(R.string.touch_and_speak);
            } else {
                ContentActivity.editAssignment(this, getNewAssignment(), REQUEST_FOR_EDIT);
            }
        });
        getBinding().fab.setColorNormal(accentColor());
        getBinding().fab.setColorPressed(accentColor());

        getBinding().etAssignmentTitle.addTextChangedListener(titleWatcher);
        getBinding().etAssignmentTitle.setOnEditorActionListener(this);

        getBinding().ivMic.setOnClickListener(v -> {
            if (isInRecognitionMode){
                exitRecognition();
            } else {
                if (!NetworkUtils.isNetworkAvailable(PalmApp.getContext())){
                    ToastUtils.makeToast(R.string.check_network_availability);
                    return;
                }
                PermissionUtils.checkRecordPermission((BaseActivity) getActivity(), this::prepareRecognition);
            }
        });

        if (isDarkTheme()) {
            getBinding().lsr.getRoot().setBackgroundResource(R.color.dark_theme_background);
            getBinding().lsr.vrCard.setCardBackgroundColor(getResources().getColor(R.color.dark_theme_foreground));
        }

        outerScaleAnim = AnimationUtils.loadAnimation(getContext(), R.anim.voice_input_scale);
        outerScaleAnim.setAnimationListener(new PalmAnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                getBinding().lsr.civOuter.startAnimation(animation);
            }
        });
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
                    mAdapter.notifyItemChanged(position);
                    updateState();
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
        return null;
        // todo
    }

    public void reload() {
        if (getActivity() instanceof AssignmentsFragmentInteraction) {
            ((AssignmentsFragmentInteraction) getActivity()).onAssignmentsLoadStateChanged(
                    me.shouheng.omnilist.model.data.Status.LOADING);
        }

        assignmentViewModel.getAssignments(category, status, assignmentPreferences.showCompleted()).observe(this, listResource -> {
            if (listResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                return;
            }
            if (getActivity() instanceof AssignmentsFragmentInteraction) {
                ((AssignmentsFragmentInteraction) getActivity()).onAssignmentsLoadStateChanged(listResource.status);
            }
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
                    mAdapter.setStateChanged(false);
                    break;
            }
        });
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

    // region speech recognition
    private void prepareRecognition() {
        if (firstTimeRecognition) {
            firstTimeRecognition = false;
            fabStartPos = getBinding().fab.getX();
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext(), new ComponentName(getContext(), VoiceRecognitionService.class));
            speechRecognizer.setRecognitionListener(recognitionListener);
            mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        }

        moveForRecognition();

        getBinding().fab.setOnTouchListener(fabTouchListener);

        isInRecognitionMode = true;
    }

    private void moveForRecognition() {
        int windowWidth = ViewUtils.getWindowWidth(getContext());
        ObjectAnimator moveAnimation = ObjectAnimator.ofFloat(getBinding().fab,
                "x",
                fabStartPos,
                windowWidth / 2 - getBinding().fab.getWidth() / 2
        ).setDuration(400);
        moveAnimation.setInterpolator(new AccelerateInterpolator());
        moveAnimation.start();
        moveAnimation.addListener(new PalmAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                getBinding().fab.setImageResource(R.drawable.ic_mic_white_24dp);
                getBinding().ivMic.setImageResource(R.drawable.ic_clear_white);
            }
        });
    }

    private SpeechRecognitionListener recognitionListener = new SpeechRecognitionListener() {

        private Integer midHeight, maxHeight;

        private void initMidHeight() {
            if (midHeight == null) {
                midHeight = getBinding().lsr.wave1Up.getLayoutParams().height;
            }
        }

        private void initMaxHeight() {
            if (maxHeight == null) {
                maxHeight = getBinding().lsr.wave2Up.getLayoutParams().height;
            }
        }

        @Override
        public void onRmsChanged(float rmsdB) {
            LogUtils.d("onRmsChanged: " + rmsdB);
            initMaxHeight();
            initMidHeight();

            changeMidHeight(rmsdB, getBinding().lsr.wave1Up);
            changeMidHeight(rmsdB, getBinding().lsr.wave1Down);

            changeMaxHeight(rmsdB, getBinding().lsr.wave2Up);
            changeMaxHeight(rmsdB, getBinding().lsr.wave2Down);
            changeMaxHeight(rmsdB, getBinding().lsr.wave3Up);
            changeMaxHeight(rmsdB, getBinding().lsr.wave3Down);

            changeMidHeight(rmsdB, getBinding().lsr.wave4Up);
            changeMidHeight(rmsdB, getBinding().lsr.wave4Down);
        }

        private void changeMidHeight(float rmsdB, RelativeLayout layout) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layout.getLayoutParams();
            lp.height = (int) (midHeight * rmsdB * 0.0025);
            lp.height = Math.max(lp.height , layout.getMeasuredWidth());
            layout.setLayoutParams(lp);
        }

        private void changeMaxHeight(float rmsdB, RelativeLayout layout) {
            RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) layout.getLayoutParams();
            lp.height = (int) (maxHeight * rmsdB * 0.0025);
            lp.height = Math.max(lp.height , layout.getMeasuredWidth());
            layout.setLayoutParams(lp);
        }

        @Override
        public void onError(int error) {
            showLog(SpeechRecognizorUtils.getErrorMessage(error));
            new Handler().postDelayed(() -> getBinding().lsr.getRoot().setVisibility(View.GONE), 1000);
        }

        @Override
        public void onResults(Bundle results) {
            long end2finish = System.currentTimeMillis() - speechEndTime;

            ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            LogUtils.d("onResults: " + nbest);
            showLog(getString(R.string.recognize_successfully) + "：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
            String json_res = results.getString("origin_result");
            try {
                showLog("origin_result=\n" + new JSONObject(json_res).toString(4));
            } catch (Exception e) {
                showLog("origin_result=[warning: bad json]\n" + json_res);
            }
            if (end2finish < 60 * 1000) {
                LogUtils.d("onResults: " + "(waited " + end2finish + "ms)");
            }
            getBinding().lsr.tvResult.setText(nbest.get(0));

            createAssignment(nbest.get(0));

            new Handler().postDelayed(() -> getBinding().lsr.getRoot().setVisibility(View.GONE), 1000);
        }

        private void showLog(String msg) {
            getBinding().lsr.tvLog.append(msg + "\n");
            LogUtils.d("----" + msg);
        }
    };

    private View.OnTouchListener fabTouchListener = new View.OnTouchListener() {

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (!isInRecognitionMode){
                return false;
            } else {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int status = mAudioManager.requestAudioFocus(null,
                                AudioManager.STREAM_MUSIC,
                                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                        if (status != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                            return true;
                        }
                        touchStartTime = System.currentTimeMillis();
                        actionDownY = event.getY();
                        getBinding().lsr.getRoot().setVisibility(View.VISIBLE);
                        speechRecognizer.cancel();
                        Intent intent = new Intent();
                        bindParams(intent);
                        intent.putExtra("vad", "touch");
                        getBinding().lsr.tvResult.setText("");
                        getBinding().lsr.tvLog.setText("");
                        speechRecognizer.startListening(intent);
                        getBinding().lsr.tvRecognizeTip.setText(R.string.listening);
                        getBinding().lsr.tvActionTip.setText(R.string.move_up_to_cancel);
                        getBinding().lsr.civOuter.startAnimation(outerScaleAnim);
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        LogUtils.d("onTouch: " + event.getY());
                        if (actionDownY - event.getY() > 230) {
                            getBinding().lsr.tvActionTip.setText(R.string.release_to_cancel);
                            getBinding().lsr.tvActionTip.setTextColor(primaryColor());
                        } else {
                            getBinding().lsr.tvActionTip.setText(R.string.move_up_to_cancel);
                            getBinding().lsr.tvActionTip.setTextColor(isDarkTheme() ? Color.WHITE : Color.BLACK);
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if (System.currentTimeMillis() - touchStartTime < 500){
                            // 只是按了一下，并非要进行识别
                            return true;
                        }
                        LogUtils.d("onTouch: " + event.getY());
                        if (actionDownY - event.getY() > 230){
                            // 放弃识别
                            getBinding().lsr.getRoot().setVisibility(View.GONE);
                            speechRecognizer.cancel();
                        } else {
                            speechRecognizer.stopListening();
                            getBinding().lsr.tvRecognizeTip.setText(R.string.recognizing);
                        }
                        outerScaleAnim.cancel();
                        mAudioManager.abandonAudioFocus(null);
                        break;
                }
                return true;
            }
        }
    };

    private void bindParams(Intent intent) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        if (sp.getBoolean("tips_sound", true)) {
            intent.putExtra(BaiduConstants.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
            intent.putExtra(BaiduConstants.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
            intent.putExtra(BaiduConstants.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
            intent.putExtra(BaiduConstants.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
            intent.putExtra(BaiduConstants.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        }
        if (sp.contains(BaiduConstants.EXTRA_INFILE)) {
            String tmp = sp.getString(BaiduConstants.EXTRA_INFILE, "").replaceAll(",.*", "").trim();
            intent.putExtra(BaiduConstants.EXTRA_INFILE, tmp);
        }
        if (sp.getBoolean(BaiduConstants.EXTRA_OUTFILE, false)) {
            intent.putExtra(BaiduConstants.EXTRA_OUTFILE, "sdcard/outfile.pcm");
        }
        if (sp.contains(BaiduConstants.EXTRA_SAMPLE)) {
            String tmp = sp.getString(BaiduConstants.EXTRA_SAMPLE, "").replaceAll(",.*", "").trim();
            if (!"".equals(tmp)) {
                intent.putExtra(BaiduConstants.EXTRA_SAMPLE, Integer.parseInt(tmp));
            }
        }
        if (sp.contains(BaiduConstants.EXTRA_LANGUAGE)) {
            String tmp = sp.getString(BaiduConstants.EXTRA_LANGUAGE, "").replaceAll(",.*", "").trim();
            if (!"".equals(tmp)) {
                intent.putExtra(BaiduConstants.EXTRA_LANGUAGE, tmp);
            }
        }
        if (sp.contains(BaiduConstants.EXTRA_NLU)) {
            String tmp = sp.getString(BaiduConstants.EXTRA_NLU, "").replaceAll(",.*", "").trim();
            if (!"".equals(tmp)) {
                intent.putExtra(BaiduConstants.EXTRA_NLU, tmp);
            }
        }
        if (sp.contains(BaiduConstants.EXTRA_VAD)) {
            String tmp = sp.getString(BaiduConstants.EXTRA_VAD, "").replaceAll(",.*", "").trim();
            if (!"".equals(tmp)) {
                intent.putExtra(BaiduConstants.EXTRA_VAD, tmp);
            }
        }
        String prop = null;
        if (sp.contains(BaiduConstants.EXTRA_PROP)) {
            String tmp = sp.getString(BaiduConstants.EXTRA_PROP, "").replaceAll(",.*", "").trim();
            if (!"".equals(tmp)) {
                intent.putExtra(BaiduConstants.EXTRA_PROP, Integer.parseInt(tmp));
                prop = tmp;
            }
        }
        // offline asr
        {
            intent.putExtra(BaiduConstants.EXTRA_OFFLINE_ASR_BASE_FILE_PATH, "/sdcard/easr/s_1");
            intent.putExtra(BaiduConstants.EXTRA_LICENSE_FILE_PATH, "/sdcard/easr/license-tmp-20150530.txt");
            if (null != prop) {
                int propInt = Integer.parseInt(prop);
                if (propInt == 10060) {
                    intent.putExtra(BaiduConstants.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_Navi");
                } else if (propInt == 20000) {
                    intent.putExtra(BaiduConstants.EXTRA_OFFLINE_LM_RES_FILE_PATH, "/sdcard/easr/s_2_InputMethod");
                }
            }
            intent.putExtra(BaiduConstants.EXTRA_OFFLINE_SLOT_DATA, buildTestSlotData());
        }
    }

    private String buildTestSlotData() {
        JSONObject slotData = new JSONObject();
        return slotData.toString();
    }

    private void exitRecognition() {
        int windowWidth = ViewUtils.getWindowWidth(PalmApp.getContext());
        ObjectAnimator moveAnimation = ObjectAnimator.ofFloat(getBinding().fab,
                "x",
                windowWidth / 2 - getBinding().fab.getWidth() / 2,
                fabStartPos
        ).setDuration(400);
        moveAnimation.setInterpolator(new AccelerateInterpolator(2));
        moveAnimation.start();
        moveAnimation.addListener(new PalmAnimatorListener(){
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                getBinding().fab.setImageResource(R.drawable.ic_add_white);
                getBinding().ivMic.setImageResource(R.drawable.ic_mic_white_24dp);
            }
        });
        isInRecognitionMode = false;
    }
    // endregion

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isInEditMode) {
            menu.findItem(R.id.action_search).setVisible(false);
            // todo
//            menu.findItem(R.id.action_filter).setIcon(R.drawable.ic_filter_list_white);
        } else {
            MenuItem menuItem = menu.findItem(R.id.action_search);
            if (menuItem != null) {
                menuItem.setVisible(true);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (isInEditMode){
            inflater.inflate(R.menu.edit_mode, menu);
        } else {
            inflater.inflate(R.menu.capture, menu);
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
                getActivity().invalidateOptionsMenu();
                getBinding().etAssignmentTitle.setText("");
                break;
            case R.id.action_capture:
                createScreenCapture(getBinding().recyclerview, ViewUtils.dp2Px(PalmApp.getContext(), 60));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

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

        createAssignment(getBinding().etAssignmentTitle.getText().toString());

        getBinding().etAssignmentTitle.setText("");
        getActivity().invalidateOptionsMenu();
        isInEditMode = false;
    }

    private void createAssignment(String title) {
        Assignment assignment = getNewAssignment();
        assignment.setName(title);

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
        if (isInRecognitionMode){
            exitRecognition();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        if (mAdapter.isStateChanged()) {
            updateState();
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
    // endregion

    public interface AssignmentsFragmentInteraction {
        default void onAssignmentDataChanged() {}
        void onActivityAttached();
        void onAssignmentsLoadStateChanged(me.shouheng.omnilist.model.data.Status status);
    }
}