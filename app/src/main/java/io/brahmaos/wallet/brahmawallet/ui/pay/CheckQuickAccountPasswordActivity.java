package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.util.ImageUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class CheckQuickAccountPasswordActivity extends BaseActivity {
    private EditText mEtPassword;
    private ImageView mIvAvatar;
    private TextView mTvAccountName, mTvAccountID;
    private CustomProgressDialog progressDialog;

    @Override
    protected String tag() {
        return CheckQuickAccountPasswordActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_account_check_password);
        mEtPassword = findViewById(R.id.et_quick_account_password);
        mIvAvatar = findViewById(R.id.iv_quick_account_avatar);
        mTvAccountName = findViewById(R.id.tv_quick_account_name);
        mTvAccountID = findViewById(R.id.tv_quick_account_id);

        progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Bitmap avatar = BrahmaConfig.getInstance().getPayAccountAvatar();
        if (avatar != null) {
            mIvAvatar.setImageBitmap(ImageUtil.getCircleBitmap(avatar));
        }
        mTvAccountName.setText(BrahmaConfig.getInstance().getPayAccountName());
        mTvAccountID.setText("" + BrahmaConfig.getInstance().getPayAccountID());
    }

    public void onButtonClick(View v) {
        String password = mEtPassword.getText() == null ? "" : mEtPassword.getText().toString();
        if (password.isEmpty()) {
            showPasswordErrorDialog();
        } else {
            checkPrivateKey(password);
        }
    }

    private void checkPrivateKey(String password) {
        progressDialog.show();
        BrahmaWeb3jService.getInstance()
                .getEcKeyByPassword(
                        BrahmaConfig.getInstance().getPayAccountWalletFileName(), password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map>() {
                    @Override
                    public void onNext(Map ecKeys) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        if (ecKeys != null && ecKeys.containsKey(BrahmaConst.PRIVATE_KEY)
                                && ecKeys.get(BrahmaConst.PRIVATE_KEY) != null
                                && BrahmaWeb3jService.getInstance().isValidPrivateKey(
                                        String.valueOf(ecKeys.get(BrahmaConst.PRIVATE_KEY)))) {
                            Intent intent = new Intent(CheckQuickAccountPasswordActivity.this,
                                    SetPayAccountPasswordActivity.class);
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID,
                                    BrahmaConfig.getInstance().getPayAccountID());
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_PRIVATE_KEY,
                                    String.valueOf(ecKeys.get(BrahmaConst.PRIVATE_KEY)));
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_PUBLIC_KEY,
                                    String.valueOf(ecKeys.get(BrahmaConst.PUBLIC_KEY)));
                            startActivity(intent);
                            finish();
                        } else {
                            showPasswordErrorDialog();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        showPasswordErrorDialog();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void showPasswordErrorDialog() {
        AlertDialog errorDialog = new AlertDialog.Builder(this)
                .setMessage(R.string.error_current_password)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .create();
        errorDialog.show();
    }

}
