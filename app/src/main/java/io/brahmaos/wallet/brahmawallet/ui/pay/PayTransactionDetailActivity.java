package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.model.pay.PayTransaction;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.transaction.BlockchainTxDetailActivity;
import io.brahmaos.wallet.brahmawallet.ui.transaction.EtherscanTxDetailActivity;
import io.brahmaos.wallet.util.PayUtil;

import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.COIN_SYMBOL_BRM;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.COIN_SYMBOL_BTC;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.COIN_SYMBOL_ETH;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.PAY_COIN_CODE_BRM;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.PAY_COIN_CODE_BTC;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.PAY_COIN_CODE_ETH;

public class PayTransactionDetailActivity extends BaseActivity {
    private PayTransaction mTransDetail;

    private ImageView ivPayTransIcon;
    private TextView tvMerchantName;
    private TextView tvOrderAmount;
    private TextView tvCoinName;
    private TextView tvPaymentStatus;
    private TextView tvPaymentMethod;
    private TextView tvOrderAmountDetail;
    private TextView tvCoinNameDetail;
    private RelativeLayout layoutTxHash;
    private TextView tvTxHash;
    private TextView tvMerchantDesc;
    private TextView tvOrderCreateTime;
    private TextView tvOrderId;
    private RelativeLayout layoutMerchantOrderId;
    private TextView tvMerchantOrderId;
    @Override
    protected String tag() {
        return PayTransactionDetailActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_trans_record_detail);
        showNavBackBtn();
        if (getIntent() != null) {
            mTransDetail = (PayTransaction) getIntent().getSerializableExtra(IntentParam.PARAM_PAY_TRANS_DETAIL);
        }
        if (null == mTransDetail) {
            finish();
        }
        initData();
    }
    private void initData() {
        ivPayTransIcon = findViewById(R.id.iv_pay_detail_icon);
        tvMerchantName = findViewById(R.id.tv_pay_merchant_name);
        tvOrderAmount = findViewById(R.id.tv_pay_order_amount);
        tvCoinName = findViewById(R.id.tv_pay_coin_name);
        tvPaymentStatus = findViewById(R.id.tv_pay_detail_status);
        tvPaymentMethod = findViewById(R.id.tv_pay_detail_pay_method);
        tvOrderAmountDetail = findViewById(R.id.tv_pay_detail_order_amount);
        tvCoinNameDetail = findViewById(R.id.tv_pay_detail_coin_name);
        layoutTxHash = findViewById(R.id.layout_pay_trans_hash);
        tvTxHash = findViewById(R.id.tv_pay_detail_hash);
        tvTxHash.getPaint().setUnderlineText(true);
        tvTxHash.setOnClickListener(v -> {
            if (mTransDetail != null && tvTxHash.getText() != null && !(tvTxHash.getText().toString().isEmpty())) {
                Intent txIntent = new Intent();

                if (PAY_COIN_CODE_BTC == mTransDetail.getTxCoinCode()) {
                    txIntent.setClass(PayTransactionDetailActivity.this, BlockchainTxDetailActivity.class);
                    txIntent.putExtra(IntentParam.PARAM_TX_HASH, tvTxHash.getText().toString());
                    startActivity(txIntent);
                } else if (PAY_COIN_CODE_BRM == mTransDetail.getTxCoinCode()
                        || PAY_COIN_CODE_ETH == mTransDetail.getTxCoinCode()) {
                    txIntent.setClass(PayTransactionDetailActivity.this, EtherscanTxDetailActivity.class);
                    txIntent.putExtra(IntentParam.PARAM_TX_HASH, tvTxHash.getText().toString());
                    startActivity(txIntent);
                }
            }
        });
        tvMerchantDesc = findViewById(R.id.tv_pay_detail_merchant_desc);
        tvOrderCreateTime = findViewById(R.id.tv_pay_detail_create_time);
        tvOrderId = findViewById(R.id.tv_pay_detail_order_id);
        layoutMerchantOrderId = findViewById(R.id.layout_pay_trans_merchant_order_id);
        tvMerchantOrderId = findViewById(R.id.tv_pay_detail_merchant_order_id);

        if (mTransDetail != null) {
            if (null != mTransDetail.getMerchantIcon() && !mTransDetail.getMerchantIcon().isEmpty()) {
                try {
                    Glide.with(PayTransactionDetailActivity.this)
                            .load(mTransDetail.getMerchantIcon())
                            .into(ivPayTransIcon);
                } catch (Exception e) {
                    Log.d(tag(), "load icon fail: " + e.toString());
                    ivPayTransIcon.setImageResource(R.drawable.ic_store_black_24dp);
                }
            } else {
                ivPayTransIcon.setImageResource(R.drawable.ic_store_black_24dp);
            }
            tvMerchantName.setText("" + mTransDetail.getMerchantName());
            String amountStr = mTransDetail.getAmount();
            try {
                amountStr = "" + Double.parseDouble(mTransDetail.getAmount());
            } catch (Exception e) {
                amountStr = mTransDetail.getAmount();
            }
            if (amountStr != null) {
                tvOrderAmount.setText(amountStr);
                tvOrderAmountDetail.setText(amountStr);
            } else {
                tvOrderAmount.setText("");
                tvOrderAmountDetail.setText("");
            }
            int txCoinCode = mTransDetail.getTxCoinCode();
            if (PAY_COIN_CODE_BTC == txCoinCode) {
                tvCoinName.setText(COIN_SYMBOL_BTC);
                tvCoinNameDetail.setText(COIN_SYMBOL_BTC);
            } else if (PAY_COIN_CODE_ETH == txCoinCode) {
                tvCoinName.setText(COIN_SYMBOL_ETH);
                tvCoinNameDetail.setText(COIN_SYMBOL_ETH);
            } else if (PAY_COIN_CODE_BRM == txCoinCode) {
                tvCoinName.setText(COIN_SYMBOL_BRM);
                tvCoinNameDetail.setText(COIN_SYMBOL_BRM);
            }
            int paymentStatus = mTransDetail.getOrderStatus();
            PayUtil.setTextByStatus(this,
                    tvPaymentStatus, paymentStatus);
            if (2 == paymentStatus) { //General pay waiting for block confirm, txHash may be empty.
                tvPaymentMethod.setText(getString(R.string.pay_trans_method_normal));
                tvTxHash.setText("" + mTransDetail.getTxHash());
                layoutTxHash.setVisibility(View.VISIBLE);
            } else if (3 == paymentStatus) {// General or quick pay success.
                if (null == mTransDetail.getTxHash() || mTransDetail.getTxHash().isEmpty()) {
                    tvPaymentMethod.setText(getString(R.string.pay_trans_method_quick));
                    layoutTxHash.setVisibility(View.GONE);
                } else {
                    tvPaymentMethod.setText(getString(R.string.pay_trans_method_normal));
                    tvTxHash.setText(mTransDetail.getTxHash());
                    layoutTxHash.setVisibility(View.VISIBLE);
                }
            } else {// Pay fail, or to be paid.
                tvPaymentMethod.setText("--");
                tvTxHash.setText("");
                layoutTxHash.setVisibility(View.VISIBLE);
            }
            if (null != mTransDetail.getDesc()) {
                tvMerchantDesc.setText(mTransDetail.getDesc());
            } else {
                tvMerchantDesc.setText("");
            }
            if (null != mTransDetail.getCreateTime()) {
                tvOrderCreateTime.setText(mTransDetail.getCreateTime());
            } else {
                tvOrderCreateTime.setText("");
            }
            if (null != mTransDetail.getOrderId()) {
                tvOrderId.setText(mTransDetail.getOrderId());
            } else {
                tvOrderId.setText("");
            }
            String merchantOrderId = mTransDetail.getMerchantOrderId();
            if (null == merchantOrderId || merchantOrderId.isEmpty()) {
                layoutMerchantOrderId.setVisibility(View.GONE);
            } else {
                layoutMerchantOrderId.setVisibility(View.VISIBLE);
                tvMerchantOrderId.setText(merchantOrderId);
            }
        }
    }
}
