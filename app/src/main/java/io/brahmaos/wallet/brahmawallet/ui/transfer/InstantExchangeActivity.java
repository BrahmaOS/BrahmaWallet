package io.brahmaos.wallet.brahmawallet.ui.transfer;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.AccountAssets;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.MainService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.CommonUtil;

public class InstantExchangeActivity extends BaseActivity {

    // UI references.
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.tv_change_account)
    TextView tvChangeAccount;

    private AccountEntity mAccount;
    private AccountViewModel mViewModel;
    private List<AccountEntity> mAccounts = new ArrayList<>();
    private List<AccountAssets> mAccountAssetsList = new ArrayList<>();

    @Override
    protected String tag() {
        return InstantExchangeActivity.class.getName();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instant_exchange);
        ButterKnife.bind(this);
        showNavBackBtn();

        mAccount = (AccountEntity) getIntent().getSerializableExtra(IntentParam.PARAM_ACCOUNT_INFO);
        initView();
    }

    private void initView() {
        mAccountAssetsList = MainService.getInstance().getAccountAssetsList();

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            mAccounts = accountEntities;
            if ((mAccount == null || mAccount.getAddress().length() == 0) &&
                    accountEntities != null) {
                mAccount = mAccounts.get(0);
            }
            if (mAccounts != null && mAccounts.size() > 1) {
                tvChangeAccount.setVisibility(View.VISIBLE);
            }
            showAccountInfo(mAccount);
        });
        tvChangeAccount.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View dialogView = getLayoutInflater().inflate(R.layout.dialog_account_list, null);
            builder.setView(dialogView);
            builder.setCancelable(true);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            LinearLayout layoutAccountList = dialogView.findViewById(R.id.layout_accounts);

            for (final AccountEntity account : mAccounts) {
                final AccountItemView accountItemView = new AccountItemView();
                accountItemView.layoutAccountItem = LayoutInflater.from(this).inflate(R.layout.dialog_list_item_account, null);
                accountItemView.ivAccountAvatar = accountItemView.layoutAccountItem.findViewById(R.id.iv_account_avatar);
                accountItemView.tvAccountName = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_name);
                accountItemView.tvAccountAddress = accountItemView.layoutAccountItem.findViewById(R.id.tv_account_address);
                accountItemView.layoutDivider = accountItemView.layoutAccountItem.findViewById(R.id.layout_divider);

                accountItemView.tvAccountName.setText(account.getName());
                ImageManager.showAccountAvatar(this, accountItemView.ivAccountAvatar, account);
                accountItemView.tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));

                accountItemView.layoutAccountItem.setOnClickListener(v1 -> {
                    alertDialog.cancel();
                    mAccount = account;
                    showAccountInfo(account);
                });

                if (mAccounts.indexOf(account) == mAccounts.size() - 1) {
                    accountItemView.layoutDivider.setVisibility(View.GONE);
                }

                layoutAccountList.addView(accountItemView.layoutAccountItem);
            }
        });
    }

    private class AccountItemView {
        View layoutAccountItem;
        ImageView ivAccountAvatar;
        TextView tvAccountName;
        TextView tvAccountAddress;
        LinearLayout layoutDivider;
    }

    private void showAccountInfo(AccountEntity account) {
        ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
        tvAccountName.setText(account.getName());
        tvAccountAddress.setText(CommonUtil.generateSimpleAddress(account.getAddress()));
    }
}
