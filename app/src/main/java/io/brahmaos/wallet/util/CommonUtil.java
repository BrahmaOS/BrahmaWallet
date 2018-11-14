package io.brahmaos.wallet.util;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;

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

    public static BigDecimal convertBTCFromSatoshi(long value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.divide(new BigDecimal(Math.pow(10, 8)), 8, BigDecimal.ROUND_HALF_UP);
    }

    public static BigDecimal convertBTCFromSatoshi(BigInteger value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.divide(new BigDecimal(Math.pow(10, 8)), 8, BigDecimal.ROUND_HALF_UP);
    }

    public static BigInteger convertSatoshiFromBTC(BigDecimal value) {
        return value.multiply(new BigDecimal(Math.pow(10, 8))).toBigInteger();
    }

    public static BigDecimal convertUnit(String tokenName, long value) {
        return convertUnit(tokenName, new BigInteger(String.valueOf(value)));
    }

    public static BigDecimal convertUnit(String tokenName, BigInteger value) {
        if (value.compareTo(BigInteger.ZERO) <= 0) {
            return new BigDecimal(0.0000);
        }
        if (tokenName.toLowerCase().equals(BrahmaConst.BITCOIN)) {
            return convertBTCFromSatoshi(value);
        } else {
            return getAccountFromWei(value);
        }
    }

    public static String parseAccountContent(String value) {
        return value.replaceAll("\\s*", "");
    }

    public static BigInteger convertWeiFromEther(BigDecimal value) {
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

    public static boolean cryptoCurrencyCompareToken(CryptoCurrency cryptoCurrency, TokenEntity token) {
        if (cryptoCurrency.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
            return token.getName().toLowerCase().equals(BrahmaConst.ETHEREUM);
        } else if (cryptoCurrency.getName().toLowerCase().equals(BrahmaConst.BITCOIN)) {
            return token.getName().toLowerCase().equals(BrahmaConst.BITCOIN);
        } else {
            return cryptoCurrency.getTokenAddress().toLowerCase().equals(token.getAddress().toLowerCase());
        }
    }

    public static boolean isFinger(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            FingerprintManager manager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
            KeyguardManager mKeyManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                BLog.d("commonUtils", "no permission");
                return false;
            }
            BLog.d("commonUtils", "have fingerprint permission");
            if (manager == null || mKeyManager == null) {
                return false;
            }
            // Is hardware detected fingerprint
            if (!manager.isHardwareDetected()) {
                BLog.d("commonUtils", "no hardware detected fingerprint");
                return false;
            }
            BLog.d("commonUtils", "have hardware detected fingerprint");

            //is open PIN
            if (!mKeyManager.isKeyguardSecure()) {
                BLog.d("commonUtils", "no PIN");
                return false;
            }
            BLog.d("commonUtils", "have PIN");

            //Is enrolled fingerprint
            if (!manager.hasEnrolledFingerprints()) {
                BLog.d("commonUtils", "no fingerprint");
                return false;
            }
            BLog.i("commonUtils", "have fingerprint");
            return true;
        } else{
            return false;
        }
    }

    /**
     * Timestamp converted to date format string
     * */
    public static String timestampToDate(long seconds, String format) {
        if(format == null || format.isEmpty()) {
            format = "yyyy-MM-dd HH:mm:ss";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(seconds * 1000));
    }

    public static String datetimeFormat(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }
}
