package io.brahmaos.wallet.brahmawallet.ui.contact;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import io.brahmaos.wallet.util.BLog;
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
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

}
