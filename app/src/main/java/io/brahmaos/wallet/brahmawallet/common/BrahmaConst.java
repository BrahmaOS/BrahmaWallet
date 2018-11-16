package io.brahmaos.wallet.brahmawallet.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;


/**
 * Definition of commonly used constants
 */
public class BrahmaConst {

    public static final int APP_ID = 1;
    public static final String IPFS_BASE_URL = "https://ipfs.io/";
    public static final String KYBER_NETWORK_URL = "https://tracker.kyber.network/";
    public static final String IMAGE_BASE_URL = "https://img.static.brahmaos.io/";
    public static final String PAGE_BASE_URL = "https://wallet.apps.brahmaos.io/";
    public static final String HELP_PREFIX = "help/";
    public static final String TOKEN_ICON_PREFIX = "tokens/icon/";
    public static final String TOKEN_ICON_SUFFIX = "-128x128.png";
    public static final String IPFS_PREFIX = "ipfs/";
    public static final int DEFAULT_TOKEN_COUNT = 20;
    public static final int DEFAULT_TOKEN_SHOW_FLAG = 1;
    public static final int DEFAULT_TOKEN_HIDE_FLAG = 0;

    public static final String UNIT_PRICE_CNY = "CNY";
    public static final String UNIT_PRICE_USD = "USD";

    public static final String BRAHMAOS_TOKEN = "brahmaos";
    public static final String ETHEREUM = "ethereum";
    public static final String BITCOIN = "bitcoin";
    public static final int DEFAULT_GAS_LIMIT = 400000;
    // unit Gwei
    public static final int DEFAULT_GAS_PRICE = 20;
    public static final BigDecimal DEFAULT_FEE = BigDecimal.valueOf(0.000400000);
    // unit sat/b
    public static final int DEFAULT_MINER_FEE = 50;

    public static final String MAINNET_URL = "https://mainnet.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String ROPSTEN_TEST_URL = "https://ropsten.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String INFURANET_TEST_URL = "https://infuranet.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String KOVAN_TEST_URL = "https://kovan.infura.io/Gy3Csyt4bzKIGsctm3g0";
    public static final String RINKEBY_TEST_URL = "https://rinkeby.infura.io/Gy3Csyt4bzKIGsctm3g0";

    public static final String BTC_MAINNET = "MainNet";
    public static final String BTC_TESTNET3 = "TestNet3";

    public static final String KYBER_NETWORK_MAINNET = "0x818e6fecd516ecc3849daf6845e3ec868087b755";
    public static final String KYBER_NETWORK_ROPSTEN = "0x85ecDf8803c35a271a87ad918B5927E5cA6a56D2";
    //public static final String TRANSACTION_ACCOUNT_ADDRESS = "0x50FC3e2B2276E3a58AA696B5112Aa10775d42bd6";

    public static final String LANGUAGE_ENGLISH = "1";
    public static final String LANGUAGE_CHINESE = "2";

    public static final String DEFAULT_KEYSTORE = "{\"address\":\"2a9a59814056035b47e23adb2c2cbe8c336fc2da\",\"id\":\"c0d16da4-739d-4df6-9875-61467e4ed0f4\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"9b7a5d09e1bbc9b0bc30e20ede122995\"},\"ciphertext\":\"9893896d014245cd20e539176c53f8d8ac5515f8d15fc69d8dc02ddb510a23c0\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":4096,\"p\":6,\"r\":8,\"salt\":\"e0da2e26926fb1a9197b388ef0fea1717eada009ca323562928d07d1c190c888\"},\"mac\":\"428c86a9f644c8cea809d976d572a37a2fc01ca9a8b0855511e84d71a278716b\"}}";
    public static final String DEFAULT_HUAWEI_KEYSTORE = "{\"address\":\"b100b71935d6107b55ea39a8105fb5d0f4d412e1\",\"id\":\"740daa89-cdea-4ded-af1b-be2dc71bb1bb\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"a3bbfcbd36e0b9c8355e0671e549828a\"},\"ciphertext\":\"bbe8102c91a93705d8c1c8cd42f59b7fc0f3176adadd3bdc62513d7b7a7c5593\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":4096,\"p\":6,\"r\":8,\"salt\":\"5ef152950a472efea2a839b20deaf51c68d1559beda773c270b6920eb4977901\"},\"mac\":\"a7b2b04561ae412db84fa085ce2a39efc2350d633ff18dd2e7d2163ae27263de\"}}";
    public static final String RELIABLE_TOKENS_ADDRESS = "0x1AAD70f0f94bAefa255cBDc29925f2DAB19c97eD";
    public static final String KYBER_MAIN_NETWORK_ADDRESS = "0x818e6fecd516ecc3849daf6845e3ec868087b755";
    public static final String KYBER_ROPSTEN_NETWORK_ADDRESS = "0x818E6FECD516Ecc3849DAf6845e3EC868087B755";
    public static final String KYBER_WRAPPER_ADDRESS = "0x6172afc8c00c46e0d07ce3af203828198194620a";

    public static final String PRIVACY_POLICY_PATH_ZH = "https://ipfs.io/ipfs/QmaNNoYQK5gKVf83qkuRw8YsqZrVoEojcZCdn7rvRxaRRz";
    public static final String PRIVACY_POLICY_PATH_EN = "https://ipfs.io/ipfs/QmbPfak4W1z9fwoX6dYEtNHBrsfSWZxbC5tz5m1bssh4Fm";
    public static final String SERVICE_PATH_ZH = "https://ipfs.io/ipfs/QmWBTYAXg5ceyGGZ8pcbvXyPneTs8ca4WwStzKY74vaxKT";
    public static final String SERVICE_PATH_EN = "https://ipfs.io/ipfs/Qmds3fqPhHZTYpXQ68e6MURXF9g3kn9BCyTRLhhQAYf2VL";
    public static final String ETHERSCAN_BASE_URL = "https://etherscan.io/";

    public static final Double MAIN_PAGE_HEADER_RATIO = 0.382;
    public static final String FEEDBACK_URL = "https://github.com/BrahmaOS/wallet/issues";

    // KNC contract addresses
    public static final String KNC_MAIN_NETWORK_CONTRACT_ADDRESS = "0xdd974d5c2e2928dea5f71b9825b8b646686bd200";
    public static final String KNC_ROPSTEN_NETWORK_CONTRACT_ADDRESS = "0x4E470dc7321E84CA96FcAEDD0C8aBCebbAEB68C6";

    // Account type
    public static final int ETH_ACCOUNT_TYPE = 1;
    public static final int BTC_ACCOUNT_TYPE = 2;
}
