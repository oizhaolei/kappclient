package org.fdroid.fdroid.updater;

import android.content.Context;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.ProgressListener;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by zhaolei on 15/3/11.
 */
public class KAppUpdater implements Updater {
    private final Context ctx;
    private List<App> apps = new ArrayList<>();
    private List<Apk> apks = new ArrayList<>();

    public KAppUpdater(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public void setProgressListener(ProgressListener progressListener) {

    }

    @Override
    public void update() throws UpdateException {
        try {
            FDroidApp.getHttpServer().getApps("", apps, apks);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean hasChanged() {
        return false;
    }

    @Override
    public Collection<? extends App> getApps() {
        return apps;
    }

    @Override
    public Collection<? extends Apk> getApks() {
        return apks;
    }
}
