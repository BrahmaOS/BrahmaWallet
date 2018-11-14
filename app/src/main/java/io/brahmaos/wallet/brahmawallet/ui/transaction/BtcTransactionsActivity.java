package io.brahmaos.wallet.brahmawallet.ui.transaction;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.bitcoinj.core.Transaction;
import org.bitcoinj.kits.WalletAppKit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BtcAccountManager;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.CommonUtil;

public class BtcTransactionsActivity extends BaseActivity {
    @Override
    protected String tag() {
        return BtcTransactionsActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.sv_content)
    NestedScrollView nestedScrollView;
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;

    @BindView(R.id.transactions_recycler)
    RecyclerView recyclerViewTransactions;
    @BindView(R.id.layout_no_transactions)
    LinearLayout layoutNoTransactions;

    private AccountEntity mAccount;
    private List<Transaction> mTransactions = new ArrayList<>();
    private WalletAppKit kit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_btc_transactions);
        ButterKnife.bind(this);
        showNavBackBtn();
        mAccount = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);

        if (mAccount == null) {
            finish();
        }
        kit = BtcAccountManager.getInstance().getBtcWalletAppKit(mAccount.getFilename());
        initView();
        getBtcTransactions();
    }

    private void initView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.master);
        swipeRefreshLayout.setOnRefreshListener(this::getBtcTransactions);

        showAccountInfo(mAccount);
        swipeRefreshLayout.setRefreshing(true);
        recyclerViewTransactions.setVisibility(View.GONE);
        layoutNoTransactions.setVisibility(View.GONE);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setSmoothScrollbarEnabled(true);
        layoutManager.setAutoMeasureEnabled(true);
        recyclerViewTransactions.setLayoutManager(layoutManager);
        recyclerViewTransactions.setAdapter(new TransactionRecyclerAdapter());
        recyclerViewTransactions.setVisibility(View.GONE);
        // Solve the sliding lag problem
        recyclerViewTransactions.setHasFixedSize(true);
        recyclerViewTransactions.setNestedScrollingEnabled(false);

        // Solve the sliding lag problem
        recyclerViewTransactions.setHasFixedSize(true);
        recyclerViewTransactions.setNestedScrollingEnabled(false);
    }

    private void showAccountInfo(AccountEntity account) {
        if (account != null) {
            ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
            tvAccountName.setText(account.getName());
        }
    }

    private void getBtcTransactions() {
        if (kit != null && kit.wallet() != null) {
            List<Transaction> transactions = kit.wallet().getTransactionsByTime();
            swipeRefreshLayout.setRefreshing(false);
            if (transactions != null && transactions.size() > 0) {
                mTransactions = transactions;
                recyclerViewTransactions.setVisibility(View.VISIBLE);
                recyclerViewTransactions.getAdapter().notifyDataSetChanged();
            } else {
                layoutNoTransactions.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * list item account
     */
    private class TransactionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_btc_transaction, parent, false);
            return new TransactionRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TransactionRecyclerAdapter.ItemViewHolder) {
                TransactionRecyclerAdapter.ItemViewHolder itemViewHolder = (TransactionRecyclerAdapter.ItemViewHolder) holder;
                Transaction transaction = mTransactions.get(position);
                setAssetsData(itemViewHolder, transaction);
            }
        }

        /*
         * set assets view
         */
        private void setAssetsData(TransactionRecyclerAdapter.ItemViewHolder holder, final Transaction transaction) {
            if (transaction == null) {
                return;
            }
            holder.layoutTransaction.setOnClickListener(v -> {
                /*Intent intent = new Intent(BtcTransactionsActivity.this, TransactionDetailActivity.class);
                intent.putExtra(IntentParam.PARAM_TOKEN_TX, tokenTransaction);
                startActivity(intent);*/
            });
            holder.tvTxTime.setText(CommonUtil.timestampToDate(transaction.getUpdateTime().getTime() / 1000, null));
            holder.tvTxSendStatus.setText(String.valueOf(transaction.getConfidence().getDepthInBlocks()));
            String sendAmount = String.valueOf(CommonUtil.convertBTCFromSatoshi(transaction.getValue(kit.wallet()).value));
            holder.tvTxAmount.setText(sendAmount);
            if (transaction.getValue(kit.wallet()).value < 0) {
                Glide.with(BtcTransactionsActivity.this)
                        .load(R.drawable.icon_send)
                        .into(holder.ivTxStatusIcon);
                holder.tvTxAmount.setTextColor(getResources().getColor(R.color.tx_send));
            } else {
                Glide.with(BtcTransactionsActivity.this)
                        .load(R.drawable.icon_receive)
                        .into(holder.ivTxStatusIcon);
                holder.tvTxAmount.setTextColor(getResources().getColor(R.color.tx_receive));
            }
        }

        @Override
        public int getItemCount() {
            return mTransactions.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            LinearLayout layoutTransaction;
            ImageView ivTxStatusIcon;
            TextView tvTxTime;
            TextView tvTxSendStatus;
            TextView tvTxAmount;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutTransaction = itemView.findViewById(R.id.layout_transaction_item);
                ivTxStatusIcon = itemView.findViewById(R.id.iv_transaction_status_icon);
                tvTxTime = itemView.findViewById(R.id.tv_transaction_time);
                tvTxSendStatus = itemView.findViewById(R.id.tv_confirm_status);
                tvTxAmount = itemView.findViewById(R.id.tv_transactions_amount);
            }
        }
    }
}
