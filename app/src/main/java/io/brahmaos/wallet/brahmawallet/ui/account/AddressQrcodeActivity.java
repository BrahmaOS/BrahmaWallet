package io.brahmaos.wallet.brahmawallet.ui.account;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.EthereumAddressUtil;
import io.brahmaos.wallet.util.QRCodeUtil;

public class AddressQrcodeActivity extends BaseActivity {

    private AccountEntity account;
    @BindView(R.id.iv_address_code)
    ImageView ivAddressCode;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.copy_iv)
    ImageView ivCopyAddress;

    @BindView(R.id.account_name_tv)
    TextView tvAccountName;

    @BindView(R.id.avatar_iv)
    ImageView ivAvatar;

    @Override
    protected String tag() {
        return AddressQrcodeActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_qrcode);
        ButterKnife.bind(this);
        showNavBackBtn();
        account = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);
        if (account == null) {
            finish();
        }
        initView();
    }

    private void initView() {

        tvAccountAddress.setText(EthereumAddressUtil.simplifyDisplay(account.getAddress()));
        tvAccountName.setText(account.getName());
        ImageManager.showAccountAvatar(this, ivAvatar, account);

        ivCopyAddress.setOnClickListener(v -> {
            ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text",account.getAddress());
            if (cm != null) {
                cm.setPrimaryClip(clipData);
                showLongToast(R.string.tip_success_copy);
            }
        });

        new Thread(() -> {
            int width = (int) (CommonUtil.getScreenWidth(AddressQrcodeActivity.this) * 0.6);
            Bitmap bitmap = QRCodeUtil.createQRImage(account.getAddress(), width, width, null);

            if (bitmap != null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(AddressQrcodeActivity.this)
                                .load(bitmap)
                                .into(ivAddressCode);
                    }
                });
            }
        }).start();
    }
}
