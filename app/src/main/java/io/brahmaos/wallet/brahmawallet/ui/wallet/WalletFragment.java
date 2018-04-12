package io.brahmaos.wallet.brahmawallet.ui.wallet;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.database.WalletDatabase;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.ui.account.CreateAccountActivity;
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

    private Button createWalletBtn;
    private Button testBtn;
    private TextView tvTest;

    private AccountViewModel mViewModel;

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
        tvTest = (TextView) parentView.findViewById(R.id.test_text);
        createWalletBtn = (Button) parentView.findViewById(R.id.btn_create_wallet);
        createWalletBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CreateAccountActivity.class);
                startActivity(intent);
            }
        });

        testBtn = (Button) parentView.findViewById(R.id.btn_import_wallet);

        return true;
    }

    @Override
    protected void initToolbar() {
        AppCompatActivity mAppCompatActivity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) mAppCompatActivity.findViewById(toolbarResId);
        toolbar.getMenu().clear();
        toolbar.setTitle(titleResId);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, new Observer<List<AccountEntity>>() {
            @Override
            public void onChanged(@Nullable List<AccountEntity> accountEntities) {
                if (accountEntities == null) {
                    tvTest.setText("the account is null");
                } else {
                    tvTest.setText("" + accountEntities.size());
                }
            }
        });

        mViewModel.getDatabaseCreated().observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(@Nullable Boolean createdFlag) {
                if (createdFlag != null && createdFlag) {
                    BLog.e(tag(), "the databases has created");
                } else {
                    BLog.e(tag(), "the databases has not created");
                }
            }
        });

        testBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.getAccounts().observe(WalletFragment.this, new Observer<List<AccountEntity>>() {
                    @Override
                    public void onChanged(@Nullable List<AccountEntity> accountEntities) {
                        if (accountEntities == null) {
                            tvTest.setText("the test account is null");
                        } else {
                            tvTest.setText("test" + accountEntities.size());
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onStart() {
        BLog.d("HomeFragment", "onStart");
        super.onStart();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        /*if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
        //mListener = null;
    }
}
