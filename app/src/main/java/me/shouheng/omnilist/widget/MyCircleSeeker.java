package me.shouheng.omnilist.widget;

import android.content.Context;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.TextView;

import me.shouheng.omnilist.R;


/**
 * Created by wangshouheng on 2017/11/4.*/
public class MyCircleSeeker extends SquareFrameLayout {

    private CircularSeekBar csb;

    private ShowProgressStrategy showProgressStrategy;

    private OnProgressChangedListener onProgressChangedListener;

    public MyCircleSeeker(@NonNull Context context) {
        super(context);
        init(context, null, -1);
    }

    public MyCircleSeeker(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, -1);
    }

    public MyCircleSeeker(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        LayoutInflater.from(context).inflate(R.layout.widget_my_circle_seeker, this);
        showProgressStrategy = new DefaultShowProgressStrategy();
        final TextView tvProgress = (TextView) findViewById(R.id.tv_progress);
        csb = (CircularSeekBar) findViewById(R.id.csb);
        csb.setOnSeekBarChangeListener(new CircularSeekBar.OnCircularSeekBarChangeListener() {
            @Override
            public void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser) {
                tvProgress.setText(showProgressStrategy.format(progress));
                if (onProgressChangedListener != null) {
                    // multiple 5
                    onProgressChangedListener.onProgressChanged(circularSeekBar, progress * 5, fromUser);
                }
            }

            @Override
            public void onStopTrackingTouch(CircularSeekBar seekBar) {}

            @Override
            public void onStartTrackingTouch(CircularSeekBar seekBar) {}
        });
    }

    public int getProgress() {
        return csb.getProgress();
    }

    public void setProgress(int progress) {
        this.csb.setProgress(progress);
    }

    public void setShowProgressStrategy(ShowProgressStrategy showProgressStrategy) {
        this.showProgressStrategy = showProgressStrategy;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener onProgressChangedListener) {
        this.onProgressChangedListener = onProgressChangedListener;
    }

    public interface ShowProgressStrategy {
        String format(int progress);
    }

    public interface OnProgressChangedListener {
        void onProgressChanged(CircularSeekBar circularSeekBar, int progress, boolean fromUser);
    }

    private class DefaultShowProgressStrategy implements ShowProgressStrategy {

        @Override
        public String format(int progress) {
            return String.valueOf(progress * 5);
        }
    }
}
