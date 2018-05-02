package io.brahmaos.wallet.brahmawallet.service;

import android.app.Activity;
import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import java.util.List;

import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.common.Config;
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
    public static void showTokenIcon(Context Context, ImageView iv, String address) {
        List<TokenEntity> allTokens = Config.getInstance().getTokenEntities();
        for (TokenEntity tokenEntity : allTokens) {
            if (tokenEntity.getAddress().equals(address)) {
                Glide.with(Context)
                        .load(tokenEntity.getIcon())
                        .into(iv);
            }
        }
    }

    public static void showTokenIcon(Context Context, ImageView iv, int resId) {
        Glide.with(Context)
                .load(resId)
                .into(iv);
    }
}
