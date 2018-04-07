package com.university.itis.emotionstestinglibrary.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;

import com.university.itis.emotionstestinglibrary.ETL;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Daria Popova on 06.04.18.
 */
public final class StorageUtils {


    public static boolean createCurrentTestDirectory(Context context, String dirName) {
        final File dir = new File(dirName);

        return dir.exists() || dir.mkdirs();
    }


    public static String getVideoFilePath(Activity activity) {
        String dirName = Environment.getExternalStorageDirectory() + "/" + "ETL/" + ETL.getTestId();
        System.out.println(createCurrentTestDirectory(activity, dirName));

        String activityName = activity.getClass().getSimpleName();
        String videoName = getDate()  + activityName + ".mp4";

        return dirName + "/" + videoName;
    }

    public static String getDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss__", Locale.getDefault());
        return format.format(new Date());
    }
}
