package com.example.hudcam;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by huangsiwei on 16/4/12.
 */
public class MainActivity extends Activity{
    protected static final String TAG = "main";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri fileUri;
    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        mCamera = getCameraInstance();

        // 创建预览类，并与Camera关联，最后添加到界面布局中
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
        ImageView darth_vader = (ImageView) findViewById(R.id.darth_vader);
        darth_vader.bringToFront();

        Button captureButton = (Button) findViewById(R.id.button_capture);
        captureButton.bringToFront();
        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 在捕获图片前进行自动对焦
                mCamera.autoFocus(new Camera.AutoFocusCallback() {

                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        // 从Camera捕获图片
                        mCamera.takePicture(null, null, mPicture);
                    }
                });
            }
        });
    }

    private static Uri getOutputMediaFileUri(int type){
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "MyCameraApp");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    /** 检测设备是否存在Camera硬件 */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA)) {
            // 存在
            return true;
        } else {
            // 不存在
            return false;
        }
    }

    /** 打开一个Camera */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
            c.setDisplayOrientation(90);
        } catch (Exception e) {
            Log.d(TAG, "打开Camera失败失败");
        }
        return c;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // 获取Jpeg图片，并保存在sd卡上
            File pictureFile = new File(fileUri.getPath());
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                fos.write(data);
                fos.close();

            } catch (Exception e) {
                Log.d(TAG, "保存图片失败");
            }
        }
    };

    @Override
    protected void onDestroy() {
        // 回收Camera资源
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.release();
            mCamera=null;
        }
        super.onDestroy();
    }
}
