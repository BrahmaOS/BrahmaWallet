package io.brahmaos.wallet.brahmawallet.common;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;

import io.rayup.sdk.RayUpConst;


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
    public static final String PRIVATE_KEY = "privateKey";
    public static final String PUBLIC_KEY = "publicKey";
    public static final String PAY_REQUEST_SIGN_TYPE = "ecdsa-keccak256";

    public static final String PAY_DEV_HOST = "https://api.dev.brmpay.com/";
    public static final String PAY_HOST = "https://api.brmpay.com/";

    public static final String rayupAccessKeyId = "727965f2c89511e8af75560001a43649";
    public static final String rayupAccessKeySecret = "cnlo/siVEeivdVYAAaQ2SQ==";

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
    public static final String TRANSACTION_ACCOUNT_ADDRESS = "0x50FC3e2B2276E3a58AA696B5112Aa10775d42bd6";

    public static final String LANGUAGE_ENGLISH = "1";
    public static final String LANGUAGE_CHINESE = "2";

    public static final String DEFAULT_KEYSTORE = "{\"address\":\"2a9a59814056035b47e23adb2c2cbe8c336fc2da\",\"id\":\"c0d16da4-739d-4df6-9875-61467e4ed0f4\",\"version\":3,\"crypto\":{\"cipher\":\"aes-128-ctr\",\"cipherparams\":{\"iv\":\"9b7a5d09e1bbc9b0bc30e20ede122995\"},\"ciphertext\":\"9893896d014245cd20e539176c53f8d8ac5515f8d15fc69d8dc02ddb510a23c0\",\"kdf\":\"scrypt\",\"kdfparams\":{\"dklen\":32,\"n\":4096,\"p\":6,\"r\":8,\"salt\":\"e0da2e26926fb1a9197b388ef0fea1717eada009ca323562928d07d1c190c888\"},\"mac\":\"428c86a9f644c8cea809d976d572a37a2fc01ca9a8b0855511e84d71a278716b\"}}";
    public static final String RELIABLE_TOKENS_ADDRESS = "0x1AAD70f0f94bAefa255cBDc29925f2DAB19c97eD";
    public static final String KYBER_MAIN_NETWORK_ADDRESS = "0x818e6fecd516ecc3849daf6845e3ec868087b755";
    public static final String KYBER_ROPSTEN_NETWORK_ADDRESS = "0x818E6FECD516Ecc3849DAf6845e3EC868087B755";
    public static final String KYBER_WRAPPER_ADDRESS = "0x6172afc8c00c46e0d07ce3af203828198194620a";

    public static final String PRIVACY_POLICY_PATH_ZH = "https://ipfs.io/ipfs/QmaNNoYQK5gKVf83qkuRw8YsqZrVoEojcZCdn7rvRxaRRz";
    public static final String PRIVACY_POLICY_PATH_EN = "https://ipfs.io/ipfs/QmbPfak4W1z9fwoX6dYEtNHBrsfSWZxbC5tz5m1bssh4Fm";
    public static final String SERVICE_PATH_ZH = "https://ipfs.io/ipfs/QmWBTYAXg5ceyGGZ8pcbvXyPneTs8ca4WwStzKY74vaxKT";
    public static final String SERVICE_PATH_EN = "https://ipfs.io/ipfs/Qmds3fqPhHZTYpXQ68e6MURXF9g3kn9BCyTRLhhQAYf2VL";
    public static final String ETHERSCAN_BASE_URL = "https://etherscan.io/";
    public static final String ETHERSCAN_ROPSTEN_BASE_URL = "https://ropsten.etherscan.io/";
    public static final String BLOCKCHAIN_BASE_URL = "https://blockchain.com/btc/tx/";

    public static final Double MAIN_PAGE_HEADER_RATIO = 0.382;
    public static final String FEEDBACK_URL = "https://github.com/BrahmaOS/wallet/issues";

    // KNC contract addresses
    public static final String KNC_MAIN_NETWORK_CONTRACT_ADDRESS = "0xdd974d5c2e2928dea5f71b9825b8b646686bd200";
    public static final String KNC_ROPSTEN_NETWORK_CONTRACT_ADDRESS = "0x4E470dc7321E84CA96FcAEDD0C8aBCebbAEB68C6";

    // Account type
    public static final int ETH_ACCOUNT_TYPE = 1;
    public static final int BTC_ACCOUNT_TYPE = 2;

    public static final String HASH_RATE_URL = "http://nebula.0592ing.com/index.html";

    public static final int COIN_COUNT = 3000;

    // coin code base rayup.io
    public static final int COIN_CODE_BTC = RayUpConst.COIN_BTC;
    public static final int COIN_CODE_ETH = RayUpConst.COIN_ETH;
    public static final int COIN_CODE_BRM = RayUpConst.COIN_BRM;
    public static final int COIN_CODE_USDT = RayUpConst.COIN_USDT;

    public static final String COIN_BTC = "Bitcoin";
    public static final String COIN_ETH = "Ethereum";
    public static final String COIN_BRM = "BrahmaOS";
    public static final String COIN_SYMBOL_BTC = "BTC";
    public static final String COIN_SYMBOL_ETH = "ETH";
    public static final String COIN_SYMBOL_BRM = "BRM";
    public static final String COIN_BRM_ADDRESS = "0xd7732e3783b0047aa251928960063f863ad022d8";
    public static final String COIN_ETH_ADDRESS = "";

    // brahma pay
    public static final int PAY_CODE_SUCCESS = 0;
    public static final int PAY_CODE_INVALID_BLOCKCHAIN = 101;
    public static final int PAY_CODE_NO_ACCOUNT = 102;

    // coin code base brmpay
    public static final int PAY_COIN_CODE_BTC = 1;
    public static final int PAY_COIN_CODE_ETH = 1027;
    public static final int PAY_COIN_CODE_USDT = 825;
    public static final int PAY_COIN_CODE_BRM = 2657;

    // withdraw config
    public static final String WITHDRAW_BTC_MIN = "withdraw.btc.min";
    public static final String WITHDRAW_BTC_FEE = "withdraw.btc.fee";
    public static final String WITHDRAW_ETH_MIN = "withdraw.eth.min";
    public static final String WITHDRAW_ETH_FEE = "withdraw.eth.fee";
    public static final String WITHDRAW_BRM_MIN = "withdraw.brm.min";
    public static final String WITHDRAW_BRM_FEE = "withdraw.brm.fee";
}
