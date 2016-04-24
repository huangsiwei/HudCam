package com.example.hudcam;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by huangsiwei on 16/4/12.
 */
public class MainActivity extends Activity{

    public static Resources mResources;

    protected static final String TAG = "main";
    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private Uri fileUri;
    private Camera mCamera;
    private static Camera.Parameters mCameraParameters;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mResources = getResources();
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN , WindowManager.LayoutParams. FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
        setContentView(R.layout.activity_main);
        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);
        mCamera = getCameraInstance();
        mCameraParameters = mCamera.getParameters();
        setOptimalSizeOfCamera();
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

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
            mCamera.setDisplayOrientation(0);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
            mCamera.setDisplayOrientation(90);
        }
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

    /** 打开一个Camera */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open();
            Configuration config = mResources.getConfiguration();
            if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                c.setDisplayOrientation(0);
            } else if (config.orientation == Configuration.ORIENTATION_PORTRAIT){
                c.setDisplayOrientation(90);
            }
            List<Camera.Size> supportedPictureSizes = mCameraParameters.getSupportedPictureSizes();
            mCameraParameters.setPictureSize(supportedPictureSizes.get(0).width,supportedPictureSizes.get(0).height);
            c.setParameters(mCameraParameters);
        } catch (Exception e) {
            Log.d(TAG, "打开Camera失败失败");
        }
        return c;
    }

    private void setOptimalSizeOfCamera() {
        List<Camera.Size> supportedPreviewSizes = mCameraParameters.getSupportedPreviewSizes();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        mCameraParameters.setPreviewSize(getOptimalSize(supportedPreviewSizes,height,width).width,getOptimalSize(supportedPreviewSizes,height,width).height);
        mCamera.setParameters(mCameraParameters);
    }

    private Camera.Size getOptimalSize(List<Camera.Size> sizes, int w, int h) {

        final double ASPECT_TOLERANCE = 0.2;
        double targetRatio = (double) w / h;
        if (sizes == null)
            return null;
        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;
        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes)
        {
//          Log.d("CameraActivity", "Checking size " + size.width + "w " + size.height + "h");
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff)
            {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }
        // Cannot find the one match the aspect ratio, ignore the requirement

        if (optimalSize == null)
        {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff)
                {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }

        SharedPreferences previewSizePref = getSharedPreferences("PREVIEW_PREF",MODE_PRIVATE);

        SharedPreferences.Editor prefEditor = previewSizePref.edit();
        prefEditor.putInt("width", optimalSize.width);
        prefEditor.putInt("height", optimalSize.height);

        prefEditor.commit();

//      Log.d("CameraActivity", "Using size: " + optimalSize.width + "w " + optimalSize.height + "h");
        return optimalSize;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            File pictureFile = new File(fileUri.getPath());
            InputStream inputStream = getResources().openRawResource(+R.drawable.darth_vader);
            Bitmap rawBitmap = BitmapFactory.decodeByteArray(data,0,data.length);
            Bitmap overlayBitmap = BitmapFactory.decodeStream(inputStream);
            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap mixBitmap = overlay(rawBitmap,overlayBitmap);
                mixBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                fos.write(data);
                fos.close();
            } catch (Exception e) {
                Log.d(TAG, "保存图片失败");
            }
        }
    };

    public static Bitmap overlay(Bitmap bmp1, Bitmap bmp2) {
        Bitmap bmOverlay = Bitmap.createBitmap(bmp1.getWidth(), bmp1.getHeight(), bmp1.getConfig());
        Canvas canvas = new Canvas(bmOverlay);
        canvas.drawBitmap(bmp1, new Matrix(), null);
        canvas.drawBitmap(scaleBitMap(bmp2,1000,1000), (bmp1.getWidth()/2 - 500), (bmp1.getHeight()/2 - 500), null);
        return bmOverlay;
    }

    private static Bitmap scaleBitMap(Bitmap rawBitMap,int scaledWidth,int scaledHeight) {
        int width = rawBitMap.getWidth();
        int height = rawBitMap.getHeight();

        float scaleWidthRate = ((float) scaledWidth) / width;
        float scaleHeightRate = ((float) scaledHeight) / height;
        // 取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidthRate, scaleHeightRate);
        // 得到新的图片
        return Bitmap.createBitmap(rawBitMap, 0, 0, width, height, matrix,
                true);
    }

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
