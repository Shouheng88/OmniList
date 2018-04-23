package me.shouheng.omnilist.fragment;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import java.util.List;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.adapter.TimeLinesAdapter;
import me.shouheng.omnilist.databinding.FragmentTimeLineBinding;
import me.shouheng.omnilist.fragment.base.CommonFragment;
import me.shouheng.omnilist.model.TimeLine;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.TimelineStore;
import me.shouheng.omnilist.provider.schema.TimelineSchema;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;

/**
 * Created by wangshouheng on 2017/8/19. */
public class TimeLineFragment extends CommonFragment<FragmentTimeLineBinding> {

    private TimeLinesAdapter adapter;

    private boolean isLoadingMore = false;

    private int modelsCount, pageNumber = 20, startIndex = 0;

    public static TimeLineFragment newInstance() {
        Bundle args = new Bundle();
        TimeLineFragment fragment = new TimeLineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_time_line;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        configToolbar();

        configTimeline();
    }

    private void configToolbar() {
        if (getActivity() != null) {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (ab != null) ab.setTitle(R.string.timeline);
        }
    }

    private void configTimeline() {
        modelsCount = TimelineStore.getInstance().getCount(null, null, false);
        List<TimeLine> timeLines = TimelineStore.getInstance().getPage(startIndex,
                pageNumber,
                TimelineSchema.ADDED_TIME + " DESC ",
                Status.NORMAL,
                false);

        adapter = new TimeLinesAdapter(getContext(), timeLines);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());

        getBinding().rv.setEmptyView(getBinding().ivEmpty);
        getBinding().rv.setLayoutManager(layoutManager);
        getBinding().rv.setAdapter(adapter);
        getBinding().rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int lastVisibleItem = layoutManager.findLastVisibleItemPosition();
                int totalItemCount = layoutManager.getItemCount();
                if (lastVisibleItem + 1 == totalItemCount && dy > 0) {
                    if (!isLoadingMore) {
                        getBinding().mpb.setVisibility(View.VISIBLE);
                        recyclerView.post(() -> loadMoreData());
                    }
                }
            }
        });
    }

    private void loadMoreData() {
        LogUtils.d("startIndex:" + startIndex);
        isLoadingMore = true;
        startIndex += pageNumber;
        if (startIndex > modelsCount) {
            startIndex -= pageNumber;
            ToastUtils.makeToast(R.string.no_more_data);
        } else {
            List<TimeLine> list = TimelineStore.getInstance().getPage(startIndex,
                    pageNumber,
                    TimelineSchema.ADDED_TIME + " DESC ",
                    Status.NORMAL,
                    false);
            adapter.addData(list);
            adapter.notifyDataSetChanged();
        }
        getBinding().mpb.setVisibility(View.GONE);
        isLoadingMore = false;
    }
}
