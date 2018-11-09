package io.brahmaos.wallet.brahmawallet.ui.transfer;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hwangjr.rxbus.RxBus;

import org.bitcoinj.kits.WalletAppKit;
import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.common.ReqCode;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.event.EventTypeDef;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.brahmawallet.ui.contact.ChooseContactActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomStatusView;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.BitcoinPaymentURI;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BtcTransferActivity extends BaseActivity {

    @Override
    protected String tag() {
        return BtcTransferActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.tv_change_account)
    TextView tvChangeAccount;
    @BindView(R.id.tv_btc_balance)
    TextView tvBtcBalance;
    @BindView(R.id.btn_show_transfer_info)
    Button btnShowTransfer;
    @BindView(R.id.et_receiver_address)
    EditText etReceiverAddress;
    @BindView(R.id.et_amount)
    EditText etAmount;
    @BindView(R.id.layout_text_input_remark)
    TextInputLayout layoutRemarkInput;
    @BindView(R.id.et_remark)
    EditText etRemark;
    @BindView(R.id.et_gas_price)
    EditText etGasPrice;
    @BindView(R.id.et_gas_limit)
    EditText etGasLimit;
    @BindView(R.id.iv_contacts)
    ImageView ivContacts;

    private AccountEntity mAccount;
    private AccountViewModel mViewModel;
    private WalletAppKit kit;
    private List<AccountEntity> mAccounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc_transfer);
        ButterKnife.bind(this);
        showNavBackBtn();
        mAccount = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);

        initView();
    }

    private void initView() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setTitle(getString(R.string.account_btc) + getString(R.string.blank_space) +
                        getString(R.string.action_transfer));
            }
        }

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null) {
                mAccounts.clear();
                for (AccountEntity accountEntity : accountEntities) {
                    if (accountEntity.getType() == BrahmaConst.BTC_ACCOUNT_TYPE) {
                        mAccounts.add(accountEntity);
                    }
                }

                if (mAccounts != null && mAccounts.size() > 1) {
                    tvChangeAccount.setVisibility(View.VISIBLE);
                } else {
                    tvChangeAccount.setVisibility(View.GONE);
                    if (mAccounts == null || mAccounts.size() == 0) {
                        finish();
                    }
                }

                if (mAccount == null) {
                    mAccount = mAccounts.get(0);
                }
                kit = BtcAccountManager.getInstance().getBtcWalletAppKit(mAccount.getFilename());
                showAccountInfo(mAccount);
            }
        });

        etGasPrice.setText(String.valueOf(BrahmaConst.DEFAULT_GAS_PRICE));
        etGasLimit.setText(String.valueOf(BrahmaConst.DEFAULT_GAS_LIMIT));
        btnShowTransfer.setOnClickListener(v -> showTransferInfo());
        getGasPrice();

        ivContacts.setOnClickListener(v -> {
            Intent intent = new Intent(BtcTransferActivity.this, ChooseContactActivity.class);
            intent.putExtra(IntentParam.PARAM_ACCOUNT_TYPE, BrahmaConst.BTC_ACCOUNT_TYPE);
            startActivityForResult(intent, ReqCode.CHOOSE_TRANSFER_CONTACT);
        });
    }

    public void getGasPrice() {
        BrahmaWeb3jService.getInstance()
                .getGasPrice()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<BigInteger>() {
                    @Override
                    public void onNext(BigInteger gasPrice) {
                        BLog.d(tag(), "the gas price is: " + String.valueOf(gasPrice));
                        BigDecimal gasPriceGwei = Convert.fromWei(new BigDecimal(gasPrice), Convert.Unit.GWEI);
                        etGasPrice.setText(String.valueOf(gasPriceGwei));
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_transfer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_scan) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestCameraScanPermission();
            } else {
                scanAddressCode();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
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
                    BitcoinPaymentURI bitcoinUri = BitcoinPaymentURI.parse(qrCode);
                    if (bitcoinUri == null) {
                        showLongToast(R.string.invalid_btc_address);
                        return;
                    }
                    etReceiverAddress.setText(bitcoinUri.getAddress());
                    etAmount.setText(String.valueOf(bitcoinUri.getAmount()));
                }
            }
        } else if (requestCode == ReqCode.CHOOSE_TRANSFER_CONTACT) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    String address = data.getStringExtra(IntentParam.PARAM_CONTACT_ADDRESS);
                    if (address != null && address.length() > 0) {
                        etReceiverAddress.setText(address);
                    }
                }
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void handleCameraScanPermission() {
        scanAddressCode();
    }

    private void showAccountInfo(AccountEntity account) {
        if (account != null && kit != null && kit.wallet() != null) {
            ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
            tvAccountName.setText(account.getName());
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(kit.wallet().currentReceiveAddress().toBase58()));
            tvBtcBalance.setText(String.valueOf(CommonUtil.convertUnit(BrahmaConst.BITCOIN, kit.wallet().getBalance().value)));
        }
    }

    private void showTransferInfo() {
        String receiverAddress = etReceiverAddress.getText().toString().trim();
        String transferAmount = etAmount.getText().toString().trim();
        String remark = etRemark.getText().toString().trim();
        BigDecimal amount = BigDecimal.ZERO;
        String gasPriceStr = etGasPrice.getText().toString().trim();
        String gasLimitStr = etGasLimit.getText().toString().trim();

        String tips = "";
        boolean cancel = false;
        if (!BrahmaWeb3jService.getInstance().isValidAddress(receiverAddress)) {
            tips = getString(R.string.tip_error_address);
            cancel = true;
        }
        if (!cancel && receiverAddress.equals(mAccount.getAddress())) {
            tips = getString(R.string.tip_same_address);
            cancel = true;
        }

        if (!cancel && gasPriceStr.length() < 1) {
            tips = getString(R.string.tip_invalid_gas_price);
            cancel = true;
        }

        if (!cancel && gasLimitStr.length() < 1) {
            tips = getString(R.string.tip_invalid_gas_limit);
            cancel = true;
        }

        BigInteger totalBalance = BigInteger.ZERO;
        BigInteger ethTotalBalance = BigInteger.ZERO;

        if (!cancel) {
            if (transferAmount.length() < 1) {
                tips = getString(R.string.tip_invalid_amount);
                cancel = true;
            } else {
                amount = new BigDecimal(transferAmount);
            }
        }

        if (!cancel && (amount.compareTo(BigDecimal.ZERO) <= 0 ||
                CommonUtil.convertWeiFromEther(amount).compareTo(totalBalance) > 0)) {
            tips = getString(R.string.tip_invalid_amount);
            cancel = true;
        }

        if (!cancel && ethTotalBalance.compareTo(BigInteger.ZERO) <= 0) {
            tips = getString(R.string.tip_insufficient_eth);
            cancel = true;
        }

        if (cancel) {
            // dialog show tip
            AlertDialog dialogTip = new AlertDialog.Builder(this)
                    .setMessage(tips)
                    .setNegativeButton(R.string.ok, (dialog, which) -> dialog.cancel())
                    .create();
            dialogTip.show();
            return;
        }

        BigDecimal gasPrice = new BigDecimal(gasPriceStr);
        BigInteger gasLimit = new BigInteger(gasLimitStr);

        final BottomSheetDialog transferInfoDialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.bottom_sheet_dialog_transfer_info, null);
        transferInfoDialog.setContentView(view);
        transferInfoDialog.setCancelable(false);
        transferInfoDialog.show();

        ImageView ivCloseDialog = view.findViewById(R.id.iv_close_dialog);
        ivCloseDialog.setOnClickListener(v -> transferInfoDialog.cancel());

        TextView tvDialogPayToAddress = view.findViewById(R.id.tv_pay_to_address);
        tvDialogPayToAddress.setText(receiverAddress);

        TextView tvDialogPayByAddress = view.findViewById(R.id.tv_pay_by_address);
        tvDialogPayByAddress.setText(CommonUtil.generateSimpleAddress(mAccount.getAddress()));

        TextView tvGasPrice = view.findViewById(R.id.tv_gas_price);
        tvGasPrice.setText(gasPriceStr);
        TextView tvGasLimit = view.findViewById(R.id.tv_gas_limit);
        tvGasLimit.setText(gasLimitStr);
        TextView tvGasValue = view.findViewById(R.id.tv_gas_value);
        BigDecimal gasValue = Convert.fromWei(Convert.toWei(new BigDecimal(gasLimit).multiply(gasPrice), Convert.Unit.GWEI), Convert.Unit.ETHER);
        tvGasValue.setText(String.valueOf(gasValue.setScale(9, BigDecimal.ROUND_HALF_UP)));

        TextView tvTransferAmount = view.findViewById(R.id.tv_dialog_transfer_amount);
        tvTransferAmount.setText(String.valueOf(amount));

        LinearLayout layoutTransferStatus = view.findViewById(R.id.layout_transfer_status);
        CustomStatusView customStatusView = view.findViewById(R.id.as_status);
        TextView tvTransferStatus = view.findViewById(R.id.tv_transfer_status);
        Button confirmBtn = view.findViewById(R.id.btn_commit_transfer);
        BigDecimal finalAmount = amount;
        confirmBtn.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);

            AlertDialog passwordDialog = new AlertDialog.Builder(BtcTransferActivity.this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.cancel();
                        // show transfer progress
                        layoutTransferStatus.setVisibility(View.VISIBLE);
                        customStatusView.loadLoading();
                        String password = ((EditText) dialogView.findViewById(R.id.et_password)).getText().toString();
                        })
                    .create();
            passwordDialog.show();
        });
    }

}
