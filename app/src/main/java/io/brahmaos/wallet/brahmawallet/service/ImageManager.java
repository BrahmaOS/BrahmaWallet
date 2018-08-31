package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.util.BLog;

/**
 * image manager
 */
public class ImageManager {

    /*
     * Avatar of account number, use random fruit picture to show
     */
    public static void showAccountAvatar(Context context, ImageView iv, AccountEntity account) {
        int resId = 0;
        if (account.getId() % 9 == 1) {
            resId = R.drawable.fruit_icons_00;
        } else if (account.getId() % 9 == 2) {
            resId = R.drawable.fruit_icons_01;
        } else if (account.getId() % 9 == 3) {
            resId = R.drawable.fruit_icons_02;
        } else if (account.getId() % 9 == 4) {
            resId = R.drawable.fruit_icons_03;
        } else if (account.getId() % 9 == 5) {
            resId = R.drawable.fruit_icons_04;
        } else if (account.getId() % 9 == 6) {
            resId = R.drawable.fruit_icons_05;
        } else if (account.getId() % 9 == 7) {
            resId = R.drawable.fruit_icons_06;
        } else if (account.getId() % 9 == 8) {
            resId = R.drawable.fruit_icons_07;
        } else {
            resId = R.drawable.fruit_icons_08;
        }
        Glide.with(context)
                .load(resId)
                .into(iv);
    }

    /*
     * Show account info background.
     */
    public static void showAccountBackground(Context context, ImageView ivLayoutBg, AccountEntity account) {
        int resId;
        int position = account.getId();
        if (position % 6 == 1) {
            resId = R.drawable.account_bg_apple;
        } else if (position % 6 == 2) {
            resId = R.drawable.account_bg_baker;
        } else if (position % 6 == 3) {
            resId = R.drawable.account_bg_charley;
        } else if (position % 6 == 4) {
            resId = R.drawable.account_bg_dog;
        } else if (position % 6 == 5) {
            resId = R.drawable.account_bg_easy;
        } else {
            resId = R.drawable.account_bg_fox;
        }
        Glide.with(context)
                .load(resId)
                .into(ivLayoutBg);
    }

    /*
     * Avatar of token
     */
    public static void showTokenIcon(Context Context, ImageView iv,
                                     String tokenName, String tokenAddress) {
        if (tokenName.toLowerCase().equals(BrahmaConst.BRAHMAOS_TOKEN)) {
            Glide.with(Context)
                    .load(R.drawable.icon_brm)
                    .into(iv);
        } else if (tokenName.toLowerCase().equals(BrahmaConst.ETHEREUM)) {
            Glide.with(Context)
                    .load(R.drawable.icon_eth)
                    .into(iv);
        } else {
            RequestOptions options = new RequestOptions()
                    .placeholder(R.drawable.token_default)
                    .error(R.drawable.token_default);
            BLog.i("icon url", BrahmaConst.IMAGE_BASE_URL + BrahmaConst.TOKEN_ICON_PREFIX +
                    tokenAddress + BrahmaConst.TOKEN_ICON_SUFFIX);
            Glide.with(Context)
                    .load(BrahmaConst.IMAGE_BASE_URL + BrahmaConst.TOKEN_ICON_PREFIX +
                            tokenAddress + BrahmaConst.TOKEN_ICON_SUFFIX)
                    .apply(options)
                    .into(iv);
        }
    }

    public static void showTokenIcon(Context Context, ImageView iv, int resId) {
        Glide.with(Context)
                .load(resId)
                .into(iv);
    }
}
