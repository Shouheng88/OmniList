package me.shouheng.omnilist.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.jeek.calendar.widget.schedule.ScheduleRecyclerView;

/**
 * Created by wangshouheng on 2017/3/31.*/
public class EmptySupportScheduleRV extends ScheduleRecyclerView {

    private View emptyView;

    public EmptySupportScheduleRV(Context context) {
        super(context);
    }

    public EmptySupportScheduleRV(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EmptySupportScheduleRV(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private AdapterDataObserver observer = new AdapterDataObserver() {

        @Override
        public void onChanged() {
            showEmptyView();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            showEmptyView();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            showEmptyView();
        }
    };

    public void showEmptyView(){
        Adapter<?> adapter = getAdapter();
        if(adapter!=null && emptyView!=null){
            if(adapter.getItemCount()==0){
                emptyView.setVisibility(View.VISIBLE);
                EmptySupportScheduleRV.this.setVisibility(View.GONE);
            } else{
                emptyView.setVisibility(View.GONE);
                EmptySupportScheduleRV.this.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if(adapter != null){
            adapter.registerAdapterDataObserver(observer);
            observer.onChanged();
        }
    }

    public void setEmptyView(View v){
        emptyView = v;
    }
}
