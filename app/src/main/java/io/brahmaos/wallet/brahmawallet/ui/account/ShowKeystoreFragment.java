package io.brahmaos.wallet.brahmawallet.ui.account;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.util.BLog;

public class ShowKeystoreFragment extends Fragment {
    protected String tag() {
        return ShowKeystoreFragment.class.getName();
    }

    public static final String ARG_PAGE = "BACKUP_KEYSTORE_PAGE";
    public static final String ARG_KEYSTORE = "OFFICIAL_KEYSTORE";
    private String keystore;

    private View parentView;
    private TextView tvKeystore;
    private Button btnCopyKeystore;

    public static ShowKeystoreFragment newInstance(int page, String keystore) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        args.putString(ARG_KEYSTORE, keystore);
        ShowKeystoreFragment pageFragment = new ShowKeystoreFragment();
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
            parentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_show_keystore, container, false);
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
    }

    private void initView() {
        tvKeystore = parentView.findViewById(R.id.tv_keystore);
        tvKeystore.setText(keystore);
        btnCopyKeystore = parentView.findViewById(R.id.btn_copy_keystore);
        btnCopyKeystore.setOnClickListener(view -> importOfficialAccount());
    }

    private void importOfficialAccount() {
        if (getActivity() != null) {
            ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clipData = ClipData.newPlainText("text", keystore);
            if (cm != null) {
                cm.setPrimaryClip(clipData);
                Toast.makeText(getContext(), R.string.tip_success_copy, Toast.LENGTH_LONG).show();
            }
        }
    }
}
