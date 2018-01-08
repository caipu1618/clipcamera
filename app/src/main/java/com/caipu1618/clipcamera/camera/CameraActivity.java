package com.caipu1618.clipcamera.camera;
/**
 * Created by caipu on 2018/1/4.
 */

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.caipu1618.clipcamera.R;
import com.caipu1618.clipcamera.util.CPUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static java.lang.Math.abs;

/**
 * Description: 拍摄社保卡
 * Company    : 大白科技
 * Author     : gene
 * Date       : 2016/11/22
 */
public class CameraActivity extends Activity implements SurfaceHolder.Callback{

    SurfaceView surfaceView;
    ImageView iv_camera;
    ImageView ivCameraPre;
    RelativeLayout rlOpera;
    private Camera mCamera;
    private SurfaceHolder mHolder;
    //默认前置1或者后置相机0 这里暂时设置为前置
    private int mCameraId = 0;
    //屏幕宽高
    private boolean isview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        surfaceView = findViewById(R.id.surfaceView);
        iv_camera = findViewById(R.id.iv_camera);
        ivCameraPre = findViewById(R.id.iv_camera_pre);
        rlOpera = findViewById(R.id.rl_opera);

        mHolder = surfaceView.getHolder();
        mHolder.addCallback(this);
        if (mCamera == null) {
            mCamera = getCamera(mCameraId);
            if (mHolder != null) {
                preview(mCamera,mHolder);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }

    /**
     * 获取Camera实例
     *
     * @return
     */
    private Camera getCamera(int id) {
        Camera camera = null;
        try {
            camera = Camera.open(id);
        } catch (Exception e) {

        }
        return camera;
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        preview(mCamera,holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        mCamera.stopPreview();
        preview(mCamera,holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.release();
        }
        mCamera = null;
    }

    public void doCamera(View v){
        if (isview) {
            isview = false;
//                    if(mCamera != null) {
//                        mPresenter.capture(value, uid, mCamera, mCameraId);
//                    }
            if(mCamera != null) {
                mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] data, Camera camera) {
                        mCamera.setOneShotPreviewCallback(null);
                        try {
                            Camera.Size previewSize = camera.getParameters().getPreviewSize();//获取尺寸,格式转换的时候要用到
                            YuvImage yuvimage = new YuvImage(
                                    data,
                                    ImageFormat.NV21,
                                    previewSize.width,
                                    previewSize.height,
                                    null);
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width, previewSize.height), 100, baos);
                            //将rawImage转换成bitmap
                            BitmapFactory.Options options = new BitmapFactory.Options();
                            options.inPreferredConfig = Bitmap.Config.RGB_565;
                            Bitmap bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(), 0, baos.toByteArray().length, options);
                            bitmap = CPUtil.setTakePicktrueOrientation(mCameraId, bitmap);
                            //截取社保卡
                            float bilix = (getResources().getDisplayMetrics().density * 13) / surfaceView.getWidth();
                            float biliy = (getResources().getDisplayMetrics().density * 211) / surfaceView.getHeight();
                            int y = Math.round(bitmap.getHeight() / 2) - Math.round(bitmap.getHeight() * biliy / 2);
                            Bitmap ret = Bitmap.createBitmap(bitmap, Math.round(bitmap.getWidth() * bilix), y, bitmap.getWidth() - Math.round(2 * bitmap.getWidth() * bilix), Math.round(bitmap.getHeight() * biliy), null, false);
//                            BitmapUtils.saveJPGE_After(CameraActivity.this, ret, Constant.IMG_PATH2, 90);
                            if (bitmap != null && !bitmap.isRecycled()) {
                                bitmap.recycle();
                            }
                            if (ret != null && !ret.isRecycled()) {
                                ret.recycle();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
//                        Intent intent = new Intent(CameraActivity.this, DisplayActivity.class);
//                        startActivity(intent);
//                        finish();
                    }
                });
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            if(mCamera != null){
                mCamera.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void preview(Camera camera, SurfaceHolder holder) {
        try {
            setupCamera(camera);
            camera.setPreviewDisplay(holder);
            //亲测的一个方法 基本覆盖所有手机 将预览矫正
            CPUtil.setCameraDisplayOrientation(this, mCameraId, camera);
            camera.startPreview();
            isview = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置
     */
    private void setupCamera(Camera camera) {
        try{
            Camera.Parameters parameters = camera.getParameters();

            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                // Autofocus mode is supported 自动对焦
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size previewSize = sizes.get(0);
            // 获取最适配屏幕尺寸的预览尺寸
            for (int i = 1; i < sizes.size(); i++) {
                Camera.Size tempSize = sizes.get(i);
                if (getDifferenceBetweenScreenScalingWithSize(tempSize) < getDifferenceBetweenScreenScalingWithSize(previewSize)) {
                    previewSize = tempSize;
                }
            }
            parameters.setPreviewSize(previewSize.width, previewSize.height);
            List<Camera.Size> sizes2 = parameters.getSupportedPictureSizes();
            Camera.Size pictureSize = sizes2.get(0);
            for (int i = 1; i < sizes2.size(); i++) {
                Camera.Size tempSize2 = sizes2.get(i);
                if (getDifferenceBetweenScreenScalingWithSize(tempSize2) < getDifferenceBetweenScreenScalingWithSize(previewSize)) {
                    pictureSize = tempSize2;
                }
            }
            parameters.setPictureSize(pictureSize.width, pictureSize.height);
//            //这里第三个参数为最小尺寸 getPropPreviewSize方法会对从最小尺寸开始升序排列 取出所有支持尺寸的最小尺寸
//            Camera.Size previewSize = CameraUtil.getInstance().getPropPreviewSize(parameters.getSupportedPreviewSizes(), screenWidth);
//            parameters.setPreviewSize(previewSize.width, previewSize.height);
            camera.setParameters(parameters);
            /**
             * 设置surfaceView的尺寸 因为camera默认是横屏，所以取得支持尺寸也都是横屏的尺寸
             * 我们在startPreview方法里面把它矫正了过来，但是这里我们设置设置surfaceView的尺寸的时候要注意 previewSize.height<previewSize.width
             * previewSize.width才是surfaceView的高度
             * 一般相机都是屏幕的宽度 这里设置为屏幕宽度 高度自适应 你也可以设置自己想要的大小
             *
             */

//            picHeight = screenWidth * pictureSize.width / pictureSize.height;
//
//            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(screenWidth, picHeight);
//            params.gravity = Gravity.CENTER;
//            //这里当然可以设置拍照位置 比如居中 我这里就置顶了
//            surfaceView.setLayoutParams(params);
//            ivCameraPre.setLayoutParams(params);
//            rlOpera.setLayoutParams(params);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 获取size宽高比和屏幕宽高比的差值的绝对值
     */
    private float getDifferenceBetweenScreenScalingWithSize(Camera.Size size) {
        WindowManager manager = this.getWindowManager();
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int screenWidth = outMetrics.widthPixels;
        int screenHeight = outMetrics.heightPixels;

        // 这边width和height调换的原因是因为竖屏width和height相反
        return abs(((float)size.height / (float)size.width) - ((float)screenWidth / (float)screenHeight));
    }
}
