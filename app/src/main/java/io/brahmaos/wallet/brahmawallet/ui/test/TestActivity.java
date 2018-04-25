package io.brahmaos.wallet.brahmawallet.ui.test;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jFactory;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthAccounts;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.core.methods.response.Web3ClientVersion;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.Transfer;
import org.web3j.utils.Convert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Collections;

import io.brahmaos.wallet.brahmawallet.BuildConfig;
import io.brahmaos.wallet.brahmawallet.R;
import io.brahmaos.wallet.brahmawallet.ui.MainActivity;
import io.brahmaos.wallet.contracts.SOT;
import io.brahmaos.wallet.util.BLog;
import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TestActivity extends Activity {
    public static String tag = TestActivity.class.getName();

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private AsyncTask task;
    private AsyncTask contractTask;
    private AsyncTask contractTransactionTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText("hello");

        final Web3j web3 = Web3jFactory.build(
                new HttpService(BuildConfig.NETWORK_BASE_URL)); // defaults to http://localhost:8545/

        final String destinationAddress = "0x5c0525e7e4af221c41a991128eeee64a83026ec0";
        final String currentWalletAddress = "0x76e1ff3d7be7ff088366056f051a9241e0535f7e";
        final String contractAddress = "0x511253935b664db2f78fb1f9aa844ab4d37cc106";

        Credentials credentials = null;
        try {
            credentials = WalletUtils.loadCredentials(
                    "123456",
                    "/data/user/0/io.brahmaos.wallet.brahmawallet/files/UTC--2018-04-25T09-26-59.588--76e1ff3d7be7ff088366056f051a9241e0535f7e.json");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CipherException e) {
            e.printStackTrace();
        }

        final Credentials finalCredentials = credentials;
        task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                /*try {
                    // FIXME: Request some Ether for the Rinkeby test network at https://www.rinkeby.io/#faucet
                    BLog.i(tag, "Sending 1 ETHER ");

                    TransactionReceipt transferReceipt = Transfer.sendFunds(
                            web3, finalCredentials,
                            "0xa0aef994f99594f92b4dcad85e0c6b1178766a8f",  // you can put any address here
                            BigDecimal.ONE, Convert.Unit.ETHER)  // 1 wei = 10^-18 Ether
                            .send();
                    BLog.i(tag, "Transaction complete, view it at https://rinkeby.etherscan.io/tx/"
                            + transferReceipt.getTransactionHash());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CipherException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TransactionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                return true;
            }
        };

        contractTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                /*try {
                    // Now lets deploy a smart contract
                    BLog.i(tag,"Deploying smart contract");
                    SOT contract = SOT.deploy(
                            web3, finalCredentials,
                            ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT).send();

                    String contractAddress = contract.getContractAddress();
                    BLog.i(tag, "Smart contract deployed to address " + contractAddress);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CipherException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (TransactionException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }*/
                return true;
            }
        };

        contractTransactionTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                try {
                    // FIXME: Request some Ether for the Rinkeby test network at https://www.rinkeby.io/#faucet
                    BLog.i(tag, "Sending Contract 1 ETHER ");

//                    Function function = new Function(
//                            "newGreeting",
//                            Arrays.<Type>asList(new org.web3j.abi.datatypes.Utf8String("smart contract transaction")),
//                            Collections.<TypeReference<?>>emptyList());
//                    String encodedFunction = FunctionEncoder.encode(function);
//
//                    BrahmaContract contract = BrahmaContract.load(contractAddress, web3, finalCredentials,
//                            ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
//                    contract.newGreeting("smart contract change greet [10]").send();
//                    BLog.i(tag, "New value stored in remote smart contract: " + contract.greet().send());
//
//                    BLog.i(tag, "Contract Transaction gas price: "
//                            + String.valueOf(ManagedTransaction.GAS_PRICE));

                    //---- BRM ----

                    Function function = new Function(
                            "transfer",
                            Arrays.<Type>asList(new Address(currentWalletAddress),
                                    new Uint256((long) (128 * Math.pow(10, 18)))),
                            Collections.<TypeReference<?>>emptyList());
                    String encodedFunction = FunctionEncoder.encode(function);

                    RawTransactionManager txManager = new RawTransactionManager(web3, finalCredentials);
                    EthSendTransaction transactionResponse = txManager.sendTransaction(
                            ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, contractAddress, encodedFunction, BigInteger.ZERO);
                    if (transactionResponse.hasError()) {
                        throw new RuntimeException("Error processing transaction request: "
                                + transactionResponse.getError().getMessage());
                    }
                    String transactionHash = transactionResponse.getTransactionHash();
                    BLog.i(tag, "===> transactionHash: " + transactionHash);

                    /**
                    // get the next available nonce
                    EthGetTransactionCount ethGetTransactionCount = web3.ethGetTransactionCount(
                            currentWalletAddress, DefaultBlockParameterName.PENDING).send();
                    BigInteger nonce = ethGetTransactionCount.getTransactionCount();

                    BLog.i(tag, "Contract Transaction nonce is: " + String.valueOf(nonce));

                    Transaction transaction = Transaction.createFunctionCallTransaction(
                            currentWalletAddress, nonce, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT, contractAddress,
                            BigInteger.ONE, encodedFunction);

                    org.web3j.protocol.core.methods.response.EthSendTransaction transactionResponse =
                            web3.ethSendTransaction(transaction).sendAsync().get();
                     String transactionHash = transactionResponse.getTransactionHash();
                    */
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };

        // transaction
        Button transactionBtn = findViewById(R.id.btn_transaction);
        transactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task.execute();
            }
        });

        // smart contract
        Button smartContractBtn = findViewById(R.id.btn_smart_contract);
        smartContractBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contractTask.execute();
            }
        });

        // smart contract transaction
        Button smartContractTransactionBtn = findViewById(R.id.btn_smart_contract_transaction);
        smartContractTransactionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contractTransactionTask.execute();
            }
        });

        // button goto wallet
        Button homeBtn = findViewById(R.id.button5);
        homeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TestActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // button goto wallet
        Button drawerBtn = findViewById(R.id.button7);
        drawerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(MainActivity.this, DrawerActivity.class);
                //startActivity(intent);
            }
        });
    }
}
