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

import org.spongycastle.jce.ECNamedCurveTable;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.jce.spec.ECNamedCurveParameterSpec;
import org.spongycastle.jce.spec.ECNamedCurveSpec;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

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

    public static int getCurrentSecondTimestamp() {
        Date curDate =  new Date(System.currentTimeMillis());
        if (null == curDate) {
            return 0;
        }
        String timestamp = String.valueOf(curDate.getTime());
        int length = timestamp.length();
        if (length > 3) {
            return Integer.valueOf(timestamp.substring(0,length-3));
        } else {
            return 0;
        }
    }

    public static int getNonce() {
        return new Random().nextInt(100);
    }

    public static String generateSignature(String uri, TreeMap<String,Object> params, String secKey) {
        String securityContent = generateSecurityContent(uri, params);
        String messageSig;
        try {
            ECKeyPair ecKeyPair = ECKeyPair.create(Hex.decode(secKey));
            Sign.SignatureData signatureData = Sign.signMessage(securityContent.getBytes(), ecKeyPair, true);
            String messageR = Numeric.cleanHexPrefix(Numeric.toHexString(signatureData.getR()));
            String messageS = Numeric.cleanHexPrefix(Numeric.toHexString(signatureData.getS()));
            String messageV = String.format("%02x", (signatureData.getV() - 27) & 0xFF);
            messageSig = String.format("%s%s%s", messageR, messageS, messageV);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return messageSig;

    }

    public static String generateSecurityContent(String uri, TreeMap<String,Object> params){
        StringBuilder sb = new StringBuilder(uri);
        if (params != null && params.keySet().size() > 0) {
            boolean firstFlag = true;
            Iterator iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry entry = (Map.Entry<String, String>) iterator.next();
                if (firstFlag) {
                    sb.append("?").append(entry.getKey()).append("=").append(entry.getValue());
                    firstFlag = false;
                } else {
                    sb.append("&").append(entry.getKey()).append("=").append(entry.getValue());
                }
            }
        }
        return sb.toString();
    }

    public static boolean isNull(String content) {
        return content == null || content.isEmpty();
    }

    public static long convertDateTimeStringToLong(String timeStr, String format) {
        long result = 0;
        if (null == timeStr || timeStr.isEmpty()) {
            return result;
        }
        if (null == format || format.isEmpty()) {
            format = "yyyy-MM-dd hh:mm:ss";
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = null;
        try {
            date = formatter.parse(timeStr);
        } catch (Exception e) {
            date = null;
        }
        if (date != null) {
            return date.getTime();
        } else {
            return result;
        }
    }

    public static String convertDateTimeLongToString(long timeMill, String format) {
        if (timeMill <= 0) {
            return null;
        }
        if (null == format || format.isEmpty()) {
            format = "yyyy-MM-dd hh:mm:ss";
        }
        try {
            Date date = new Date(timeMill);
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return sdf.format(date);
        } catch (Exception e) {
            return null;
        }
    }

}
