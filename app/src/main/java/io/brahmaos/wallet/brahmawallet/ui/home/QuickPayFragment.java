package io.brahmaos.wallet.brahmawallet.ui.home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.model.pay.AccountBalance;
import io.brahmaos.wallet.brahmawallet.model.pay.PayTransaction;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseWebActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.CheckQuickAccountPasswordActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountReceiptActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountTransferActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountRechargeActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayAccountWithdrawActivity;
import io.brahmaos.wallet.brahmawallet.ui.pay.PayTransactionDetailActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.view.HeightWrappingViewPager;
import io.brahmaos.wallet.util.BLog;
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

    private List<PayTransaction> mTransDataList = new ArrayList<>();
    private List<View> mTransLayout = new ArrayList<>();

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

        LinearLayout layoutReceipt = parentView.findViewById(R.id.layout_pay_receipt);
        layoutReceipt.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayAccountReceiptActivity.class);
            startActivity(intent);
        });

        LinearLayout layoutTransfer = parentView.findViewById(R.id.layout_pay_transfer);
        layoutTransfer.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PayAccountTransferActivity.class);
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
        mTransLayout.add(parentView.findViewById(R.id.bill_one));
        mTransLayout.add(parentView.findViewById(R.id.bill_two));
        mTransLayout.add(parentView.findViewById(R.id.bill_three));
        return true;
    }

    private void getLatestPayTransList() {
        final int count = 3;
        PayService.getInstance().getPayTransactions(0, 0, null, null, 0, count)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<List<PayTransaction>>() {
                    @Override
                    public void onNext(List<PayTransaction> apr) {
                        if (apr != null && apr.size() > 0) {
                            for (PayTransaction transNew : apr) {
                                mTransDataList = apr;
                            }
                            notifyDataChange();
                        } else {
                            Log.d(tag(), "getLatestPayTransList---no pay transaction");
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        showShortToast(e == null ? "failed to get pay transactions." : e.toString());
                    }

                    @Override
                    public void onCompleted() {
                    }
                });
    }

    private void notifyDataChange() {
        if (mTransDataList != null) {
            for (int i = 0; i < mTransDataList.size() && i < mTransLayout.size(); i++) {
                View view = mTransLayout.get(i);
                view.setVisibility(View.VISIBLE);
                PayTransaction data = mTransDataList.get(i);
                setData(new ItemViewHolder(view), data);
                if (null != view && null != data && getActivity() != null) {
                    view.setOnClickListener(v -> {
                        Intent detailIntent = new Intent(getActivity(),
                                PayTransactionDetailActivity.class);
                        detailIntent.putExtra(IntentParam.PARAM_PAY_TRANS_DETAIL, data);
                        startActivity(detailIntent);
                    });
                }
            }
        }
    }

    private void setData(ItemViewHolder holder, PayTransaction trans) {
        if (null == holder || null == trans) {
            return;
        }
        if (trans.getMerchantIcon() != null && !trans.getMerchantIcon().isEmpty()) {
            try {
                Glide.with(getActivity())
                        .load(trans.getMerchantIcon())
                        .into(holder.ivTransIcon);
            } catch (Exception e) {
                Log.d(tag(), "load icon fail: " + e.toString());
                holder.ivTransIcon.setImageResource(R.drawable.ic_store_black_24dp);
            }
        } else {
            holder.ivTransIcon.setImageResource(R.drawable.ic_store_black_24dp);
        }
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
        if (amountStr != null) {
            holder.tvAmount.setText(amountStr);
        } else {
            holder.tvAmount.setText("");
        }
        int txCoinCode = trans.getTxCoinCode();
        if (PAY_COIN_CODE_BTC == txCoinCode) {
            holder.tvCoinName.setText(COIN_SYMBOL_BTC);
        } else if (PAY_COIN_CODE_ETH == txCoinCode) {
            holder.tvCoinName.setText(COIN_SYMBOL_ETH);
        } else if (PAY_COIN_CODE_BRM == txCoinCode) {
            holder.tvCoinName.setText(COIN_SYMBOL_BRM);
        }
        PayUtil.setTextByStatus(getActivity(),
                holder.tvPayStatus, trans.getOrderStatus());
    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layoutTransItem;
        ImageView ivTransIcon;
        TextView tvMerchantName;
        TextView tvTransTime;
        TextView tvAmount;
        TextView tvPayStatus;
        TextView tvCoinName;

        ItemViewHolder(View itemView) {
            super(itemView);
            layoutTransItem = itemView.findViewById(R.id.layout_pay_trans_item);
            ivTransIcon = itemView.findViewById(R.id.iv_trans_icon);
            tvMerchantName = itemView.findViewById(R.id.tv_merchant_name);
            tvTransTime = itemView.findViewById(R.id.tv_trans_time);
            tvAmount = itemView.findViewById(R.id.tv_trans_amount);
            tvPayStatus = itemView.findViewById(R.id.tv_pay_status);
            tvCoinName = itemView.findViewById(R.id.tv_coin_name);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        BLog.d(tag(), "quick pay fragment onstart");
        if (BrahmaConfig.getInstance().getPayAccount() != null) {
            layoutAddQuickPayAccount.setVisibility(View.GONE);
            swipeRefreshLayout.setVisibility(View.VISIBLE);
            getAccountBalance();
            getLatestPayTransList();
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

