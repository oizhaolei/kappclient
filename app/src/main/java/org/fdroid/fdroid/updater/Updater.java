package org.fdroid.fdroid.updater;

import org.fdroid.fdroid.ProgressListener;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;

import java.util.Collection;

/**
 * Created by zhaolei on 15/3/10.
 */
public interface Updater {
    public void setProgressListener(ProgressListener progressListener);

    void update() throws UpdateException;

    boolean hasChanged();

    Collection<? extends App> getApps();

    Collection<? extends Apk> getApks();
}
