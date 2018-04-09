package me.shouheng.omnilist.provider;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import me.shouheng.omnilist.PalmApp;
import me.shouheng.omnilist.model.Weather;
import me.shouheng.omnilist.model.enums.WeatherType;
import me.shouheng.omnilist.provider.helper.StoreHelper;
import me.shouheng.omnilist.provider.schema.WeatherSchema;


/**
 * Created by shouh on 2018/3/19.*/
public class WeatherStore extends BaseStore<Weather> {

    private static WeatherStore sInstance = null;

    public static WeatherStore getInstance(){
        if (sInstance == null){
            synchronized (WeatherStore.class) {
                if (sInstance == null) {
                    sInstance = new WeatherStore();
                }
            }
        }
        return sInstance;
    }

    private WeatherStore() {
        super(PalmApp.getContext());
    }

    @Override
    protected void afterDBCreated(SQLiteDatabase db) {}

    @Override
    protected void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    @Override
    protected void fillModel(Weather model, Cursor cursor) {
        model.setType(WeatherType.getTypeById(cursor.getInt(cursor.getColumnIndex(WeatherSchema.WEATHER_TYPE))));
        model.setTemperature(cursor.getColumnIndex(WeatherSchema.TEMPERATURE));
    }

    @Override
    protected void fillContentValues(ContentValues values, Weather model) {
        values.put(WeatherSchema.WEATHER_TYPE, model.getType().id);
        values.put(WeatherSchema.TEMPERATURE, model.getTemperature());
    }
}
