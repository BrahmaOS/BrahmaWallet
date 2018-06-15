package io.brahmaos.wallet.brahmawallet.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;


/**
 * Definition of commonly used constants
 */
public class BrahmaConst {

    public static final String IPFS_BASE_URL = "https://ipfs.io/";
    public static final String IPFS_PREFIX = "ipfs/";
    public static final int DEFAULT_TOKEN_COUNT = 20;
    public static final int DEFAULT_TOKEN_SHOW_FLAG = 1;
    public static final int DEFAULT_TOKEN_HIDE_FLAG = 0;

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

    public static final String DEFAULT_KEYSTORE = "{\"address\":\"2a9a59814056035b47e23adb2c2cbe8c336fc2da\",\"id\":\"c0d16da4-739d-4df6-9875-61467e4ed0f4\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"9b7a5d09e1bbc9b0bc30e20ede122995\"},\"ciphertext\":\"9893896d014245cd20e539176c53f8d8ac5515f8d15fc69d8dc02ddb510a23c0\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":4096,\"p\":6,\"r\":8,\"salt\":\"e0da2e26926fb1a9197b388ef0fea1717eada009ca323562928d07d1c190c888\"},\"mac\":\"428c86a9f644c8cea809d976d572a37a2fc01ca9a8b0855511e84d71a278716b\"}}";
    public static final String RELIABLE_TOKENS_ADDRESS = "0x1AAD70f0f94bAefa255cBDc29925f2DAB19c97eD";

    public static final String PRIVACY_POLICY_PATH_ZH = "https://ipfs.io/ipfs/QmaNNoYQK5gKVf83qkuRw8YsqZrVoEojcZCdn7rvRxaRRz";
    public static final String PRIVACY_POLICY_PATH_EN = "https://ipfs.io/ipfs/QmbPfak4W1z9fwoX6dYEtNHBrsfSWZxbC5tz5m1bssh4Fm";
    public static final String SERVICE_PATH_ZH = "https://ipfs.io/ipfs/QmWBTYAXg5ceyGGZ8pcbvXyPneTs8ca4WwStzKY74vaxKT";
    public static final String SERVICE_PATH_EN = "https://ipfs.io/ipfs/Qmds3fqPhHZTYpXQ68e6MURXF9g3kn9BCyTRLhhQAYf2VL";
}
