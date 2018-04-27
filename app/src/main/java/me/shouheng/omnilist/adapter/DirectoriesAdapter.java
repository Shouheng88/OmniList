package me.shouheng.omnilist.adapter;

import android.graphics.drawable.Drawable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.LinkedList;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.model.tools.Directory;
import me.shouheng.omnilist.utils.ColorUtils;

/**
 * Created by shouh on 2018/3/30.*/
public class DirectoriesAdapter extends BaseQuickAdapter<Directory, BaseViewHolder> {

    private Drawable dirIcon;

    public DirectoriesAdapter() {
        super(R.layout.item_directory, new LinkedList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, Directory item) {
        if (ColorUtils.isDarkTheme()) helper.itemView.setBackgroundResource(R.color.dark_theme_background);
        helper.setText(R.id.tv_title, item.getName());
        helper.setImageDrawable(R.id.iv_icon, getDirIcon());
    }

    private Drawable getDirIcon() {
        if (dirIcon == null) {
            dirIcon = ColorUtils.tintDrawable(PalmApp.getDrawableCompact(R.drawable.ic_folder_black_24dp), ColorUtils.primaryColor());
        }
        return dirIcon;
    }
}
