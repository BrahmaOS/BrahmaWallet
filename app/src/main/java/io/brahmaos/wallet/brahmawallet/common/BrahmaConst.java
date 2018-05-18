package io.brahmaos.wallet.brahmawallet.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;


/**
 * Definition of commonly used constants
 */
public class BrahmaConst {

    public static final int DEFAULT_CURRENCY_START = 0;
    public static final int DEFAULT_CURRENCY_LIMIT = 20;
    public static final String UNIT_PRICE_CNY = "CNY";
    public static final String UNIT_PRICE_USD = "USD";

    public static final String BRAHMAOS_TOKEN = "brahmaos";
    public static final String ETHEREUM = "ethereum";
    public static final int DEFAULT_GAS_LIMIT = 200000;
    // unit Gwei
    public static final int DEFAULT_GAS_PRICE = 20;
    public static final BigDecimal DEFAULT_FEE = BigDecimal.valueOf(0.000400000);

    public static final String MAINNET_URL = "https://mainnet.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String ROPSTEN_TEST_URL = "https://ropsten.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String INFURANET_TEST_URL = "https://infuranet.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String KOVAN_TEST_URL = "https://kovan.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String RINKEBY_TEST_URL = "https://rinkeby.infura.io/Gy3Csyt4bzKIGsctm3g0";

    public static final String LANGUAGE_ENGLISH = "1";
    public static final String LANGUAGE_CHINESE = "2";
}
