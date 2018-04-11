package me.shouheng.omnilist.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import me.shouheng.omnilist.async.NormalAsyncTask;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.provider.BaseStore;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;

public class AssignmentRepository extends BaseRepository<Assignment> {

    @Override
    protected BaseStore<Assignment> getStore() {
        return AssignmentsStore.getInstance();
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(Category category, Status status, boolean includeCompleted) {
        MutableLiveData<Resource<List<Assignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            String whereSQL = AssignmentSchema.CATEGORY_CODE + " = " + category.getCode()
                    + (includeCompleted ? "" : " AND " + AssignmentSchema.PROGRESS + " != 100 ");
            return getStore().get(whereSQL, AssignmentSchema.ASSIGNMENT_ORDER, status, false);
        }).execute();
        return result;
    }
}
