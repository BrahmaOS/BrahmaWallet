package io.brahmaos.wallet.brahmawallet.ui.home;

import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.pay.AccountBalance;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountRechargeActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.SetPayAccountPasswordActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.view.HeightWrappingViewPager;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QuickPayFragment extends BaseFragment {
    @Override
    protected String tag() {
        return QuickPayFragment.class.getName();
    }

    private NestedScrollView layoutAddQuickPayAccount;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LinearLayout layoutHeader;
    private HeightWrappingViewPager pagerGuide;
    private LinearLayout layoutGuidePageIndicator;
    private RecyclerView recyclerViewAccounts;
    private CustomProgressDialog progressDialog;
    private TextView mTvEthBalance;
    private TextView mTvBrmBalance;
    private TextView mTvBtcBalance;

    private List<ImageView> lstGuideIndicator = new ArrayList<>();
    private int pageNum = 3;
    private AccountViewModel mViewModel;
    private List<AccountEntity> cacheAccounts = new ArrayList<>();
    private List<AccountBalance> accountBalances = new ArrayList<>();

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
        pagerGuide = parentView.findViewById(R.id.guide_vpager);
        layoutGuidePageIndicator = parentView.findViewById(R.id.indicator_layout);
        recyclerViewAccounts = parentView.findViewById(R.id.accounts_recycler);
        swipeRefreshLayout = parentView.findViewById(R.id.swipe_refresh_layout_pay_account_info);
        LinearLayout layoutAddCredit = parentView.findViewById(R.id.layout_pay_add_credit);
        layoutAddCredit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayAccountRechargeActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutPayReceipt = parentView.findViewById(R.id.layout_pay_receipt);
        layoutPayReceipt.setOnClickListener(v -> {
            BrahmaConfig.getInstance().setPayAccount(null);
            layoutAddQuickPayAccount.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        });

        DisplayMetrics display = this.getResources().getDisplayMetrics();

        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        int toolbarHeight = getResources().getDimensionPixelSize(R.dimen.height_toolbar);

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) layoutHeader.getLayoutParams();
        params.width = display.widthPixels;
        params.height = ((int) (display.heightPixels * 0.6) - statusBarHeight - toolbarHeight);
        layoutHeader.setLayoutParams(params);

        LinearLayoutManager btcLayoutManager = new LinearLayoutManager(getActivity());
        recyclerViewAccounts.setLayoutManager(btcLayoutManager);
        recyclerViewAccounts.setAdapter(new AccountRecyclerAdapter());
        // Solve the sliding lag problem
        recyclerViewAccounts.setHasFixedSize(true);
        recyclerViewAccounts.setNestedScrollingEnabled(false);

        progressDialog = new CustomProgressDialog(getActivity(), R.style.CustomProgressDialogStyle, "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        swipeRefreshLayout.setColorSchemeResources(R.color.master);
        swipeRefreshLayout.setRefreshing(true);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
        });

        GuidePagerAdapter adapterGuidePage = new GuidePagerAdapter(getFragmentManager(), pageNum);
        pagerGuide.setAdapter(adapterGuidePage);
        pagerGuide.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                updateGuidePageIndicatorWithPos(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        layoutGuidePageIndicator = parentView.findViewById(R.id.indicator_layout);
        layoutGuidePageIndicator.removeAllViews();
        lstGuideIndicator.clear();
        for (int idx = 0; idx < pageNum; ++idx) {
            ImageView ivIndicator = (ImageView) getLayoutInflater().inflate(R.layout.page_indicator, null);
            lstGuideIndicator.add(ivIndicator);
            layoutGuidePageIndicator.addView(ivIndicator);
        }
        updateGuidePageIndicatorWithPos(0);

        mTvEthBalance = parentView.findViewById(R.id.tv_eth_amount);
        mTvBrmBalance = parentView.findViewById(R.id.tv_brm_amount);
        mTvBtcBalance = parentView.findViewById(R.id.tv_btc_amount);

        initData();
        return true;
    }

    private void initData() {
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);

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
        if (BrahmaConfig.getInstance().getPayAccount() != null) {
            getAccountBalance();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BLog.d(tag(), "quick pay fragment onstart");
        if (BrahmaConfig.getInstance().getPayAccount() != null) {
            layoutAddQuickPayAccount.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
        } else {
            layoutAddQuickPayAccount.setVisibility(View.VISIBLE);
            swipeRefreshLayout.setVisibility(View.GONE);
        }
    }

    private void updateGuidePageIndicatorWithPos(int position) {
        if (lstGuideIndicator == null || position >= lstGuideIndicator.size()) {
            return ;
        }

        for (ImageView ivIndicator : lstGuideIndicator) {
            if (ivIndicator != null) {
                ivIndicator.setSelected(false);
            }
        }

        lstGuideIndicator.get(position).setSelected(true);
    }

    private class GuidePagerAdapter extends FragmentPagerAdapter {
        private int pageNum;

        GuidePagerAdapter(FragmentManager fm, int pageNum) {
            super(fm);
            this.pageNum = pageNum;
        }

        @Override
        public Fragment getItem(int position) {
            GuideFragment fragment = new GuideFragment();
            Bundle args = new Bundle();
            args.putInt(GuideFragment.GUIDE_FRAGMENT_POSITION, position);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return pageNum;
        }
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
                final View dialogView = getLayoutInflater().inflate(R.layout.dialog_set_quick_pay_account, null);
                EditText etPassword = dialogView.findViewById(R.id.et_password);
                AlertDialog passwordDialog = new AlertDialog.Builder(getActivity())
                        .setView(dialogView)
                        .setCancelable(true)
                        .setPositiveButton(R.string.confirm, (dialog, which) -> {
                            dialog.cancel();
                            String password = etPassword.getText().toString();
                            checkPrivateKey(account, password);
                        })
                        .create();
                passwordDialog.setOnShowListener(dialog -> {
                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(etPassword, InputMethodManager.SHOW_IMPLICIT);
                });
                passwordDialog.show();
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

    private void checkPrivateKey(AccountEntity account, String password) {
        progressDialog.show();
        BrahmaWeb3jService.getInstance()
                .getEcKeyByPassword(account.getFilename(), password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Map>() {
                    @Override
                    public void onNext(Map ecKeys) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        if (ecKeys != null && ecKeys.containsKey(BrahmaConst.PRIVATE_KEY)
                                && ecKeys.get(BrahmaConst.PRIVATE_KEY) != null
                                && BrahmaWeb3jService.getInstance().isValidPrivateKey(String.valueOf(ecKeys.get(BrahmaConst.PRIVATE_KEY)))) {
                            Intent intent = new Intent(getActivity(), SetPayAccountPasswordActivity.class);
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_ID, account.getId());
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_PRIVATE_KEY, String.valueOf(ecKeys.get(BrahmaConst.PRIVATE_KEY)));
                            intent.putExtra(IntentParam.PARAM_ACCOUNT_PUBLIC_KEY, String.valueOf(ecKeys.get(BrahmaConst.PUBLIC_KEY)));
                            startActivity(intent);
                        } else {
                            showPasswordErrorDialog();;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        showPasswordErrorDialog();
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    private void showPasswordErrorDialog() {
        AlertDialog errorDialog = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.error_current_password)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .create();
        errorDialog.show();
    }

    private void getAccountBalance() {
        PayService.getInstance().getAccountBalance()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<AccountBalance>>() {
                    @Override
                    public void onNext(List<AccountBalance> results) {
                        swipeRefreshLayout.setRefreshing(false);
                        showAccountBalance();
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                    }

                    @Override
                    public void onCompleted() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });
    }

    private void showAccountBalance() {
        accountBalances = PayService.getInstance().getAccountBalances();
        for (AccountBalance accountBalance : accountBalances) {
            if (accountBalance.getCoinCode() == BrahmaConst.PAY_COIN_CODE_BRM) {
                mTvBrmBalance.setText(accountBalance.getBalance());
            } else if (accountBalance.getCoinCode() == BrahmaConst.PAY_COIN_CODE_ETH) {
                mTvEthBalance.setText(accountBalance.getBalance());
            } else if (accountBalance.getCoinCode() == BrahmaConst.PAY_COIN_CODE_BTC) {
                mTvBtcBalance.setText(accountBalance.getBalance());
            }
        }
    }
}

