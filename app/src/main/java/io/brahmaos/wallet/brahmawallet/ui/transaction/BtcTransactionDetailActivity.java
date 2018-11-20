package io.brahmaos.wallet.brahmawallet.ui.transaction;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.kits.WalletAppKit;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;

public class BtcTransactionDetailActivity extends BaseActivity {
    @Override
    protected String tag() {
        return BtcTransactionDetailActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.tv_transaction_amount)
    TextView mTvBtcAmount;
    @BindView(R.id.tv_transaction_hash)
    TextView mTvTxHash;
    @BindView(R.id.tv_transaction_block_height)
    TextView mTvTxFirstConfirmedBlock;
    @BindView(R.id.tv_confirmations)
    TextView mTvConfirmations;
    @BindView(R.id.tv_transaction_date)
    TextView mTvTxDatetime;
    @BindView(R.id.layout_input_transaction)
    LinearLayout mLayoutTransactionInput;

    private Transaction mTransaction;
    private WalletAppKit kit;
    private AccountEntity mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc_transaction_detail);
        ButterKnife.bind(this);
        showNavBackBtn();
        mAccount = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);
        if (mAccount == null) {
            finish();
        }
        Sha256Hash transactionHash = (Sha256Hash) getIntent().getSerializableExtra(IntentParam.PARAM_TX_HASH);
        if (transactionHash == null) {
            finish();
        }
        kit = BtcAccountManager.getInstance().getBtcWalletAppKit(mAccount.getFilename());
        if (kit != null && kit.wallet() != null && kit.wallet().getTransaction(transactionHash) != null) {
            mTransaction = kit.wallet().getTransaction(transactionHash);
            initView();
        } else {
            finish();
        }
    }

    private void initView() {
        String sendAmount = String.valueOf(CommonUtil.convertBTCFromSatoshi(mTransaction.getValue(kit.wallet()).value));
        try {
            mTvBtcAmount.setText(sendAmount);
            mTvTxHash.setText(mTransaction.getHashAsString());
            mTvTxFirstConfirmedBlock.setText(String.valueOf(mTransaction.getConfidence().getAppearedAtChainHeight()));
            mTvConfirmations.setText(String.valueOf(mTransaction.getConfidence().getDepthInBlocks()));
            mTvTxDatetime.setText(mTransaction.getUpdateTime().toString());

            if (mTransaction.getInputs() != null && mTransaction.getInputs().size() > 0) {
                for (TransactionInput input : mTransaction.getInputs()) {
                    System.out.println(input.toString());
                    final ItemView itemView = new ItemView();
                    itemView.layoutItem = LayoutInflater.from(this).inflate(R.layout.item_transaction, null);
                    itemView.tvAddress = itemView.layoutItem.findViewById(R.id.tv_address);
                    itemView.tvAmount = itemView.layoutItem.findViewById(R.id.tv_amount);

                    if (input.getValue() != null) {
                        itemView.tvAmount.setText(input.getValue().toFriendlyString());
                    }
                    itemView.tvAddress.setText(Long.toHexString(input.getSequenceNumber()));

                    mLayoutTransactionInput.addView(itemView.layoutItem);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ItemView {
        View layoutItem;
        TextView tvAmount;
        TextView tvAddress;
    }
}
