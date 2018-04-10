package me.shouheng.omnilist.viewmodel;


import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.repository.BaseRepository;
import me.shouheng.omnilist.repository.LocationRepository;

/**
 * Created by shouh on 2018/3/17.*/
public class LocationViewModel extends BaseViewModel<Location> {

    @Override
    protected BaseRepository<Location> getRepository() {
        return new LocationRepository();
    }
}