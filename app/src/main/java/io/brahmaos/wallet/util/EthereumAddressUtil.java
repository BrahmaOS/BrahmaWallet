package io.brahmaos.wallet.util;


/**
 * Ethereum address utilities
 */
public class EthereumAddressUtil {

    public static String simplifyDisplay(String fullAddress) {

        StringBuilder sbufSimplify = new StringBuilder();
        if (!fullAddress.startsWith("0x")) {
            sbufSimplify.append("0x");
        }

        return sbufSimplify.append(fullAddress.substring(0, 12))
                .append("...")
                .append(fullAddress.substring(fullAddress.length() - 10))
                .toString();
    }
}
