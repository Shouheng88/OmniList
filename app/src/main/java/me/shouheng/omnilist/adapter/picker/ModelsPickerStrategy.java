package me.shouheng.omnilist.adapter.picker;

import android.graphics.drawable.Drawable;

import me.shouheng.omnilist.model.Model;


/**
 * Created by wangshouheng on 2017/10/5.*/
public interface ModelsPickerStrategy<T extends Model> {

    String getTitle(T model);

    String getSubTitle(T model);

    Drawable getIconDrawable(T model);

    boolean shouldShowMore();

    boolean isMultiple();
}
