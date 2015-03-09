package com.ruptech.k_app.http;

import org.fdroid.fdroid.FDroidApp;
import org.fdroid.fdroid.data.App;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HttpServer extends HttpConnection {
    private final String TAG = HttpConnection.class.getPackage() + "." + HttpConnection.class.getSimpleName();

    protected String getAppServerUrl() {
        return FDroidApp.properties.getProperty("SERVER_BASE_URL1");
    }

    public List<App> getApps(String lastQueryTime) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("lastQueryTime",lastQueryTime);

        Response res = _get("user.php", params);
                JSONArray items =  res.asJSONArray();
        int size = items.length();
        List<App> appList = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            JSONObject jo = items.getJSONObject(i);
            App app = new App(jo);
            appList.add(app);
        }
        return appList;
    }

}
