/*
 * Copyright (C) 2010-12  Ciaran Gultnieks, ciaran@ciarang.com
 * Copyright (C) 2009  Roberto Jacinto, roberto.jacinto@caixamagica.pt
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package org.fdroid.fdroid;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ruptech.k_app.R;

import org.fdroid.fdroid.compat.TabManager;
import org.fdroid.fdroid.data.AppProvider;
import org.fdroid.fdroid.data.NewRepoConfig;
import org.fdroid.fdroid.views.AppListFragmentPagerAdapter;
import org.fdroid.fdroid.views.ManageReposActivity;

public class FDroid extends ActionBarActivity {

    public static final int REQUEST_APPDETAILS = 0;
    public static final int REQUEST_MANAGEREPOS = 1;
    public static final int REQUEST_PREFS = 2;
    public static final int REQUEST_ENABLE_BLUETOOTH = 3;
    public static final int REQUEST_SWAP = 4;

    public static final String EXTRA_TAB_UPDATE = "extraTab";

    public static final String ACTION_ADD_REPO = "org.fdroid.fdroid.FDroid.ACTION_ADD_REPO";

    private FDroidApp fdroidApp = null;

    private ViewPager viewPager;

    private TabManager tabManager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        fdroidApp = ((FDroidApp) getApplication());
        fdroidApp.applyTheme(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.fdroid);
        createViews();

        getTabManager().createTabs();

        // Start a search by just typing
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        Intent i = getIntent();
        Uri data = i.getData();
        String appid = null;
        if (data != null) {
            if (data.isHierarchical()) {
                // http(s)://f-droid.org/repository/browse?fdid=app.id
                appid = data.getQueryParameter("fdid");
            }
        } else if (i.hasExtra(EXTRA_TAB_UPDATE)) {
            boolean showUpdateTab = i.getBooleanExtra(EXTRA_TAB_UPDATE, false);
            if (showUpdateTab) {
                getTabManager().selectTab(2);
            }
        }
        if (appid != null && appid.length() > 0) {
            Intent call = new Intent(this, AppDetails.class);
            call.putExtra(AppDetails.EXTRA_APPID, appid);
            startActivityForResult(call, REQUEST_APPDETAILS);
        }

        Uri uri = AppProvider.getContentUri();
        getContentResolver().registerContentObserver(uri, true, new AppObserver());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // AppDetails and RepoDetailsActivity set different NFC actions, so reset here
        NfcHelper.setAndroidBeam(this, getApplication().getPackageName());
        checkForAddRepoIntent();
    }

    private void checkForAddRepoIntent() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        getTabManager().onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        if (fdroidApp.bluetoothAdapter == null) {
            // ignore on devices without Bluetooth
            MenuItem btItem = menu.findItem(R.id.action_bluetooth_apk);
            btItem.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_update_repo:
                updateRepos();
                return true;

            case R.id.action_manage_repos:
                Intent i = new Intent(this, ManageReposActivity.class);
                startActivityForResult(i, REQUEST_MANAGEREPOS);
                return true;

            case R.id.action_settings:
                Intent prefs = new Intent(getBaseContext(), PreferencesActivity.class);
                startActivityForResult(prefs, REQUEST_PREFS);
                return true;

            case R.id.action_search:
                onSearchRequested();
                return true;

            case R.id.action_bluetooth_apk:
                /*
                 * If Bluetooth has not been enabled/turned on, then enabling
                 * device discoverability will automatically enable Bluetooth
                 */
                Intent discoverBt = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverBt.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 121);
                startActivityForResult(discoverBt, REQUEST_ENABLE_BLUETOOTH);
                // if this is successful, the Bluetooth transfer is started
                return true;

            case R.id.action_about:
                View view = null;
                if (Build.VERSION.SDK_INT >= 11) {
                    LayoutInflater li = LayoutInflater.from(this);
                    view = li.inflate(R.layout.about, null);
                } else {
                    view = View.inflate(
                            new ContextThemeWrapper(this, R.style.AboutDialogLight),
                            R.layout.about, null);
                }

                // Fill in the version...
                try {
                    PackageInfo pi = getPackageManager()
                            .getPackageInfo(getApplicationContext()
                                    .getPackageName(), 0);
                    ((TextView) view.findViewById(R.id.version))
                            .setText(pi.versionName);
                } catch (Exception e) {
                }

                Builder p = null;
                if (Build.VERSION.SDK_INT >= 11) {
                    p = new Builder(this).setView(view);
                } else {
                    p = new Builder(
                            new ContextThemeWrapper(
                                    this, R.style.AboutDialogLight)
                            ).setView(view);
                }
                final AlertDialog alrt = p.create();
                alrt.setIcon(R.drawable.ic_launcher);
                alrt.setTitle(getString(R.string.about_title));
                alrt.setButton(AlertDialog.BUTTON_NEUTRAL,
                        getString(R.string.about_website),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                Uri uri = Uri.parse("https://f-droid.org");
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            }
                        });
                alrt.setButton(AlertDialog.BUTTON_NEGATIVE, getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                            }
                        });
                alrt.show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch (requestCode) {
        case REQUEST_APPDETAILS:
            break;
        case REQUEST_MANAGEREPOS:
            if (data != null && data.hasExtra(ManageReposActivity.REQUEST_UPDATE)) {
                Builder ask_alrt = new Builder(this);
                ask_alrt.setTitle(getString(R.string.repo_update_title));
                ask_alrt.setIcon(android.R.drawable.ic_menu_rotate);
                ask_alrt.setMessage(getString(R.string.repo_alrt));
                ask_alrt.setPositiveButton(getString(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                updateRepos();
                            }
                        });
                ask_alrt.setNegativeButton(getString(R.string.no),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                    int whichButton) {
                                // do nothing
                            }
                        });
                AlertDialog alert = ask_alrt.create();
                alert.show();
            }
            break;
        case REQUEST_PREFS:
            // The automatic update settings may have changed, so reschedule (or
            // unschedule) the service accordingly. It's cheap, so no need to
            // check if the particular setting has actually been changed.
            UpdateService.schedule(getBaseContext());

            if ((resultCode & PreferencesActivity.RESULT_RESTART) != 0) {
                ((FDroidApp) getApplication()).reloadTheme();
                final Intent intent = getIntent();
                overridePendingTransition(0, 0);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                finish();
                overridePendingTransition(0, 0);
                startActivity(intent);
            }
            break;
        case REQUEST_ENABLE_BLUETOOTH:
            fdroidApp.sendViaBluetooth(this, resultCode, "org.fdroid.fdroid");
            break;
        }
    }

    private void createViews() {
        viewPager = (ViewPager)findViewById(R.id.main_pager);
        AppListFragmentPagerAdapter viewPagerAdapter = new AppListFragmentPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                getTabManager().selectTab(position);
            }
        });
    }

    /**
     * The first time the app is run, we will have an empty app list.
     * If this is the case, we will attempt to update with the default repo.
     * However, if we have tried this at least once, then don't try to do
     * it automatically again, because the repos or internet connection may
     * be bad.
     */
    public boolean updateEmptyRepos() {
        final String TRIED_EMPTY_UPDATE = "triedEmptyUpdate";
        boolean hasTriedEmptyUpdate = getPreferences(MODE_PRIVATE).getBoolean(TRIED_EMPTY_UPDATE, false);
        if (!hasTriedEmptyUpdate) {
            Log.d("FDroid", "Empty app list, and we haven't done an update yet. Forcing repo update.");
            getPreferences(MODE_PRIVATE).edit().putBoolean(TRIED_EMPTY_UPDATE, true).commit();
            updateRepos();
            return true;
        } else {
            Log.d("FDroid", "Empty app list, but it looks like we've had an update previously. Will not force repo update.");
            return false;
        }
    }

    // Force a repo update now. A progress dialog is shown and the UpdateService
    // is told to do the update, which will result in the database changing. The
    // UpdateReceiver class should get told when this is finished.
    public void updateRepos() {
        UpdateService.updateNow(this);
    }

    private TabManager getTabManager() {
        if (tabManager == null) {
            tabManager = new TabManager(this, viewPager);
        }
        return tabManager;
    }

    public void refreshUpdateTabLabel() {
        getTabManager().refreshTabLabel(TabManager.INDEX_CAN_UPDATE);
    }

    public void removeNotification(int id) {
        NotificationManager nMgr = (NotificationManager) getBaseContext()
            .getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(id);
    }

    private class AppObserver extends ContentObserver {

        public AppObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            FDroid.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    refreshUpdateTabLabel();
                }
            });
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

    }

}
