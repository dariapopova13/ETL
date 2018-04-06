package com.university.itis.emotionstestinglibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * Created by Daria Popova on 05.04.18.
 */
public class VideoRecorder {

    private static final int SENSOR_ORIENTATION_DEFAULT_DEGREES = 90;
    private static final int SENSOR_ORIENTATION_INVERSE_DEGREES = 270;
    private static final SparseIntArray DEFAULT_ORIENTATIONS = new SparseIntArray();
    private static final SparseIntArray INVERSE_ORIENTATIONS = new SparseIntArray();

    static {
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_0, 90);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_90, 0);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_180, 270);
        DEFAULT_ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    static {
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_0, 270);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_90, 180);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_180, 90);
        INVERSE_ORIENTATIONS.append(Surface.ROTATION_270, 0);
    }

    private Activity activity;
    private boolean isInit;
    private String frontCameraId;
    private boolean isRecordingVideo;

    private TextureView textureView;
    private CameraDevice cameraDevice;
    private CameraCaptureSession previewSession;
    private Size videoSize;
    private MediaRecorder mediaRecorder;
    private CameraManager manager;

    //    private HandlerThread mBackgroundThread;
//    private Handler mBackgroundHandler;

    private Semaphore cameraOpenCloseLock = new Semaphore(1);
    private Integer sensorOrientation;
    private String nextVideoAbsolutePath;
    private CaptureRequest.Builder previewBuilder;

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            VideoRecorder.this.cameraDevice = cameraDevice;
            startPreview();
            cameraOpenCloseLock.release();
            if (null != textureView) {
                configureTransform(textureView.getWidth(), textureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            VideoRecorder.this.cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            cameraOpenCloseLock.release();
            cameraDevice.close();
            VideoRecorder.this.cameraDevice = null;
        }

    };
    private TextureView.SurfaceTextureListener surfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture,
                                              int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture,
                                                int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
            if (!isRecordingVideo) {
                startRecordingVideo();
            }
        }

    };

    public VideoRecorder(Activity activity) {
        this.activity = activity;
        this.manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        textureView = VideoUtils.createTextureView(activity);
        frontCameraId = VideoUtils.getFrontCameraId(manager);
    }


    public void startRecording() {
        PermissionUtils.requestPermissions(activity);
        if (!PermissionUtils.hasPermissions(activity)) return;
//        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
        isInit = true;
    }

    public void stopRecording() {
        if (!isInit) return;
        if (isRecordingVideo) {
            stopRecordingVideo();
        }
        closeCamera();
//        stopBackgroundThread();
    }


    private void closePreviewSession() {
        if (previewSession != null) {
            previewSession.close();
            previewSession = null;
        }
    }

    private void stopRecordingVideo() {
        isRecordingVideo = false;
        mediaRecorder.stop();
        mediaRecorder.reset();
        nextVideoAbsolutePath = null;
    }


//    private void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("CameraBackground");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    private void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    @SuppressWarnings("MissingPermission")
    private void openCamera(int width, int height) {
        if (null == activity || activity.isFinishing()) {
            return;
        }

        try {
            if (!cameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(frontCameraId);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            if (map == null) {
                throw new RuntimeException("Cannot get available preview/video sizes");
            }
            videoSize = VideoUtils.chooseVideoSize(map.getOutputSizes(MediaRecorder.class));

            int orientation = activity.getResources().getConfiguration().orientation;
//            configureTransform(width, height);
            mediaRecorder = new MediaRecorder();
            manager.openCamera(frontCameraId, stateCallback, null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void closeCamera() {
        try {
            cameraOpenCloseLock.acquire();
            if (null != cameraDevice) {
                cameraDevice.close();
                cameraDevice = null;
            }
            if (null != mediaRecorder) {
                mediaRecorder.release();
                mediaRecorder = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.");
        } finally {
            cameraOpenCloseLock.release();
        }
    }

    private void startPreview() {
        if (null == cameraDevice || !textureView.isAvailable()) {
            return;
        }
        try {
            closePreviewSession();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

            Surface previewSurface = new Surface(texture);
            previewBuilder.addTarget(previewSurface);

            cameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            previewSession = session;
                            updatePreview();
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        }
                    }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (null == cameraDevice) {
            return;
        }
        try {
            setUpCaptureRequestBuilder(previewBuilder);
//            HandlerThread thread = new HandlerThread("CameraPreview");
//            thread.start();
            previewSession.setRepeatingRequest(previewBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == textureView || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        textureView.setTransform(matrix);
    }

    private void setUpMediaRecorder() throws IOException {
        if (null == activity) {
            return;
        }
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);

        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        if (nextVideoAbsolutePath == null || nextVideoAbsolutePath.isEmpty()) {
            nextVideoAbsolutePath = StorageUtils.getVideoFilePath(activity);
        }
        mediaRecorder.setOutputFile(nextVideoAbsolutePath);
        mediaRecorder.setVideoEncodingBitRate(10000000);
        mediaRecorder.setVideoFrameRate(30);
        mediaRecorder.setVideoSize(videoSize.getWidth(), videoSize.getHeight());
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        switch (sensorOrientation) {
            case SENSOR_ORIENTATION_DEFAULT_DEGREES:
                mediaRecorder.setOrientationHint(DEFAULT_ORIENTATIONS.get(rotation));
                break;
            case SENSOR_ORIENTATION_INVERSE_DEGREES:
                mediaRecorder.setOrientationHint(INVERSE_ORIENTATIONS.get(rotation));
                break;
        }
        mediaRecorder.prepare();
    }

    private void startRecordingVideo() {
        if (null == cameraDevice || !textureView.isAvailable()) {
            return;
        }
        try {
            closePreviewSession();
            setUpMediaRecorder();
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            previewBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            List<Surface> surfaces = new ArrayList<>();
            Surface previewSurface = new Surface(texture);
            surfaces.add(previewSurface);
            previewBuilder.addTarget(previewSurface);

            Surface recorderSurface = mediaRecorder.getSurface();
            surfaces.add(recorderSurface);
            previewBuilder.addTarget(recorderSurface);

            cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {

                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    previewSession = cameraCaptureSession;
                    updatePreview();
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            isRecordingVideo = true;
                            mediaRecorder.start();
                        }
                    });
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                }
            }, null);
        } catch (CameraAccessException | IOException e) {
            e.printStackTrace();
        }

    }

}
