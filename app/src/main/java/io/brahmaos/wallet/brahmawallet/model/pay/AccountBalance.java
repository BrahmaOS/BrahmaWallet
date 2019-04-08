package io.brahmaos.wallet.brahmawallet.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AccountBalance {
    @JsonProperty("coin_name")
    private String coinName;
    @JsonProperty("balance")
    private String balance;
    @JsonProperty("coin_code")
    private int coinCode;

    public String getCoinName() {
        return coinName;
    }

    public void setCoinName(String coinName) {
        this.coinName = coinName;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public int getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(int coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public String toString() {
        return "AccountBalance{" +
                "coinName='" + coinName + '\'' +
                ", balance='" + balance + '\'' +
                ", coinCode=" + coinCode +
                '}';
    }
}
