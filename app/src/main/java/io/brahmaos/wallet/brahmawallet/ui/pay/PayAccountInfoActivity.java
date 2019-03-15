package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.ImageUtil;

public class PayAccountInfoActivity extends BaseActivity {
    private ImageView mAvatar;
    private TextView mName, mID;
    private File mTakePhotoTempFile;
    private File mCropImageFile;

    @Override
    protected String tag() {
        return PayAccountInfoActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_activity_account_info);
        showNavBackBtn();
        mAvatar = findViewById(R.id.iv_quick_account_avatar);
        mName = findViewById(R.id.tv_quick_account_name);
        mID = findViewById(R.id.tv_quick_account_id);
        mID.setText(BrahmaConfig.getInstance().getPayAccountID());
        Bitmap avatar = BrahmaConfig.getInstance().getPayAccountAvatar();
        if (avatar != null) {
            mAvatar.setImageBitmap(ImageUtil.getCircleBitmap(avatar));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        String accountName = BrahmaConfig.getInstance().getPayAccountName();
        if (null == accountName || accountName.isEmpty()) {
            mName.setText(getString(R.string.pay_account_info));
        } else {
            mName.setText(accountName);
        }
    }

    public void changeAccountAvatar(View view) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestExternalStorage();
        } else {
            showChangeAvatarMenu();
        }
    }

    private void showChangeAvatarMenu() {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setItems(getResources().getStringArray(R.array.avatar_change_choices), (dialog, which) -> {
                    switch (which) {
                        case 0:
                            takePhoto();
                            break;
                        case 1:
                            choosePhoto();
                            break;
                        default:
                            break;
                    }
                }).create();
        alertDialog.show();
    }

    public void changeAccountName(View view) {
        startActivity(new Intent(this, ChangePayAccountNameActivity.class));
    }

    private void takePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            requestCameraScanPermission();
        } else {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (cameraIntent.resolveActivity(getPackageManager()) != null) {
                try {
                    mTakePhotoTempFile = getCameraImageFile();
                    if (!mTakePhotoTempFile.exists()) {
                        mTakePhotoTempFile.createNewFile();
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                                FileProvider.getUriForFile(
                                        this,
                                        getApplicationContext().getPackageName() + ".fileprovider",
                                        mTakePhotoTempFile));
                    } else {
                        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mTakePhotoTempFile));
                    }
                    startActivityForResult(cameraIntent, ReqCode.TAKE_PHOTO);
                } catch (Exception e){
                    BLog.e(tag(), "take photo: " + e.toString());
                    showShortToast(getString(R.string.avatar_camera_fail));
                }
            }
        }

    }

    private void choosePhoto() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestExternalStorage();
        } else {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, ReqCode.CHOOSE_IMAGE);
        }
    }

    @Override
    public void handleCameraScanPermission() {
        takePhoto();
    }

    @Override
    public void handleExternalStoragePermission() {
        showChangeAvatarMenu();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (ReqCode.CHOOSE_IMAGE == requestCode) {
            if (RESULT_OK == resultCode && data != null && data.getData() != null) {
                cropAvatar(getChooseImageUriPath(data.getData()));
            } else {
                showShortToast(getString(R.string.avatar_choose_fail));
            }
        } else if (ReqCode.TAKE_PHOTO == requestCode) {
            if (RESULT_OK == resultCode) {
                cropAvatar(mTakePhotoTempFile.getAbsolutePath());
            } else {
                showShortToast(getString(R.string.avatar_camera_fail));
            }
        } else if (ReqCode.CROP_IMAGE == requestCode) {
            if (RESULT_OK == resultCode){
                // Save avatar image to local
                InputStream imageStream = null;
                try {
                    imageStream = getContentResolver().openInputStream(Uri.fromFile(mCropImageFile));
                    Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                    if (BrahmaConfig.getInstance().savePayAccountAvatar(bitmap)) {
                        mAvatar.setImageBitmap(ImageUtil.getCircleBitmap(bitmap));
                        showShortToast(getString(R.string.avatar_changed));
                    } else {
                        showShortToast(getString(R.string.avatar_save_fail));
                    }
                    bitmap = null;
                    System.gc();
                } catch (FileNotFoundException fe) {
                    BLog.w(tag(), "Cannot find image file" + fe.toString());
                } finally {
                    if (imageStream != null) {
                        try {
                            imageStream.close();
                        } catch (IOException ioe) {
                            BLog.w(tag(), "Cannot close image stream" + ioe);
                        }
                    }
                }
            }else {
                showShortToast(getString(R.string.avatar_crop_fail));
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void cropAvatar(String imagePath){
        mCropImageFile = getTempCropImageFile();
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(getImageContentUri(new File(imagePath)), "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 500);
        intent.putExtra("outputY", 500);
        intent.putExtra("scale", true);
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(mCropImageFile));
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, ReqCode.CROP_IMAGE);
    }

    private String getChooseImageUriPath(Uri imageUri) {
        String imagePath = null;
        if (DocumentsContract.isDocumentUri(this, imageUri)) {
            String docId = DocumentsContract.getDocumentId(imageUri);
            if ("com.android.providers.media.documents".equals(imageUri.getAuthority())) {
                String id = docId.split(":")[1];
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
            } else if ("com.android.providers.downloads.documents".equals(imageUri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("" +
                        "content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
            }
        } else if ("content".equals(imageUri.getScheme())) {
            imagePath = getImagePath(imageUri, null);
        }
        return imagePath;
    }

    private File getTempCropImageFile(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file = new File(getExternalCacheDir(), "crop_pay_avatar.jpg");
            return file;
        } else {
            File file = new File(getCacheDir().getPath(), "crop_pay_avatar.jpg");
            return file;
        }
    }
    private File getCameraImageFile(){
        if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            File file = new File(getExternalCacheDir(), "camera_pay_avatar.jpg");
            return file;
        } else {
            File file = new File(getCacheDir().getPath(), "camera_pay_avatar.jpg");
            return file;
        }
    }

    public Uri getImageContentUri(File imageFile){
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[]{MediaStore.Images.Media._ID},
                MediaStore.Images.Media.DATA + "=? ",
                new String[]{filePath}, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    private String getImagePath(Uri uri, String selection) {
        String path = null;
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }
}
