package me.shouheng.omnilist.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import me.shouheng.omnilist.async.NormalAsyncTask;
import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.provider.BaseStore;
import me.shouheng.omnilist.provider.CategoryStore;
import me.shouheng.omnilist.provider.schema.CategorySchema;


/**
 * Created by WangShouheng on 2018/3/13.*/
public class CategoryRepository extends BaseRepository<Category> {

    @Override
    protected BaseStore<Category> getStore() {
        return CategoryStore.getInstance();
    }

    public LiveData<Resource<List<Category>>> getCategories(Status status) {
        MutableLiveData<Resource<List<Category>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            if (status == Status.ARCHIVED) {
                return getStore().getArchived(null, CategorySchema.CATEGORY_ORDER);
            } else if (status == Status.TRASHED) {
                return getStore().getTrashed(null, CategorySchema.CATEGORY_ORDER);
            } else {
                return getStore().get(null, CategorySchema.CATEGORY_ORDER);
            }
        }).execute();
        return result;
    }

    public LiveData<Resource<List<Category>>> updateOrders(List<Category> categories) {
        MutableLiveData<Resource<List<Category>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            ((CategoryStore) getStore()).updateOrders(categories);
            return categories;
        }).execute();
        return result;
    }
}
