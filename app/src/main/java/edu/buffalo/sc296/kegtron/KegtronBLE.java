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

import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class KegtronBLE {
    
    public static final String DEVICE_PREFIX = "Kegtron";
    
    public static final int SINGLE_PORT = 0x00;
    public static final int DUAL_PORT = 0x01;
    public static final int PORT_1 = 0x00;
    public static final int PORT_2 = 0x01;
    public static final int UNCONFIGURED = 0x00;
    public static final int CONFIGURED = 0x01;

    private static final int BYTE_MASK = 0xFF;
    private static final String TAG = "KEGTRON_BLE";

    private static final int SCAN_RECORD_IDX_START = 22;
    private static final int SCAN_RECORD_LEN = 11;
    private static final int SCAN_RECORD_NAME_IDX_START = SCAN_RECORD_IDX_START + SCAN_RECORD_LEN;
    private static final int SCAN_RECORD_NAME_LEN = 20;

    private final byte[] mRecord;
    private final byte[] nameBytes;

    public KegtronBLE(byte[] data) {
        mRecord = Arrays.copyOfRange(data, SCAN_RECORD_IDX_START, SCAN_RECORD_IDX_START + SCAN_RECORD_LEN);
        nameBytes = Arrays.copyOfRange(data, SCAN_RECORD_NAME_IDX_START, SCAN_RECORD_NAME_IDX_START + SCAN_RECORD_NAME_LEN);
    }
    
    public KegStats getKegStats() {

        KegStats stat = new KegStats();

        if ((mRecord[0] & BYTE_MASK) != 0x1E || (mRecord[1] & BYTE_MASK) != 0xFF ||
                (mRecord[2] & BYTE_MASK) != 0xFF || (mRecord[3] & BYTE_MASK) != 0xFF) {
            Log.e(TAG, "Header Mismatch. Retuning empty data.");
            return stat;
        }

        String name = new String(nameBytes, StandardCharsets.UTF_8);

        int kegSize = ((mRecord[4] & BYTE_MASK) << 8) | (mRecord[5] & BYTE_MASK);
        int volInit = ((mRecord[6] & BYTE_MASK) << 8) | (mRecord[7] & BYTE_MASK);
        int volDispensed = ((mRecord[8] & BYTE_MASK) << 8) | (mRecord[9] & BYTE_MASK);

        stat.setInitVol(volInit);
        stat.setDispensed(volDispensed);
        stat.setSize(kegSize);
        stat.setBeerName(name.replaceAll("\\s+", ""));
        stat.setTapNumber(getTapNumber());
        return stat;
    }

    public int getPortStateRaw () {
        return mRecord[10] & 0x0F; //0000 1111
    }

    public int getPortIndexRaw() {
        return (mRecord[10] & 0x30) >> 4; //0011 0000
    }

    public int getPortCountRaw() {
        return (mRecord[10] & 0xC0) >> 6; //1100 0000
    }

    public int getTapNumber() {
        int portIdx = getPortIndexRaw();
        if (portIdx == PORT_1) {
            return 1;
        } else if (portIdx == PORT_2) {
            return 2;
        } else {
            return -1;
        }
    }
}
