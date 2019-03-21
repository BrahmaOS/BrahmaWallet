package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.model.pay.PayTransaction;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.PayUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.COIN_SYMBOL_BRM;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.COIN_SYMBOL_BTC;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.COIN_SYMBOL_ETH;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.PAY_COIN_CODE_BRM;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.PAY_COIN_CODE_BTC;
import static io.brahmaos.wallet.brahmawallet.common.BrahmaConst.PAY_COIN_CODE_ETH;

public class PayTransactionsListActivity extends BaseActivity {
    private RecyclerView mPayTransRecyclerView;
    private LinearLayout mPayTransLayout, mNoPayTransLayout;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private NestedScrollView mNestedScrollView;
    private boolean loadMoreFinished = false;
    private int page = 0;
    private int count = 10;
    private long lastTimeGroup = 0;

    @Override
    protected String tag() {
        return PayTransactionsListActivity.class.getName();
    }

    private List<PayTransaction> mPayTransList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_transactions);
        mPayTransRecyclerView = findViewById(R.id.pay_transaction);
        mPayTransLayout = findViewById(R.id.layout_pay_trans);
        mNoPayTransLayout = findViewById(R.id.layout_no_pay_trans);
        mSwipeRefreshLayout = findViewById(R.id.swipe_refresh_pay_trans_layout);
        mNestedScrollView = findViewById(R.id.scroll_pay_trans);

        LinearLayoutManager transLayoutManager = new LinearLayoutManager(this);
        transLayoutManager.setSmoothScrollbarEnabled(true);
        mPayTransRecyclerView.setLayoutManager(transLayoutManager);
        mPayTransRecyclerView.setAdapter(new PayTransRecyclerAdapter());
        mPayTransRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        // Solve the sliding lag problem
        mPayTransRecyclerView.setHasFixedSize(true);
        mPayTransRecyclerView.setNestedScrollingEnabled(false);

        mSwipeRefreshLayout.setColorSchemeResources(R.color.master);
        mSwipeRefreshLayout.setOnRefreshListener(this::getLatestPayTransList);
        mSwipeRefreshLayout.setRefreshing(true);
        mPayTransLayout.setVisibility(View.GONE);
        mNoPayTransLayout.setVisibility(View.GONE);
        mNestedScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY == (v.getChildAt(0).getMeasuredHeight() - v.getMeasuredHeight())) {
                    getPayTransList();
                }
            }
        });
        getPayTransList();
    }

    // Called when scroll down.
    private void getLatestPayTransList() {
        page = 0;
        PayService.getInstance().getPayTransactions(0, 0, null, null, 0, count)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<List<PayTransaction>>() {
                @Override
                public void onNext(List<PayTransaction> apr) {
                    if (apr != null && apr.size() > 0) {
                        if (apr.size() < count) {
                            loadMoreFinished = true;
                        } else {
                            loadMoreFinished = false;
                        }
                        for (PayTransaction transNew : apr) {
                            Log.d(tag(), "getLatestPayTransList--" + transNew);
                            mPayTransList = apr;
                        }
                        lastTimeGroup = 0;
                        mPayTransRecyclerView.getAdapter().notifyDataSetChanged();
                    } else {
                        Log.d(tag(), "getLatestPayTransList---no pay transaction");
                    }
                    page++;
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                    showShortToast(e == null ? "failed to get pay transactions." : e.toString());
                    mSwipeRefreshLayout.setRefreshing(false);
                }

                @Override
                public void onCompleted() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });
    }

    // Called when scroll up to next page
    private void getPayTransList() {
        PayService.getInstance().getPayTransactions(0, 0, null, null, page, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PayTransaction>>() {
                    @Override
                    public void onNext(List<PayTransaction> apr) {
                        handlePayTransList(apr);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showShortToast(e == null ? "failed to get pay transactions." : e.toString());
                        mSwipeRefreshLayout.setRefreshing(false);
                    }

                    @Override
                    public void onCompleted() {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void handlePayTransList(List<PayTransaction> apr) {
        if (0 == page) {
            if (apr != null && apr.size() > 0) {
                if (apr.size() < count) {
                    loadMoreFinished = true;
                }
                mPayTransList = apr;
                mPayTransLayout.setVisibility(View.VISIBLE);
                lastTimeGroup = 0;
                mPayTransRecyclerView.getAdapter().notifyDataSetChanged();
            } else {
                mNoPayTransLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (null == apr) {
                loadMoreFinished = true;
                return;
            } else if (apr.size() < count) {
                loadMoreFinished = true;
            }
            for (PayTransaction transNew : apr) {
                Log.d(tag(), "handlePayTransList--" + transNew);
                mPayTransList.add(transNew);
            }
            lastTimeGroup = 0;
            mPayTransRecyclerView.getAdapter().notifyDataSetChanged();
        }
        page++;
    }

    private class PayTransRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_FOOTER = 1;
        private static final int TYPE_CONTENT = 2;
        private static final int TYPE_DATE = 3;
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_FOOTER) {
                View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.load_more, parent, false);
                return new FootViewHolder(rootView);
            } else if (TYPE_DATE == viewType) {
                View DateView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycle_date_group, parent, false);
                return new DateViewHolder(DateView);
            } else {
                View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_pay_transaction, parent, false);
                return new ItemViewHolder(rootView);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder && mPayTransList != null && mPayTransList.size() > position) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                setData(itemViewHolder, mPayTransList.get(position));
            } else if (holder instanceof DateViewHolder && mPayTransList != null && mPayTransList.size() > position) {
                setDateAndData((DateViewHolder)holder, mPayTransList.get(position));
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (mPayTransList != null && position < mPayTransList.size()) {
                if (0 == position) {
                    lastTimeGroup = 0;
                }
                PayTransaction trans = mPayTransList.get(position);
                if (trans != null) {
                    long currTransDate = CommonUtil.convertDateTimeStringToLong(trans.getCreateTime(), "yyyy-MM");
                    if (0 == lastTimeGroup || (currTransDate > 0 && currTransDate < lastTimeGroup)) {
                        lastTimeGroup = currTransDate;
                        return TYPE_DATE;
                    }
                }
            }
            if (!loadMoreFinished && position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return TYPE_CONTENT;
            }
        }

        @Override
        public int getItemCount() {
            if (loadMoreFinished) {
                return (null == mPayTransList ? 0 : mPayTransList.size());
            } else {
                return (null == mPayTransList ? 0 : mPayTransList.size() + 1);
            }
        }

        private void setData(ItemViewHolder holder, PayTransaction trans) {
            if (null == trans) {
                return;
            }
            holder.ivTransIcon.setImageResource(R.drawable.icon_brm);
            holder.tvMerchantName.setText(trans.getMerchantName());
            long time = CommonUtil.convertDateTimeStringToLong(trans.getCreateTime(), "yyyy-MM-dd hh:mm:ss");
            String timeStr = CommonUtil.convertDateTimeLongToString(time, "MM-dd hh:mm");
            holder.tvTransTime.setText(timeStr != null ? timeStr : trans.getCreateTime());
            String amountStr = trans.getAmount();
            try {
                amountStr = "" + Double.parseDouble(trans.getAmount());
            } catch (Exception e) {
                amountStr = trans.getAmount();
            }
            if (2 == trans.getOrderType()) {
                holder.tvAmount.setText("+" + amountStr);
            } else {
                holder.tvAmount.setText("-" + amountStr);
            }
            int txCoinCode = trans.getTxCoinCode();
            if (PAY_COIN_CODE_BTC == txCoinCode) {
                holder.tvCoinName.setText(COIN_SYMBOL_BTC);
            } else if (PAY_COIN_CODE_ETH == txCoinCode) {
                holder.tvCoinName.setText(COIN_SYMBOL_ETH);
            } else if (PAY_COIN_CODE_BRM == txCoinCode) {
                holder.tvCoinName.setText(COIN_SYMBOL_BRM);
            }
            PayUtil.setTextByStatus(PayTransactionsListActivity.this,
                    holder.tvPayStatus, trans.getOrderStatus());
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView ivTransIcon;
            TextView tvMerchantName;
            TextView tvTransTime;
            TextView tvAmount;
            TextView tvPayStatus;
            TextView tvCoinName;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivTransIcon = itemView.findViewById(R.id.iv_trans_icon);
                tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
                tvTransTime = itemView.findViewById(R.id.tv_trans_time);
                tvAmount = itemView.findViewById(R.id.tv_trans_amount);
                tvPayStatus = itemView.findViewById(R.id.tv_pay_status);
                tvCoinName = itemView.findViewById(R.id.tv_coin_name);

            }
        }

        class FootViewHolder extends RecyclerView.ViewHolder {

            FootViewHolder(View itemView) {
                super(itemView);
            }
        }

        private void setDateAndData(DateViewHolder viewHolder, PayTransaction payTrans) {
            if (null == payTrans) {
                return;
            }
            long month = CommonUtil.convertDateTimeStringToLong(payTrans.getCreateTime(), "yyyy-MM");
            String monthStr = CommonUtil.convertDateTimeLongToString(month, "yyyy-MM");

            viewHolder.tvDate.setText(monthStr != null ? monthStr : payTrans.getCreateTime());

            setData(new ItemViewHolder(viewHolder.layoutItemView), payTrans);
        }

        class DateViewHolder extends RecyclerView.ViewHolder {
            TextView tvDate;
            LinearLayout layoutItemView;
            DateViewHolder(View itemView) {
                super(itemView);
                tvDate = itemView.findViewById(R.id.tv_date_group);
                layoutItemView = itemView.findViewById(R.id.item_view);
            }
        }
    }
}
