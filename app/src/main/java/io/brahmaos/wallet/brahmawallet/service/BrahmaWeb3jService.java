package io.brahmaos.wallet.brahmawallet.service;

import android.app.Application;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Array;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Bytes;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.Utf8String;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.ObjectMapperFactory;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthCall;
import org.web3j.protocol.core.methods.response.EthGasPrice;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import io.brahmaos.wallet.brahmawallet.WalletApp;
import io.brahmaos.wallet.brahmawallet.api.Networks;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConfig;
import io.brahmaos.wallet.brahmawallet.common.BrahmaConst;
import io.brahmaos.wallet.brahmawallet.db.entity.AccountEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.AllTokenEntity;
import io.brahmaos.wallet.brahmawallet.db.entity.TokenEntity;
import io.brahmaos.wallet.brahmawallet.model.CryptoCurrency;
import io.brahmaos.wallet.brahmawallet.model.KyberToken;
import io.brahmaos.wallet.util.BLog;
import io.brahmaos.wallet.util.CommonUtil;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import rx.Completable;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class BrahmaWeb3jService extends BaseService{
    private static final int SLEEP_DURATION = 5000;
    private static final int ATTEMPTS = 40;

    @Override
    protected String tag() {
        return BrahmaWeb3jService.class.getName();
    }

    // singleton
    private static BrahmaWeb3jService instance = new BrahmaWeb3jService();
    public static BrahmaWeb3jService getInstance() {
        return instance;
    }

    public String generateLightNewWalletFile(String password, File destinationDirectory)
            throws CipherException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchProviderException, IOException {
        return WalletUtils.generateLightNewWalletFile(password, destinationDirectory);
    }

    public String getWalletAddress(String password, String filePath)
            throws IOException, CipherException {
        Credentials credentials = WalletUtils.loadCredentials(
                password, filePath);
        return credentials.getAddress();
    }

    public boolean isValidKeystore(WalletFile walletFile, String password)
            throws Exception {
        Credentials credentials = Credentials.create(Wallet.decrypt(password, walletFile));
        BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
        BLog.e("viewModel", "the private key is:" + privateKey.toString(16));
        return WalletUtils.isValidPrivateKey(privateKey.toString(16));
    }

    public String prependHexPrefix(String input) {
        return Numeric.prependHexPrefix(input);
    }

    public Request<?, EthGetBalance> getEthBalance(AccountEntity account) {
        Web3j web3 = Web3jFactory.build(
                new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
        return web3.ethGetBalance(account.getAddress(), DefaultBlockParameterName.LATEST);
    }

    public Request<?, EthCall> getTokenBalance(AccountEntity account, TokenEntity token) {
        Web3j web3 = Web3jFactory.build(
                new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
        Function function = new Function(
                "balanceOf",
                Arrays.asList(new Address(account.getAddress())),  // Solidity Types in smart contract functions
                Arrays.asList(new TypeReference<Uint256>(){}));

        String encodedFunction = FunctionEncoder.encode(function);
        return web3.ethCall(
                Transaction.createEthCallTransaction(account.getAddress(), token.getAddress(), encodedFunction),
        DefaultBlockParameterName.LATEST);
    }

    public boolean isValidAddress(String address) {
        return address != null && address.length() >= 2 &&
                address.charAt(0) == '0' && address.charAt(1) == 'x' &&
                WalletUtils.isValidAddress(address);
    }

    /**
     * Initiate a transaction request and
     * issue different events at different stages of the transaction,
     * for example: verifying the account, sending request
     * @return 1: verifying the account 2: sending request 10: transfer success
     */
    public Observable<Object> sendTransfer(AccountEntity account, TokenEntity token, String password,
                                            String destinationAddress, BigDecimal amount,
                                            BigDecimal gasPrice, BigInteger gasLimit, String remark) {
        return Observable.create(e -> {
            try {
                e.onNext(1);
                Web3j web3 = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
                Credentials credentials = WalletUtils.loadCredentials(
                        password, context.getFilesDir() + "/" +  account.getFilename());
                BLog.i(tag(), "load credential success");
                e.onNext(2);
                String transactionHash = "";
                BigDecimal gasPriceWei = Convert.toWei(gasPrice, Convert.Unit.GWEI);
                if (token.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
                    RawTransactionManager txManager = new RawTransactionManager(web3, credentials);
                    EthSendTransaction transactionResponse = txManager.sendTransaction(gasPriceWei.toBigIntegerExact(),
                            gasLimit, destinationAddress, Numeric.toHexString(remark.getBytes()), Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger());
                    if (transactionResponse.hasError()) {
                        throw new RuntimeException("Error processing transaction request: "
                                + transactionResponse.getError().getMessage());
                    }

                    BLog.i(tag(), "remark: "
                            + remark + "; hex remark:" + Numeric.toHexString(remark.getBytes()));

                    transactionHash = transactionResponse.getTransactionHash();
                    BLog.i(tag(), "Transaction begin, view it at https://rinkeby.etherscan.io/tx/"
                            + transactionHash);

                    TransactionReceipt transferReceipt = sendTransactionReceiptRequest(web3, transactionHash);
                    for (int i = 0; i < ATTEMPTS; i++) {
                        if (transferReceipt == null) {
                            Thread.sleep(SLEEP_DURATION);
                            transferReceipt = sendTransactionReceiptRequest(web3, transactionHash);
                        } else {
                            break;
                        }
                    }
                    if (transferReceipt == null) {
                        throw new TransactionException("Transaction receipt was not generated after "
                                + ((SLEEP_DURATION * ATTEMPTS) / 1000
                                + " seconds for transaction: " + transactionHash));
                    }
                    BLog.i(tag(), "Transaction complete, view it at https://rinkeby.etherscan.io/tx/"
                            + transferReceipt.getTransactionHash());
                } else {
                    Function function = new Function(
                            "transfer",
                            Arrays.<Type>asList(new Address(destinationAddress),
                                    new Uint256(amount.multiply(new BigDecimal(Math.pow(10, 18))).toBigInteger())),
                            Collections.<TypeReference<?>>emptyList());
                    String encodedFunction = FunctionEncoder.encode(function);

                    RawTransactionManager txManager = new RawTransactionManager(web3, credentials);
                    EthSendTransaction transactionResponse = txManager.sendTransaction(
                            gasPriceWei.toBigIntegerExact(), gasLimit, token.getAddress(), encodedFunction, BigInteger.ZERO);

                    if (transactionResponse.hasError()) {
                        throw new RuntimeException("Error processing transaction request: "
                                + transactionResponse.getError().getMessage());
                    }
                    transactionHash = transactionResponse.getTransactionHash();
                    BLog.i(tag(), "===> transactionHash: " + transactionHash);
                }
                e.onNext(transactionHash);
                e.onCompleted();
            } catch (IOException | CipherException | TransactionException | InterruptedException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    private TransactionReceipt sendTransactionReceiptRequest(
            Web3j web3j, String transactionHash) throws IOException {
        EthGetTransactionReceipt transactionReceipt =
                web3j.ethGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError()) {
            throw new RuntimeException("Error processing request: "
                    + transactionReceipt.getError().getMessage());
        }

        return transactionReceipt.getTransactionReceipt();
    }

    public Observable<String> getPrivateKeyByPassword(String fileName, String password) {
        return Observable.create(e -> {
            try {
                Credentials credentials = WalletUtils.loadCredentials(
                        password, context.getFilesDir() + "/" +  fileName);
                BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
                if (WalletUtils.isValidPrivateKey(privateKey.toString(16))) {
                    e.onNext(privateKey.toString(16));
                } else {
                    e.onNext("");
                }
            } catch (IOException | CipherException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    public boolean isValidPrivateKey(String privateKey) {
        return WalletUtils.isValidPrivateKey(privateKey);
    }

    public Observable<String> getKeystore(String fileName, String password) {
        return Observable.create(e -> {
            try {
                Credentials credentials = WalletUtils.loadCredentials(
                        password, context.getFilesDir() + "/" +  fileName);
                BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
                BLog.e("viewModel", "the private key is:" + privateKey.toString(16));
                if (WalletUtils.isValidPrivateKey(privateKey.toString(16))) {
                    File file = new File(context.getFilesDir() + "/" +  fileName);
                    FileInputStream fis = new FileInputStream(file);
                    int length = fis.available();
                    byte [] buffer = new byte[length];
                    fis.read(buffer);
                    String keystore = new String(buffer);
                    fis.close();
                    e.onNext(keystore);
                } else {
                    e.onNext("");
                }
            } catch (IOException | CipherException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    /**
     *  Get the hash value of the toke list based on the ReliableTokens contract address
     */
    public Observable<String> getTokensHash() {
        return Observable.create(e -> {
            try {
                Web3j web3 = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                WalletFile walletFile = objectMapper.readValue(BrahmaConst.DEFAULT_KEYSTORE, WalletFile.class);
                Credentials credentials = Credentials.create(Wallet.decrypt("654321", walletFile));

                TransactionManager transactionManager = new RawTransactionManager(web3, credentials);
                Function function = new Function("getLatestFileHash",
                        Arrays.<Type>asList(),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Utf8String>() {}));
                String encodedFunction = FunctionEncoder.encode(function);
                org.web3j.protocol.core.methods.response.EthCall ethCall = web3.ethCall(
                        Transaction.createEthCallTransaction(
                                transactionManager.getFromAddress(), BrahmaConst.RELIABLE_TOKENS_ADDRESS, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .send();

                String contractHashValue = ethCall.getValue();
                List<Type> values = FunctionReturnDecoder.decode(contractHashValue, function.getOutputParameters());
                Utf8String result = null;
                if (!values.isEmpty()) {
                    result = (Utf8String) values.get(0);
                }
                if (result == null) {
                    throw new Exception("Empty value (0x) returned from contract");
                }
                String tokenListIpfsHash = result.toString();
                BLog.i(tag(), "New value stored in remote smart contract: " + tokenListIpfsHash);
                e.onNext(tokenListIpfsHash);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    /**
     * get gas price
     */
    public Observable<BigInteger> getGasPrice() {
        return Observable.create(e -> {
            try {
                Web3j web3j = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
                EthGasPrice ethGasPrice = web3j.ethGasPrice().send();
                e.onNext(ethGasPrice.getGasPrice());
            } catch (IOException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    /**
     *  Get the hash value of the toke list based on the ReliableTokens contract address
     */
    public Observable<List<Uint256>> getExpectedRate(String srcAddress, String destAddress) {
        return Observable.create((Subscriber<? super List<Uint256>> e) -> {
            try {
                Web3j web3 = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));

                String rateContractAddress = BrahmaConst.KYBER_MAIN_NETWORK_ADDRESS;
                if (BrahmaConfig.getInstance().getNetworkUrl().equals(BrahmaConst.ROPSTEN_TEST_URL)) {
                    rateContractAddress = BrahmaConst.KYBER_ROPSTEN_NETWORK_ADDRESS;
                }

                BLog.i(tag(), "the srcAddress is: " + srcAddress);
                BLog.i(tag(), "the destAddress is: " + destAddress);
                Function function = new Function("getExpectedRate",
                        Arrays.<Type>asList(new Address(srcAddress),
                                new Address(destAddress),
                                new Uint256(BigInteger.ONE)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}, new TypeReference<Uint256>() {}));
                String encodedFunction = FunctionEncoder.encode(function);
                org.web3j.protocol.core.methods.response.EthCall ethCall = web3.ethCall(
                        Transaction.createEthCallTransaction(
                                BrahmaConst.TRANSACTION_ACCOUNT_ADDRESS, rateContractAddress, encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .send();

                String rateValue = ethCall.getValue();

                List<Type> values = FunctionReturnDecoder.decode(rateValue, function.getOutputParameters());
                BLog.i(tag(), "the kyber wrapper contract rate origal result : " + rateValue);
                List<Uint256> rateResult = new ArrayList<>();
                for (Type rates : values) {
                    Uint256 rate = (Uint256) rates;
                    BLog.i(tag(), "rate is: " + rate.getValue().toString());
                    rateResult.add(rate);
                }
                e.onNext(rateResult);
            } catch (Exception e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    /**
     * Initiate a instant exchange transaction request and
     * issue different events at different stages of the transaction,
     * for example: verifying the account, sending request
     * @return 1: verifying the account 2: sending request 10: transfer success
     */
    public Observable<Integer> sendInstantExchangeTransfer(AccountEntity account, KyberToken sendToken, KyberToken receiveToken,
                                                           BigDecimal sendAmount, BigDecimal maxReceiveAmount, BigInteger minConversionRate,
                                                           String password, BigDecimal gasPrice, BigInteger gasLimit) {
        return Observable.create(e -> {
            try {
                e.onNext(1);
                Web3j web3 = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
                Credentials credentials = WalletUtils.loadCredentials(
                        password, context.getFilesDir() + "/" +  account.getFilename());
                BLog.i(tag(), "load credential success");
                e.onNext(2);
                BigDecimal gasPriceWei = Convert.toWei(gasPrice, Convert.Unit.GWEI);
                Function function = new Function(
                        "trade",
                        Arrays.<Type>asList(new Address(sendToken.getContractAddress()),
                                new Uint256(CommonUtil.convertWeiFromEther(sendAmount)),
                                new Address(receiveToken.getContractAddress()),
                                new Address(account.getAddress()),
                                new Uint256(CommonUtil.convertWeiFromEther(maxReceiveAmount)),
                                new Uint256(minConversionRate),
                                new Address("0x0000000000000000000000000000000000000000")),
                        Collections.<TypeReference<?>>emptyList());
                String encodedFunction = FunctionEncoder.encode(function);

                String rateContractAddress = BrahmaConst.KYBER_MAIN_NETWORK_ADDRESS;
                if (BrahmaConfig.getInstance().getNetworkUrl().equals(BrahmaConst.ROPSTEN_TEST_URL)) {
                    rateContractAddress = BrahmaConst.KYBER_ROPSTEN_NETWORK_ADDRESS;
                }

                // if send ERC20 Token ,the send values is ZERO
                BigInteger sendValue = CommonUtil.convertWeiFromEther(sendAmount);
                if (!sendToken.getName().toLowerCase().equals(BrahmaConst.ETHEREUM)) {
                    sendValue = BigInteger.ZERO;
                }

                RawTransactionManager txManager = new RawTransactionManager(web3, credentials);
                EthSendTransaction transactionResponse = txManager.sendTransaction(
                        gasPriceWei.toBigIntegerExact(), gasLimit, rateContractAddress, encodedFunction, sendValue);

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Error processing transaction request: "
                            + transactionResponse.getError().getMessage());
                }
                String transactionHash = transactionResponse.getTransactionHash();
                BLog.i(tag(), "===> transactionHash: " + transactionHash);
                e.onNext(10);
            } catch (IOException | CipherException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    /**
     * Initiate contract approve request and
     * issue different events at different stages of the transaction,
     * for example: verifying the account, sending request
     * @return 1: verifying the account 2: sending request 10: transfer success
     */
    public Observable<Integer> sendContractApproveTransfer(AccountEntity account, KyberToken sendToken, BigDecimal sendAmount,
                                                           String password, BigDecimal gasPrice, BigInteger gasLimit) {
        return Observable.create(e -> {
            try {
                e.onNext(1);
                Web3j web3 = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
                Credentials credentials = WalletUtils.loadCredentials(
                        password, context.getFilesDir() + "/" +  account.getFilename());
                BLog.i(tag(), "load credential success");
                e.onNext(2);
                BigDecimal gasPriceWei = Convert.toWei(gasPrice, Convert.Unit.GWEI);

                String kyberContractAddress = BrahmaConst.KYBER_MAIN_NETWORK_ADDRESS;
                if (BrahmaConfig.getInstance().getNetworkUrl().equals(BrahmaConst.ROPSTEN_TEST_URL)) {
                    kyberContractAddress = BrahmaConst.KYBER_ROPSTEN_NETWORK_ADDRESS;
                }

                Function function = new Function(
                        "approve",
                        Arrays.<Type>asList(new Address(kyberContractAddress),
                                new Uint256(CommonUtil.convertWeiFromEther(sendAmount))),
                        Collections.<TypeReference<?>>emptyList());
                String encodedFunction = FunctionEncoder.encode(function);

                RawTransactionManager txManager = new RawTransactionManager(web3, credentials);
                EthSendTransaction transactionResponse = txManager.sendTransaction(
                        gasPriceWei.toBigIntegerExact(), gasLimit, sendToken.getContractAddress(),
                        encodedFunction, BigInteger.ZERO);

                if (transactionResponse.hasError()) {
                    throw new RuntimeException("Error processing transaction request: "
                            + transactionResponse.getError().getMessage());
                }
                String transactionHash = transactionResponse.getTransactionHash();
                BLog.i(tag(), "===> transactionHash: " + transactionHash);
                e.onNext(10);
            } catch (IOException | CipherException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }

    /**
     * Get contract allowance.
     * issue different events at different stages of the transaction,
     * for example: verifying the account, sending request
     * @return Uint: allowance amount
     */
    public Observable<BigInteger> getContractAllowance(AccountEntity account, KyberToken sendToken) {
        return Observable.create(e -> {
            try {
                Web3j web3 = Web3jFactory.build(
                        new HttpService(BrahmaConfig.getInstance().getNetworkUrl()));
                ObjectMapper objectMapper = ObjectMapperFactory.getObjectMapper();
                WalletFile walletFile = objectMapper.readValue(BrahmaConst.DEFAULT_KEYSTORE, WalletFile.class);
                Credentials credentials = Credentials.create(Wallet.decrypt("654321", walletFile));
                TransactionManager transactionManager = new RawTransactionManager(web3, credentials);

                String kyberContractAddress = BrahmaConst.KYBER_MAIN_NETWORK_ADDRESS;
                if (BrahmaConfig.getInstance().getNetworkUrl().equals(BrahmaConst.ROPSTEN_TEST_URL)) {
                    kyberContractAddress = BrahmaConst.KYBER_ROPSTEN_NETWORK_ADDRESS;
                }
                Function function = new Function(
                        "allowance",
                        Arrays.<Type>asList(new Address(account.getAddress()),
                                new Address(kyberContractAddress)),
                        Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
                String encodedFunction = FunctionEncoder.encode(function);

                org.web3j.protocol.core.methods.response.EthCall ethCall = web3.ethCall(
                        Transaction.createEthCallTransaction(
                                transactionManager.getFromAddress(), sendToken.getContractAddress(), encodedFunction),
                        DefaultBlockParameterName.LATEST)
                        .send();

                String enableResult = ethCall.getValue();

                List<Type> values = FunctionReturnDecoder.decode(enableResult, function.getOutputParameters());
                Uint256 allowAmount = (Uint256) values.get(0);
                BLog.i(tag(), "the enabled is :" + allowAmount.getValue());
                e.onNext(allowAmount.getValue());
            } catch (IOException | CipherException e1) {
                e1.printStackTrace();
                e.onError(e1);
            }
            e.onCompleted();
        });
    }
}
