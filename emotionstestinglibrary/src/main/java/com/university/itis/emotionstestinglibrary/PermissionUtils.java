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


    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    private static final String[] VIDEO_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
    };

    public static boolean hasPermissions(Context context) {
        boolean hasCameraPermission = ContextCompat.checkSelfPermission(context, VIDEO_PERMISSIONS[0])
                == PackageManager.PERMISSION_GRANTED;
        boolean hasRecordAudioPermission = ContextCompat.checkSelfPermission(context, VIDEO_PERMISSIONS[1])
                == PackageManager.PERMISSION_GRANTED;
        return hasCameraPermission && hasRecordAudioPermission;
    }

    public static void requestPermissions(Activity activity) {
        if (!hasPermissions(activity)) {
            ActivityCompat.requestPermissions(activity,
                    VIDEO_PERMISSIONS,
                    REQUEST_VIDEO_PERMISSIONS);
        }
    }
}
