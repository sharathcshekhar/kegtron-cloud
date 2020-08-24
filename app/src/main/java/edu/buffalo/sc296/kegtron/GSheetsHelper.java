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
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class GSheetsHelper {

    private static final String KEG_TAG = "KEGTRON_GSHEET_HELPER";

    private final String gURL;
    private final String accessKey;

    private static final String ACTION_NAME = "addItem";
    private static final int SOCKET_TIMEOUT = 5000;

    private Context mContext;

    public GSheetsHelper(Context ctx) {
        AppSettingsHelper settings = new AppSettingsHelper(ctx);
        gURL = settings.getAppUrl();
        accessKey = settings.getAppAccessKey();
        mContext = ctx;
    }

    public void addItemToSheet(KegStats keg,
                               Response.Listener<String> responseHandler,
                               Response.ErrorListener errHandler) {
        Log.i(KEG_TAG, "Uploading data");
        StringRequest stringRequest =
                new StringRequest(Request.Method.POST, gURL, responseHandler, errHandler) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<>();

                        params.put("action", ACTION_NAME);
                        params.put("tap", String.valueOf(keg.getTapNumber()));
                        params.put("dispensed", String.valueOf(keg.getDispensed()));
                        params.put("remaining", String.valueOf(keg.getRemaining()));
                        params.put("size", String.valueOf(keg.getSize()));
                        params.put("access_key", accessKey);

                        return params;
                    }
                };

        RetryPolicy retryPolicy = new DefaultRetryPolicy(SOCKET_TIMEOUT, 5, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        stringRequest.setRetryPolicy(retryPolicy);

        RequestQueue queue = Volley.newRequestQueue(mContext);

        queue.add(stringRequest);
    }
}
