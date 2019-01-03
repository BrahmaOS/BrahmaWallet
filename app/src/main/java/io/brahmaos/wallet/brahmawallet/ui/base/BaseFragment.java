package io.brahmaos.wallet.brahmawallet.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import io.brahmaos.wallet.util.BLog;

/**
 * base fragment
 *
 * the arguments：
 *      - ARGS_LAYOUT_RES: the layout resource id
 *      - ARGS_MENU_RES:   the menu resource id
 */
public abstract class BaseFragment extends Fragment {

    private static final String ARGS_LAYOUT_RES = "layout_res";
    private static final String ARGS_TOOLBAR_RES = "toolbar_res";

    // toolbar's title resource id 
    private static final String ARGS_TITLE_RES = "title_res";
    
    protected abstract String tag();

    /**
     * init fragment UI component
     */
    protected abstract boolean initView();

    // fragment layout resource id
    protected int layoutResId;

    // toolbar title resource id
    protected int titleResId;

    /**
     * the arguments of init BaseFragment
     *
     * @param layoutResId  the layout resource，：R.layout.fragment_home
     */
    protected static Bundle newArguments(int layoutResId, int titleResId) {
        Bundle args = new Bundle();
        args.putInt(ARGS_LAYOUT_RES, layoutResId);
        args.putInt(ARGS_TITLE_RES, titleResId);
        return args;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BLog.d(tag(), "onCreate");
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            layoutResId = args.getInt(ARGS_LAYOUT_RES);
            titleResId = args.getInt(ARGS_TITLE_RES);
        }
    }

    /**
     * save parent view.
     */
    protected View parentView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        BLog.d(tag(), "onCreateView");
        if (parentView == null) {
            parentView = LayoutInflater.from(getActivity()).inflate(layoutResId, container, false);
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
    public void onStart() {
        BLog.d(tag(), "onStart");
        super.onStart();
    }

    @Override
    public void onDestroy() {
        BLog.d(tag(), "onDestroy");
        super.onDestroy();
    }

    @Override
    public void onResume() {
        BLog.d(tag(), "onResume");
        super.onResume();
    }

    @Override
    public void onPause() {
        BLog.d(tag(), "onPause");
        super.onPause();
    }

    protected void showShortToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    protected void showShortToast(int res) {
        showShortToast(getString(res));
    }

    protected void showLongToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    }

    protected void showLongToast(int res) {
        showLongToast(getString(res));
    }
}
