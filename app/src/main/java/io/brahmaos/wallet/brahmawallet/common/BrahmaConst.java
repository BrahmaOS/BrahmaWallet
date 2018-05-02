package io.brahmaos.wallet.brahmawallet.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;


/**
 * Definition of commonly used constants
 */
public class BrahmaConst {

    public static final int DEFAULT_CURRENCY_START = 0;
    public static final int DEFAULT_CURRENCY_LIMIT = 2;
    public static final String UNIT_PRICE_CNY = "CNY";
    public static final String UNIT_PRICE_USD = "USD";

    public static final String BRAHMAOS_TOKEN = "brahmaos";
    public static final String ETHEREUM = "ethereum";
    public static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(21000);
    public static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(20000000000L);
    public static final BigDecimal DEFAULT_FEE = BigDecimal.valueOf(0.000400000);

}
