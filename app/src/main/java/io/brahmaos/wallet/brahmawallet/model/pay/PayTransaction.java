package io.brahmaos.wallet.brahmawallet.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayTransaction {
    @JsonProperty("id")
    private String orderId;
    @JsonProperty("merchant_id")
    private int merchantId;
    @JsonProperty("merchant_name")
    private String merchantName;
    @JsonProperty("type")
    private int orderType;
    @JsonProperty("status")
    private int orderStatus;
    @JsonProperty("coin_code")
    private int coinCode;
    @JsonProperty("amount")
    private String amount;
    @JsonProperty("exchange_rate")
    private String exchangeRate;
    @JsonProperty("tx_amount")
    private String txAmount;
    @JsonProperty("tx_coin_code")
    private int txCoinCode;
    @JsonProperty("tx_hash")
    private String txHash;
    @JsonProperty("create_time")
    private String createTime;
    @JsonProperty("desc")
    private String desc;

    @Override
    public String toString() {
        return "PayTransaction{" +
                "id=" + orderId +
                ", merchant_id=" + merchantId +
                ", merchant_name=" + merchantName +
                ", type=" + orderType +
                ", status=" + orderStatus +
                ", coin_code=" + coinCode +
                ", amount=" + amount +
                ", exchange_rate=" + exchangeRate +
                ", tx_amount=" + txAmount +
                ", tx_coin_code=" + txCoinCode +
                ", tx_hash=" + txHash +
                ", create_time=" + createTime +
                ", desc=" + desc +
                "}";
    }
}
