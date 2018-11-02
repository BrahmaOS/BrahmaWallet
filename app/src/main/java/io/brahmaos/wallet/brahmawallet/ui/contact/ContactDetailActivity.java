package io.brahmaos.wallet.brahmawallet.ui.contact;

import android.annotation.SuppressLint;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.ImageUtil;
import io.brahmaos.wallet.util.QRCodeUtil;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ContactDetailActivity extends BaseActivity {

    private int contactId;
    private ContactEntity contact;
    private ContactViewModel mViewModel;

    @BindView(R.id.img_contact_avatar)
    ImageView ivContactAvatar;

    @BindView(R.id.tv_contact_name)
    TextView tvContactName;

    // Ethereum

    @BindView(R.id.tv_contact_address)
    TextView tvContactAddress;

    @BindView(R.id.tv_contact_remark)
    TextView tvContactRemark;

    @BindView(R.id.iv_address_code)
    ImageView ivContactAddress;

    @BindView(R.id.eth_account_layout)
    LinearLayout layoutEthAccount;

    @BindView(R.id.eth_btc_divider)
    LinearLayout dividerEthBtc;

    // Bitcoin

    @BindView(R.id.btc_account_layout)
    LinearLayout layoutBtcAccount;

    @BindView(R.id.tv_btc_contact_address)
    TextView tvContactBtcAddress;

    @BindView(R.id.iv_btc_address_code)
    ImageView ivContactBtcAddressCode;

    @Override
    protected String tag() {
        return ContactDetailActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        ButterKnife.bind(this);
        showNavBackBtn();
        contactId = getIntent().getIntExtra(IntentParam.PARAM_CONTACT_ID, 0);
        if (contactId <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);
    }

    protected void onStart() {
        super.onStart();
        mViewModel.getContactById(contactId)
                .observe(this, (ContactEntity contactEntity) -> {
                    if (contactEntity != null) {
                        contact = contactEntity;
                        initContactInfo();
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void initContactInfo() {
        tvContactName.setText(contact.getName() + " " + contact.getFamilyName());

        if (contact.getAddress() != null && contact.getAddress().length() > 0) {
            layoutEthAccount.setVisibility(View.VISIBLE);
            tvContactAddress.setText(contact.getAddress());
        } else {
            layoutEthAccount.setVisibility(View.GONE);
            dividerEthBtc.setVisibility(View.GONE);
        }

        if (contact.getBtcAddress() != null && contact.getBtcAddress().length() > 0) {
            layoutBtcAccount.setVisibility(View.VISIBLE);
            tvContactBtcAddress.setText(contact.getBtcAddress());
        } else {
            layoutBtcAccount.setVisibility(View.GONE);
            dividerEthBtc.setVisibility(View.GONE);
        }

        tvContactRemark.setText(contact.getRemark());
        ivContactAddress.setOnClickListener(v -> showQrcodeDialog(contact.getAddress()));
        ivContactBtcAddressCode.setOnClickListener(v -> showBtcQrcodeDialog(contact.getBtcAddress()));
        if (contact.getAvatar() != null && contact.getAvatar().length() > 0 && !contact.getAvatar().equals("null")) {
            Uri uriAvatar = Uri.parse(contact.getAvatar());
            try {
                Bitmap bmpAvatar = MediaStore.Images.Media.getBitmap(getContentResolver(), uriAvatar);
                ivContactAvatar.setImageBitmap(ImageUtil.getCircleBitmap(bmpAvatar));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "show failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_edit) {
            Intent intent = new Intent(ContactDetailActivity.this, EditContactActivity.class);
            intent.putExtra(IntentParam.PARAM_CONTACT_ID, contactId);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_delete) {
            AlertDialog deleteDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.delete_contact_tip)
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, ((dialog, which) -> dialog.cancel()))
                    .setPositiveButton(R.string.delete, (dialog, which) -> deleteContact())
                    .create();
            deleteDialog.show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showQrcodeDialog(String address) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_contact_address, null);
        TextView tvKeystore = dialogView.findViewById(R.id.tv_contact_address);
        ImageView ivQrcode = dialogView.findViewById(R.id.iv_contact_address_code);
        Button btnCopyAddress = dialogView.findViewById(R.id.btn_copy_address);
        tvKeystore.setText(address);
        AlertDialog qrcodeDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        new Thread(() -> {
            Bitmap bitmap = QRCodeUtil.createQRImageTransparentBg(address, 200, 200, null);

            if (bitmap != null) {
                runOnUiThread(() -> Glide.with(ContactDetailActivity.this)
                        .load(bitmap)
                        .into(ivQrcode));
            }
        }).start();
        qrcodeDialog.show();
        btnCopyAddress.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", address);
            if (cm != null) {
                cm.setPrimaryClip(clipData);
                //qrcodeDialog.cancel();
                showLongToast(R.string.tip_success_copy);
            }
        });
    }

    private void showBtcQrcodeDialog(String btcAddress) {
        final View dialogView = getLayoutInflater().inflate(R.layout.dialog_contact_address, null);
        TextView tvContactAddress = dialogView.findViewById(R.id.tv_contact_address);
        ImageView ivQrcode = dialogView.findViewById(R.id.iv_contact_address_code);
        Button btnCopyAddress = dialogView.findViewById(R.id.btn_copy_address);
        tvContactAddress.setText(btcAddress);
        AlertDialog qrcodeDialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();
        new Thread(() -> {
            Bitmap bitmap = QRCodeUtil.createQRImageTransparentBg("bitcoin:" + btcAddress, 200, 200, null);
            if (bitmap != null) {
                runOnUiThread(() -> Glide.with(ContactDetailActivity.this)
                        .load(bitmap)
                        .into(ivQrcode));
            }
        }).start();
        qrcodeDialog.show();
        btnCopyAddress.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", btcAddress);
            if (cm != null) {
                cm.setPrimaryClip(clipData);
                showLongToast(R.string.tip_success_copy);
            }
        });
    }

    private void deleteContact() {
        mViewModel.deleteContact(contactId).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                            showLongToast(R.string.success_delete_account);
                            finish();
                        },
                        throwable -> {
                            BLog.e(tag(), "Unable to delete contact", throwable);
                            showLongToast(R.string.error_delete_account);
                        });
    }
}
