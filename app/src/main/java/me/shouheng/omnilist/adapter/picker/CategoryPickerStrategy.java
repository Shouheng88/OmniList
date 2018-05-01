package me.shouheng.omnilist.adapter.picker;

import android.graphics.drawable.Drawable;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.utils.ColorUtils;


/**
 * Created by shouh on 2018/3/20. */
public class CategoryPickerStrategy implements ModelsPickerStrategy<Category> {

    @Override
    public String getTitle(Category model) {
        return model.getName();
    }

    @Override
    public String getSubTitle(Category model) {
        return PalmApp.getContext().getResources().getQuantityString(
                R.plurals.assignments_number, model.getCount(), model.getCount());
    }

    @Override
    public Drawable getIconDrawable(Category model) {
        Drawable drawable =  PalmApp.getDrawableCompact(model.getPortrait().iconRes);
        return ColorUtils.tintDrawable(drawable, model.getColor());
    }

    @Override
    public boolean shouldShowMore() {
        return false;
    }

    @Override
    public boolean isMultiple() {
        return true;
    }
}
