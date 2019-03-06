package io.brahmaos.wallet.brahmawallet.ui.pay;

import android.Manifest;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.api.PayApi;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.model.VersionInfo;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.service.ImageManager;
import io.brahmaos.wallet.brahmawallet.service.PayService;
import io.brahmaos.wallet.brahmawallet.ui.account.AccountsActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseActivity;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.MeFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.QuickPayFragment;
import io.brahmaos.wallet.brahmawallet.ui.home.WalletFragment;
import io.brahmaos.wallet.brahmawallet.ui.setting.SettingsActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.view.HomeViewPager;
import io.brahmaos.wallet.brahmawallet.view.PassWordLayout;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.PermissionUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SetPayAccountPasswordActivity extends BaseActivity {
    @Override
    protected String tag() {
        return SetPayAccountPasswordActivity.class.getName();
    }

    // UI references.
    @BindView(R.id.iv_account_avatar)
    ImageView ivAccountAvatar;
    @BindView(R.id.tv_account_name)
    TextView tvAccountName;
    @BindView(R.id.tv_account_address)
    TextView tvAccountAddress;
    @BindView(R.id.layout_set_password)
    LinearLayout layoutSetPassword;
    @BindView(R.id.et_password)
    PassWordLayout etPassword;
    @BindView(R.id.btn_next)
    Button btnNext;
    @BindView(R.id.layout_repeat_password)
    LinearLayout layoutRepeatPassword;
    @BindView(R.id.et_repeat_password)
    PassWordLayout etRepeatPassword;
    @BindView(R.id.tv_tip_error_password)
    TextView tvTipErrorPassword;
    @BindView(R.id.btn_set_account)
    Button btnSetAccount;

    private int passwordLength = 6;
    private int accountId;
    private String accountPrivateKey;
    private String accountPublicKey;
    private AccountEntity account;
    private AccountViewModel mViewModel;

    private CustomProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_account_set_password);
        ButterKnife.bind(this);
        showNavBackBtn();
        accountId = getIntent().getIntExtra(IntentParam.PARAM_ACCOUNT_ID, 0);
        if (accountId <= 0) {
            finish();
        }
        accountPrivateKey = getIntent().getStringExtra(IntentParam.PARAM_ACCOUNT_PRIVATE_KEY);
        accountPublicKey = getIntent().getStringExtra(IntentParam.PARAM_ACCOUNT_PUBLIC_KEY);
        BLog.d(tag(), "private: " + accountPrivateKey + "/n" + "public: " + accountPublicKey);
        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        initView();
    }

    private void initView() {
        layoutSetPassword.setVisibility(View.VISIBLE);
        layoutRepeatPassword.setVisibility(View.INVISIBLE);
        etPassword.setPwdChangeListener(new PassWordLayout.pwdChangeListener() {
            @Override
            public void onChange(String pwd) {
                if (pwd != null && pwd.length() == passwordLength) {
                    btnNext.setEnabled(true);
                } else {
                    btnNext.setEnabled(false);
                }
            }

            @Override
            public void onNull() {

            }

            @Override
            public void onFinished(String pwd) {
                btnNext.setEnabled(true);
            }
        });

        etRepeatPassword.setPwdChangeListener(new PassWordLayout.pwdChangeListener() {
            @Override
            public void onChange(String pwd) {
                tvTipErrorPassword.setVisibility(View.INVISIBLE);
                btnSetAccount.setEnabled(false);
            }

            @Override
            public void onNull() {

            }

            @Override
            public void onFinished(String pwd) {
                String password = etPassword.getPassString();
                if (!pwd.equals(password)) {
                    tvTipErrorPassword.setVisibility(View.VISIBLE);
                    btnSetAccount.setEnabled(false);
                } else {
                    tvTipErrorPassword.setVisibility(View.INVISIBLE);
                    btnSetAccount.setEnabled(true);
                }
            }
        });

        btnNext.setOnClickListener(v -> {
            layoutSetPassword.setVisibility(View.INVISIBLE);
            layoutRepeatPassword.setVisibility(View.VISIBLE);
            etRepeatPassword.callOnClick();
        });

        btnSetAccount.setOnClickListener(v -> {
            createPayAccount(etRepeatPassword.getPassString());
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViewModel.getAccountById(accountId).observe(this, accountEntity -> {
            account = accountEntity;
            initAccountInfo(account);
        });
        etPassword.setNoInput(0,  true, "");

        progressDialog = new CustomProgressDialog(this, R.style.CustomProgressDialogStyle, "");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
    }

    private void initAccountInfo(AccountEntity account) {
        ImageManager.showAccountAvatar(this, ivAccountAvatar, account);
        tvAccountName.setText(account.getName());
        tvAccountAddress.setText(account.getAddress());
    }

    private void createPayAccount(String password) {
        progressDialog.show();
        PayService.getInstance()
                .createPayAccount(account.getAddress(), BrahmaConst.ETH_ACCOUNT_TYPE,
                        password, accountPrivateKey, accountPublicKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onNext(String privateKey) {
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                        BLog.d(tag(), privateKey);
                    }

                    @Override
                    public void onError(Throwable e) {
                        e.printStackTrace();
                        if (progressDialog != null) {
                            progressDialog.cancel();
                        }
                    }

                    @Override
                    public void onCompleted() {

                    }
                });
    }

    @Override
    public void onBackPressed() {
        if (layoutRepeatPassword.getVisibility() == View.VISIBLE) {
            etRepeatPassword.removeAllPwd();
            layoutRepeatPassword.setVisibility(View.INVISIBLE);
            layoutSetPassword.setVisibility(View.VISIBLE);
            etPassword.callOnClick();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (layoutRepeatPassword.getVisibility() == View.VISIBLE) {
                etRepeatPassword.removeAllPwd();
                layoutRepeatPassword.setVisibility(View.INVISIBLE);
                layoutSetPassword.setVisibility(View.VISIBLE);
                etPassword.callOnClick();
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (layoutRepeatPassword.getVisibility() == View.VISIBLE) {
                etRepeatPassword.removeAllPwd();
                layoutRepeatPassword.setVisibility(View.INVISIBLE);
                layoutSetPassword.setVisibility(View.VISIBLE);
                etPassword.callOnClick();
            } else {
                finish();
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (layoutRepeatPassword.getVisibility() == View.VISIBLE) {
                etRepeatPassword.removeAllPwd();
                layoutRepeatPassword.setVisibility(View.INVISIBLE);
                layoutSetPassword.setVisibility(View.VISIBLE);
                etPassword.callOnClick();
            } else {
                finish();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
