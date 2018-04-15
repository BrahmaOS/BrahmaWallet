package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.CommonUtil;

public class AccountsActivity extends BaseActivity {
    @Override
    protected String tag() {
        return AccountsActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.accounts_recycler)
    RecyclerView recyclerViewAccounts;

    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accounts);
        ButterKnife.bind(this);
        showNavBackBtn();
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerViewAccounts.setLayoutManager(layoutManager);
        recyclerViewAccounts.setAdapter(new AccountRecyclerAdapter());

        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities == null) {
                accounts = new ArrayList<>();
            } else {
                accounts = accountEntities;
            }
            recyclerViewAccounts.getAdapter().notifyDataSetChanged();
        });
    }

    /**
     * list item account
     */
    private class AccountRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_account, parent, false);
            rootView.setOnClickListener(v -> {
                int position = recyclerViewAccounts.getChildAdapterPosition(v);
                AccountEntity account = accounts.get(position);
                if (account.getAddress() != null && account.getAddress().length() > 0) {

                }
            });
            return new ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                AccountEntity accountEntity = accounts.get(position);
                setData(itemViewHolder, accountEntity);
            }
        }

        /**
         * set account view
         */
        private void setData(ItemViewHolder holder, final AccountEntity account) {
            if (account == null) {
                return ;
            }
            holder.tvAccountName.setText(account.getName());
            holder.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
            holder.tvTotalAssets.setText("0");
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivAccountAvatar;
            TextView tvAccountName;
            TextView tvAccountAddress;
            TextView tvTotalAssetsDesc;
            TextView tvTotalAssets;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivAccountAvatar = itemView.findViewById(R.id.iv_account_avatar);
                tvAccountName = itemView.findViewById(R.id.tv_account_name);
                tvAccountAddress = itemView.findViewById(R.id.tv_account_address);
                tvTotalAssetsDesc = itemView.findViewById(R.id.tv_total_assets_desc);
                tvTotalAssets = itemView.findViewById(R.id.tv_total_assets);
            }
        }
    }

}
