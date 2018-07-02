package io.brahmaos.wallet.brahmawallet.ui.account;

import android.app.Activity;
import android.app.ProgressDialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;

import java.io.IOException;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.service.BrahmaWeb3jService;
import io.brahmaos.wallet.brahmawallet.ui.contact.ContactDetailActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.PrivacyPolicyActivity;
import io.brahmaos.wallet.brahmawallet.ui.setting.ServiceTermsActivity;
import io.brahmaos.wallet.brahmawallet.view.CustomProgressDialog;
import io.brahmaos.wallet.brahmawallet.viewmodel.AccountViewModel;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import io.brahmaos.wallet.util.QRCodeUtil;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ShowKeystoreQRCodeFragment extends Fragment {
    protected String tag() {
        return ShowKeystoreQRCodeFragment.class.getName();
    }

    public static final String ARG_PAGE = "BACKUP_KEYSTORE_QR_CODE_PAGE";
    public static final String ARG_KEYSTORE = "OFFICIAL_KEYSTORE";
    private String keystore;

    private View parentView;
    private ImageView ivQRCode;
    private LinearLayout layoutTipInfo;

    public static ShowKeystoreQRCodeFragment newInstance(int page, String keystore) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ARG_KEYSTORE, keystore);
        ShowKeystoreQRCodeFragment pageFragment = new ShowKeystoreQRCodeFragment();
        pageFragment.setArguments(args);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null) {
            keystore = bundle.getString(ARG_KEYSTORE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        BLog.d(tag(), "onCreateView");
        if (parentView == null) {
            parentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_show_keystore_qr_code, container, false);
            initView();
        } else {
            ViewGroup parent = (ViewGroup)parentView.getParent();
            if (parent != null) {
                parent.removeView(parentView);
            }
        }
        return parentView;
    }

    private void initView() {
        ivQRCode = parentView.findViewById(R.id.iv_keystore_qrcode);
        ivQRCode.setVisibility(View.INVISIBLE);
        layoutTipInfo = parentView.findViewById(R.id.layout_tip_info);
        layoutTipInfo.setVisibility(View.VISIBLE);
        Button showQRCodeButton = parentView.findViewById(R.id.btn_show_keystore_qr_code);
        showQRCodeButton.setOnClickListener(v -> {
            ivQRCode.setVisibility(View.VISIBLE);
            layoutTipInfo.setVisibility(View.INVISIBLE);
            new Thread(() -> {
                int width = CommonUtil.dip2px(getContext(), 240);
                Bitmap bitmap = QRCodeUtil.createQRImageTransparentBg(keystore, width, width, null);

                if (bitmap != null && getActivity() != null) {
                    getActivity().runOnUiThread(() -> Glide.with(this)
                            .load(bitmap)
                            .into(ivQRCode));
                }
            }).start();
        });
    }
}
