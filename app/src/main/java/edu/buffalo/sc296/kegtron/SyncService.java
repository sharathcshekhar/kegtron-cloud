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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class SyncService extends BroadcastReceiver {

    public static final int FIST_SCAN_REQ_CODE = 1;
    public static final int PERIODIC_SCAN_REQ_CODE = 2;
    public static final int STOP_SCAN_REQ_CODE = 3;

    private static String TAG = "SYNC_SERVICE";

    private BluetoothAdapter mBluetoothAdapter;
    private Context mContext;

    private boolean mFirstScan = false;
    private AppSettingsHelper mAppSettings;
    private boolean [] mPortsSeen;
    private GSheetsHelper gSheetsHelper;

    private SimpleDateFormat mDateFormatter = new SimpleDateFormat("MM/dd HH:mm:ss", Locale.US);

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received Event.");
        mContext = context;
        mAppSettings = new AppSettingsHelper(mContext);
        gSheetsHelper = new GSheetsHelper(mContext);

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Log.e(TAG, "Failed to init to Bluetooth Manager. Exiting");
            return;
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();

        int code = intent.getIntExtra("requestCode", FIST_SCAN_REQ_CODE);

        mPortsSeen = new boolean[] {false, false};  //currently maximum supported

        if (code == FIST_SCAN_REQ_CODE) {
            Log.i(TAG, "First Scan...");
            mFirstScan = true;
            mAppSettings.setAppScanActive(true);
            scanAllLeDevice();
        } else if (code == PERIODIC_SCAN_REQ_CODE) {
            Log.i(TAG, "Periodic Scan...");
            if (!mAppSettings.getAppScanActive()) {
                Log.i(TAG, "Scanning already stopped");
                return;
            }
            filteredScanLeDevice();
        } else if (code == STOP_SCAN_REQ_CODE) {
            Log.i(TAG, "Stop Scan requested...");
            mAppSettings.setAppScanActive(false);
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
            return;
        } else {
            Log.i(TAG, "Unknown request...");
            return;
        }
        registerAlarm();
    }

    private void cancelAlarm() {
        Intent intent = new Intent(mContext, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, PERIODIC_SCAN_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        if (alarmManager == null) {
            Log.e(TAG, "Failed to get AlarmManager");
        } else {
            Log.i(TAG, "Cancelling previous alarms.");
            alarmManager.cancel(pendingIntent);
        }
    }

    private void registerAlarm() {
        Intent intent = new Intent(mContext, this.getClass());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, PERIODIC_SCAN_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        int syncInterval = mAppSettings.getSyncFrequency();

        long targetTimeInMillis = System.currentTimeMillis() +  TimeUnit.MINUTES.toMillis(syncInterval);

        if (alarmManager == null) {
            Log.e(TAG, "Failed to register alarm.");
        } else {
            Log.i(TAG, "Registering alarm at " + getTimeFromTs(targetTimeInMillis));
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, targetTimeInMillis, pendingIntent);
        }
    }

    private String getTimeFromTs(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return mDateFormatter.format(cal.getTime());
    }

    private ScanCallback mLeScanCallback = new ScanCallback() {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i(TAG, "Received scan results");
            if (result.getDevice() == null || result.getDevice().getName() == null) {
                Log.i(TAG, "Device name null");
                return;
            } else if (!result.getDevice().getName().startsWith(KegtronBLE.DEVICE_PREFIX)) {
                Log.i(TAG, "Device name prefix does not match: " + result.getDevice().getName());
                return;
            } else {
                Log.i(TAG, "Results for Kegtron device: " + result.getDevice().getName());
            }

            if (mFirstScan) {
                String mac = result.getDevice().getAddress();
                String name = result.getDevice().getName();
                Log.i(TAG, "Results from first scan. Mac: " + mac + " Name: " + name);
                mAppSettings.setDeviceMac(mac);
                mAppSettings.setDeviceName(name);
            }

            if (result.getScanRecord() == null) {
                mAppSettings.setLastSyncStatus("Null Scan data " + mDateFormatter.format(new Date()));
                Log.e(TAG, "Can't get scan Record. Null.");
                return;
            }

            KegtronBLE kegBleData = new KegtronBLE(result.getScanRecord().getBytes());

            if (mPortsSeen[kegBleData.getPortIndexRaw()]) {
                // if we have seen this port in this scan, skip it.
                Log.i(TAG, "Port number: " + kegBleData.getPortIndexRaw() + " already seen.");
                return;
            } else {
                mPortsSeen[kegBleData.getPortIndexRaw()] = true;
            }

            int portCntRaw = kegBleData.getPortCountRaw();

            if ((portCntRaw == KegtronBLE.SINGLE_PORT) ||
                    (portCntRaw == KegtronBLE.DUAL_PORT && mPortsSeen[0] && mPortsSeen[1])) {
                Log.i(TAG, "Seen all ports. Stopping scan.");
                mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
            }

            Log.i(TAG, "Port count: " + kegBleData.getPortCountRaw() + " Port State: " +
                    kegBleData.getPortStateRaw() + " Port Index: " + kegBleData.getPortIndexRaw());

            if (kegBleData.getPortStateRaw() == KegtronBLE.UNCONFIGURED) {
                Log.i(TAG, "Port not configured..");
                return;
            }

            KegStats stats = kegBleData.getKegStats();

            Log.i(TAG, stats.toString());

            gSheetsHelper.addItemToSheet(stats, response -> {
                        Log.i(TAG, "Upload Success. Response: " + response);
                        mAppSettings.setLastSyncStatus("Upload Success " +  mDateFormatter.format(new Date()));
                    },
                    error -> {
                        String dt = mDateFormatter.format(new Date());
                        int err = error.networkResponse.statusCode;
                        if (error instanceof AuthFailureError) {
                            Log.e(TAG, "AuthFailureError on upload with status code: " +  err);
                            mAppSettings.setLastSyncStatus("Upload AuthError (" + err + ") " + dt);
                        } else if (error instanceof NetworkError) {
                            Log.e(TAG, "NetworkError on upload with status code: " + err);
                            mAppSettings.setLastSyncStatus("Upload NetworkError (" + err + ") " + dt);
                        } else if (error instanceof ParseError){
                            Log.e(TAG, "ParseError on upload with status code: " + err);
                            mAppSettings.setLastSyncStatus("Upload ParseError (" + err + ") " + dt);
                        } else  if (error instanceof ServerError) {
                            Log.e(TAG, "ServerError on upload with status code: " + err);
                            mAppSettings.setLastSyncStatus("Upload AuthError (" + err + ") " + dt);
                        } else if (error instanceof TimeoutError) {
                            Log.e(TAG, "TimeoutError on upload with status code: " + err);
                            mAppSettings.setLastSyncStatus("Upload TimeoutError (" + err + ") " + dt);
                        } else {
                            Log.e(TAG, "Unknown Error on upload with status code: " + err);
                            mAppSettings.setLastSyncStatus("Upload Error (" + err + ") " + dt);
                        }
                    }
            );
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e(TAG, "Unexpected Batch results");
            mAppSettings.setLastSyncStatus("Unhandled Batch results "  +  mDateFormatter.format(new Date()));
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e(TAG, "Error in scanning " + errorCode);
            mAppSettings.setLastSyncStatus("Scan Failed (" + errorCode + ") " +  mDateFormatter.format(new Date()));
        }
    };

    private void filteredScanLeDevice() {
        Log.i(TAG, "Starting Filtered Scan...");

        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        String name = mAppSettings.getDeviceName();

        ScanFilter filters = new ScanFilter.Builder().setDeviceName(name).build();

        ScanSettings scanSettings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_POWER)
                .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setNumOfMatches(ScanSettings.MATCH_NUM_ONE_ADVERTISEMENT)
                .setReportDelay(0L)
                .build();
        bluetoothLeScanner.startScan(Collections.singletonList(filters), scanSettings, mLeScanCallback);
    }

    private void scanAllLeDevice() {
        Log.i(TAG, "Starting first scan..");
        BluetoothLeScanner bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
        bluetoothLeScanner.startScan(mLeScanCallback);
    }
}