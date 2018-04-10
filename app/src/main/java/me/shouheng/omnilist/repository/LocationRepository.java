package me.shouheng.omnilist.repository;

import me.shouheng.omnilist.model.Location;
import me.shouheng.omnilist.provider.BaseStore;
import me.shouheng.omnilist.provider.LocationsStore;


/**
 * Created by shouh on 2018/3/17.*/
public class LocationRepository extends BaseRepository<Location> {

    @Override
    protected BaseStore<Location> getStore() {
        return LocationsStore.getInstance();
    }
}
