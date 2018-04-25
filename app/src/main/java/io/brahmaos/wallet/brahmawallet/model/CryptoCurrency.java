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
    private int rank;
    @JsonProperty("price_usd")
    private double priceUsd;
    @JsonProperty("price_btc")
    private BigDecimal priceBtc;
    @JsonProperty("percent_change_1h")
    private double oneHourChange;
    @JsonProperty("percent_change_24h")
    private double oneDayChange;
    @JsonProperty("percent_change_7d")
    private double sevenDayChange;
    @JsonProperty("price_cny")
    private double priceCny;

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

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
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

    public double getOneHourChange() {
        return oneHourChange;
    }

    public void setOneHourChange(double oneHourChange) {
        this.oneHourChange = oneHourChange;
    }

    public double getOneDayChange() {
        return oneDayChange;
    }

    public void setOneDayChange(double oneDayChange) {
        this.oneDayChange = oneDayChange;
    }

    public double getSevenDayChange() {
        return sevenDayChange;
    }

    public void setSevenDayChange(double sevenDayChange) {
        this.sevenDayChange = sevenDayChange;
    }

    public double getPriceCny() {
        return priceCny;
    }

    public void setPriceCny(double priceCny) {
        this.priceCny = priceCny;
    }

    @Override
    public String toString() {
        return "CryptoCurrency{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", symbol='" + symbol + '\'' +
                ", rank=" + rank +
                ", priceUsd=" + priceUsd +
                ", priceBtc=" + priceBtc +
                ", oneHourChange=" + oneHourChange +
                ", oneDayChange=" + oneDayChange +
                ", sevenDayChange=" + sevenDayChange +
                ", priceCny=" + priceCny +
                '}';
    }
}
