/*
* Copyright 2015 The Android Open Source Project
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package com.example.android.basicpermissions;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.android.basicpermissions.camera.CameraPreviewActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * Launcher Activity that demonstrates the use of runtime permissions for Android M.
 * This Activity requests permissions to access the camera
 * ({@link android.Manifest.permission#CALL_PHONE})
 * when the 'Show Camera Preview' button is clicked to start  {@link CameraPreviewActivity} once
 * the permission has been granted.
 * <p>
 * First, the status of the Calling permission is checked using {@link
 * ActivityCompat#checkSelfPermission(Context, String)}
 * If it has not been granted ({@link PackageManager#PERMISSION_GRANTED}), it is requested by
 * calling
 * {@link ActivityCompat#requestPermissions(Activity, String[], int)}. The result of the request is
 * returned to the
 * {@link androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback}, which starts
 * {@link MainActivity#startCalling(String)} if the permission has been granted.
 * <p>
 * Note that there is no need to check the API level, the support library
 * already takes care of this. Similar helper methods for permissions are also available in
 * ({@link ActivityCompat},
 * {@link androidx.core.content.ContextCompat} and {@link androidx.fragment.app.Fragment}).
 */
public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int PERMISSION_REQUEST_CALL_PHONE = 0;
    private static final int PERMISSION_REQUEST_READ_PHONE_STATE = 0;

    private View mLayout;
    private String cUssd_glob;
    private boolean cSIM_glob;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLayout = findViewById(R.id.main_layout);
        int perm=ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
        perm=ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE);
        // Register a listener for the 'Show Calling Preview' button.
        findViewById(R.id.button_call).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makeCall();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
            @NonNull int[] grantResults) {
        // BEGIN_INCLUDE(onRequestPermissionsResult)
        if (requestCode == PERMISSION_REQUEST_CALL_PHONE) {
            // Request for camera permission.
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted. Start camera preview Activity.
                Snackbar.make(mLayout, R.string.camera_permission_granted,
                        Snackbar.LENGTH_SHORT)
                        .show();
                startCalling(cUssd_glob,cSIM_glob);
            } else {
                // Permission request was denied.
                Snackbar.make(mLayout, R.string.camera_permission_denied,
                        Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        // END_INCLUDE(onRequestPermissionsResult)
    }

    private void makeCall() {
        // BEGIN_INCLUDE(startCalling)
        // Check if the Calling permission has been granted
        EditText medit=(EditText)findViewById(R.id.FtNo);
        String num=medit.getText().toString();
        String req="*#67#";
        String code="67";

        RadioGroup rg = findViewById(R.id.rb_gr);
        RadioGroup rg2 = findViewById(R.id.rb_gr2);
        Switch sw=findViewById(R.id.switch1);
        cSIM_glob=sw.isChecked();
        switch ( rg.getCheckedRadioButtonId())
        {
            case R.id.rb_all:code="21";break;
            case R.id.rb_busy:code="67";break;
            case R.id.rb_na:code="61";break;
            case R.id.rb_nr:code="62";break;
        };
        switch ( rg2.getCheckedRadioButtonId())
        {
            case R.id.rb_Set:req="**"+code+"*"+num+"#";break;
            case R.id.rb_Check:req="*#"+code+"#";break;
            case R.id.rb_Cancel:req="##"+code+"#";break;

        };
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            // Permission is already available, start camera preview
            Snackbar.make(mLayout,
                    R.string.camera_permission_available,
                    Snackbar.LENGTH_SHORT).show();
            startCalling(req,cSIM_glob);
        } else {
            // Permission is missing and must be requested.
            cUssd_glob=req;
            requestCallingPermission();
        }
        // END_INCLUDE(startCalling)
    }

    /**
     * Requests the {@link android.Manifest.permission#CALL_PHONE} permission.
     * If an additional rationale should be displayed, the user has to launch the request from
     * a SnackBar that includes additional information.
     */
    private void requestCallingPermission() {
        // Permission has not been granted and must be requested.
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CALL_PHONE)) {
            // Provide an additional rationale to the user if the permission was not granted
            // and the user would benefit from additional context for the use of the permission.
            // Display a SnackBar with cda button to request the missing permission.
            Snackbar.make(mLayout, R.string.camera_access_required,
                    Snackbar.LENGTH_INDEFINITE).setAction(R.string.ok, new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request the permission
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.CALL_PHONE},
                            PERMISSION_REQUEST_CALL_PHONE);
                }
            }).show();

        } else {
            Snackbar.make(mLayout, R.string.camera_unavailable, Snackbar.LENGTH_SHORT).show();
            // Request the permission. The result will be received in onRequestPermissionResult().
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSION_REQUEST_READ_PHONE_STATE);
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, PERMISSION_REQUEST_CALL_PHONE);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startCalling(String cUssd, boolean cSIM) {
//        String cUssd =; //тут коды
        String cToSend = "tel:" + Uri.encode(cUssd);
//        startActivityForResult(new Intent("android.intent.action.CALL", Uri.parse(cToSend)), 1);
//        int RC=0;
//        onActivityResult(1, RC, (Intent)null);
        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        TelephonyManager tMSIM;
        //Строго говоря, здесь нужно выбрать оличество сабскрипшн ид, но можно и так, дальше тут
        // надо реализовать переключатель и прочие красивости
        SubscriptionManager subscriptionManager = SubscriptionManager.from(this);
        List<SubscriptionInfo> subs=subscriptionManager.getActiveSubscriptionInfoList();
//        List<String> simIds = new ArrayList<>();
//        for (SubscriptionInfo subscriptionInfo : SubscriptionManager.getActiveSubscriptionInfoList()) {
//            simIds.add(String.valueOf(subscriptionInfo.getSubscriptionId()))};

        if (!cSIM) tMSIM=telephonyManager.createForSubscriptionId(subs.get(0).getSubscriptionId());
        else
                   tMSIM=telephonyManager.createForSubscriptionId(subs.get(1).getSubscriptionId());

        Handler handler = new Handler();

        TelephonyManager.UssdResponseCallback responseCallback = new TelephonyManager.UssdResponseCallback() {
            @Override
            public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                super.onReceiveUssdResponse(telephonyManager, request, response);
                Snackbar.make(mLayout, "USSD request Success:"+response.toString(), Snackbar.LENGTH_SHORT).show();

            }

            @Override
            public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);

                Snackbar.make(mLayout, String.format("USSD request fail, Code:%d", failureCode), Snackbar.LENGTH_SHORT).show();
            }
        };

        Snackbar.make(mLayout, "USSD request:"+cUssd, 1000).show();
        tMSIM.sendUssdRequest( cUssd, responseCallback, handler);

    }

}
