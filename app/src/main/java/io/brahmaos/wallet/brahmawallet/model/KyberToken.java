package io.brahmaos.wallet.brahmawallet.model;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class KyberToken implements Serializable, Comparable<KyberToken> {
    private String symbol;
    private String name;
    private String contractAddress;
    private int decimals;
    private BigDecimal currentPrice;
    private BigDecimal lastPrice;
    private long lastTimestamp;
    private BigDecimal baseVolume;
    private BigDecimal quotoVolume;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.currentPrice = currentPrice;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public long getLastTimestamp() {
        return lastTimestamp;
    }

    public void setLastTimestamp(long lastTimestamp) {
        this.lastTimestamp = lastTimestamp;
    }

    public BigDecimal getBaseVolume() {
        return baseVolume;
    }

    public void setBaseVolume(BigDecimal baseVolume) {
        this.baseVolume = baseVolume;
    }

    public BigDecimal getQuotoVolume() {
        return quotoVolume;
    }

    public void setQuotoVolume(BigDecimal quotoVolume) {
        this.quotoVolume = quotoVolume;
    }

    @Override
    public int compareTo(@NonNull KyberToken another) {
        return (another.getSymbol().compareTo(this.getSymbol()) < 0) ? 1 : -1;
    }

    @Override
    public String toString() {
        return "KyberToken{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", contractAddress='" + contractAddress + '\'' +
                ", decimals=" + decimals +
                ", currentPrice=" + currentPrice +
                ", lastPrice=" + lastPrice +
                ", lastTimestamp=" + lastTimestamp +
                ", baseVolume=" + baseVolume +
                ", quotoVolume=" + quotoVolume +
                '}';
    }
}
