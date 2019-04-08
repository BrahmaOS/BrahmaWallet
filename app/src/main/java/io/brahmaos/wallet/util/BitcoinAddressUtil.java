package io.brahmaos.wallet.util;


/**
 * Ethereum address utilities
 */
public class BitcoinAddressUtil {

    public static String simplifyDisplay(String fullAddress) {

        return fullAddress.substring(0, 10) +
                "..." +
                fullAddress.substring(fullAddress.length() - 10);
    }
}
