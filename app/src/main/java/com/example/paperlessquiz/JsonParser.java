package com.example.paperlessquiz;

import org.json.JSONException;
import org.json.JSONObject;

public interface JsonParser<T> {
    T parse(JSONObject jo) throws JSONException;
}
