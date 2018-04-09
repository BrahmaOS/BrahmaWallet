package io.brahmaos.wallet.brahmawallet.ui.market;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * to handle interaction events.
 * Use the {@link MarketFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MarketFragment extends BaseFragment {

    @Override
    protected String tag() {
        return MarketFragment.class.getName();
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     */
    public static MarketFragment newInstance(int layoutResId, int toolbarResId, int titleResId) {
        MarketFragment fragment = new MarketFragment();
        fragment.setArguments(newArguments(layoutResId, toolbarResId, titleResId));
        return fragment;
    }

    @Override
    protected boolean initView() {
        return true;
    }

    @Override
    protected void initToolbar() {
        AppCompatActivity mAppCompatActivity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) mAppCompatActivity.findViewById(toolbarResId);
        toolbar.getMenu().clear();
        toolbar.setTitle(titleResId);
    }
}
