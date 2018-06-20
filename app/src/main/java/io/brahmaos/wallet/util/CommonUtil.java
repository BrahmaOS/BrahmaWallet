package io.brahmaos.wallet.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class CommonUtil {
    public static int MNEMONIC_WORD_LENGTH = 12;

    public static String generateSimpleAddress(String fullAddress) {
        String simpleName = fullAddress;
        if (fullAddress.length() > 18) {
            simpleName = fullAddress.substring(0, 10) + "..." + fullAddress.substring(fullAddress.length() - 8);
        }
        return simpleName;
    }

    public static BigDecimal getAccountFromWei(BigInteger value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.divide(new BigDecimal(Math.pow(10, 18)), 4, BigDecimal.ROUND_HALF_UP);
    }

    public static String parseAccountContent(String value) {
        return value.replaceAll("\\s*", "");
    }

    public static BigInteger convertFormWeiToEther(BigDecimal value) {
        return value.multiply(new BigDecimal(Math.pow(10, 18))).toBigInteger();
    }

    public static boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    // Return network name base of url
    public static String generateNetworkName(String url) {
        if (url.contains("mainnet")) {
            return "Mainnet";
        } else if (url.contains("ropsten")) {
            return "Ropsten";
        } else if (url.contains("infuranet")) {
            return "INFURAnet";
        } else if (url.contains("kovan")) {
            return "Kovan";
        } else if (url.contains("rinkeby")) {
            return "Rinkeby";
        } else {
            return "";
        }
    }

    /**
     * unit：px
     */
    public static int getScreenWidth(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * unit：px
     */
    public static int getScreenHeight(Activity context) {
        DisplayMetrics dm = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static boolean isValidMnemonics(String mnemonics) {
        String[] mnemonicList = mnemonics.split(" ");
        return mnemonicList.length == MNEMONIC_WORD_LENGTH;
    }
}
