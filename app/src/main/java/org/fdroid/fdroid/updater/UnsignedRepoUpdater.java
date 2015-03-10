package org.fdroid.fdroid.updater;

import android.content.Context;
import android.util.Log;

import org.fdroid.fdroid.data.Repo;

import java.io.File;

public class UnsignedRepoUpdater extends RepoUpdater {

    public UnsignedRepoUpdater(Context ctx) {
        super(ctx);
    }

    @Override
    protected File getIndexFromFile(File file) throws UpdateException {
        Log.d("FDroid", "Getting unsigned index from " + getIndexAddress());
        return file;
    }

    @Override
    protected String getIndexAddress() {
        return Repo.address + "/index.xml";
    }
}
