package io.brahmaos.wallet.brahmawallet.ui.wallet;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.base.BaseFragment;
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
        return true;
    }

    @Override
    protected void initToolbar() {
        AppCompatActivity mAppCompatActivity = (AppCompatActivity) getActivity();
        Toolbar toolbar = (Toolbar) mAppCompatActivity.findViewById(toolbarResId);
        toolbar.getMenu().clear();
        toolbar.setTitle(titleResId);
    }

    /*// TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }*/

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
