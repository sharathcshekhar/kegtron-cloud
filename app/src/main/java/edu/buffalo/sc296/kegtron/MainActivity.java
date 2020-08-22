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

import android.Manifest;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private BluetoothAdapter mBTAdapter;
    private static final String KEG_TAG = "MAIN_ACTIVITY";

    private static final int FIRST_SYNC_DELAY_MS = 1000;
    private static final int PERM_REQ_CODE_LOCATION = 0;

    public static final String STATUS_NOT_INITIALIZED = "Status: Not Initialized";
    public static final String STATUS_SYNCING = "Status: Syncing";
    public static final String STATUS_STOPPED = "Status: Stopped";

    private static final String[] APP_PERMISSIONS =
            new String[]{/*"android.permission.ACCESS_COARSE_LOCATION", */"android.permission.ACCESS_FINE_LOCATION"};

    private AppSettingsHelper mSettingsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        checkBt();
        mSettingsHelper = new AppSettingsHelper(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        init();
    }

    private void init() {

        String key = mSettingsHelper.getAppAccessKey();
        if (key != null) {
            EditText keyEt = findViewById(R.id.editTextAccessKey);
            keyEt.setText(key);
        }
        String url = mSettingsHelper.getAppUrl();
        if (url != null) {
            EditText urlEt = findViewById(R.id.editTextWebUrl);
            urlEt.setText(url);
        }
        Integer f = mSettingsHelper.getSyncFrequency();
        if (f != null) {
            EditText syncEt = findViewById(R.id.editTextSyncInterval);
            syncEt.setText(String.valueOf(f));
        }

        String status = mSettingsHelper.getAppStatus();
        if (status == null) {
            status = STATUS_NOT_INITIALIZED;
            mSettingsHelper.setAppStatus(status);
        }

        TextView statusTv = findViewById(R.id.textViewStatus);
        statusTv.setText(status);

        String syncStatus = mSettingsHelper.getLastSycStatus();
        if (syncStatus != null) {
            statusTv.append('\n' + syncStatus);
        }
    }

    private void checkBt() {

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // BT check
        BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            mBTAdapter = manager.getAdapter();
        }
        if (mBTAdapter == null) {
            Toast.makeText(this, R.string.bt_unavailable, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!mBTAdapter.isEnabled()) {
            Toast.makeText(this, R.string.bt_disabled, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
    }

    private void startSyncService() {
        Log.i(KEG_TAG, "Starting device Service");

        mSettingsHelper.setAppStatus(STATUS_SYNCING);
        TextView statusTv = findViewById(R.id.textViewStatus);
        statusTv.setText(STATUS_SYNCING);

        Intent intent = new Intent(this, SyncService.class);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,
                SyncService.FIST_SCAN_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        long delay_ts = System.currentTimeMillis() + FIRST_SYNC_DELAY_MS;
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delay_ts, pendingIntent);
    }

    private void startSyncServiceWithPermissionCheck() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            startSyncService();
        } else {
            ActivityCompat.requestPermissions(this, APP_PERMISSIONS, PERM_REQ_CODE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERM_REQ_CODE_LOCATION:
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSyncService();
                }
                break;
            default:
                break;
        }
    }

    public void onStartClick(View v) {
        EditText etSync = findViewById(R.id.editTextSyncInterval);
        EditText etUrl = findViewById(R.id.editTextWebUrl);
        EditText etAccessKey = findViewById(R.id.editTextAccessKey);

        int syncInterval = Integer.parseInt(etSync.getText().toString());
        mSettingsHelper.setSyncFrequency(syncInterval);

        mSettingsHelper.setAppUrl(etUrl.getText().toString());
        mSettingsHelper.setAppAccessKey(etAccessKey.getText().toString());

        startSyncServiceWithPermissionCheck();
    }

    public void onStopClick(View v) {
        Log.i(KEG_TAG, "Stop Scanning");

        mSettingsHelper.setAppStatus(STATUS_STOPPED);
        TextView statusTv = findViewById(R.id.textViewStatus);
        statusTv.setText(STATUS_STOPPED);

        Intent intent = new Intent(this, SyncService.class);
        intent.putExtra("requestCode", SyncService.STOP_SCAN_REQ_CODE);
        sendBroadcast(intent);
    }
}
