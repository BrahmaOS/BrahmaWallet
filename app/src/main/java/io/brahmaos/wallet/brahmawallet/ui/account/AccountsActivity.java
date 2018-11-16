package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;

public class AccountsActivity extends BaseActivity {
    @Override
    protected String tag() {
        return AccountsActivity.class.getName();
    }

    public static final int REQ_IMPORT_ACCOUNT = 20;

    // UI references.
    @BindView(R.id.accounts_recycler)
    RecyclerView recyclerViewAccounts;
    private CustomProgressDialog progressDialog;

    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts = new ArrayList<>();
    private List<AccountAssets> accountAssetsList = new ArrayList<>();
    private List<CryptoCurrency> cryptoCurrencies = new ArrayList<>();

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
            if (accountEntities == null || accountEntities.size() < 1) {
                accounts = new ArrayList<>();
                finish();
            } else {
                accounts = accountEntities;
                recyclerViewAccounts.getAdapter().notifyDataSetChanged();
            }
        });

        accountAssetsList = MainService.getInstance().getAccountAssetsList();
        cryptoCurrencies = MainService.getInstance().getCryptoCurrencies();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account_add, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_create_account) {
            Intent intent = new Intent(this, CreateEthAccountActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.menu_import_account) {
            Intent intent = new Intent(this, ImportEthereumAccountActivity.class);
            startActivityForResult(intent, REQ_IMPORT_ACCOUNT);
            return true;
        } else if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQ_IMPORT_ACCOUNT) {
            if (resultCode == RESULT_OK) {
                progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, getString(R.string.sync));
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                progressDialog.setCancelable(false);
                //progressDialog.show();
                BLog.i(tag(), "import account success");
                mViewModel.getTokens().observe(this, tokenEntities -> {
                    BLog.i(tag(), "get all tokens for get total account assets");
                });
                mViewModel.getAssets().observe(this, (List<AccountAssets> accountAssets) -> {
                    BLog.i(tag(), "the assets length is: " + accountAssets);
                    if (accountAssets != null && accountAssets.size() > 0) {
                        BLog.i(tag(), "the assets length is: " + accountAssets.size());
                        progressDialog.cancel();
                        accountAssetsList = accountAssets;
                        recyclerViewAccounts.getAdapter().notifyDataSetChanged();
                    }
                });
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
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
                BLog.i(tag(), account.getFilename());
                if (account.getAddress() != null && account.getAddress().length() > 0) {
                    File file = new File(getFilesDir() + "/" +  account.getFilename());
                    if (file != null) {
                        FileInputStream fis = null;
                        try {
                            fis = new FileInputStream(file);
                            int length = fis.available();
                            byte [] buffer = new byte[length];
                            fis.read(buffer);
                            String encoded = new String(buffer);
                            fis.close();
                            BLog.i(tag(), file.getAbsolutePath() + "---:" + encoded);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                Intent intent = new Intent(AccountsActivity.this, AccountAssetsActivity.class);
                intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
                startActivity(intent);
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
            ImageManager.showAccountAvatar(AccountsActivity.this, holder.ivAccountAvatar, account);
            ImageManager.showAccountBackground(AccountsActivity.this, holder.ivAccountBg, account);
            String currencyUnit = BrahmaConfig.getInstance().getCurrencyUnit();
            if (currencyUnit != null) {
                holder.tvCurrencyUnit.setText(currencyUnit);
            } else {
                holder.tvCurrencyUnit.setText(BrahmaConst.UNIT_PRICE_CNY);
            }
            holder.tvAccountName.setText(account.getName());
            holder.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
            BigDecimal totalAssets = BigDecimal.ZERO;
            for (AccountAssets assets : accountAssetsList) {
                if (assets.getAccountEntity().getAddress().equals(account.getAddress()) &&
                        assets.getBalance().compareTo(BigInteger.ZERO) > 0) {
                    for (CryptoCurrency currency : cryptoCurrencies) {
                        if (CommonUtil.cryptoCurrencyCompareToken(currency, assets.getTokenEntity())) {
                            double tokenPrice = currency.getPriceCny();
                            if (BrahmaConfig.getInstance().getCurrencyUnit().equals(BrahmaConst.UNIT_PRICE_USD)) {
                                tokenPrice = currency.getPriceUsd();
                            }
                            BigDecimal tokenValue = new BigDecimal(tokenPrice).multiply(CommonUtil.getAccountFromWei(assets.getBalance()));
                            totalAssets = totalAssets.add(tokenValue);
                        }
                    }
                }
            }
            holder.tvTotalAssets.setText(String.valueOf(totalAssets.setScale(2, BigDecimal.ROUND_HALF_UP)));
        }

        @Override
        public int getItemCount() {
            return accounts.size();
        }

        class ItemViewHolder extends RecyclerView.ViewHolder {

            ImageView ivAccountBg;
            ImageView ivAccountAvatar;
            TextView tvAccountName;
            TextView tvAccountAddress;
            TextView tvTotalAssetsDesc;
            TextView tvTotalAssets;
            TextView tvCurrencyUnit;

            ItemViewHolder(View itemView) {
                super(itemView);
                ivAccountBg = itemView.findViewById(R.id.iv_account_bg);
                ivAccountAvatar = itemView.findViewById(R.id.iv_account_avatar);
                tvAccountName = itemView.findViewById(R.id.tv_account_name);
                tvAccountAddress = itemView.findViewById(R.id.tv_account_address);
                tvTotalAssetsDesc = itemView.findViewById(R.id.tv_total_assets_desc);
                tvTotalAssets = itemView.findViewById(R.id.tv_total_assets);
                tvCurrencyUnit = itemView.findViewById(R.id.tv_currency_unit);
            }
        }
    }

}
