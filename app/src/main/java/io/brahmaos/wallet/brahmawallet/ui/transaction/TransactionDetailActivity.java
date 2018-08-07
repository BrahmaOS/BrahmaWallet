package io.brahmaos.wallet.brahmawallet.ui.transaction;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.web3j.utils.Convert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.ContactEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.EthTransaction;
import io.brahmaos.wallet.brahmawallet.model.TokenTransaction;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.TransactionService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.contact.AddContactActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.TransferActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.ContactViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import me.yokeyword.indexablerv.IndexableLayout;
import me.yokeyword.indexablerv.SimpleHeaderAdapter;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class TransactionDetailActivity extends BaseActivity {
    @Override
    protected String tag() {
        return TransactionDetailActivity.class.getName();
    }

    public static final int REQ_CODE_TRANSFER = 10;

    // UI references.
    @BindView(R.id.iv_send_account_avatar)
    ImageView ivSendAccountAvatar;
    @BindView(R.id.tv_send_account_name)
    TextView tvSendAccountName;
    @BindView(R.id.tv_send_account_address)
    TextView tvSendAccountAddress;
    @BindView(R.id.iv_receive_account_avatar)
    ImageView ivReceiveAccountAvatar;
    @BindView(R.id.tv_receive_account_name)
    TextView tvReceiveAccountName;
    @BindView(R.id.tv_receive_account_address)
    TextView tvReceiveAccountAddress;

    @BindView(R.id.tv_transaction_tx_hash)
    TextView tvTxHash;
    @BindView(R.id.tv_transaction_block_height)
    TextView tvBlockHeight;
    @BindView(R.id.tv_transaction_time)
    TextView tvTxTime;
    @BindView(R.id.layout_transaction_token_transfered)
    RelativeLayout layoutTokenTransfered;
    @BindView(R.id.tv_transaction_token_transfered)
    TextView tvTokenTransfered;
    @BindView(R.id.laytou_divider_token_transfered)
    LinearLayout layoutTokenTransferedDivider;
    @BindView(R.id.tv_transaction_value)
    TextView tvTransactionValue;
    @BindView(R.id.tv_gas_value)
    TextView tvTxGasValue;
    @BindView(R.id.tv_gas_used)
    TextView tvTxGasUsed;
    @BindView(R.id.tv_gas_price)
    TextView tvTxGasPrice;

    private EthTransaction mEthTx;
    private TokenTransaction mTokenTx;
    private String fromAddress;
    private String toAddress;
    private String txHash;
    private ContactViewModel mContactViewModel;

    // Determine whether to traverse
    private boolean contactFlag = false;
    private boolean accountFlag = false;

    private boolean fromFlag = false;
    private boolean toFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_detail);
        ButterKnife.bind(this);
        showNavBackBtn();
        mContactViewModel = ViewModelProviders.of(this).get(ContactViewModel.class);

        mEthTx = (EthTransaction) getIntent().getSerializableExtra(IntentParam.PARAM_ETH_TX);
        mTokenTx = (TokenTransaction) getIntent().getSerializableExtra(IntentParam.PARAM_TOKEN_TX);

        if (mEthTx == null && mTokenTx == null) {
            finish();
        }

        if (mEthTx != null) {
            fromAddress = mEthTx.getFromAddress();
            toAddress = mEthTx.getToAddress();
            txHash = mEthTx.getHash();
            initEthData();
        } else {
            fromAddress = mTokenTx.getFromAddress();
            toAddress = mTokenTx.getToAddress();
            txHash = mTokenTx.getEthTransaction().getHash();
            initTokenData();
        }
        initHeader();

    }

    private void initHeader() {
        tvSendAccountAddress.setText(fromAddress);
        tvReceiveAccountAddress.setText(toAddress);
        mContactViewModel.getContacts().observe(this, contactEntities -> {
            if (contactEntities != null) {
                for (ContactEntity contactEntity : contactEntities) {
                    if (contactEntity.getAddress().toLowerCase().equals(fromAddress.toLowerCase())) {
                        tvSendAccountName.setVisibility(View.VISIBLE);
                        tvSendAccountName.setText(new StringBuilder().append(contactEntity.getName()).append(" ").append(contactEntity.getFamilyName()).toString());
                        ivSendAccountAvatar.setBackgroundResource(R.drawable.icon_contact_circle_bg);
                        Glide.with(this)
                                .load(R.drawable.ic_person_account)
                                .into(ivSendAccountAvatar);

                        fromFlag = true;
                    }
                    if (contactEntity.getAddress().toLowerCase().equals(toAddress.toLowerCase())) {
                        tvReceiveAccountName.setVisibility(View.VISIBLE);
                        tvReceiveAccountName.setText(new StringBuilder().append(contactEntity.getName()).append(" ").append(contactEntity.getFamilyName()).toString());
                        ivReceiveAccountAvatar.setBackgroundResource(R.drawable.icon_contact_circle_bg);
                        Glide.with(this)
                                .load(R.drawable.ic_person_account)
                                .into(ivReceiveAccountAvatar);

                        toFlag = true;
                    }
                }
            }
            contactFlag = true;
            judgeAddressStatus();
        });
        mContactViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities != null) {
                for (AccountEntity accountEntity : accountEntities) {
                    if (accountEntity.getAddress().toLowerCase().equals(fromAddress.toLowerCase())) {
                        tvSendAccountName.setVisibility(View.VISIBLE);
                        tvSendAccountName.setText(accountEntity.getName());
                        ImageManager.showAccountAvatar(this, ivSendAccountAvatar, accountEntity);

                        fromFlag = true;
                    }
                    if (accountEntity.getAddress().toLowerCase().equals(toAddress.toLowerCase())) {
                        tvReceiveAccountName.setVisibility(View.VISIBLE);
                        tvReceiveAccountName.setText(accountEntity.getName());
                        ImageManager.showAccountAvatar(this, ivReceiveAccountAvatar, accountEntity);

                        toFlag = true;
                    }
                }
            }
            accountFlag = true;
            judgeAddressStatus();
        });
    }

    private void judgeAddressStatus() {
        if (accountFlag && contactFlag) {
            if (!fromFlag) {
                tvSendAccountName.setVisibility(View.GONE);
                ivSendAccountAvatar.setBackgroundResource(0);
                Glide.with(this)
                        .load(R.drawable.ic_person_add)
                        .into(ivSendAccountAvatar);
                ivSendAccountAvatar.setOnClickListener(v -> {
                    Intent intent = new Intent(TransactionDetailActivity.this, AddContactActivity.class);
                    intent.putExtra(IntentParam.PARAM_ETH_ADDRESS, fromAddress);
                    startActivity(intent);
                });
            }
            if (!toFlag) {
                tvReceiveAccountName.setVisibility(View.GONE);
                ivReceiveAccountAvatar.setBackgroundResource(0);
                Glide.with(this)
                        .load(R.drawable.ic_person_add)
                        .into(ivReceiveAccountAvatar);
                ivReceiveAccountAvatar.setOnClickListener(v -> {
                    Intent intent = new Intent(TransactionDetailActivity.this, AddContactActivity.class);
                    intent.putExtra(IntentParam.PARAM_ETH_ADDRESS, toAddress);
                    startActivity(intent);
                });
            }
        }
    }

    private void initEthData() {
        tvTxHash.setText(CommonUtil.generateSimpleAddress(mEthTx.getHash()));
        tvBlockHeight.setText(String.valueOf(mEthTx.getBlockHeight()));
        layoutTokenTransfered.setVisibility(View.GONE);
        layoutTokenTransferedDivider.setVisibility(View.GONE);
        tvTransactionValue.setText(String.valueOf(CommonUtil.getAccountFromWei(mEthTx.getValue())));
        tvTxGasUsed.setText(String.valueOf(mEthTx.getGasUsed()));
        tvTxGasPrice.setText(String.valueOf(Convert.fromWei(new BigDecimal(mEthTx.getGasPrice()), Convert.Unit.GWEI).setScale(3, BigDecimal.ROUND_HALF_UP)));
        BigDecimal gasValue = Convert.fromWei(new BigDecimal(mEthTx.getGasUsed()).multiply(new BigDecimal(mEthTx.getGasPrice())), Convert.Unit.ETHER);
        tvTxGasValue.setText(String.valueOf(gasValue.setScale(9, BigDecimal.ROUND_HALF_UP)));
        tvTxTime.setText(CommonUtil.timestampToDate(mEthTx.getTxTime(), null));
    }

    private void initTokenData() {
        tvTxHash.setText(CommonUtil.generateSimpleAddress(mTokenTx.getEthTransaction().getHash()));
        tvBlockHeight.setText(String.valueOf(mTokenTx.getEthTransaction().getBlockHeight()));
        layoutTokenTransfered.setVisibility(View.VISIBLE);
        layoutTokenTransferedDivider.setVisibility(View.VISIBLE);
        tvTokenTransfered.setText(String.valueOf(CommonUtil.getAccountFromWei(mTokenTx.getValue())));
        tvTransactionValue.setText(String.valueOf(CommonUtil.getAccountFromWei(mTokenTx.getEthTransaction().getValue())));
        tvTxGasUsed.setText(String.valueOf(mTokenTx.getEthTransaction().getGasUsed()));
        tvTxGasPrice.setText(String.valueOf(Convert.fromWei(new BigDecimal(mTokenTx.getEthTransaction().getGasPrice()), Convert.Unit.GWEI).setScale(3, BigDecimal.ROUND_HALF_UP)));
        BigDecimal gasValue = Convert.fromWei(new BigDecimal(mTokenTx.getEthTransaction().getGasUsed()).multiply(new BigDecimal(mTokenTx.getEthTransaction().getGasPrice())), Convert.Unit.ETHER);
        tvTxGasValue.setText(String.valueOf(gasValue.setScale(9, BigDecimal.ROUND_HALF_UP)));
        tvTxTime.setText(CommonUtil.timestampToDate(mTokenTx.getEthTransaction().getTxTime(), null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tx_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_tx_detail) {
            Intent intent = new Intent(TransactionDetailActivity.this, EtherscanTxDetailActivity.class);
            intent.putExtra(IntentParam.PARAM_TX_HASH, txHash);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
