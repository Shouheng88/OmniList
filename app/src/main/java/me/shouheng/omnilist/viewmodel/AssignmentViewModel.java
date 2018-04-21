package me.shouheng.omnilist.viewmodel;

import android.arch.lifecycle.LiveData;

import java.util.List;

import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.provider.schema.BaseSchema;
import me.shouheng.omnilist.repository.AssignmentRepository;
import me.shouheng.omnilist.repository.BaseRepository;
import me.shouheng.omnilist.utils.preferences.SearchPreferences;

public class AssignmentViewModel extends BaseViewModel<Assignment> {

    @Override
    protected BaseRepository<Assignment> getRepository() {
        return new AssignmentRepository();
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(Category category, Status status, boolean includeCompleted) {
        return ((AssignmentRepository) getRepository()).getAssignments(category, status, includeCompleted);
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(String queryString, String whereSQL) {
        return getRepository().get(getQueryConditions(queryString), whereSQL);
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(long startMillis, long endMillis) {
        return ((AssignmentRepository) getRepository()).getAssignments(startMillis, endMillis);
    }

    private String getQueryConditions(String queryString) {
        SearchPreferences searchPreferences = SearchPreferences.getInstance();
        return (searchPreferences.isTagsIncluded() ?
                " ( " + AssignmentSchema.NAME + " LIKE '%'||'" + queryString + "'||'%' " + " OR " + AssignmentSchema.TAGS + " LIKE '%'||'" + queryString + "'||'%' ) " : AssignmentSchema.NAME + " LIKE '%'||'" + queryString + "'||'%'")
                + (searchPreferences.isArchivedIncluded() ? "" : " AND " + BaseSchema.STATUS + " != " + Status.ARCHIVED.id)
                + (searchPreferences.isTrashedIncluded() ? "" : " AND " + BaseSchema.STATUS + " != " + Status.TRASHED.id)
                + " AND " + BaseSchema.STATUS + " != " + Status.DELETED.id;
    }

    public LiveData<Resource<List<Assignment>>> updateAssignments(List<Assignment> assignments) {
        return ((AssignmentRepository) getRepository()).updateAssignments(assignments);
    }

    public LiveData<Resource<List<Assignment>>> updateOrders(List<Assignment> assignments) {
        return ((AssignmentRepository) getRepository()).updateOrders(assignments);
    }
}
