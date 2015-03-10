package com.ruptech.k_app.task.impl;

import com.ruptech.k_app.task.GenericTask;
import com.ruptech.k_app.task.TaskResult;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.data.App;

import java.util.List;

public class RetrieveAppsTask extends GenericTask {

    private final String lastRetrieveTime;
    private List<App> appList;

	public RetrieveAppsTask(String lastRetrieveTime) {
		super();
        this.lastRetrieveTime = lastRetrieveTime;
	}

	@Override
	protected TaskResult _doInBackground() throws Exception {
		appList = FDroidApp.getHttpServer().getApps(lastRetrieveTime);
		return TaskResult.OK;
	}

	public List<App> getAppList() {
		return appList;
	}
}