package me.shouheng.omnilist.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import me.shouheng.omnilist.R;


/**
 * Created by wangshouheng on 2017/4/9.*/
public class AttachmentsRecyclerView extends RecyclerView {

    private View emptyView;

    private TextView tvAttachmentsNum;

    public AttachmentsRecyclerView(Context context) {
        super(context);
    }

    public AttachmentsRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttachmentsRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private AdapterDataObserver observer = new AdapterDataObserver() {
        @Override
        public void onChanged() {
            validAttachmentsNumberTip();
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            validAttachmentsNumberTip();
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
            validAttachmentsNumberTip();
        }
    };

    public void validAttachmentsNumberTip() {
        Adapter<?> adapter = getAdapter();
        if(adapter!=null && emptyView!=null) {
            if(adapter.getItemCount()==0) {
                emptyView.setVisibility(View.VISIBLE);
                AttachmentsRecyclerView.this.setVisibility(View.GONE);

                if (tvAttachmentsNum != null){
                    tvAttachmentsNum.setText(R.string.text_attachment);
                }
            } else {
                emptyView.setVisibility(View.GONE);
                AttachmentsRecyclerView.this.setVisibility(View.VISIBLE);

                if (tvAttachmentsNum != null) {
                    tvAttachmentsNum.setText(getContext().getResources().getQuantityString(R.plurals.attachments_number, adapter.getItemCount(), adapter.getItemCount()));
                }
            }
        }
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if(adapter != null) {
            adapter.registerAdapterDataObserver(observer);
            observer.onChanged();
        }
    }

    public void setEmptyView(View v) {
        emptyView = v;
    }

    public void setAttachmentsNumberTextView(TextView tvAttachmentsNum) {
        this.tvAttachmentsNum = tvAttachmentsNum;
    }
}
