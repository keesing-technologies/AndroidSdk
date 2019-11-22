package com.keesing.kvsclient.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

public class Store {

    public static SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences("KVS-CLIENT", Context.MODE_PRIVATE);
    }

    public static SharedPreferences.Editor getSharedPrefsEditor(Context context){
        return getSharedPrefs(context).edit();
    }

    public static void Save(Context context, String key, Object data) {

        SharedPreferences.Editor prefs = getSharedPrefsEditor(context);
        Gson gson = new Gson();
        String json = gson.toJson(data);

        prefs.putString(key, json);
        prefs.commit();
    }

    public static <T extends Object> T Retreive(Context context, String key, Class<T> type){

        String json = getSharedPrefs(context).getString(key, null);
        if(json == null || json.length() == 0)
            return null;
        GsonBuilder gson = new GsonBuilder();
        Type collectionType = new TypeToken<T>(){}.getType();
        T obj = gson.create().fromJson("", collectionType);
        return obj;
    }

}
