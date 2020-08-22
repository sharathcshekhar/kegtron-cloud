/*
 * Copyright 2020 Sharath Chandrashekhara
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package edu.buffalo.sc296.kegtron;

import android.content.Context;
import android.content.SharedPreferences;

public class AppSettingsHelper {
    private static final String SYNC_FREQUENCY = "sync_frequency_key";
    private static final String KEGTRON_NAME = "kegtron_name_key";
    private static final String KEGTRON_MAC = "kegtron_mac_key";
    private static final String SYNC_STATUS_ACTIVE = "sync_status_active";
    private static final String APP_URL = "app_url";
    private static final String APP_ACCESS_KEY = "app_access_key";
    private static final String APP_STATUS = "app_status";
    private static final String LAST_SYC_STATUS = "last_sync_status";

    private SharedPreferences mPref;

    public AppSettingsHelper(Context ctx) {
        mPref = ctx.getSharedPreferences(
                ctx.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
    }

    public Integer getSyncFrequency() {
        if (!mPref.contains(SYNC_FREQUENCY)) {
            return null;
        }
        return mPref.getInt(SYNC_FREQUENCY, 0);
    }

    public void setSyncFrequency(int f) {
        mPref.edit().putInt(SYNC_FREQUENCY, f).apply();
    }

    public String getDeviceName() {
        if (!mPref.contains(KEGTRON_NAME)) {
            return null;
        }
        return mPref.getString(KEGTRON_NAME, "");
    }

    public void setDeviceName(String name) {
        mPref.edit().putString(KEGTRON_NAME, name).apply();
    }

    public String getAppUrl() {
        if (!mPref.contains(APP_URL)) {
            return null;
        }
        return mPref.getString(APP_URL, "");
    }

    public void setAppUrl(String url) {
        mPref.edit().putString(APP_URL, url).apply();
    }

    public String getAppAccessKey() {
        if (!mPref.contains(APP_ACCESS_KEY)) {
            return null;
        }
        return mPref.getString(APP_ACCESS_KEY, "");
    }

    public void setAppAccessKey(String key) {
        mPref.edit().putString(APP_ACCESS_KEY, key).apply();
    }

    public String getDeviceMac() {
        if (!mPref.contains(KEGTRON_MAC)) {
            return null;
        }
        return mPref.getString(KEGTRON_MAC, "");
    }

    public void setDeviceMac(String mac) {
        mPref.edit().putString(KEGTRON_MAC, mac).apply();
    }

    public void setAppScanActive(boolean active) {
        mPref.edit().putBoolean(SYNC_STATUS_ACTIVE, active).apply();
    }

    public boolean getAppScanActive() {
        return mPref.getBoolean(SYNC_STATUS_ACTIVE, false);
    }

    public String getAppStatus() {
        if (!mPref.contains(APP_STATUS)) {
            return null;
        }
        return mPref.getString(APP_STATUS, "");
    }

    public void setAppStatus(String key) {
        mPref.edit().putString(APP_STATUS, key).apply();
    }

    public void setLastSyncStatus(String key) {
        mPref.edit().putString(LAST_SYC_STATUS, key).apply();
    }

    public String getLastSycStatus() {
        if (!mPref.contains(LAST_SYC_STATUS)) {
            return null;
        }
        return mPref.getString(LAST_SYC_STATUS, "");
    }
}
