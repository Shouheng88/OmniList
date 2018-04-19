package me.shouheng.omnilist.manager;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import me.shouheng.omnilist.utils.LogUtils;

/**
 * Utility class to pass {@link WakeLock} objects with intents. It contains a
 * factory, which has to be initialized in {@link Application#onCreate()} or
 * otherwise there will be an exception. Instance maintains a {@link Map} of
 * String tag to {@link WakeLock} wakelock */
public class WakeLockManager {

    private static final String EXTRA_WAKELOCK_TAG = "WakeLockManager.EXTRA_WAKELOCK_TAG";

    private static final String EXTRA_WAKELOCK_HASH = "WakeLockManager.EXTRA_WAKELOCK_HASH";

    private static volatile WakeLockManager sInstance;

    private final CopyOnWriteArrayList<WakeLock> wakeLocks;

    private final PowerManager powerManager;

    @Deprecated
    public static WakeLockManager getWakeLockManager() {
        if (sInstance == null) {
            throw new RuntimeException("WakeLockManager was not initialized");
        }
        return sInstance;
    }

    @Deprecated
    public static void init(Context context, boolean debug) {
        if (sInstance != null) {
            LogUtils.d("init: " + "Attempt to re-initialize");
            WakeLockManager oldManager = sInstance;
            sInstance = new WakeLockManager(context, debug);
            sInstance.wakeLocks.addAllAbsent(oldManager.wakeLocks);
        } else {
            sInstance = new WakeLockManager(context, debug);
        }
    }

    private WakeLockManager(Context context, boolean debug) {
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLocks = new CopyOnWriteArrayList<>();
    }

    /**
     * Acquires a partial {@link WakeLock}, stores it internally and puts the
     * tag into the {@link Intent}. To be used with {@link WakeLockManager#releasePartialWakeLock(Intent)}
     *
     * @param intent intent
     * @param wlTag tag */
    public void acquirePartialWakeLock(Intent intent, String wlTag) {
        final WakeLock wl = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, wlTag);
        wl.acquire();
        wakeLocks.add(wl);
        intent.putExtra(WakeLockManager.EXTRA_WAKELOCK_HASH, wl.hashCode());
        intent.putExtra(WakeLockManager.EXTRA_WAKELOCK_TAG, wlTag);
        LogUtils.d("acquirePartialWakeLock: " + wl.toString() + " " + wlTag + " was acquired");
    }

    /**
     * Releases a partial {@link WakeLock} with a tag contained in the given{@link Intent}
     *
     * @param intent intent*/
    private void releasePartialWakeLock(Intent intent) {
        if (intent.hasExtra(WakeLockManager.EXTRA_WAKELOCK_TAG)) {
            final int hash = intent.getIntExtra(WakeLockManager.EXTRA_WAKELOCK_HASH, -1);
            final String tag = intent.getStringExtra(WakeLockManager.EXTRA_WAKELOCK_TAG);
            // We use copy on write list. Iterator won't cause ConcurrentModificationException
            for (WakeLock wakeLock : wakeLocks) {
                if (hash == wakeLock.hashCode()) {
                    if (wakeLock.isHeld()) {
                        wakeLock.release();
                        LogUtils.d("releasePartialWakeLock: " + wakeLock.toString() + " " + tag + " was released");
                    } else {
                        LogUtils.d("releasePartialWakeLock: " + wakeLock.toString() + " " + tag + " was already released!");
                    }
                    wakeLocks.remove(wakeLock);
                    return;
                }
            }
            LogUtils.e("releasePartialWakeLock: " + "Hash " + hash + " was not found");
        }
    }
}
