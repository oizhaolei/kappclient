package com.ruptech.k_app;

import android.app.Application;
import android.test.ApplicationTestCase;

import junit.framework.Assert;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;

import java.util.ArrayList;
import java.util.List;

/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class ApplicationTest extends ApplicationTestCase<Application> {
    public ApplicationTest() {
        super(Application.class);
    }

    public void testGetApps() throws Exception {
        List<App> apps = new ArrayList<>();
        List<Apk> apks = new ArrayList<>();
        FDroidApp.getHttpServer().getApps("", apps, apks);
        Assert.assertTrue(apps.size() >= 0);
        Assert.assertTrue(apks.size() >= 0);

    }
}