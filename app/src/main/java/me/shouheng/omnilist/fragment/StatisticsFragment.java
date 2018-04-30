package me.shouheng.omnilist.fragment;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.FragmentStatisticsBinding;
import me.shouheng.omnilist.fragment.base.BaseFragment;
import me.shouheng.omnilist.model.data.Status;
import me.shouheng.omnilist.model.tools.Stats;
import me.shouheng.omnilist.utils.ColorUtils;
import me.shouheng.omnilist.utils.LogUtils;
import me.shouheng.omnilist.utils.ToastUtils;
import me.shouheng.omnilist.viewmodel.StatisticViewModel;

/**
 * Created by wang shouheng on 2018/1/19. */
public class StatisticsFragment extends BaseFragment<FragmentStatisticsBinding> {

    private StatisticViewModel statisticViewModel;

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_statistics;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        statisticViewModel = ViewModelProviders.of(this).get(StatisticViewModel.class);

        /*Config toolbar*/
        if (getActivity() != null) {
            ActionBar ab = ((AppCompatActivity) getActivity()).getSupportActionBar();
            if (ab != null) {
                ab.setTitle(R.string.statistic);
            }
        }

        /*Config default values*/
        getBinding().lcvNote.setValueSelectionEnabled(false);
        getBinding().lcvNote.setLineChartData(statisticViewModel.getDefaultAssignmentData(ColorUtils.primaryColor()));
        getBinding().ccvModels.setColumnChartData(statisticViewModel.getDefaultModelsData());
        getBinding().ccvAttachment.setColumnChartData(statisticViewModel.getDefaultAttachmentData());

        /*Output stats*/
        outputStats();
    }

    private void outputStats() {
        notifyLoadStatus(Status.LOADING);
        statisticViewModel.getStats().observe(this, statsResource -> {
            LogUtils.d(statsResource);
            if (statsResource == null) {
                ToastUtils.makeToast(R.string.text_failed_to_load_data);
                notifyLoadStatus(Status.FAILED);
                return;
            }
            notifyLoadStatus(statsResource.status);
            switch (statsResource.status) {
                case SUCCESS:
                    assert statsResource.data != null;
                    outputStats(statsResource.data);
                    break;
                case LOADING:
                    break;
                case FAILED:
                    ToastUtils.makeToast(R.string.text_failed_to_load_data);
                    break;
            }
        });
    }

    private void notifyLoadStatus(Status status) {
        if (getActivity() instanceof OnStatisticInteractListener) {
            ((OnStatisticInteractListener) getActivity()).onStatisticLoadStateChanged(status);
        }
    }

    // region animate to value
    private void outputStats(Stats stats) {
        outputNotesStats(stats.getAssignmentsStats());

        outputModelsStats(Arrays.asList(
                stats.getTotalCategories(),
                stats.getTotalAssignments(),
                stats.getTotalSubAssignments(),
                stats.getTotalAttachments(),
                stats.getTotalLocations()));

        outputAttachmentStats(Arrays.asList(
                stats.getFiles(),
                stats.getImages(),
                stats.getSketches(),
                stats.getVideos(),
                stats.getAudioRecordings()));
    }

    private void outputNotesStats(List<Integer> notes) {
        for (Line line : getBinding().lcvNote.getLineChartData().getLines()) {
            int length = line.getValues().size();
            PointValue pointValue;
            for (int i=0; i<length; i++) {
                pointValue = line.getValues().get(i);
                pointValue.setTarget(pointValue.getX(), notes.get(i));
            }
        }
        getBinding().lcvNote.startDataAnimation();
    }

    private void outputModelsStats(List<Integer> addedModels) {
        int i = 0;
        for (Column column : getBinding().ccvModels.getChartData().getColumns()) {
            for (SubcolumnValue subcolumnValue : column.getValues()) {
                subcolumnValue.setTarget(addedModels.get(i));
            }
            i++;
        }
        getBinding().ccvModels.startDataAnimation();
    }

    private void outputAttachmentStats(List<Integer> attachments) {
        int i = 0;
        for (Column column : getBinding().ccvAttachment.getChartData().getColumns()) {
            for (SubcolumnValue subcolumnValue : column.getValues()) {
                subcolumnValue.setTarget(attachments.get(i));
            }
            i++;
        }
        getBinding().ccvAttachment.startDataAnimation();
    }
    // endregion

    public interface OnStatisticInteractListener {
        void onStatisticLoadStateChanged(Status status);
    }
}
