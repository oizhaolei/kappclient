package com.ruptech.k_app.http;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.data.Apk;
import org.fdroid.fdroid.data.App;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer extends HttpConnection {
    private final String TAG = HttpConnection.class.getPackage() + "." + HttpConnection.class.getSimpleName();

    protected String getAppServerUrl() {
        return FDroidApp.properties.getProperty("SERVER_BASE_URL1");
    }

    public void getApps(String lastQueryTime, List<App> apps, List<Apk> apks) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("last_query_time", lastQueryTime);

        Response res = _get("apps", params);
        JSONArray items = res.asJSONArray();
        int size = items.length();

        for (int i = 0; i < size; i++) {
            JSONObject jo = items.getJSONObject(i);
            App app = new App(jo);
            apps.add(app);
        }
    }

}
