package io.brahmaos.wallet.brahmawallet.model.pay;

import android.support.annotation.NonNull;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.brahmaos.wallet.util.CommonUtil;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PayTransaction implements Serializable, Comparable<PayTransaction>{
    @JsonProperty("id")
    private String orderId;
    @JsonProperty("merchant_order_id")
    private String merchantOrderId;
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
                ", merchant_order_id=" + merchantOrderId +
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

    @Override
    public int compareTo(@NonNull PayTransaction o) {
        return CommonUtil.convertDateTimeStringToLong(
                o.getCreateTime(), "") > CommonUtil.convertDateTimeStringToLong(
                        this.getCreateTime(), "") ? 1 : -1;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantOrderId() {
        return merchantOrderId;
    }

    public void setMerchantOrderId(String merchantOrderId) {
        this.merchantOrderId = merchantOrderId;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }

    public int getCoinCode() {
        return coinCode;
    }

    public void setCoinCode(int coinCode) {
        this.coinCode = coinCode;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(String exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public String getTxAmount() {
        return txAmount;
    }

    public void setTxAmount(String txAmount) {
        this.txAmount = txAmount;
    }

    public int getTxCoinCode() {
        return txCoinCode;
    }

    public void setTxCoinCode(int txCoinCode) {
        this.txCoinCode = txCoinCode;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
