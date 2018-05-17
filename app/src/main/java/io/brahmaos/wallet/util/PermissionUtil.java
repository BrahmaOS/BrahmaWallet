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

package io.brahmaos.wallet.util;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;

/**
 * Utility class that wraps access to the runtime permissions API in M and provides basic helper
 * methods.
 */
public class PermissionUtil {

    private static final String TAG = PermissionUtil.class.getSimpleName();
    public static final int CODE_CAMERA_SCAN = 10000;

    public static final String PERMISSION_CAMERA = Manifest.permission.CAMERA;

    public static final String[] CAMERA_PERMISSIONS = {
            PERMISSION_CAMERA
    };

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Activity#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Request multi permissions one time.
     */
    public static void requestMultiPermissions(final Activity activity, String[] permissionList, int requestCode) {

        final List<String> unGrantPermissionsList = getNoGrantedPermission(activity, permissionList);

        if (unGrantPermissionsList == null) {
            return;
        }
        Log.d(TAG, "requestMultiPermissions permissionsList:" + unGrantPermissionsList.size());

        if (unGrantPermissionsList.size() > 0) {
            ActivityCompat.requestPermissions(activity, unGrantPermissionsList.toArray(new String[unGrantPermissionsList.size()]),
                    requestCode);
            Log.d(TAG, "showMessageOKCancel requestPermissions");
        }
    }

    public static ArrayList<String> getNoGrantedPermission(Activity activity, String[] permissionList) {

        ArrayList<String> permissions = new ArrayList<>();

        for (int i = 0; i < permissionList.length; i++) {
            String requestPermission = permissionList[i];

            // default PERMISSION DENIED
            int checkSelfPermission = PackageManager.PERMISSION_DENIED;
            try {
                checkSelfPermission = ActivityCompat.checkSelfPermission(activity, requestPermission);
            } catch (RuntimeException e) {
                Toast.makeText(activity, "please open those permission", Toast.LENGTH_SHORT)
                        .show();
                Log.e(TAG, "RuntimeException:" + e.getMessage());
                return null;
            }

            if (checkSelfPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(requestPermission);
            }
        }
        return permissions;
    }

    // Show the tip dialog
    public static void openSettingActivity(final Activity activity, String message) {
        new AlertDialog.Builder(activity)
                .setMessage(message)
                .setPositiveButton(R.string.action_go_settings, (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Log.d(TAG, "getPackageName(): " + activity.getPackageName());
                    Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                })
                .setNegativeButton(R.string.cancel, null)
                .create()
                .show();
    }
}
