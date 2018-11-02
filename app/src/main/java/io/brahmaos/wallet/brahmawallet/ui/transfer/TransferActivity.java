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

import org.web3j.crypto.CipherException;
import org.web3j.protocol.exceptions.TransactionTimeoutException;
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
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.CaptureActivity;
import io.brahmaos.wallet.brahmawallet.ui.common.barcode.Intents;
import io.brahmaos.wallet.brahmawallet.ui.contact.ChooseContactActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomStatusView;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TransferActivity extends BaseActivity {

    @Override
    protected String tag() {
        return TransferActivity.class.getName();
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

    @BindView(R.id.tv_eth_balance)
    TextView tvEthBalance;
    @BindView(R.id.layout_send_token_balance)
    RelativeLayout layoutSendTokenBalance;
    @BindView(R.id.tv_send_token_name)
    TextView tvSendTokenName;
    @BindView(R.id.tv_send_token_balance)
    TextView tvSendTokenBalance;

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
    private TokenEntity mToken;
    private AccountViewModel mViewModel;
    private List<AccountEntity> mAccounts = new ArrayList<>();
    private List<AccountAssets> mAccountAssetsList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer);
        ButterKnife.bind(this);
        showNavBackBtn();
        mAccount = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);
        mToken = (TokenEntity) getIntent().getSerializableExtra(IntentParam.PARAM_TOKEN_INFO);

        if (mToken == null) {
            finish();
        }
        initView();
    }

    private void initView() {
        String tokenShortName = mToken.getShortName();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setTitle(tokenShortName + getString(R.string.blank_space) +
                        getString(R.string.action_transfer));
            }
        }

        if (mToken.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
            layoutSendTokenBalance.setVisibility(View.GONE);
            layoutRemarkInput.setVisibility(View.VISIBLE);
        } else {
            layoutSendTokenBalance.setVisibility(View.VISIBLE);
            tvSendTokenName.setText(mToken.getShortName());
            layoutRemarkInput.setVisibility(View.GONE);
        }

        mAccountAssetsList = MainService.getInstance().getAccountAssetsList();

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            mAccounts = accountEntities;

            if (mAccounts != null && mAccounts.size() > 1) {
                tvChangeAccount.setVisibility(View.VISIBLE);
            } else {
                tvChangeAccount.setVisibility(View.GONE);
                if (mAccounts == null || mAccounts.size() == 0) {
                    finish();
                }
            }

            if ((mAccount == null || mAccount.getAddress().length() == 0) &&
                    accountEntities != null) {
                mAccount = mAccounts.get(0);
            }
            showAccountInfo(mAccount);
        });
        tvChangeAccount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_list, null);
            builder.setView(dialogView);
            builder.setCancelable(true);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            LinearLayout layoutAccountList = dialogView.findViewById(R.id.layout_accounts);

            for (final AccountEntity account : mAccounts) {
                final AccountItemView accountItemView = new AccountItemView();
                accountItemView.layoutAccountItem = LayoutInflater.from(this).inflate(R.layout.dialog_list_item_account, null);
                accountItemView.ivAccountAvatar = accountItemView.layoutAccountItem.findViewById(R.id.iv_account_avatar);
                accountItemView.tvAccountName = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_name);
                accountItemView.tvAccountAddress = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_address);
                accountItemView.layoutDivider = accountItemView.layoutAccountItem.findViewById(R.id.layout_divider);

                accountItemView.tvAccountName.setText(account.getName());
                ImageManager.showAccountAvatar(this, accountItemView.ivAccountAvatar, account);
                accountItemView.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));

                accountItemView.layoutAccountItem.setOnClickListener(v1 -> {
                    alertDialog.cancel();
                    mAccount = account;
                    showAccountInfo(account);
                });

                if (mAccounts.indexOf(account) == mAccounts.size() - 1) {
                    accountItemView.layoutDivider.setVisibility(View.GONE);
                }

                layoutAccountList.addView(accountItemView.layoutAccountItem);
            }
        });

        etGasPrice.setText(String.valueOf(BrahmaConst.DEFAULT_GAS_PRICE));
        etGasLimit.setText(String.valueOf(BrahmaConst.DEFAULT_GAS_LIMIT));
        btnShowTransfer.setOnClickListener(v -> showTransferInfo());
        getGasPrice();

        ivContacts.setOnClickListener(v -> {
            Intent intent = new Intent(TransferActivity.this, ChooseContactActivity.class);
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

    private void showAccountBalance() {
        for (AccountAssets assets : mAccountAssetsList) {
            if (assets.getAccountEntity().getAddress().toLowerCase().equals(mAccount.getAddress().toLowerCase())) {
                if (assets.getTokenEntity().getAddress().toLowerCase().equals(mToken.getAddress().toLowerCase())) {
                    tvSendTokenBalance.setText(String.valueOf(CommonUtil.getAccountFromWei(assets.getBalance())));
                }
                if (assets.getTokenEntity().getName().toLowerCase().equals(BrahmaConst.ETHEREUM.toLowerCase())) {
                    tvEthBalance.setText(String.valueOf(CommonUtil.getAccountFromWei(assets.getBalance())));
                }
            }
        }
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
                    if (qrCode != null && qrCode.length() > 0) {
                        etReceiverAddress.setText(qrCode);
                    } else {
                        showLongToast(R.string.tip_scan_code_failed);
                    }
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

    private class AccountItemView {
        View layoutAccountItem;
        ImageView ivAccountAvatar;
        TextView tvAccountName;
        TextView tvAccountAddress;
        LinearLayout layoutDivider;
    }

    private void showAccountInfo(AccountEntity account) {
        if (account != null) {
            ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
            tvAccountName.setText(account.getName());
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
            showAccountBalance();
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
        for (AccountAssets assets : mAccountAssetsList) {
            if (assets.getAccountEntity().getAddress().equals(mAccount.getAddress())) {
                if (assets.getTokenEntity().getAddress().equals(mToken.getAddress())) {
                    totalBalance = assets.getBalance();
                }
                if (assets.getTokenEntity().getName().toLowerCase().equals(BrahmaConst.ETHEREUM.toLowerCase())) {
                    ethTotalBalance = assets.getBalance();
                }
            }
        }

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

        // check the ether is enough
        if (!cancel && mToken.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
            totalBalance = ethTotalBalance;
            if (CommonUtil.convertWeiFromEther(amount.add(BrahmaConst.DEFAULT_FEE)).compareTo(totalBalance) > 0) {
                tips = getString(R.string.tip_insufficient_eth);
                cancel = true;
            }
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

        TextView tvTransferToken = view.findViewById(R.id.tv_dialog_transfer_token);
        tvTransferToken.setText(mToken.getShortName());

        LinearLayout layoutTransferStatus = view.findViewById(R.id.layout_transfer_status);
        CustomStatusView customStatusView = view.findViewById(R.id.as_status);
        TextView tvTransferStatus = view.findViewById(R.id.tv_transfer_status);
        Button confirmBtn = view.findViewById(R.id.btn_commit_transfer);
        BigDecimal finalAmount = amount;
        confirmBtn.setOnClickListener(v -> {
            final View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_password, null);

            AlertDialog passwordDialog = new AlertDialog.Builder(TransferActivity.this)
                    .setView(dialogView)
                    .setCancelable(false)
                    .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                    .setPositiveButton(R.string.ok, (dialog, which) -> {
                        dialog.cancel();
                        // show transfer progress
                        layoutTransferStatus.setVisibility(View.VISIBLE);
                        customStatusView.loadLoading();
                        String password = ((EditText) dialogView.findViewById(R.id.et_password)).getText().toString();
                        BrahmaWeb3jService.getInstance().sendTransfer(mAccount, mToken, password, receiverAddress,
                                finalAmount, gasPrice, gasLimit, remark)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<Integer>() {
                                    @Override
                                    public void onNext(Integer flag) {
                                        if (flag == 10) {
                                            tvTransferStatus.setText(R.string.progress_transfer_success);
                                            BLog.i(tag(), "the transfer success");
                                            customStatusView.loadSuccess();
                                            new Handler().postDelayed(() -> {
                                                transferInfoDialog.cancel();
                                                // Eth transfer is a real-time arrival, and token transfer may take longer,
                                                // so there is no need to refresh
                                                if (mToken.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
                                                    RxBus.get().post(EventTypeDef.ACCOUNT_ASSETS_TRANSFER, "succ");
                                                }
                                                finish();
                                            }, 1200);
                                        } else if (flag == 1) {
                                            tvTransferStatus.setText(R.string.progress_verify_account);
                                        } else if (flag == 2) {
                                            tvTransferStatus.setText(R.string.progress_send_request);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e) {
                                        e.printStackTrace();
                                        customStatusView.loadFailure();
                                        tvTransferStatus.setText(R.string.progress_transfer_fail);
                                        new Handler().postDelayed(() -> {
                                            layoutTransferStatus.setVisibility(View.GONE);
                                            int resId = R.string.tip_error_transfer;
                                            if (e instanceof CipherException) {
                                                resId = R.string.tip_error_password;
                                            } else if (e instanceof TransactionTimeoutException) {
                                                resId = R.string.tip_error_net;
                                            }
                                            new AlertDialog.Builder(TransferActivity.this)
                                                    .setMessage(resId)
                                                    .setNegativeButton(R.string.ok, (dialog1, which1) -> dialog1.cancel())
                                                    .create().show();
                                        }, 1500);

                                        BLog.i(tag(), "the transfer failed");
                                    }

                                    @Override
                                    public void onCompleted() {
                                    }
                                });
                        })
                    .create();
            passwordDialog.show();
        });
    }

}
