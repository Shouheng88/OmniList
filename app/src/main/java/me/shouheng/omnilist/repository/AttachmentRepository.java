package me.shouheng.omnilist.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import java.util.List;

import me.shouheng.omnilist.async.NormalAsyncTask;
import me.shouheng.omnilist.model.Assignment;
import me.shouheng.omnilist.model.Attachment;
import me.shouheng.omnilist.model.data.Resource;
import me.shouheng.omnilist.provider.AttachmentsStore;
import me.shouheng.omnilist.provider.BaseStore;


/**
 * Created by WangShouheng on 2018/3/13.*/
public class AttachmentRepository extends BaseRepository<Attachment> {

    @Override
    protected BaseStore<Attachment> getStore() {
        return AttachmentsStore.getInstance();
    }

    public LiveData<Resource<Attachment>> saveIfNew(Attachment attachment) {
        MutableLiveData<Resource<Attachment>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            if (getStore().isNewModel(attachment.getCode())) {
                getStore().saveModel(attachment);
            }
            return attachment;
        }).execute();
        return result;
    }

    public LiveData<Resource<List<Attachment>>> updateAttachments(Assignment assignment, List<Attachment> attachments) {
        MutableLiveData<Resource<List<Attachment>>> result = new MutableLiveData<>();
        new NormalAsyncTask<>(result, () -> {
            ((AttachmentsStore) getStore()).updateAttachments(assignment, attachments);
            return attachments;
        }).execute();
        return result;
    }
}
