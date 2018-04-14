package me.shouheng.omnilist.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.afollestad.materialdialogs.color.ColorChooserDialog;
import com.afollestad.materialdialogs.color.ColorChooserDialog.ColorCallback;

import java.io.Serializable;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.ActivityContentBinding;
import me.shouheng.omnilist.fragment.AssignmentFragment;
import me.shouheng.omnilist.fragment.base.CommonFragment;
import me.shouheng.omnilist.manager.FragmentHelper;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;


public class ContentActivity extends CommonActivity<ActivityContentBinding> implements ColorCallback {

    // region edit and view note
    public static void editAssignment(Fragment fragment, @NonNull Assignment assignment, int requestCode){
        fragment.startActivityForResult(editIntent(fragment.getContext(), assignment, requestCode), requestCode);
    }

    public static void editAssignment(Activity activity, @NonNull Assignment assignment, int requestCode){
        activity.startActivityForResult(editIntent(activity, assignment, requestCode), requestCode);
    }

    private static Intent editIntent(Context context, @NonNull Assignment note, int requestCode) {
        Intent intent = new Intent(context, ContentActivity.class);
        intent.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_ASSIGNMENT);
        intent.putExtra(Constants.EXTRA_MODEL, (Serializable) note);
        intent.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode);
        return intent;
    }
    // endregion

    public static void resolveThirdPart(Activity activity, Intent i, int requestCode) {
        i.setClass(activity, ContentActivity.class);
        i.putExtra(Constants.EXTRA_IS_GOOGLE_NOW, Constants.INTENT_GOOGLE_NOW.equals(i.getAction()));
        i.setAction(Constants.ACTION_TO_NOTE_FROM_THIRD_PART);
        i.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_ASSIGNMENT);
        i.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode);
        activity.startActivity(i);
    }

    public static void resolveAction(Activity activity, @NonNull Assignment assignment, String action, int requestCode) {
        Intent i = new Intent(activity, ContentActivity.class);
        i.setAction(action);
        i.putExtra(Constants.EXTRA_MODEL, (Parcelable) assignment);
        i.putExtra(Constants.EXTRA_FRAGMENT, Constants.VALUE_FRAGMENT_ASSIGNMENT);
        i.putExtra(Constants.EXTRA_REQUEST_CODE, requestCode);
        activity.startActivity(i);
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_content;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        handleIntent();
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent == null || !intent.hasExtra(Constants.EXTRA_FRAGMENT)) {
            ToastUtils.makeToast(R.string.content_failed_to_parse_intent);
            LogUtils.d("Failed to handle intent : " + intent);
            finish();
            return;
        }
        switch (intent.getStringExtra(Constants.EXTRA_FRAGMENT)) {
            case Constants.VALUE_FRAGMENT_ASSIGNMENT:
                handleAssignmentIntent();
                break;
            default:
                ToastUtils.makeToast(R.string.content_failed_to_parse_intent);
                finish();
                break;
        }
    }

    private void handleAssignmentIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(Constants.EXTRA_MODEL)) {
            if (!(intent.getSerializableExtra(Constants.EXTRA_MODEL) instanceof Assignment)) {
                ToastUtils.makeToast(R.string.content_failed_to_parse_intent);
                LogUtils.d("Failed to resolve note intent : " + intent);
                finish();
                return;
            }
            Assignment assignment = (Assignment) intent.getSerializableExtra(Constants.EXTRA_MODEL);
            int requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, -1);
            toAssignmentFragment(assignment, requestCode == -1 ? null : requestCode, false);
        } else if (Constants.ACTION_TO_NOTE_FROM_THIRD_PART.equals(intent.getAction())) {
            Assignment assignment = ModelFactory.getAssignment();
            toAssignmentFragment(assignment, null, true);
        }

        // The case below mainly used for the intent from shortcut
        if (intent.hasExtra(Constants.EXTRA_CODE)) {
            long code = intent.getLongExtra(Constants.EXTRA_CODE, -1);
            int requestCode = intent.getIntExtra(Constants.EXTRA_REQUEST_CODE, -1);
            Assignment assignment = AssignmentsStore.getInstance().get(code);
            if (assignment == null){
                ToastUtils.makeToast(R.string.text_no_such_assignment);
                LogUtils.d("Failed to resolve intent : " + intent);
                finish();
                return;
            }
            toAssignmentFragment(assignment, requestCode == -1 ? null : requestCode, false);
        }
    }

    private void toAssignmentFragment(Assignment assignment, @Nullable Integer requestCode, boolean isThirdPart){
        String TAG_ASSIGNMENT_FRAGMENT = "tag_assignment_fragment";
        Fragment fragment = AssignmentFragment.newInstance(assignment, requestCode);
        FragmentHelper.replace(this, fragment, R.id.fragment_container, TAG_ASSIGNMENT_FRAGMENT);
    }

    /**
     * Register your events here to receive the color selection message.
     *
     * @param colorChooserDialog the dialog
     * @param i the color selected */
    @Override
    public void onColorSelection(@NonNull ColorChooserDialog colorChooserDialog, @ColorInt int i) {

    }

    @Override
    public void onColorChooserDismissed(@NonNull ColorChooserDialog colorChooserDialog) {}

    @Override
    public void onBackPressed() {
        Fragment currentFragment = getCurrentFragment(R.id.fragment_container);
        if (currentFragment instanceof CommonFragment){
            ((CommonFragment) currentFragment).onBackPressed();
        }
    }
}
