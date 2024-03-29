package com.ruptech.k_app.http;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Response {
	private String body;

	public Response(String body) {
		this.body = body;
	}

	public JSONArray asJSONArray() throws Exception {
		try {
			return new JSONArray(body);
		} catch (Exception jsone) {
			throw new Exception(jsone.getMessage() + ":" + body, jsone);
		}
	}

	public JSONObject asJSONObject() throws Exception {
		try {
			return new JSONObject(body);
		} catch (JSONException jsone) {
			throw new Exception(jsone.getMessage() + ":" + body, jsone);
		}
	}

	public String getBody() {
		return body;
	}
}
