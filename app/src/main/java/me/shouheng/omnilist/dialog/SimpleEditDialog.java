package me.shouheng.omnilist.dialog;

import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.DialogSimpleEditLayoutBinding;
import me.shouheng.omnilist.model.enums.SubAssignmentType;
import me.shouheng.omnilist.utils.ColorUtils;


/**
 * Created by wangshouheng on 2017/3/15. */
public class SimpleEditDialog extends DialogFragment {
    private String content;
    private String title;
    private String previousContent;
    private boolean isNumeric;
    private Integer maxLength;
    private SubAssignmentType subAssignmentType;
    private SimpleAcceptListener simpleAcceptListener;
    private OnGetSubAssignmentListener onGetSubAssignmentListener;
    private DialogSimpleEditLayoutBinding binding;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(LayoutInflater.from(getContext()), R.layout.dialog_simple_edit_layout, null, false);

        if (subAssignmentType != null) {
            binding.llType.setVisibility(View.VISIBLE);
            binding.spType.setSelection(subAssignmentType.ordinal());
            binding.spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (position) {
                        case 0:
                            subAssignmentType = SubAssignmentType.TODO;
                            break;
                        case 1:
                            subAssignmentType = SubAssignmentType.NOTE;
                            break;
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

        binding.etContent.setText(content);
        binding.etContent.addTextChangedListener(textWatcher);
        if (isNumeric) {
            binding.etContent.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            binding.etContent.setSingleLine(true);
        }
        if (maxLength != null) {
            binding.etContent.setFilters(new InputFilter[] {new InputFilter.LengthFilter(maxLength)});
        }

        binding.tvWatcher.setTextColor(ColorUtils.accentColor());
        int len = (content == null ? 0 : content.length());
        binding.tvWatcher.setText(maxLength != null ? len + "/" + maxLength : String.valueOf(len));

        return new AlertDialog.Builder(getContext())
                .setTitle(TextUtils.isEmpty(title) ? PalmApp.getStringCompact(R.string.text_edit) : title)
                .setView(binding.getRoot())
                .setPositiveButton(R.string.text_accept, (dialog, which) -> {
                    String content = binding.etContent.getText().toString();
                    if (simpleAcceptListener != null) {
                        simpleAcceptListener.onAccept(content);
                    }
                    if (onGetSubAssignmentListener != null) {
                        onGetSubAssignmentListener.onAccept(content, subAssignmentType);
                    }
                })
                .setNegativeButton(R.string.text_give_up, (dialog, which) -> dismiss())
                .create();
    }

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
            SimpleEditDialog.this.setCancelable(previousContent != null && previousContent.equals(s.toString()));
            binding.tvWatcher.setText(maxLength != null ? s.length() + "/" + maxLength : String.valueOf(s.length()));
        }
    };

    private void setBuilder(Builder builder) {
        this.maxLength = builder.maxLength;
        this.isNumeric = builder.isNumeric;
        this.title = builder.title;
        this.content = builder.content;
        this.previousContent = builder.content;
        this.subAssignmentType = builder.subAssignmentType;
        this.simpleAcceptListener = builder.simpleAcceptListener;
        this.onGetSubAssignmentListener = builder.onGetSubAssignmentListener;
    }

    public interface SimpleAcceptListener {
        void onAccept(String content);
    }

    public interface OnGetSubAssignmentListener {
        void onAccept(String content, SubAssignmentType subAssignmentType);
    }

    public static class Builder {
        private SimpleAcceptListener simpleAcceptListener;
        private OnGetSubAssignmentListener onGetSubAssignmentListener;
        private String title;
        private String content;
        private boolean isNumeric;
        private Integer maxLength;
        private SubAssignmentType subAssignmentType;

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setContent(String content) {
            this.content = content;
            return this;
        }

        public Builder setNumeric(boolean numeric) {
            isNumeric = numeric;
            return this;
        }

        public Builder setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder setSubAssignmentType(SubAssignmentType subAssignmentType) {
            this.subAssignmentType = subAssignmentType;
            return this;
        }

        public Builder setSimpleAcceptListener(SimpleAcceptListener simpleAcceptListener) {
            this.simpleAcceptListener = simpleAcceptListener;
            return this;
        }

        public Builder setOnGetSubAssignmentListener(OnGetSubAssignmentListener onGetSubAssignmentListener) {
            this.onGetSubAssignmentListener = onGetSubAssignmentListener;
            return this;
        }

        public SimpleEditDialog build() {
            SimpleEditDialog dialog = new SimpleEditDialog();
            dialog.setBuilder(this);
            return dialog;
        }
    }
}
