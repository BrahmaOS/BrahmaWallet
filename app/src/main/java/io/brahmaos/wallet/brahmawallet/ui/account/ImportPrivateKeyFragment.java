package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.ui.setting.PrivacyPolicyActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.ServiceTermsActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ImportPrivateKeyFragment extends Fragment {
    protected String tag() {
        return ImportPrivateKeyFragment.class.getName();
    }

    public static final String ARG_PAGE = "PRIVATE_KEY_PAGE";
    private AccountViewModel mViewModel;
    private List<AccountEntity> accounts;

    private View parentView;
    private EditText etPrivateKey;
    private EditText etAccountName;
    private EditText etPassword;
    private EditText etRepeatPassword;
    private Button btnImportAccount;
    private CheckBox checkBoxReadProtocol;
    private CustomProgressDialog customProgressDialog;
    private TextView tvService;
    private TextView tvPrivacyPolicy;

    public static ImportPrivateKeyFragment newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        ImportPrivateKeyFragment pageFragment = new ImportPrivateKeyFragment();
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
            parentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_import_private_key, container, false);
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

        etPrivateKey = parentView.findViewById(R.id.et_private_key);
        etAccountName = parentView.findViewById(R.id.et_account_name);
        etPassword = parentView.findViewById(R.id.et_password);
        etRepeatPassword = parentView.findViewById(R.id.et_repeat_password);
        btnImportAccount = parentView.findViewById(R.id.btn_import_private_key);
        checkBoxReadProtocol= parentView.findViewById(R.id.checkbox_read_protocol);
        checkBoxReadProtocol.setOnCheckedChangeListener((buttonView, isChecked) -> btnImportAccount.setEnabled(isChecked));

        btnImportAccount.setOnClickListener(view -> importOfficialAccount());

        tvService = parentView.findViewById(R.id.service_tv);
        tvService.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ServiceTermsActivity.class);
            startActivity(intent);
        });

        tvPrivacyPolicy = parentView.findViewById(R.id.privacy_policy_tv);
        tvPrivacyPolicy.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), PrivacyPolicyActivity.class);
            startActivity(intent);
        });
    }

    private void importOfficialAccount() {
        btnImportAccount.setEnabled(false);
        // Reset errors.
        etAccountName.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        // Store values at the time of the create account.
        String privateKey = CommonUtil.parseAccountContent(etPrivateKey.getText().toString().trim());
        String name = etAccountName.getText().toString().trim();
        String password = etPassword.getText().toString();
        String repeatPassword = etRepeatPassword.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid private key.
        if (TextUtils.isEmpty(privateKey)) {
            focusView = etPrivateKey;
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

        // check the private key valid
        if (WalletUtils.isValidPrivateKey(privateKey)) {

            customProgressDialog = new CustomProgressDialog(getActivity(),
                    R.style.CustomProgressDialogStyle,
                    getString(R.string.progress_import_account));
            customProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            customProgressDialog.setCancelable(false);
            customProgressDialog.show();
            mViewModel.importAccountWithPrivateKey(privateKey, password, name)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onNext(String address) {
                            customProgressDialog.cancel();
                            if (address != null && WalletUtils.isValidAddress(address)) {
                                Toast.makeText(getContext(), R.string.success_import_account, Toast.LENGTH_SHORT).show();
                                // hide soft input board
                                getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                                Intent intent = new Intent();
                                getActivity().setResult(Activity.RESULT_OK, intent);
                                getActivity().finish();
                            } else if (address != null && address.length() == 0) {
                                AlertDialog dialogTip = new AlertDialog.Builder(getContext())
                                        .setMessage(R.string.error_account_exists)
                                        .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel())
                                        .create();
                                dialogTip.show();
                                etPrivateKey.requestFocus();
                                btnImportAccount.setEnabled(true);
                            } else {
                                Toast.makeText(getContext(), R.string.error_import_private_key, Toast.LENGTH_LONG).show();
                                etPrivateKey.requestFocus();
                                btnImportAccount.setEnabled(true);
                            }
                        }

                        @Override
                        public void onError(Throwable e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), R.string.error_import_private_key, Toast.LENGTH_LONG).show();
                            customProgressDialog.cancel();
                            etPrivateKey.requestFocus();
                            btnImportAccount.setEnabled(true);
                        }

                        @Override
                        public void onCompleted() {

                        }
                    });

        } else {
            Toast.makeText(getContext(), R.string.error_private_key, Toast.LENGTH_LONG).show();
            etPrivateKey.requestFocus();
            btnImportAccount.setEnabled(true);
        }
    }
}
