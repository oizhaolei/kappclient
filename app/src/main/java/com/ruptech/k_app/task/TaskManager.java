package com.ruptech.k_app.task;

import android.util.Log;

import com.ruptech.k_app.BuildConfig;

import java.util.Observable;
import java.util.Observer;

public class TaskManager extends Observable {
	public static final Integer CANCEL_ALL = 1;

	protected final String TAG = "TaskManager";

	public void addTask(Observer task) {
		super.addObserver(task);
	}

	public void cancelAll() {
		if (BuildConfig.DEBUG)
			Log.d(TAG, "All task Cancelled.");
		setChanged();
		notifyObservers(CANCEL_ALL);
	}
}