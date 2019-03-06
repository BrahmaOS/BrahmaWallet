package io.brahmaos.wallet.brahmawallet.ui.home;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.IntentParam;
import io.brahmaos.wallet.brahmawallet.model.Dapp;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
import io.brahmaos.wallet.brahmawallet.ui.contact.ContactsActivity;
import io.brahmaos.wallet.brahmawallet.ui.dapp.DappActivity;
import io.brahmaos.wallet.brahmawallet.ui.dapp.DappTestActivity;
import io.brahmaos.wallet.util.BLog;

public class MeFragment extends BaseFragment {
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
    protected boolean initView() {
        return false;
    }
}

