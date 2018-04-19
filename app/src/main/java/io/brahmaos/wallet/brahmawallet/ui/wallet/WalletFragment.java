package io.brahmaos.wallet.brahmawallet.ui.wallet;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountsActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.account.ImportAccountActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;

/**
 * Use the {@link WalletFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WalletFragment extends BaseFragment {
    @Override
    protected String tag() {
        return WalletFragment.class.getName();
    }

    private ConstraintLayout createAccountLayout;
    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvTokenCategories;
    private RecyclerView recyclerViewAssets;

    private AccountViewModel mViewModel;
    private List<AccountEntity> cacheAccounts = new ArrayList<>();
    private List<String> assetsClass = new ArrayList<>();

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     */
    public static WalletFragment newInstance(int layoutResId, int toolbarResId, int titleResId) {
        WalletFragment fragment = new WalletFragment();
        fragment.setArguments(newArguments(layoutResId, toolbarResId, titleResId));
        return fragment;
    }

    @Override
    protected boolean initView() {
        assetsClass.add("BRM");
        assetsClass.add("ETH");

        swipeRefreshLayout = parentView.findViewById(R.id.swipe_refresh_layout);
        recyclerViewAssets = parentView.findViewById(R.id.assets_recycler);
        tvTokenCategories = parentView.findViewById(R.id.tv_assets_categories_num);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAssets.setLayoutManager(layoutManager);
        recyclerViewAssets.setAdapter(new AssetsRecyclerAdapter());

        // Solve the sliding lag problem
        recyclerViewAssets.setHasFixedSize(true);
        recyclerViewAssets.setNestedScrollingEnabled(false);

        createAccountLayout = parentView.findViewById(R.id.layout_new_account);
        Button createWalletBtn = parentView.findViewById(R.id.btn_create_account);
        createWalletBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), CreateAccountActivity.class);
            startActivity(intent);
        });
        Button importAccountBtn = parentView.findViewById(R.id.btn_import_account);
        importAccountBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ImportAccountActivity.class);
            startActivity(intent);
        });

        return true;
    }

    @Override
    protected void initToolbar() {
        AppCompatActivity mAppCompatActivity = (AppCompatActivity) getActivity();
        if (mAppCompatActivity != null) {
            Toolbar toolbar = mAppCompatActivity.findViewById(toolbarResId);
            toolbar.getMenu().clear();
            toolbar.setTitle(titleResId);
            toolbar.inflateMenu(R.menu.fragment_wallet);

            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.menu_accounts) {
                    if (cacheAccounts != null && cacheAccounts.size() > 0) {
                        Intent intent = new Intent(getActivity(), AccountsActivity.class);
                        startActivity(intent);
                    }
                }
                return true;
            });
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            cacheAccounts = accountEntities;
            WalletFragment.this.checkContentShow();
        });

        mViewModel.getTokens().observe(this, tokenEntities -> {
            if (tokenEntities != null) {
                tvTokenCategories.setText("" + tokenEntities.size());
            }
        });
    }

    @Override
    public void onStart() {
        BLog.d("HomeFragment", "onStart");
        super.onStart();
    }

    /**
     *  if account's length > 0, show the total assets;
     *  else show the create account.
     */
    private void checkContentShow() {
        if (cacheAccounts == null || cacheAccounts.size() == 0) {
            BLog.e(tag(), "the account is null");
            createAccountLayout.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);

        } else {
            BLog.e(tag(), "the account size is: " + cacheAccounts.size());
            createAccountLayout.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * list item account
     */
    private class AssetsRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View rootView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_assets, parent, false);
            rootView.setOnClickListener(v -> {

            });
            return new ItemViewHolder(rootView);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof ItemViewHolder) {
                ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
                String assets = assetsClass.get(position);
                setAssetsData(itemViewHolder, assets, position);
            }
        }

        /**
         * set assets view
         */
        private void setAssetsData(ItemViewHolder holder, final String assets, int position) {
            if (assets == null) {
                return;
            }
            holder.tvTokenName.setText(assets);
            if (position == 1) {
                holder.ivTokenIcon.setImageResource(R.drawable.icon_eos);
            }
        }

        @Override
        public int getItemCount() {
            return assetsClass.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivTokenIcon;
            TextView tvTokenName;
            TextView tvTokenAccount;
            TextView tvTokenAssetsCount;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivTokenIcon = itemView.findViewById(R.id.iv_token_icon);
                tvTokenName = itemView.findViewById(R.id.tv_token_name);
                tvTokenAccount = itemView.findViewById(R.id.tv_token_count);
                tvTokenAssetsCount = itemView.findViewById(R.id.tv_token_assets_count);
            }
        }
    }
}
