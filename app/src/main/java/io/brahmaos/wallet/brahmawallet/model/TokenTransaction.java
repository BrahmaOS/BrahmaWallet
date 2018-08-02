package io.brahmaos.wallet.brahmawallet.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigInteger;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenTransaction implements Serializable, Comparable<TokenTransaction> {
    @JsonProperty("token_addr")
    private String tokenAddress;
    @JsonProperty("from")
    private String fromAddress;
    @JsonProperty("to")
    private String toAddress;
    private BigInteger value;
    @JsonProperty("tx")
    private EthTransaction ethTransaction;

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public EthTransaction getEthTransaction() {
        return ethTransaction;
    }

    public void setEthTransaction(EthTransaction ethTransaction) {
        this.ethTransaction = ethTransaction;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    @Override
    public int compareTo(@NonNull TokenTransaction another) {
        return another.getEthTransaction().getTxTime() > this.getEthTransaction().getTxTime() ? 1 : -1;
    }

    @Override
    public String toString() {
        return "TokenTransaction{" +
                "tokenAddress='" + tokenAddress + '\'' +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", value=" + value +
                ", ethTransaction=" + ethTransaction +
                '}';
    }
}
