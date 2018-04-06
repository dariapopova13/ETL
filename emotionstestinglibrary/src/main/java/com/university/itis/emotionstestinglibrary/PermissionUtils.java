package com.university.itis.emotionstestinglibrary;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

/**
 * Created by Daria Popova on 06.04.18.
 */
public final class PermissionUtils {


    private static final int ETL_PERMISSION_CODE = 1;
    private static final String[] ETL_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static boolean hasPermissions(Context context) {
        for (String permission : ETL_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    public static void requestPermissions(Activity activity) {
        if (!hasPermissions(activity)) {
            ActivityCompat.requestPermissions(activity,
                    ETL_PERMISSIONS,
                    ETL_PERMISSION_CODE);
        }
    }
}
