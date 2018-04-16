package me.shouheng.omnilist.viewmodel;

import me.shouheng.omnilist.model.SubAssignment;
import me.shouheng.omnilist.repository.BaseRepository;
import me.shouheng.omnilist.repository.SubAssignmentRepository;

public class SubAssignmentViewModel extends BaseViewModel<SubAssignment> {

    @Override
    protected BaseRepository<SubAssignment> getRepository() {
        return new SubAssignmentRepository();
    }
}
