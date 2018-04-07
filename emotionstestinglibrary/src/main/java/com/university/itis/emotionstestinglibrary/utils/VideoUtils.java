package com.university.itis.emotionstestinglibrary.utils;

import android.app.Activity;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.util.Size;
import android.view.TextureView;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Comparator;

/**
 * Created by Daria Popova on 06.04.18.
 */
public final class VideoUtils {

    public static Size getVideoSize;
    private static CameraCharacteristics cameraCharacteristics;
    private static String frontCameraId;
    private static Size videoSize;

    public static Size chooseVideoSize(Size[] choices) {
        if (videoSize == null) {
            for (Size size : choices) {
                if (size.getWidth() == size.getHeight() * 4 / 3 && size.getWidth() <= 1080) {
                    return size;
                }
            }
            videoSize = choices[choices.length - 1];
        }
        return videoSize;
    }

    public static TextureView createTextureView(Activity activity) {
        TextureView textureView = new TextureView(activity);
        textureView.setLayoutParams(new FrameLayout.LayoutParams(1, 1));
        final ViewGroup viewGroup = (ViewGroup) ((ViewGroup) activity
                .findViewById(android.R.id.content)).getChildAt(0);
        viewGroup.addView(textureView);
        return textureView;
    }

    public static String getFrontCameraId(CameraManager manager) throws CameraAccessException {
        if (frontCameraId == null) {
            for (final String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT)
                    frontCameraId = cameraId;
            }
        }
        return frontCameraId;

    }

    public static CameraCharacteristics getCameraCharacteristics(CameraManager manager) throws CameraAccessException {
        if (cameraCharacteristics == null) {
            cameraCharacteristics = manager.getCameraCharacteristics(getFrontCameraId(manager));
        }
        return cameraCharacteristics;
    }

    static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            return Long.signum((long) lhs.getWidth() * lhs.getHeight() -
                    (long) rhs.getWidth() * rhs.getHeight());
        }

    }
}
