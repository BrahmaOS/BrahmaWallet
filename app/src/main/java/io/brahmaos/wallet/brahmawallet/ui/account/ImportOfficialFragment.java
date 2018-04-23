package io.brahmaos.wallet.brahmawallet.ui.account;

import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImportOfficialFragment extends Fragment {
    protected String tag() {
        return ImportOfficialFragment.class.getName();
    }

    public static final String ARG_PAGE = "OFFICIAL_KEYSTORE_PAGE";
    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts;

    private View parentView;
    private EditText etKeystore;
    private EditText etAccountName;
    private EditText etPassword;
    private EditText etRepeatPassword;
    private Button btnImportAccount;
    private CheckBox checkBoxReadProtocol;
    private View mProgressBar;

    public static ImportOfficialFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ImportOfficialFragment pageFragment = new ImportOfficialFragment();
        pageFragment.setArguments(args);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BLog.d(tag(), "onCreateView");
        if (parentView == null) {
            parentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_import_official, container, false);
            initView();
        } else {
            ViewGroup parent = (ViewGroup)parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }
        return parentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mViewModel = ViewModelProviders.of(this).get(AccountViewModel.class);
        mViewModel.getAccounts().observe(this, accountEntities -> {
            if (accountEntities == null) {
                accounts = null;
            } else {
                accounts = accountEntities;
            }
        });
    }

    private void initView() {

        etKeystore = parentView.findViewById(R.id.et_official_json);
        etAccountName = parentView.findViewById(R.id.et_account_name);
        etPassword = parentView.findViewById(R.id.et_password);
        etRepeatPassword = parentView.findViewById(R.id.et_repeat_password);
        btnImportAccount = parentView.findViewById(R.id.btn_import_keystore);
        checkBoxReadProtocol= parentView.findViewById(R.id.checkbox_read_protocol);
        checkBoxReadProtocol.setOnCheckedChangeListener((buttonView, isChecked) -> btnImportAccount.setEnabled(isChecked));

        btnImportAccount.setOnClickListener(view -> importOfficialAccount());
        mProgressBar = parentView.findViewById(R.id.import_progress);
    }

    private void importOfficialAccount() {
        btnImportAccount.setEnabled(false);
        // Reset errors.
        etAccountName.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        // Store values at the time of the create account.
        String officialKeystore = CommonUtil.parseAccountContent(etKeystore.getText().toString().trim());
        String name = etAccountName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid keystore.
        if (TextUtils.isEmpty(officialKeystore)) {
            focusView = etKeystore;
            Toast.makeText(getActivity(), R.string.error_field_required, Toast.LENGTH_SHORT).show();
            cancel = true;
        }

        // Check for a valid account name.
        if (!cancel && TextUtils.isEmpty(name)) {
            etAccountName.setError(getString(R.string.error_field_required));
            focusView = etAccountName;
            cancel = true;
        }

        if (!cancel && accounts != null && accounts.size() > 0) {
            for (AccountEntity accountEntity : accounts) {
                if (accountEntity.getName().equals(name)) {
                    cancel = true;
                    break;
                }
            }
            if (cancel) {
                etAccountName.setError(getString(R.string.error_incorrect_name));
                focusView = etAccountName;
            }
        }

        // Check for a valid password, if the user entered one.
        if (!cancel && !password.equals(repeatPassword)) {
            etPassword.setError(getString(R.string.error_incorrect_password));
            focusView = etPassword;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            btnImportAccount.setEnabled(true);
            return;
        }
        BLog.i(tag(), "the password is:" + password);
        try {
            ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
            WalletFile walletFile = objectMapper.readValue(officialKeystore, WalletFile.class);
            String address = BrahmaWeb3jService.getInstance().prependHexPrefix(walletFile.getAddress());
            BLog.i(tag(), "the address is :" + address);

            // check the account address
            if (accounts != null && accounts.size() > 0) {
                for (AccountEntity accountEntity : accounts) {
                    if (accountEntity.getAddress().equals(address)) {
                        cancel = true;
                        break;
                    }
                }
                if (cancel) {
                    // dialog show the account exists
                    AlertDialog dialogTip = new AlertDialog.Builder(getActivity())
                            .setMessage(R.string.error_account_exists)
                            .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                            .create();
                    dialogTip.show();
                    etKeystore.requestFocus();
                    btnImportAccount.setEnabled(true);
                    return;
                }
            }

            mProgressBar.setVisibility(View.VISIBLE);
            if (WalletUtils.isValidAddress(address)) {
                mViewModel.importAccount(walletFile, password, name)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Boolean>() {
                            @Override
                            public void onNext(Boolean flag) {
                                if (flag) {
                                    Toast.makeText(getContext(), R.string.success_import_account, Toast.LENGTH_SHORT).show();
                                    getActivity().finish();
                                } else {
                                    Toast.makeText(getContext(), R.string.error_import_keystore, Toast.LENGTH_SHORT).show();
                                    mProgressBar.setVisibility(View.GONE);
                                    etKeystore.requestFocus();
                                    btnImportAccount.setEnabled(true);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), R.string.error_import_keystore, Toast.LENGTH_SHORT).show();
                                mProgressBar.setVisibility(View.GONE);
                                etKeystore.requestFocus();
                                btnImportAccount.setEnabled(true);
                            }

                            @Override
                            public void onCompleted() {

                            }
                        });
            } else {
                BLog.e(tag(), "the error keystore about address");
                Toast.makeText(getContext(), R.string.error_account_address, Toast.LENGTH_SHORT).show();
                btnImportAccount.setEnabled(true);
                etKeystore.requestFocus();
                mProgressBar.setVisibility(View.GONE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), R.string.error_keystore, Toast.LENGTH_SHORT).show();
            btnImportAccount.setEnabled(true);
            etKeystore.requestFocus();
            mProgressBar.setVisibility(View.GONE);
        }
    }
}
