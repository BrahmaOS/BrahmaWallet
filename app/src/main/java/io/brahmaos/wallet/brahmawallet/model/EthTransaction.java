package io.brahmaos.wallet.brahmawallet.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EthTransaction implements Serializable, Comparable<EthTransaction> {
    private long idx;
    private String hash;
    @JsonProperty("block_height")
    private int blockHeight;
    @JsonProperty("from")
    private String fromAddress;
    @JsonProperty("to")
    private String toAddress;
    private BigInteger value;
    private int status;
    @JsonProperty("gas_limit")
    private int gasLimit;
    @JsonProperty("gas_used")
    private int gasUsed;
    @JsonProperty("gas_price")
    private BigInteger gasPrice;
    @JsonProperty("tx_time")
    private long txTime;
    private int nonce;

    public long getIdx() {
        return idx;
    }

    public void setIdx(long idx) {
        this.idx = idx;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(int blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getFromAddress() {
        return fromAddress;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public String getToAddress() {
        return toAddress;
    }

    public void setToAddress(String toAddress) {
        this.toAddress = toAddress;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(int gasLimit) {
        this.gasLimit = gasLimit;
    }

    public int getGasUsed() {
        return gasUsed;
    }

    public void setGasUsed(int gasUsed) {
        this.gasUsed = gasUsed;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public void setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
    }

    public long getTxTime() {
        return txTime;
    }

    public void setTxTime(long txTime) {
        this.txTime = txTime;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    @Override
    public int compareTo(@NonNull EthTransaction another) {
        return another.getTxTime() > this.getTxTime() ? 1 : -1;
    }

    @Override
    public String toString() {
        return "EthTransaction{" +
                "idx=" + idx +
                ", hash='" + hash + '\'' +
                ", blockHeight=" + blockHeight +
                ", fromAddress='" + fromAddress + '\'' +
                ", toAddress='" + toAddress + '\'' +
                ", value=" + value +
                ", status=" + status +
                ", gasLimit=" + gasLimit +
                ", gasUsed=" + gasUsed +
                ", gasPrice=" + gasPrice +
                ", txTime=" + txTime +
                ", nonce=" + nonce +
                '}';
    }
}
