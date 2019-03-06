package io.brahmaos.wallet.brahmawallet.ui.home;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.BitcoinDownloadProgress;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.RestoreAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.pay.SetPayAccountPasswordActivity;
import io.brahmaos.wallet.brahmawallet.ui.token.TokensActivity;
import io.brahmaos.wallet.brahmawallet.ui.transfer.InstantExchangeActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observable;

public class QuickPayFragment extends BaseFragment {
    @Override
    protected String tag() {
        return QuickPayFragment.class.getName();
    }

    private LinearLayout layoutAddQuickPayAccount;
    private LinearLayout layoutHeader;
    private RecyclerView recyclerViewAccounts;

    private AccountViewModel mViewModel;
    private List<AccountEntity> cacheAccounts = new ArrayList<>();

    /**
     * instance
     *
     * @param layoutResId  layout resource，e.g. R.layout.fragment_home
     * @return  return fragment
     */
    public static QuickPayFragment newInstance(int layoutResId, int titleResId) {
        QuickPayFragment fragment = new QuickPayFragment();
        fragment.setArguments(newArguments(layoutResId, titleResId));
        return fragment;
    }

    @Override
    protected boolean initView() {
        layoutAddQuickPayAccount = parentView.findViewById(R.id.layout_add_quick_pay_account);
        layoutHeader = parentView.findViewById(R.id.layout_header);
        recyclerViewAccounts = parentView.findViewById(R.id.accounts_recycler);

        DisplayMetrics display = this.getResources().getDisplayMetrics();

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.height_toolbar);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutHeader.getLayoutParams();
        params.width = display.widthPixels;
        params.height = ((int) (display.heightPixels * BrahmaConst.MAIN_PAGE_HEADER_RATIO) - statusBarHeight - toolbarHeight);
        layoutHeader.setLayoutParams(params);

        LinearLayoutManager btcLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAccounts.setLayoutManager(btcLayoutManager);
        recyclerViewAccounts.setAdapter(new AccountRecyclerAdapter());
        // Solve the sliding lag problem
        recyclerViewAccounts.setHasFixedSize(true);
        recyclerViewAccounts.setNestedScrollingEnabled(false);

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initData();
        return true;
    }

    private void initData() {
        mViewModel.getAccounts().observe(this, accountEntities -> {
            cacheAccounts = new ArrayList<>();
            if (accountEntities != null && accountEntities.size() > 0) {
                for (AccountEntity account : accountEntities) {
                    if (account.getType() == BrahmaConst.ETH_ACCOUNT_TYPE) {
                        cacheAccounts.add(account);
                    }
                }
                recyclerViewAccounts.getAdapter().notifyDataSetChanged();
            }
        });
    }

    /**
     * list item eth account
     */
    private class AccountRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_account_eth_for_quick_pay, parent, false);
            rootView.setOnClickListener(v -> {
                int position = recyclerViewAccounts.getChildAdapterPosition(v);
                AccountEntity account = cacheAccounts.get(position);
                Intent intent = new Intent(getActivity(), SetPayAccountPasswordActivity.class);
                intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
                startActivity(intent);
            });
            return new AccountRecyclerAdapter.ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof AccountRecyclerAdapter.ItemViewHolder) {
                AccountRecyclerAdapter.ItemViewHolder itemViewHolder = (AccountRecyclerAdapter.ItemViewHolder) holder;
                AccountEntity accountEntity = cacheAccounts.get(position);
                setData(itemViewHolder, accountEntity);
            }
        }

        /**
         * set account view
         */
        private void setData(AccountRecyclerAdapter.ItemViewHolder holder, final AccountEntity account) {
            if (account == null) {
                return ;
            }
            ImageManager.showAccountAvatar(getContext(), holder.ivAccountAvatar, account);

            holder.tvAccountName.setText(account.getName());
            holder.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
        }

        @Override
        public int getItemCount() {
            return cacheAccounts.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivAccountAvatar;
            TextView tvAccountName;
            TextView tvAccountAddress;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivAccountAvatar = itemView.findViewById(R.id.iv_account_avatar);
                tvAccountName = itemView.findViewById(R.id.tv_account_name);
                tvAccountAddress = itemView.findViewById(R.id.tv_account_address);
            }
        }
    }
}

