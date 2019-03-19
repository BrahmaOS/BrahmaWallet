package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.model.pay.PayTransaction;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class PayTransactionsListActivity extends BaseActivity {
    private RecyclerView mPayTransRecyclerView;

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
        LinearLayoutManager transLayoutManager = new LinearLayoutManager(this);
        mPayTransRecyclerView.setLayoutManager(transLayoutManager);
        mPayTransRecyclerView.setAdapter(new PayTransRecyclerAdapter());
        // Solve the sliding lag problem
        mPayTransRecyclerView.setHasFixedSize(true);
        mPayTransRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        PayService.getInstance().getPayTransactions(0, 0, null, null, 0, 10)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PayTransaction>>() {
                    @Override
                    public void onNext(List<PayTransaction> apr) {
                        if (null != apr && apr.size() > 0) {
                            for (PayTransaction transaction : apr) {
                                Log.d(tag(), "" + transaction.toString());
                            }
                        } else {
                            Log.d(tag(), "no PayTransaction record.");
                        }
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

    private class PayTransRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_pay_transaction, parent, false);
            return new PayTransRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return (null == mPayTransList ? 0 : mPayTransList.size());
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {
            ImageView ivTransIcon;
            TextView tvMerchantName;
            TextView tvTransTime;
            TextView tvAmount;
            ImageView ivCoinIcon;
            TextView tvCoinName;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivTransIcon = itemView.findViewById(R.id.iv_trans_icon);
                tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
                tvTransTime = itemView.findViewById(R.id.tv_trans_time);
                tvAmount = itemView.findViewById(R.id.tv_amount);
                ivCoinIcon = itemView.findViewById(R.id.iv_coin_icon);
                tvCoinName = itemView.findViewById(R.id.tv_coin_name);

            }
        }
    }
}
