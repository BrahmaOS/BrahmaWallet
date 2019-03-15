package io.brahmaos.wallet.brahmawallet.ui.home;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;

public class GuideFragment extends Fragment{
    public static String GUIDE_FRAGMENT_POSITION = "guide-position";

    protected String tag() {
        return GuideFragment.class.getName();
    }

    public GuideFragment() {
        // Required empty public constructor
    }

    // 缓存 fragment view
    private View parentView;
    // fragment的序号
    private int pagePosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            pagePosition = args.getInt(GUIDE_FRAGMENT_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (parentView == null) {
            parentView = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_guide, null);
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
        ImageView ivGuide = parentView.findViewById(R.id.iv_guide);
        if (BrahmaConfig.getInstance().getLanguageLocale().equals(BrahmaConst.LANGUAGE_CHINESE)) {
            if (pagePosition == 0) {
                Glide.with(this)
                        .load(R.drawable.pay_banner_zh_1)
                        .into(ivGuide);
            } else if (pagePosition == 1) {
                Glide.with(this)
                        .load(R.drawable.pay_banner_zh_2)
                        .into(ivGuide);
            } else if (pagePosition == 2) {
                Glide.with(this)
                        .load(R.drawable.pay_banner_zh_3)
                        .into(ivGuide);
            }
        } else {
            if (pagePosition == 0) {
                Glide.with(this)
                        .load(R.drawable.pay_banner_1)
                        .into(ivGuide);
            } else if (pagePosition == 1) {
                Glide.with(this)
                        .load(R.drawable.pay_banner_2)
                        .into(ivGuide);
            } else if (pagePosition == 2) {
                Glide.with(this)
                        .load(R.drawable.pay_banner_3)
                        .into(ivGuide);
            }
        }
    }
}
