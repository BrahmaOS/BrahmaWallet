package io.brahmaos.wallet.brahmawallet.ui.account;

import android.animation.ObjectAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BitcoinPaymentURI;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.QRCodeUtil;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BtcAddressQrcodeActivity extends BaseActivity {

    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_main_address)
    TextView tvMainAddress;
    @BindView(R.id.tv_child_address)
    TextView tvChildAddress;
    @BindView(R.id.tv_address_type)
    TextView tvAddressType;
    @BindView(R.id.layout_account_address)
    LinearLayout layoutAccountAddress;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.iv_address_code)
    ImageView ivAddressCode;
    @BindView(R.id.tv_btc_receive_amount)
    TextView tvReceiveAmount;
    @BindView(R.id.tv_input_amount)
    TextView tvInputAmount;

    private int accountId;
    private String currentAddress;
    private double amount;
    private AccountEntity account;
    private AccountViewModel mViewModel;
    private WalletAppKit kit;
    private Observable<Boolean> btcAppkitSetup;

    @Override
    protected String tag() {
        return BtcAddressQrcodeActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc_address_qrcode);
        ButterKnife.bind(this);
        showNavBackBtn();

        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

        btcAppkitSetup = RxEventBus.get().register(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, Boolean.class);
        btcAppkitSetup.observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onNext(Boolean flag) {
                        if (flag) {
                            initView();
                        }
                    }

                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        Log.i(tag(), e.toString());
                    }
                });

        mViewModel.getAccountById(accountId)
                .observe(this, (AccountEntity accountEntity) -> {
                    if (accountEntity != null) {
                        account = accountEntity;
                        kit = BtcAccountManager.getInstance().getBtcWalletAppKit(account.getFilename());
                        initView();
                    } else {
                        finish();
                    }
                });
    }

    private void initView() {
        if (kit != null && kit.wallet() != null) {
            ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
            tvAccountName.setText(account.getName());
            final Wallet wallet = kit.wallet();
            String firstAddress = wallet.currentChangeAddress().toBase58();
            if (wallet.getActiveKeyChain() != null &&
                    wallet.getActiveKeyChain().getIssuedReceiveKeys() != null &&
                    wallet.getActiveKeyChain().getIssuedReceiveKeys().size() > 0) {
                firstAddress = wallet.getActiveKeyChain().getIssuedReceiveKeys().get(0).
                        toAddress(BtcAccountManager.getInstance().getNetworkParams()).toBase58();
            }
            final String mainAddress = firstAddress;
            final String childAddress = wallet.currentReceiveAddress().toBase58();
            currentAddress = mainAddress;
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(currentAddress));

            layoutAccountAddress.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", currentAddress);
                if (cm != null) {
                    cm.setPrimaryClip(clipData);
                    showLongToast(R.string.tip_success_copy);
                }
            });

            showQRCode();

            tvMainAddress.setOnClickListener(v -> {
                ObjectAnimator.ofFloat(tvAddressType, "translationX", 0).start();
                tvAddressType.setText(R.string.btc_main_address);
                currentAddress = mainAddress;
                tvAccountAddress.setText(CommonUtil.generateSimpleAddress(currentAddress));
                showQRCode();
            });
            tvChildAddress.setOnClickListener(v -> {
                ObjectAnimator.ofFloat(tvAddressType, "translationX", tvAddressType.getWidth()).start();
                tvAddressType.setText(R.string.btc_child_address);
                currentAddress = childAddress;
                tvAccountAddress.setText(CommonUtil.generateSimpleAddress(currentAddress));
                showQRCode();
            });
        }

        tvInputAmount.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_transfer_btc_amount, null);
            final EditText etAmount = dialogView.findViewById(R.id.et_btc_amount);

            AlertDialog passwordDialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Light_Dialog_Alert_Self)
                    .setView(dialogView)
                    .setCancelable(true)
                    .setPositiveButton(R.string.confirm, (dialog, which) -> {
                        dialog.cancel();
                        try {
                            amount = Double.valueOf(etAmount.getText().toString());
                            if (amount > 0) {
                                String text = String.format(Locale.getDefault(), "%s %s %s",
                                        getString(R.string.prompt_receive),
                                        etAmount.getText().toString(),
                                        getString(R.string.account_btc));
                                tvReceiveAmount.setText(text);
                            } else {
                                tvReceiveAmount.setText(R.string.transfer_btc);
                            }
                        } catch (Exception e) {
                            e.fillInStackTrace();
                        }
                        showQRCode();
                    })
                    .create();
            passwordDialog.setOnShowListener(dialog -> {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(etAmount, InputMethodManager.SHOW_IMPLICIT);
            });

            passwordDialog.show();
        });
    }

    private void showQRCode() {
        BitcoinPaymentURI.Builder receiveUriBuilder = new BitcoinPaymentURI.Builder();
        String receiveUri = receiveUriBuilder
                .address(currentAddress)
                .amount(amount)
                .build()
                .getURI();
        System.out.println(receiveUri);

        Observable<Bitmap> observable = Observable.create(e -> {
            int width = ivAddressCode.getWidth();
            Bitmap bitmap = QRCodeUtil.createQRImage(receiveUri, width, width, null);
            e.onNext(bitmap);
            e.onCompleted();
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Bitmap>() {
                    @Override
                    public void onNext(Bitmap bitmap) {
                        int width = ivAddressCode.getWidth();
                        if (width > 0) {
                            ivAddressCode.getLayoutParams().height = width;
                            ivAddressCode.requestLayout();
                        }
                        RequestOptions options = RequestOptions.placeholderOf(R.drawable.btc_address_bg);
                        Glide.with(BtcAddressQrcodeActivity.this)
                                .load(bitmap)
                                .apply(options)
                                .into(ivAddressCode);
                    }

                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }
                });
        ;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxEventBus.get().unregister(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, btcAppkitSetup);
    }
}
