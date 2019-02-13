package io.brahmaos.wallet.brahmawallet.ui.transaction;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.ScriptException;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.bitcoinj.core.Utils;
import org.bitcoinj.kits.WalletAppKit;
import org.bitcoinj.script.Script;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
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
    @BindView(R.id.layout_output_transaction)
    LinearLayout mLayoutTransactionOutput;
    @BindView(R.id.tv_transaction_fee)
    TextView mTvTransactionFee;
    @BindView(R.id.tv_transaction_fee_unit)
    TextView mTvTransactionFeeUnit;
    @BindView(R.id.layout_copy_blockchain_url)
    LinearLayout mLayoutCopyBlockchainUrl;

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
            String txHash = mTransaction.getHashAsString();
            mTvBtcAmount.setText(String.format("%s %s", sendAmount, getString(R.string.account_btc)));
            mTvTxHash.setText(txHash);
            mTvTxFirstConfirmedBlock.setText(String.valueOf(mTransaction.getConfidence().getAppearedAtChainHeight()));
            mTvConfirmations.setText(String.valueOf(mTransaction.getConfidence().getDepthInBlocks()));
            mTvTxDatetime.setText(mTransaction.getUpdateTime().toString());
            Coin fee = mTransaction.getFee();
            int size = mTransaction.unsafeBitcoinSerialize().length;
            if (fee != null) {
                mTvTransactionFee.setText(new StringBuilder().append(fee.toFriendlyString()).append(" for ").append(size).append(" bytes"));
                mTvTransactionFeeUnit.setText(new StringBuilder().append(fee.divide(size)).append(" sat/byte"));
            }

            if (mTransaction.getInputs() != null && mTransaction.getInputs().size() > 0) {
                for (TransactionInput input : mTransaction.getInputs()) {
                    final ItemView itemView = new ItemView();
                    itemView.layoutItem = LayoutInflater.from(this).inflate(R.layout.item_transaction, null);
                    itemView.tvAddress = itemView.layoutItem.findViewById(R.id.tv_address);
                    itemView.tvAmount = itemView.layoutItem.findViewById(R.id.tv_amount);

                    if (input.getValue() != null) {
                        itemView.tvAmount.setText(input.getValue().toFriendlyString());
                    }
                    try {
                        byte[] bytes = input.getScriptSig().getPubKey();
                        itemView.tvAddress.setText(new Address(BtcAccountManager.getInstance().getNetworkParams(), Utils.sha256hash160(bytes)).toBase58());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    mLayoutTransactionInput.addView(itemView.layoutItem);
                }
            }

            if (mTransaction.getOutputs() != null && mTransaction.getOutputs().size() > 0) {
                for (TransactionOutput output : mTransaction.getOutputs()) {
                    final ItemView itemView = new ItemView();
                    itemView.layoutItem = LayoutInflater.from(this).inflate(R.layout.item_transaction, null);
                    itemView.tvAddress = itemView.layoutItem.findViewById(R.id.tv_address);
                    itemView.tvAmount = itemView.layoutItem.findViewById(R.id.tv_amount);

                    if (output.getValue() != null) {
                        itemView.tvAmount.setText(output.getValue().toFriendlyString());
                    }
                    try {
                        Script script = output.getScriptPubKey();
                        if (script.isSentToAddress() || script.isPayToScriptHash()) {
                            itemView.tvAddress.setText(script.getToAddress(BtcAccountManager.getInstance().getNetworkParams()).toBase58());
                        } else {
                            itemView.tvAddress.setText("");
                        }
                    } catch (ScriptException e) {
                        itemView.tvAddress.setText("");
                    }

                    mLayoutTransactionOutput.addView(itemView.layoutItem);
                }
            }

            mLayoutCopyBlockchainUrl.setOnClickListener(v -> {
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clipData = ClipData.newPlainText("text", BrahmaConfig.getInstance().getBlochchainTxDetailUrl(txHash));
                if (cm != null) {
                    cm.setPrimaryClip(clipData);
                    showLongToast(R.string.tip_success_copy);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class ItemView {
        View layoutItem;
        TextView tvAmount;
        TextView tvAddress;
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
            Intent intent = new Intent(BtcTransactionDetailActivity.this, BlockchainTxDetailActivity.class);
            intent.putExtra(IntentParam.PARAM_TX_HASH, mTransaction.getHashAsString());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
