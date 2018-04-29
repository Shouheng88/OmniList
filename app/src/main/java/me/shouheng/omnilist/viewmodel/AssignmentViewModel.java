package me.shouheng.omnilist.viewmodel;

import android.arch.lifecycle.LiveData;

import java.util.List;

import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.repository.AssignmentRepository;
import me.shouheng.omnilist.repository.BaseRepository;

public class AssignmentViewModel extends BaseViewModel<Assignment> {

    @Override
    protected BaseRepository<Assignment> getRepository() {
        return new AssignmentRepository();
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(Category category, Status status, boolean includeCompleted) {
        return ((AssignmentRepository) getRepository()).getAssignments(category, status, includeCompleted);
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(String queryString, String whereSQL) {
        return ((AssignmentRepository) getRepository()).getAssignments(queryString, whereSQL);
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(long startMillis, long endMillis, boolean includeCompleted) {
        return ((AssignmentRepository) getRepository()).getAssignments(startMillis, endMillis, includeCompleted);
    }

    public LiveData<Resource<List<Assignment>>> updateAssignments(List<Assignment> assignments) {
        return ((AssignmentRepository) getRepository()).updateAssignments(assignments);
    }

    public LiveData<Resource<List<Assignment>>> updateOrders(List<Assignment> assignments) {
        return ((AssignmentRepository) getRepository()).updateOrders(assignments);
    }

    public LiveData<Resource<List<Assignment>>> getToday() {
        return ((AssignmentRepository) getRepository()).getToday();
    }
}
