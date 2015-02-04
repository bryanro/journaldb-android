package com.bryankrosenbaum.journaldb.app.rest;

import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import retrofit.RequestInterceptor;

public class JournalRequestInterceptor implements RequestInterceptor {

    private String TAG = "JournalRequestInterceptor";

    private SharedPreferences sharedPref;
    private static String encodedCredentials = "";

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", encodedCredentials);
    }

    public void setEncodedCredentials(String username, String password) {
        Log.d(TAG, "Credentials: " + username + " | " + password);
        String userAndPassword = username + ":" + password;
        encodedCredentials = "Basic " + Base64.encodeToString(userAndPassword.getBytes(), Base64.NO_WRAP);
    }
}