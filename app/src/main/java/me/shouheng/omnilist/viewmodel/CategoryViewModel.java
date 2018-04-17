package me.shouheng.omnilist.viewmodel;


import android.arch.lifecycle.LiveData;

import java.util.List;

import me.shouheng.omnilist.model.Category;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.model.enums.Status;
import me.shouheng.omnilist.repository.BaseRepository;
import me.shouheng.omnilist.repository.CategoryRepository;

/**
 * Created by WangShouheng on 2018/3/13.*/
public class CategoryViewModel extends BaseViewModel<Category> {

    @Override
    protected BaseRepository<Category> getRepository() {
        return new CategoryRepository();
    }

    public LiveData<Resource<List<Category>>> getCategories(Status status, boolean showCompleted) {
        return ((CategoryRepository) getRepository()).getCategories(status, showCompleted);
    }

    public LiveData<Resource<List<Category>>> updateOrders(List<Category> categories) {
        CategoryRepository categoryRepository = new CategoryRepository();
        return categoryRepository.updateOrders(categories);
    }
}
