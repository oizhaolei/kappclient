package org.fdroid.fdroid.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.ruptech.k_app.R;

import org.fdroid.fdroid.Utils;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String TAG = "org.fdroid.fdroid.data.DBHelper";

    public static final String DATABASE_NAME = "fdroid";

    // The TABLE_APK table stores details of all the application versions we
    // know about. Each relates directly back to an entry in TABLE_APP.
    // This information is retrieved from the repositories.
    public static final String TABLE_APK = "fdroid_apk";

    private static final String CREATE_TABLE_APK =
            "CREATE TABLE " + TABLE_APK + " ( "
                    + "id text not null, "
                    + "version text not null, "
                    + "repo integer not null, "
                    + "hash text not null, "
                    + "vercode int not null,"
                    + "apkName text not null, "
                    + "size int not null, "
                    + "sig string, "
                    + "srcname string, "
                    + "minSdkVersion integer, "
                    + "maxSdkVersion integer, "
                    + "permissions string, "
                    + "features string, "
                    + "nativecode string, "
                    + "hashType string, "
                    + "added string, "
                    + "compatible int not null, "
                    + "incompatibleReasons text, "
                    + "primary key(id, vercode)"
                    + ");";

    public static final String TABLE_APP = "fdroid_app";
    private static final String CREATE_TABLE_APP = "CREATE TABLE " + TABLE_APP
            + " ( "
            + "id text not null, "
            + "name text not null, "
            + "summary text not null, "
            + "icon text, "
            + "description text not null, "
            + "license text not null, "
            + "webURL text, "
            + "trackerURL text, "
            + "sourceURL text, "
            + "suggestedVercode text,"
            + "upstreamVersion text,"
            + "upstreamVercode integer,"
            + "antiFeatures string,"
            + "donateURL string,"
            + "bitcoinAddr string,"
            + "litecoinAddr string,"
            + "dogecoinAddr string,"
            + "flattrID string,"
            + "requirements string,"
            + "categories string,"
            + "added string,"
            + "lastUpdated string,"
            + "compatible int not null,"
            + "ignoreAllUpdates int not null,"
            + "ignoreThisUpdate int not null,"
            + "iconUrl text, "
            + "primary key(id));";

    public static final String TABLE_INSTALLED_APP = "fdroid_installedApp";
    private static final String CREATE_TABLE_INSTALLED_APP = "CREATE TABLE " + TABLE_INSTALLED_APP
            + " ( "
            + InstalledAppProvider.DataColumns.APP_ID + " TEXT NOT NULL PRIMARY KEY, "
            + InstalledAppProvider.DataColumns.VERSION_CODE + " INT NOT NULL, "
            + InstalledAppProvider.DataColumns.VERSION_NAME + " TEXT NOT NULL, "
            + InstalledAppProvider.DataColumns.APPLICATION_LABEL + " TEXT NOT NULL "
            + " );";

    private static final int DB_VERSION = 46;

    private Context context;

    public DBHelper(Context context, SQLiteDatabase.CursorFactory cf) {
        super(context, DATABASE_NAME, cf, DB_VERSION);
        this.context = context;
    }




    @Override
    public void onCreate(SQLiteDatabase db) {

        createAppApk(db);
        createInstalledApp(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i("FDroid", "Upgrading database from v" + oldVersion + " v"
                + newVersion);

        if (oldVersion < 43) createInstalledApp(db);
        addAppLabelToInstalledCache(db, oldVersion);
    }


    private static void createAppApk(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_APP);
        db.execSQL("create index app_id on " + TABLE_APP + " (id);");
        db.execSQL(CREATE_TABLE_APK);
        db.execSQL("create index apk_vercode on " + TABLE_APK + " (vercode);");
        db.execSQL("create index apk_id on " + TABLE_APK + " (id);");
    }

    private void createInstalledApp(SQLiteDatabase db) {
        Log.d(TAG, "Creating 'installed app' database table.");
        db.execSQL(CREATE_TABLE_INSTALLED_APP);
    }

    private void addAppLabelToInstalledCache(SQLiteDatabase db, int oldVersion) {
        if (oldVersion < 45) {
            Log.i(TAG, "Adding applicationLabel to installed app table. " +
                    "Turns out we will need to repopulate the cache after doing this, " +
                    "so just dropping and recreating the table (instead of altering and adding a column). " +
                    "This will force the entire cache to be rebuilt, including app names.");
            db.execSQL("DROP TABLE fdroid_installedApp;");
            createInstalledApp(db);
        }
    }

    private static boolean columnExists(SQLiteDatabase db,
                                        String table, String column) {
        return (db.rawQuery("select * from " + table + " limit 0,1", null)
                .getColumnIndex(column) != -1);
    }

}
