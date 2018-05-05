package me.shouheng.omnilist.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import me.shouheng.omnilist.async.NormalAsyncTask;
import me.shouheng.omnilist.config.Constants;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.AssignmentsStore;
import me.shouheng.omnilist.provider.BaseStore;
import me.shouheng.omnilist.provider.schema.AssignmentSchema;
import me.shouheng.omnilist.provider.schema.BaseSchema;
import me.shouheng.omnilist.utils.TimeUtils;
import me.shouheng.omnilist.utils.preferences.SearchPreferences;

public class AssignmentRepository extends BaseRepository<Assignment> {

    @Override
    protected BaseStore<Assignment> getStore() {
        return AssignmentsStore.getInstance();
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(String queryString, String whereSQL) {
        MutableLiveData<Resource<List<Assignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> getStore().get(getQueryConditions(queryString), whereSQL)).execute();
        return result;
    }

    private String getQueryConditions(String queryString) {
        SearchPreferences searchPreferences = SearchPreferences.getInstance();
        return (searchPreferences.isTagsIncluded() ?
                " ( " + AssignmentSchema.NAME + " LIKE '%'||'" + queryString + "'||'%' " + " OR " + AssignmentSchema.TAGS + " LIKE '%'||'" + queryString + "'||'%' ) " : AssignmentSchema.NAME + " LIKE '%'||'" + queryString + "'||'%'")
                + (searchPreferences.isArchivedIncluded() ? "" : " AND " + BaseSchema.STATUS + " != " + Status.ARCHIVED.id)
                + (searchPreferences.isTrashedIncluded() ? "" : " AND " + BaseSchema.STATUS + " != " + Status.TRASHED.id)
                + " AND " + BaseSchema.STATUS + " != " + Status.DELETED.id;
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

    public LiveData<Resource<List<Assignment>>> updateAssignments(List<Assignment> assignments) {
        MutableLiveData<Resource<List<Assignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            ((AssignmentsStore) getStore()).updateAssignments(assignments);
            return assignments;
        }).execute();
        return result;
    }

    public LiveData<Resource<List<Assignment>>> updateOrders(List<Assignment> assignments) {
        MutableLiveData<Resource<List<Assignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            ((AssignmentsStore) getStore()).updateOrders(assignments);
            return assignments;
        }).execute();
        return result;
    }

    public LiveData<Resource<List<Assignment>>> getAssignments(
            long startMillis, long endMillis, boolean includeCompleted) {
        MutableLiveData<Resource<List<Assignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () ->
                ((AssignmentsStore) getStore()).getAssignments(startMillis, endMillis,
                        (includeCompleted ? "" : AssignmentSchema.TABLE_NAME + "." + AssignmentSchema.PROGRESS + " != 100 "))
        ).execute();
        return result;
    }

    /**
     * Get all assignments to display fot today fragment: overdue assignments and those start today.
     *
     * @return the assignments */
    public LiveData<Resource<List<Assignment>>> getToday() {
        MutableLiveData<Resource<List<Assignment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            long todayEnd = TimeUtils.endToday().getTime();
            return getStore().get(AssignmentSchema.START_TIME + " <= " + todayEnd +
                    " AND " + AssignmentSchema.PROGRESS + " != " + Constants.MAX_ASSIGNMENT_PROGRESS,
                    AssignmentSchema.START_TIME + " DESC, " + AssignmentSchema.NOTICE_TIME, Status.NORMAL, false);
        }).execute();
        return result;
    }
}
