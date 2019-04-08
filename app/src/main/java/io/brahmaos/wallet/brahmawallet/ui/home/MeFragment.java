package io.brahmaos.wallet.brahmawallet.ui.home;

import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.TextView;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.util.ImageUtil;

public class MeFragment extends BaseFragment {
    private ImageView mAvatar;
    private TextView mAccName;

    @Override
    protected String tag() {
        return MeFragment.class.getName();
    }

    /**
     * instance
     *
     * @param layoutResId  layout resource，e.g. R.layout.fragment_home
     * @return  return fragment
     */
    public static MeFragment newInstance(int layoutResId, int titleResId) {
        MeFragment fragment = new MeFragment();
        fragment.setArguments(newArguments(layoutResId, titleResId));
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAvatar = getActivity().findViewById(R.id.iv_quick_account_avatar);
        mAccName = getActivity().findViewById(R.id.tv_quick_account_name);
        String accountName = BrahmaConfig.getInstance().getPayAccountName();
        if (null == accountName || accountName.isEmpty()) {
            mAccName.setText(getString(R.string.pay_account_info));
        } else {
            mAccName.setText(accountName);
        }
        Bitmap avatar = BrahmaConfig.getInstance().getPayAccountAvatar();
        if (avatar != null) {
            mAvatar.setImageBitmap(ImageUtil.getCircleBitmap(avatar));
        }
    }


    @Override
    protected boolean initView() {
        return false;
    }
}

