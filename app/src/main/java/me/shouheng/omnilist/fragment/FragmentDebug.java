package me.shouheng.omnilist.fragment;

import android.os.Bundle;
import android.util.SparseArray;

import java.util.List;

import me.shouheng.omnilist.R;
import me.shouheng.omnilist.databinding.FragmentDebugBinding;
import me.shouheng.omnilist.fragment.base.CommonFragment;
import me.shouheng.omnilist.manager.AlarmsManager;
import me.shouheng.omnilist.model.Alarm;
import me.shouheng.omnilist.provider.AlarmsStore;

public class FragmentDebug extends CommonFragment<FragmentDebugBinding> {

    @Override
    protected int getLayoutResId() {
        return R.layout.fragment_debug;
    }

    @Override
    protected void doCreateView(Bundle savedInstanceState) {
        SparseArray<Alarm> array = AlarmsManager.getsInstance().getAlarms();
        StringBuilder sb = new StringBuilder("管理中的闹钟：\n");
        int size = array.size();
        for (int i=0; i<size; i++) {
            sb.append(array.valueAt(i).toChinese()).append("\n==========\n");
        }

        sb.append("数据库中的闹钟：\n");
        List<Alarm> alarms = AlarmsStore.getInstance().get(null, null);
        for (Alarm alarm : alarms) {
            sb.append(alarm.toChinese()).append("\n==========\n");
        }

        getBinding().tvAlarms.setText(sb.toString());
    }
}
