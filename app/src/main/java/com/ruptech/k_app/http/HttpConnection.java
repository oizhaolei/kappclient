package com.ruptech.k_app.http;

import android.text.TextUtils;
import android.util.Log;

import com.github.kevinsawicki.http.HttpRequest;
import com.ruptech.k_app.BuildConfig;

import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.github.kevinsawicki.http.HttpRequest.HttpRequestException;

public abstract class HttpConnection {
    private static final String ANONYMOS_USER_ID = "3637";

    private final String TAG = HttpConnection.class.getPackage() + "." + HttpConnection.class.getSimpleName();

    public Response get(String url) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, url);
        }

        String body = HttpRequest.get(url).body();

        return new Response(body);
    }

    public Response post(String url, Map<String, String> postParams) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, url + ", " + postParams);
        }

        String body = HttpRequest.post(url).form(postParams).body();

        return new Response(body);
    }

    /**
     * Returns the base URL
     *
     * @param ifPage 业务接口名
     * @return the base URL
     */

    public String genRequestURL(String ifPage, Map<String, String> params) {

        String url = ifPage;
        if (!url.startsWith("http")) {
            url = getAppServerUrl() + url;
        }
        url += "?" + encodeParameters(params);
        return url;
    }

    private static String encodeParameters(Map<String, String> params)
            throws RuntimeException {
        StringBuffer buf = new StringBuffer();
        String[] keyArray = params.keySet().toArray(new String[0]);
        Arrays.sort(keyArray);
        int j = 0;
        for (String key : keyArray) {
            String value = params.get(key);
            if (j++ != 0) {
                buf.append("&");
            }
            if (!TextUtils.isEmpty(value)) {
                try {
                    buf.append(URLEncoder.encode(key, "UTF-8")).append("=")
                            .append(URLEncoder.encode(value, "UTF-8"));
                } catch (java.io.UnsupportedEncodingException neverHappen) {
                    // throw new RuntimeException(neverHappen.getMessage(),
                    // neverHappen);
                }
            }
        }

        return buf.toString();
    }

    /**
     * Issues an HTTP GET request.
     *
     * @param ifPage the request url
     * @return the response
     * @throws org.apache.http.HttpException
     */
    protected Response _get(String ifPage, Map<String, String> params)
            throws Exception {
        params = genParams(params);

        String url = "";
        for (int i = 0; i < 2; i++) {
            url = genRequestURL(ifPage, params);
            try {
                Response response = get(url);
                return response;
            } catch (HttpRequestException e1) {
                // App.setServerAppInfo(null);
                // Utils.sendClientException(e1);
            }
        }

        throw new NetworkException();
    }

    public static Map<String, String> genParams(Map<String, String> params) {
        if (params == null) {
            params = new HashMap<>();
        }
//		params.put("source", getSource());

        return params;
    }


    abstract String getAppServerUrl();
}
