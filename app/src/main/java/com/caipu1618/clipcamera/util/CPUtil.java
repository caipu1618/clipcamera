package com.caipu1618.clipcamera.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.view.Surface;
import android.widget.Toast;

import com.caipu1618.clipcamera.camera.CameraActivity;

import java.io.File;

/**
 * Created by caipu on 2018/1/4.
 */

public class CPUtil {

    /**
     * 调用自定义拍照
     * @param context
     */
    public static void startCamera(Context context){
        Intent intent = new Intent();
        intent.setClass(context, CameraActivity.class);
        startCamera(context);
    }

    /**
     * 调用系统拍照
     * @param fileDir
     * @param filename
     * @param fileprovider
     * @param requestCode
     * @param activity
     */
    public static void startLocalCamera(String fileDir, String filename, String fileprovider, int requestCode, Activity activity) {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(activity.getPackageManager()) != null) {
                File dir = new File(fileDir);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File photoFile = new File(fileDir, filename);
                if (photoFile != null) {
                    Uri photoURI;
                    if(TextUtils.isEmpty(fileprovider)){
                        photoURI = Uri.fromFile(photoFile);
                    } else {
                        photoURI = FileProvider.getUriForFile(activity, fileprovider, photoFile);
                    }
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    try {
                        activity.startActivityForResult(intent, requestCode);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        Toast.makeText(activity, "您拒绝了应用拍摄照片权限的开启，请到系统设置中开启后再使用此功能！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 保证预览方向正确
     *
     * @param activity
     * @param cameraId
     * @param camera
     */
    public static void setCameraDisplayOrientation(Activity activity,
                                            int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }


    public static Bitmap setTakePicktrueOrientation(int id, Bitmap bitmap) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(id, info);
        if(info.orientation != 0 && info.orientation != 360) {
            bitmap = rotaingImageView(id, info.orientation, bitmap);
        }
        return bitmap;
    }

    /**
     * 把相机拍照返回照片转正
     *
     * @param angle 旋转角度
     * @return bitmap 图片
     */
    public static Bitmap rotaingImageView(int id, int angle, Bitmap bitmap) {
        //旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        //加入翻转 把相机拍照返回照片转正
        if (id == 1) {
            matrix.postScale(-1, 1);
        }
        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizedBitmap;
    }
}
