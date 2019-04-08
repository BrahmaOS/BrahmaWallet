package io.brahmaos.wallet.brahmawallet.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ReceiverInfo {
    @JsonProperty("receiver")
    private String address;
    @JsonProperty("coin_code")
    private int coinCode;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(int coinCode) {
        this.coinCode = coinCode;
    }

    @Override
    public String toString() {
        return "ReceiverInfo{" +
                "address='" + address + '\'' +
                ", coinCode=" + coinCode +
                '}';
    }
}
