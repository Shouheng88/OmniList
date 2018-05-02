package me.shouheng.omnilist.activity;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.databinding.ActivityWidgetConfigurationBinding;
import me.shouheng.omnilist.dialog.picker.CategoryPickerDialog;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.viewmodel.CategoryViewModel;
import me.shouheng.omnilist.widget.desktop.ListRemoteViewsFactory;


public class ConfigActivity extends AppCompatActivity {

    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    private Category selectedCategory;

    private ActivityWidgetConfigurationBinding binding;

    private CategoryViewModel categoryViewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setResult(RESULT_CANCELED);
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.inflate(getLayoutInflater(),
                R.layout.activity_widget_configuration, null, false);
        setContentView(binding.getRoot());

        categoryViewModel = ViewModelProviders.of(this).get(CategoryViewModel.class);

        handleArguments();

        doCreateView();
    }

    private void handleArguments() {
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        SharedPreferences sharedPreferences = getApplication().getSharedPreferences(
                Constants.PREFS_NAME, Context.MODE_MULTI_PROCESS);
        long categoryCode = sharedPreferences.getLong(
                Constants.PREF_WIDGET_CATEGORY_CODE_PREFIX + String.valueOf(mAppWidgetId),
                0);

        if (categoryCode != 0) {
            fetchCategory(categoryCode);
        }
    }

    private void doCreateView() {
        binding.tvTitle.setBackgroundColor(ColorUtils.primaryColor());
        binding.tvListFilterTip.setTextColor(ColorUtils.accentColor());
        binding.tvListOptionsTip.setTextColor(ColorUtils.accentColor());
        binding.btnPositive.setTextColor(ColorUtils.accentColor());

        binding.spType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                binding.tvCategory.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        updateWhenSelectCategory();

        binding.tvCategory.setOnClickListener(view -> showCategoryPicker());
        binding.btnPositive.setOnClickListener(view -> onConfirm());
    }

    private void fetchCategory(long nbCode) {
        categoryViewModel.get(nbCode).observe(this, notebookResource -> {
            if (notebookResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                return;
            }
            switch (notebookResource.status) {
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_load_data);
                    break;
                case SUCCESS:
                    selectedCategory = notebookResource.data;
                    updateWhenSelectCategory();
                    break;
            }
        });
    }

    private void onConfirm() {
        if (binding.spType.getSelectedItemPosition() == 1 && selectedCategory == null) {
            ToastUtils.makeToast(R.string.widget_category_required);
            return;
        }
        ListRemoteViewsFactory.updateConfiguration(
                getApplicationContext(),
                mAppWidgetId,
                selectedCategory,
                binding.spType.getSelectedItemPosition() == 0,
                binding.cbShowCompleted.isChecked());
        finishWithOK();
    }

    private void showCategoryPicker() {
        CategoryPickerDialog.newInstance()
                .setOnItemSelectedListener((dialog, notebook, position) -> {
                    selectedCategory = notebook;
                    updateWhenSelectCategory();
                    dialog.dismiss();
                })
                .show(getSupportFragmentManager(), "CATEGORY_PICKER");
    }

    private void updateWhenSelectCategory() {
        binding.spType.setSelection(selectedCategory == null ? 0 : 1);
        binding.tvCategory.setVisibility(selectedCategory == null ? View.GONE : View.VISIBLE);
        if (selectedCategory != null) {
            binding.tvCategory.setText(selectedCategory.getName());
            binding.tvCategory.setTextColor(selectedCategory.getColor());
        }
    }

    private void finishWithOK() {
        Intent intent = new Intent();
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
        setResult(RESULT_OK, intent);
        finish();
    }
}
