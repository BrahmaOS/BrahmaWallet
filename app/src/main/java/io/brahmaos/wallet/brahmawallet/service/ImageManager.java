package io.brahmaos.wallet.brahmawallet.service;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;

/**
 * image manager
 */
public class ImageManager {

    /*
     * Avatar of account number, use random fruit picture to show
     */
    public static void showAccountAvatar(Context Context, ImageView iv, AccountEntity account) {
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
        Glide.with(Context)
                .load(resId)
                .into(iv);
    }

    /*
     * Avatar of token
     */
    public static void showTokenIcon(Context Context, ImageView iv, String avatar, String tokenName) {
        if (tokenName.toLowerCase().equals(BrahmaConst.BRAHMAOS_TOKEN)) {
            Glide.with(Context)
                    .load(R.drawable.icon_brm)
                    .into(iv);
        } else if (tokenName.toLowerCase().equals(BrahmaConst.ETHEREUM)) {
            Glide.with(Context)
                    .load(R.drawable.icon_eth)
                    .into(iv);
        } else {
            Glide.with(Context)
                    .load(BrahmaConst.IPFS_BASE_URL + BrahmaConst.IPFS_PREFIX + avatar)
                    .into(iv);
        }
    }

    public static void showTokenIcon(Context Context, ImageView iv, int resId) {
        Glide.with(Context)
                .load(resId)
                .into(iv);
    }
}
