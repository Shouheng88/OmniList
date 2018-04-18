package me.shouheng.omnilist.activity;

import android.support.v4.app.Fragment;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.activity.base.BaseListActivity;
import me.shouheng.omnilist.fragment.AssignmentsFragment;
import me.shouheng.omnilist.fragment.CategoriesFragment;
import me.shouheng.omnilist.manager.FragmentHelper;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.enums.Status;

/**
 * Created by wangshouheng on 2017/10/10.*/
public class TrashedActivity extends BaseListActivity {

    @Override
    protected CharSequence getActionbarTitle() {
        return getString(R.string.drawer_menu_trash);
    }

    @Override
    protected Fragment getCategoryFragment() {
        return CategoriesFragment.newInstance(Status.TRASHED);
    }

    @Override
    public void onCategorySelected(Category category) {
        AssignmentsFragment assignmentsFragment = AssignmentsFragment.newInstance(category, Status.TRASHED);
        FragmentHelper.replaceWithCallback(this, assignmentsFragment, R.id.fragment_container);
    }
}
