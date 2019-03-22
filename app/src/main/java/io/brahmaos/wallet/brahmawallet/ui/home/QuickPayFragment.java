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
import android.widget.Button;
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
import io.brahmaos.wallet.brahmawallet.ui.base.BaseWebActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.CheckQuickAccountPasswordActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountRechargeActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountWithdrawActivity;
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
    private CustomProgressDialog progressDialog;
    private TextView mTvEthBalance;
    private TextView mTvBrmBalance;
    private TextView mTvBtcBalance;
    private TextView mTvQuickAccountHelp;

    private List<ImageView> lstGuideIndicator = new ArrayList<>();
    private int pageNum = 3;
    private List<AccountBalance> accountBalances = new ArrayList<>();
    private Button mCreateQuickAccount;

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
        mCreateQuickAccount = parentView.findViewById(R.id.btn_create_quick_account);
        mCreateQuickAccount.setOnClickListener(v -> {
            if (null == BrahmaConfig.getInstance().getPayAccountID()) {
                //todo
                //create account
            } else {
                Intent i = new Intent(getActivity(), CheckQuickAccountPasswordActivity.class);
                startActivity(i);
            }

        });
        layoutAddQuickPayAccount = parentView.findViewById(R.id.layout_add_quick_pay_account);
        layoutHeader = parentView.findViewById(R.id.layout_header);
        pagerGuide = parentView.findViewById(R.id.guide_vpager);
        layoutGuidePageIndicator = parentView.findViewById(R.id.indicator_layout);
        swipeRefreshLayout = parentView.findViewById(R.id.swipe_refresh_layout_pay_account_info);
        LinearLayout layoutAddCredit = parentView.findViewById(R.id.layout_pay_add_credit);
        layoutAddCredit.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayAccountRechargeActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutWithdraw = parentView.findViewById(R.id.layout_pay_withdraw);
        layoutWithdraw.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayAccountWithdrawActivity.class);
            startActivity(intent);
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
        params.height = ((int) (display.heightPixels * 0.65) - statusBarHeight - toolbarHeight);
        layoutHeader.setLayoutParams(params);

        progressDialog = new CustomProgressDialog(getActivity(), R.style.CustomProgressDialogStyle, "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);

        swipeRefreshLayout.setColorSchemeResources(R.color.master);
        swipeRefreshLayout.setRefreshing(true);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            getAccountBalance();
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
        mTvQuickAccountHelp = parentView.findViewById(R.id.tv_quick_account_help);
        mTvQuickAccountHelp.setOnClickListener(v -> {
            BaseWebActivity.startWeb(getActivity(),
                    getResources().getString(R.string.quick_pay_account_help),
                    BrahmaConfig.getInstance().getQuickAccountHelpUrl());
        });
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        BLog.d(tag(), "quick pay fragment onstart");
        if (BrahmaConfig.getInstance().getPayAccount() != null) {
            layoutAddQuickPayAccount.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            getAccountBalance();
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

    private void getAccountBalance() {
        if (BrahmaConfig.getInstance().getPayAccount() != null) {
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

