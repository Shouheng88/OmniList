package me.shouheng.omnilist.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.CommonActivity;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.FragmentAssignmentViewerBinding;
import me.shouheng.omnilist.dialog.OpenResolver;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.manager.AttachmentHelper;
import me.shouheng.omnilist.manager.ModelHelper;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.tools.ModelFactory;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.FileHelper;
import me.shouheng.omnilist.utils.IntentUtils;
import me.shouheng.omnilist.utils.PrintUtils;
import me.shouheng.omnilist.utils.ToastUtils;

import static me.shouheng.omnilist.config.Constants.PDF_MIME_TYPE;
import static me.shouheng.omnilist.config.Constants.SCHEME_HTTP;
import static me.shouheng.omnilist.config.Constants.SCHEME_HTTPS;
import static me.shouheng.omnilist.config.Constants.VIDEO_MIME_TYPE;
import static me.shouheng.omnilist.config.Constants._3GP;
import static me.shouheng.omnilist.config.Constants._MP4;
import static me.shouheng.omnilist.config.Constants._PDF;

public class AssignmentViewFragment extends BaseFragment<FragmentAssignmentViewerBinding> {

    private Assignment assignment;
    private List<Attachment> attachments;
    private String mdText;

    public static AssignmentViewFragment newInstance(Assignment assignment, ArrayList<Attachment> attachments, String mdText) {
        Bundle args = new Bundle();
        AssignmentViewFragment fragment = new AssignmentViewFragment();
        args.putSerializable(Constants.EXTRA_MODEL, assignment);
        args.putParcelableArrayList(Constants.EXTRA_ATTACHMENTS, attachments);
        args.putString(Constants.EXTRA_MARKDOWN_CONTENT, mdText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_assignment_viewer;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        handleIntent();

        configToolbar();

        configViews();
    }

    private void handleIntent() {
        Bundle args = getArguments();
        assert args != null;
        assignment = (Assignment) args.getSerializable(Constants.EXTRA_MODEL);
        attachments = args.getParcelableArrayList(Constants.EXTRA_ATTACHMENTS);
        mdText = args.getString(Constants.EXTRA_MARKDOWN_CONTENT);
    }

    private void configToolbar() {
        if (getActivity() != null) {
            ((AppCompatActivity) getActivity()).setSupportActionBar(getBinding().toolbar);
            final ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.setTitle(assignment.getName());
                ab.setDisplayHomeAsUpEnabled(false);
            }
            if (!isDarkTheme()) getBinding().toolbar.setPopupTheme(R.style.AppTheme_PopupOverlay);
        }
    }

    private void configViews() {
        getBinding().mdView.getDelegate().setThumbDrawable(PalmApp.getDrawableCompact(
                isDarkTheme() ? R.drawable.fast_scroll_bar_dark : R.drawable.fast_scroll_bar_light));
        getBinding().mdView.getDelegate().setThumbSize(16, 40);
        getBinding().mdView.getDelegate().setThumbDynamicHeight(false);
        getBinding().mdView.setHtmlResource(isDarkTheme());
        getBinding().mdView.parseMarkdown(mdText);
        getBinding().mdView.setOnImageClickedListener((url, urls) -> {
            List<Attachment> attachments = new ArrayList<>();
            Attachment clickedAttachment = null;
            for (String u : urls) {
                Attachment attachment = getAttachmentFormUrl(u);
                attachments.add(attachment);
                if (u.equals(url)) clickedAttachment = attachment;
            }
            AttachmentHelper.resolveClickEvent(getContext(), clickedAttachment, attachments, assignment.getName());
        });
        getBinding().mdView.setOnAttachmentClickedListener(url -> {
            if (!TextUtils.isEmpty(url)){
                Uri uri = Uri.parse(url);

                // Open the http or https link from chrome tab.
                if (SCHEME_HTTPS.equalsIgnoreCase(uri.getScheme())
                        || SCHEME_HTTP.equalsIgnoreCase(uri.getScheme())) {
                    IntentUtils.openWebPage(getContext(), url);
                    return;
                }

                // Open the files of given format.
                if (url.endsWith(_3GP) || url.endsWith(_MP4)) {
                    startActivity(uri, VIDEO_MIME_TYPE);
                } else if (url.endsWith(_PDF)) {
                    startActivity(uri, PDF_MIME_TYPE);
                } else {
                    OpenResolver.newInstance(mimeType ->
                            startActivity(uri, mimeType.mimeType)
                    ).show(getFragmentManager(), "OPEN RESOLVER");
                }
            }
        });
    }

    private void startActivity(Uri uri, String mimeType) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(uri, mimeType);
        if (IntentUtils.isAvailable(getContext(), intent, null)) {
            startActivity(intent);
        } else {
            ToastUtils.makeToast(R.string.activity_not_found_to_resolve);
        }
    }

    private Attachment getAttachmentFormUrl(String url) {
        Uri uri = Uri.parse(url);
        Attachment attachment = ModelFactory.getAttachment();
        attachment.setUri(uri);
        attachment.setMineType(Constants.MIME_TYPE_IMAGE);
        return attachment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.assignment_viewer, menu);
        MenuItem searchItem = menu.findItem(R.id.action_find);
        initSearchView((SearchView) searchItem.getActionView());
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                share();
                break;
            case R.id.action_export:
                export();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void share() {
        new BottomSheet.Builder(Objects.requireNonNull(getContext()))
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
                                ModelHelper.share(getContext(), assignment.getName(), mdText, attachments);
                                break;
                            case R.id.action_share_html:
                                outHtml(true);
                                break;
                            case R.id.action_share_image:
                                createWebCapture(getBinding().mdView, file -> ModelHelper.shareFile(getContext(), file, Constants.MIME_TYPE_IMAGE));
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
                                // Export html
                                outHtml(false);
                                break;
                            case R.id.capture:
                                createWebCapture(getBinding().mdView, file -> ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), file.getPath())));
                                break;
                            case R.id.print:
                                PrintUtils.print(getContext(), getBinding().mdView, assignment.getName());
                                break;
                            case R.id.export_text:
                                outText(false);
                                break;
                        }
                    }

                    @Override
                    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
                })
                .show();
    }

    private void outHtml(boolean isShare) {
        getBinding().mdView.outHtml(html -> {
            try {
                File exDir = FileHelper.getHtmlExportDir();
                File outFile = new File(exDir, FileHelper.getDefaultFileName(".html"));
                FileUtils.writeStringToFile(outFile, html, "utf-8");
                if (isShare) {
                    // Share, do share option
                    ModelHelper.shareFile(getContext(), outFile, Constants.MIME_TYPE_FILES);
                } else {
                    // Not share, just show a message
                    ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), outFile.getPath()));
                }
            } catch (IOException e) {
                ToastUtils.makeToast(R.string.failed_to_create_file);
            }
        });
    }

    private void outText(boolean isShare) {
        try {
            File exDir = FileHelper.getTextExportDir();
            File outFile = new File(exDir, FileHelper.getDefaultFileName(".md"));
            FileUtils.writeStringToFile(outFile, mdText, "utf-8");
            if (isShare) {
                // Share, do share option
                ModelHelper.shareFile(getContext(), outFile, Constants.MIME_TYPE_FILES);
            } else {
                // Not share, just show a message
                ToastUtils.makeToast(String.format(getString(R.string.text_file_saved_to), outFile.getPath()));
            }
        } catch (IOException e) {
            ToastUtils.makeToast(R.string.failed_to_create_file);
        }
    }

    private void initSearchView(SearchView searchView) {
        if (searchView != null) {
            searchView.setQueryHint(getString(R.string.text_find_in_page));
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

                @Override
                public boolean onQueryTextSubmit(String query) {
                    getBinding().mdView.findAllAsync(query);
                    AppCompatActivity activity = (AppCompatActivity) getActivity();
                    if (activity != null) {
                        activity.startSupportActionMode(new ActionModeCallback());
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    return false;
                }
            });
        }
    }

    private class ActionModeCallback implements ActionMode.Callback {
        @Override
        public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
            actionMode.getMenuInflater().inflate(R.menu.note_find_action, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.action_close:
                    actionMode.finish();
                    break;
                case R.id.action_next:
                    getBinding().mdView.findNext(true);
                    break;
                case R.id.action_last:
                    getBinding().mdView.findNext(false);
                    break;
            }
            return true;
        }

        @Override
        public void onDestroyActionMode(ActionMode actionMode) {
            getBinding().mdView.clearMatches();
        }
    }

    @Override
    public void onBackPressed() {
        if (getActivity() != null) {
            ((CommonActivity) getActivity()).superOnBackPressed();
        }
    }
}
