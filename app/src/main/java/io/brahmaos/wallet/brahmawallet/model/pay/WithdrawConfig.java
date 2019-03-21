package io.brahmaos.wallet.brahmawallet.model.pay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class WithdrawConfig {
    @JsonProperty("id")
    private int id;
    @JsonProperty("merchant_id")
    private int merchantId;
    @JsonProperty("name")
    private String name;
    @JsonProperty("value")
    private String value;
    @JsonProperty("description")
    private String description;
    @JsonProperty("create_time")
    private String createTime;
    @JsonProperty("status")
    private int status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "WithdrawConfig{" +
                "id=" + id +
                ", merchantId=" + merchantId +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", description='" + description + '\'' +
                ", createTime='" + createTime + '\'' +
                ", status=" + status +
                '}';
    }
}
