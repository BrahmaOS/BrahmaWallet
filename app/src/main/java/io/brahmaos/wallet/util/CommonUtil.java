package io.brahmaos.wallet.util;

public class CommonUtil {
    public static String generateSimpleAddress(String fullAddress) {
        String simpleName = fullAddress;
        if (fullAddress.length() > 16) {
            simpleName = fullAddress.substring(0, 7) + "..." + fullAddress.substring(fullAddress.length() - 8);
        }
        return simpleName;
    }
}
