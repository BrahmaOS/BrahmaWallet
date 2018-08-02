package io.brahmaos.wallet.brahmawallet.ui.transaction;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.EthTransaction;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.TransactionService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class EthTransactionsActivity extends BaseActivity {
    @Override
    protected String tag() {
        return EthTransactionsActivity.class.getName();
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
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;

    @BindView(R.id.loading_pbar)
    ProgressBar loadingProgressBar;
    @BindView(R.id.transactions_recycler)
    RecyclerView recyclerViewTransactions;
    @BindView(R.id.layout_no_transactions)
    LinearLayout layoutNoTransactions;

    private AccountEntity mAccount;
    private TokenEntity mToken;
    private List<EthTransaction> mEthTransactions = new ArrayList<>();
    private boolean loadMoreFinished = false;
    private int page = 0;
    private int count = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eth_transactions);
        ButterKnife.bind(this);
        showNavBackBtn();
        mAccount = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);
        mToken = (TokenEntity) getIntent().getSerializableExtra(IntentParam.PARAM_TOKEN_INFO);

        if (mAccount == null || mToken == null) {
            finish();
        }
        initView();
        getTxList();
    }

    private void initView() {
        swipeRefreshLayout.setColorSchemeResources(R.color.master);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
        });

        String tokenShortName = mToken.getShortName();
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setTitle(tokenShortName + getString(R.string.blank_space) +
                        getString(R.string.title_transactions));
            }
        }
        showAccountInfo(mAccount);

        loadingProgressBar.setVisibility(View.VISIBLE);
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

        nestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY > oldScrollY) {
                    // Slide down
                }

                if (scrollY < oldScrollY) {
                    // Slide up
                }

                if (scrollY == 0) {
                    // top of list
                }

                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    getTxList();
                }
            }
        });
    }

    private void showAccountInfo(AccountEntity account) {
        if (account != null) {
            ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
            tvAccountName.setText(account.getName());
            tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
        }
    }

    private void getTxList() {
        TransactionService.getInstance().getEthTransactions(mAccount.getAddress().toLowerCase(), page, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<EthTransaction>>() {

                    @Override
                    public void onCompleted() {
                        swipeRefreshLayout.setRefreshing(false);
                        loadingProgressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        throwable.printStackTrace();
                    }

                    @Override
                    public void onNext(List<EthTransaction> apr) {
                        handleTxList(apr);
                    }
                });
    }

    private void handleTxList(List<EthTransaction> ethTxList) {
        if (page == 0) {
            if (ethTxList != null && ethTxList.size() > 0) {
                if (ethTxList.size() < count) {
                    loadMoreFinished = true;
                }
                mEthTransactions = ethTxList;
                recyclerViewTransactions.setVisibility(View.VISIBLE);
                recyclerViewTransactions.getAdapter().notifyDataSetChanged();
            } else {
                layoutNoTransactions.setVisibility(View.VISIBLE);
            }
        } else {
            if (ethTxList == null) {
                loadMoreFinished = true;
                return;
            } else if (ethTxList.size() < count) {
                loadMoreFinished = true;
            }
            for (EthTransaction txNew : ethTxList) {
                for (EthTransaction txLocal : mEthTransactions) {
                    if (txLocal.getHash().equals(txNew.getHash())) {
                        mEthTransactions.remove(txLocal);
                        break;
                    }
                }
                mEthTransactions.add(txNew);
            }
            Collections.sort(mEthTransactions);
            recyclerViewTransactions.getAdapter().notifyDataSetChanged();
        }
        page += 1;
    }

    /**
     * list item account
     */
    private class TransactionRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int TYPE_FOOTER = 1;
        private static final int TYPE_CONTENT = 2;

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);
                return new FootViewHolder(rootView);
            } else {
                View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_transaction, parent, false);
                return new TransactionRecyclerAdapter.ItemViewHolder(rootView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TransactionRecyclerAdapter.ItemViewHolder) {
                TransactionRecyclerAdapter.ItemViewHolder itemViewHolder = (TransactionRecyclerAdapter.ItemViewHolder) holder;
                EthTransaction ethTransaction = mEthTransactions.get(position);
                setAssetsData(itemViewHolder, ethTransaction);
            }
        }

        /*
         * set assets view
         */
        private void setAssetsData(TransactionRecyclerAdapter.ItemViewHolder holder, final EthTransaction ethTransaction) {
            if (ethTransaction == null) {
                return;
            }
            holder.layoutTransaction.setOnClickListener(v -> {
                /*Intent intent = new Intent(MainActivity.this, TransferActivity.class);
                intent.putExtra(IntentParam.PARAM_TOKEN_INFO, tokenEntity);
                startActivityForResult(intent, REQ_CODE_TRANSFER);*/
            });
            holder.tvTxTime.setText(CommonUtil.timestampToDate(ethTransaction.getTxTime(), null));
            holder.tvTxSenderAddress.setText(CommonUtil.generateSimpleAddress(ethTransaction.getFromAddress()));
            holder.tvTxReceiverAddress.setText(CommonUtil.generateSimpleAddress(ethTransaction.getToAddress()));
            if (ethTransaction.getFromAddress().toLowerCase().equals(mAccount.getAddress().toLowerCase())) {
                Glide.with(EthTransactionsActivity.this)
                        .load(R.drawable.icon_send)
                        .into(holder.ivTxStatusIcon);
                Glide.with(EthTransactionsActivity.this)
                        .load(R.drawable.icon_send_arrow)
                        .into(holder.ivTxArrow);
                String sendAmount = "- " + String.valueOf(CommonUtil.getAccountFromWei(ethTransaction.getValue()));
                holder.tvTxAmount.setText(sendAmount);
                holder.tvTxAmount.setTextColor(getResources().getColor(R.color.tx_send));
            } else {
                Glide.with(EthTransactionsActivity.this)
                        .load(R.drawable.icon_receive)
                        .into(holder.ivTxStatusIcon);
                Glide.with(EthTransactionsActivity.this)
                        .load(R.drawable.icon_receive_arrow)
                        .into(holder.ivTxArrow);
                String sendAmount = "+ " + String.valueOf(CommonUtil.getAccountFromWei(ethTransaction.getValue()));
                holder.tvTxAmount.setText(sendAmount);
                holder.tvTxAmount.setTextColor(getResources().getColor(R.color.tx_receive));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (!loadMoreFinished && position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return TYPE_CONTENT;
            }
        }

        @Override
        public int getItemCount() {
            if (loadMoreFinished) {
                return mEthTransactions.size();
            }
            return mEthTransactions.size() + 1;
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            LinearLayout layoutTransaction;
            ImageView ivTxStatusIcon;
            TextView tvTxTime;
            TextView tvTxSenderAddress;
            ImageView ivTxArrow;
            TextView tvTxReceiverAddress;
            TextView tvTxAmount;

            ItemViewHolder(View itemView) {
                super(itemView);
                layoutTransaction = itemView.findViewById(R.id.layout_transaction_item);
                ivTxStatusIcon = itemView.findViewById(R.id.iv_transaction_status_icon);
                tvTxTime = itemView.findViewById(R.id.tv_transaction_time);
                tvTxSenderAddress = itemView.findViewById(R.id.tv_sender_address);
                ivTxArrow = itemView.findViewById(R.id.iv_transaction_arrow);
                tvTxReceiverAddress = itemView.findViewById(R.id.tv_receiver_address);
                tvTxAmount = itemView.findViewById(R.id.tv_transactions_amount);
            }
        }

        class FootViewHolder extends RecyclerView.ViewHolder {

            FootViewHolder(View itemView) {
                super(itemView);
            }
        }
    }
}
