package io.brahmaos.wallet.brahmawallet.ui.contact;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.yalantis.ucrop.UCrop;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.FileHelper;
import io.brahmaos.wallet.util.ImageUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AddContactActivity extends BaseActivity {

    // UI references.
    @BindView(R.id.img_avatar)
    ImageView ivContactAvatar;
    @BindView(R.id.et_contact_name)
    EditText etContactName;
    @BindView(R.id.et_contact_family_name)
    EditText etContactFamilyName;
    @BindView(R.id.et_contact_address)
    EditText etContactAddress;
    @BindView(R.id.iv_scan)
    ImageView ivScan;
    @BindView(R.id.et_contact_remark)
    EditText etContactRemark;

    private ContactViewModel mViewModel;

    // File cropped for contact avatar
    // if not save, need remove from filesystem
    private File fileContactAvatar;
    private File tempFileContactAvatar;
    private Uri urlContactAvatar;

    @Override
    protected String tag() {
        return AddContactActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        mViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
        showNavBackBtn();
        ButterKnife.bind(this);
        ivScan.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraScanPermission();
            } else {
                scanAddressCode();
            }
        });

        ivContactAvatar.setOnClickListener(v -> {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestExternalStorage();
            } else {
                selectContactAvatar();
            }
        });

        String address = getIntent().getStringExtra(IntentParam.PARAM_ETH_ADDRESS);
        if (address != null && address.length() > 0) {
            etContactAddress.setText(address);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_save, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //noinspection SimplifiableIfStatement
        if (item.getItemId() == R.id.action_save) {
            String name = etContactName.getText().toString();
            if (TextUtils.isEmpty(name)) {
                etContactName.setError(getString(R.string.error_field_required));
                return false;
            }
            String familyName = etContactFamilyName.getText().toString();
            String address = etContactAddress.getText().toString();
            if (TextUtils.isEmpty(address)) {
                etContactAddress.setError(getString(R.string.error_field_required));
                return false;
            } else if (!BrahmaWeb3jService.getInstance().isValidAddress(address)) {
                etContactAddress.setError(getString(R.string.tip_error_address));
                return false;
            }
            String remark = etContactRemark.getText().toString();
            ContactEntity contact = new ContactEntity();
            contact.setAddress(address);
            contact.setName(name);
            contact.setFamilyName(familyName);
            contact.setRemark(remark);
            if (urlContactAvatar != null) {
                contact.setAvatar(urlContactAvatar.toString());
            }

            CustomProgressDialog progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.show();

            mViewModel.createContact(contact)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(() -> {
                                progressDialog.cancel();
                                showLongToast(R.string.success_create_contact);
                                finish();
                            },
                            throwable -> {
                                BLog.e(tag(), "Unable to add contact", throwable);
                                progressDialog.cancel();
                                showLongToast(R.string.error_create_contact);
                            });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void handleCameraScanPermission() {
        scanAddressCode();
    }

    private void scanAddressCode() {
        Intent intent = new Intent(this, CaptureActivity.class);
        intent.putExtra(Intents.Scan.PROMPT_MESSAGE, "");
        startActivityForResult(intent, ReqCode.SCAN_QR_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        BLog.d(tag(), "requestCode: " + requestCode + "  ;resultCode" + resultCode);
        if (requestCode == ReqCode.SCAN_QR_CODE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String qrCode = data.getStringExtra(Intents.Scan.RESULT);
                    if (qrCode != null && qrCode.length() > 0) {
                        etContactAddress.setText(qrCode);
                    } else {
                        showLongToast(R.string.tip_scan_code_failed);
                    }
                }
            }
        } else if (requestCode == ReqCode.CHOOSE_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri imageUri = data.getData();
                    Log.i(tag(), "select image from " + imageUri);
                    tempFileContactAvatar = FileHelper.getOutputImageFile(0);
                    if (tempFileContactAvatar != null) {
                        UCrop.Options options = new UCrop.Options();
                        options.setHideBottomControls(true);
                        options.setToolbarColor(getResources().getColor(R.color.master));
                        options.setStatusBarColor(getResources().getColor(R.color.master));
                        UCrop uCrop = UCrop.of(imageUri, Uri.fromFile(tempFileContactAvatar));
                        uCrop.withAspectRatio(1, 1);
                        uCrop.withMaxResultSize(256, 256);
                        uCrop.withOptions(options);
                        uCrop.start(this, ReqCode.CROP_IMAGE);
                    }
                }
            }
        } else if (requestCode == ReqCode.CROP_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    urlContactAvatar = UCrop.getOutput(data);
                    Log.i(tag(), "crop image finished - " + urlContactAvatar);
                    try {
                        if (fileContactAvatar != null) {
                            if (fileContactAvatar.delete()) {
                                Log.i(tag(), "delete file success");
                            } else {
                                Log.i(tag(), "delete file failed!");
                            }
                        }
                        fileContactAvatar = tempFileContactAvatar;
                        Bitmap bmpAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(), urlContactAvatar);
                        ivContactAvatar.setImageBitmap(ImageUtil.getCircleBitmap(bmpAvatar));
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Crop failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    public void selectContactAvatar() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, ReqCode.CHOOSE_IMAGE);
    }
}
