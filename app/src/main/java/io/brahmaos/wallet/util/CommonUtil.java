package io.brahmaos.wallet.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class CommonUtil {
    public static String generateSimpleAddress(String fullAddress) {
        String simpleName = fullAddress;
        if (fullAddress.length() > 18) {
            simpleName = fullAddress.substring(0, 10) + "..." + fullAddress.substring(fullAddress.length() - 8);
        }
        return simpleName;
    }

    public static double getAccountFromWei(BigInteger value) {
        BigDecimal bigDecimal = new BigDecimal(value);
        return bigDecimal.divide(new BigDecimal(Math.pow(10, 18)), 4, BigDecimal.ROUND_HALF_UP).doubleValue() ;
    }

    public static String parseAccountContent(String value) {
        return value.replaceAll("\\s*", "");
    }

    public static BigInteger convertFormWeiToEther(BigDecimal value) {
        return value.multiply(new BigDecimal(Math.pow(10, 18))).toBigInteger();
    }
}
