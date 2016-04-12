package com.example.hudcam;

import android.content.Context;
import android.content.pm.PackageManager;

/**
 * Created by huangsiwei on 16/4/12.
 */
public class CameraActivity {

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

}
