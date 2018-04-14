package me.shouheng.omnilist.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import me.shouheng.omnilist.async.NormalAsyncTask;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.provider.BaseStore;
import me.shouheng.omnilist.provider.SubAssignmentStore;

public class SubAssignmentRepository extends BaseRepository<SubAssignment> {

    @Override
    protected BaseStore<SubAssignment> getStore() {
        return SubAssignmentStore.getInstance();
    }

    public LiveData<Resource<List<SubAssignment>>> getSubAssignments(Assignment assignment, String orderSQL) {
        MutableLiveData<Resource<List<SubAssignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> ((SubAssignmentStore) getStore()).getSubAssignments(assignment, orderSQL)).execute();
        return result;
    }

    public LiveData<Resource<List<SubAssignment>>> updateOrders(List<SubAssignment> subAssignments) {
        MutableLiveData<Resource<List<SubAssignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            ((SubAssignmentStore) getStore()).updateOrders(subAssignments);
            return subAssignments;
        }).execute();
        return result;
    }
}
