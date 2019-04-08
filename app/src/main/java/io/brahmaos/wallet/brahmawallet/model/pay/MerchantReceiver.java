package io.brahmaos.wallet.brahmawallet.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MerchantReceiver {
    @JsonProperty("merchant_info")
    private Merchant merchant;
    @JsonProperty("receiver_info")
    private ReceiverInfo receiver;

    public Merchant getMerchant() {
        return merchant;
    }

    public void setMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public ReceiverInfo getReceiver() {
        return receiver;
    }

    public void setReceiver(ReceiverInfo receiver) {
        this.receiver = receiver;
    }

    @Override
    public String toString() {
        return "MerchantReceiver{" +
                "merchant=" + merchant +
                ", receiver=" + receiver +
                '}';
    }
}
