package me.shouheng.omnilist.fragment;


import android.os.Bundle;
import android.support.v7.widget.RecyclerView;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.FragmentTodayBinding;
import me.shouheng.omnilist.fragment.base.BaseFragment;

public class TodayFragment extends BaseFragment<FragmentTodayBinding> {

    private RecyclerView.OnScrollListener scrollListener;

    public static TodayFragment newInstance() {
        Bundle args = new Bundle();
        TodayFragment fragment = new TodayFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_today;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {

    }

    public void setScrollListener(RecyclerView.OnScrollListener scrollListener) {
        this.scrollListener = scrollListener;
    }
}
