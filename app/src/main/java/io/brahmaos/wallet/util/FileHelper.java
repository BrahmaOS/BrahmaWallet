package io.brahmaos.wallet.util;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;

public class FileHelper {

    private static String TAG = FileHelper.class.getName();

    private String sdcardRootDir;
    private String sdcardWalletRootPath;

    // singleton
    private static FileHelper instance = null;

    // access
    public static FileHelper getInstance() {
        if (null == instance) {
            instance = new FileHelper();
        }

        return instance;
    }

    public boolean init(Context ctx)
    {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File fileSdcardRootDir = Environment.getExternalStorageDirectory();
            if (null == fileSdcardRootDir) {
                Log.e(TAG, "there is no sdcard!");
                return false;
            }

            sdcardRootDir = fileSdcardRootDir.getAbsolutePath();
            sdcardWalletRootPath = sdcardRootDir + "/BrahmaWallet";

            File fileSxcRootDir = new File(sdcardWalletRootPath);
            if (!fileSxcRootDir.exists()) {
                if (!fileSxcRootDir.mkdir()) {
                    Log.e(TAG, "create wallet root dir failed - " + sdcardWalletRootPath);
                    return false;
                }
            }
        }

        return true;
    }

    public String getSdcardRootDir()
    {
        return sdcardRootDir;
    }

    private String getSdcardWalletRootPath()
    {
        return sdcardWalletRootPath;
    }

    public static File getOutputImageFile(int seqNo) {
        String cachePath = FileHelper.getInstance().getSdcardWalletRootPath() + "/image";
        File mediaStorageDir = new File(cachePath);
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(TAG, "failed to create directory!");
                return null;
            }
        }

        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        if (seqNo > 0) {
            timeStamp += "_" + seqNo;
        }

        return new File(mediaStorageDir.getPath()
                + File.separator + "IMG_" + timeStamp + ".jpg");
    }
}
