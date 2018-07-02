package io.brahmaos.wallet.brahmawallet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptoCurrency implements Serializable {
    private String id;
    private String name;
    private String symbol;
    @JsonProperty("usd_price")
    private double priceUsd;
    @JsonProperty("price_btc")
    private BigDecimal priceBtc;
    @JsonProperty("cny_price")
    private double priceCny;
    @JsonProperty("eth_token_addr")
    private String tokenAddress;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public double getPriceUsd() {
        return priceUsd;
    }

    public void setPriceUsd(double priceUsd) {
        this.priceUsd = priceUsd;
    }

    public BigDecimal getPriceBtc() {
        return priceBtc;
    }

    public void setPriceBtc(BigDecimal priceBtc) {
        this.priceBtc = priceBtc;
    }

    public double getPriceCny() {
        return priceCny;
    }

    public void setPriceCny(double priceCny) {
        this.priceCny = priceCny;
    }

    public String getTokenAddress() {
        return tokenAddress;
    }

    public void setTokenAddress(String tokenAddress) {
        this.tokenAddress = tokenAddress;
    }

    @Override
    public String toString() {
        return "CryptoCurrency{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", priceUsd=" + priceUsd +
                ", priceBtc=" + priceBtc +
                ", priceCny=" + priceCny +
                ", tokenAddress='" + tokenAddress + '\'' +
                '}';
    }
}
