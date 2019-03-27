package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.BrahmaOSURI;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.QRCodeUtil;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PayAccountReceiptActivity extends BaseActivity {
    @BindView(R.id.iv_address_code)
    ImageView ivPaymentCode;
    @BindView(R.id.tv_choose_token)
    TextView tvChosenToken;
    @BindView(R.id.tv_input_amount)
    TextView tvPaymentAmount;

    private String mQuickAccountID;
    private String mChosenCoin = "brm";
    private Double mAmount;

    @Override
    protected String tag() {
        return PayAccountReceiptActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_account_receipt);
        showNavBackBtn();
        ButterKnife.bind(this);
        mQuickAccountID = BrahmaConfig.getInstance().getPayAccount();
        if (CommonUtil.isNull(mQuickAccountID)) {
            finish();
            return;
        }
        tvChosenToken.setOnClickListener(v -> {
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_recharge_coins, null);
            builder.setView(dialogView);
            builder.setCancelable(true);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            RelativeLayout layoutCoinBrm = dialogView.findViewById(R.id.layout_coin_brm);
            layoutCoinBrm.setOnClickListener(v1 -> {
                mChosenCoin = BrahmaConst.COIN_SYMBOL_BRM;
                tvChosenToken.setText(BrahmaConst.COIN_SYMBOL_BRM);
                alertDialog.cancel();
                showQrCode();
            });
            RelativeLayout layoutCoinEth = dialogView.findViewById(R.id.layout_coin_eth);
            layoutCoinEth.setOnClickListener(v1 -> {
                mChosenCoin = BrahmaConst.COIN_SYMBOL_ETH;
                tvChosenToken.setText(BrahmaConst.COIN_SYMBOL_ETH);
                alertDialog.cancel();
                showQrCode();
            });
            RelativeLayout layoutCoinBtc = dialogView.findViewById(R.id.layout_coin_btc);
            layoutCoinBtc.setOnClickListener(v1 -> {
                mChosenCoin = BrahmaConst.COIN_SYMBOL_BTC;
                tvChosenToken.setText(BrahmaConst.COIN_SYMBOL_BTC);
                alertDialog.cancel();
                showQrCode();
            });
        });
        tvPaymentAmount.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_transfer_btc_amount, null);
            final EditText etAmount = dialogView.findViewById(R.id.et_btc_amount);

            AlertDialog passwordDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        try {
                            mAmount = Double.valueOf(etAmount.getText().toString());
                            if (mAmount > 0) {
                                dialog.cancel();
                                String text = String.format(Locale.getDefault(), "%s %s %s",
                                        getString(R.string.prompt_receive),
                                        etAmount.getText().toString(),
                                        mChosenCoin);
                                tvPaymentAmount.setText(text);
                                showQrCode();
                            } else {
                                showShortToast(getString(R.string.tip_invalid_amount));
                            }
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                    })
                    .create();
            passwordDialog.setOnShowListener(dialog -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
            });

            passwordDialog.show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showQrCode();
    }

    private void showQrCode() {
        String content = new BrahmaOSURI.Builder()
                .address(mQuickAccountID)
                .coin(mChosenCoin)
                .amount(mAmount)
                .build()
                .getURI();
        Log.d(tag(), "uri content=" + content);
        Observable<Bitmap> observable = Observable.create(e -> {
            int width = ivPaymentCode.getWidth();
            if (width <= 0) {
                width = (CommonUtil.getScreenWidth(PayAccountReceiptActivity.this) - CommonUtil.dip2px(PayAccountReceiptActivity.this, 80)) * 55 / 100;
            }
            Bitmap bitmap = QRCodeUtil.createQRImage(content, width, width, null);
            e.onNext(bitmap);
            e.onCompleted();
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onNext(Bitmap bitmap) {
                        int width = ivPaymentCode.getWidth();
                        if (width > 0) {
                            ivPaymentCode.getLayoutParams().height = width;
                            ivPaymentCode.requestLayout();
                        }
                        Glide.with(PayAccountReceiptActivity.this)
                                .load(bitmap)
                                .into(ivPaymentCode);
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
