package io.brahmaos.wallet.brahmawallet.ui.account;

import android.animation.ObjectAnimator;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.wallet.Wallet;

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
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.BitcoinPaymentURI;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.QRCodeUtil;
import io.brahmaos.wallet.util.RxEventBus;
import rx.Observable;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;

public class AddressQrcodeBtcActivity extends BaseActivity {

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
    private AccountEntity account;
    private AccountViewModel mViewModel;
    private WalletAppKit kit;
    private Observable<Boolean> btcAppkitSetup;

    @Override
    protected String tag() {
        return AddressQrcodeBtcActivity.class.getName();
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
    }

    @Override
    protected void onStart() {
        super.onStart();
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
            Wallet wallet = kit.wallet();
            String receiveAddress = wallet.currentReceiveAddress().toBase58();
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(receiveAddress));

            BitcoinPaymentURI.Builder receiveUriBuilder = new BitcoinPaymentURI.Builder();
            String receiveUri = receiveUriBuilder.address(receiveAddress).build().getURI();
            layoutAccountAddress.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text",account.getAddress());
                if (cm != null) {
                    cm.setPrimaryClip(clipData);
                    showLongToast(R.string.tip_success_copy);
                }
            });

            new Thread(() -> {
                Bitmap bitmap = QRCodeUtil.createQRImage(receiveUri, 200, 200, null);

                if (bitmap != null) {
                    runOnUiThread(() -> Glide.with(AddressQrcodeBtcActivity.this)
                            .load(bitmap)
                            .into(ivAddressCode));
                }
            }).start();

            tvMainAddress.setOnClickListener(v -> {
                ObjectAnimator.ofFloat(tvAddressType, "translationX", 0).start();
                tvAddressType.setText(R.string.btc_main_address);
            });
            tvChildAddress.setOnClickListener(v -> {
                ObjectAnimator.ofFloat(tvAddressType, "translationX", tvAddressType.getWidth()).start();
                tvAddressType.setText(R.string.btc_child_address);
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxEventBus.get().unregister(EventTypeDef.BTC_APP_KIT_INIT_SET_UP, btcAppkitSetup);
    }
}
