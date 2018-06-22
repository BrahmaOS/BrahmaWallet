package io.brahmaos.wallet.brahmawallet.api;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

import java.util.Map;

/**
 * Unified API response object
 * The response structure returned by all owned services is unified
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiRespResult {

    // Response result code
    @JsonProperty("ret")
    private int result;

    // Response data
    @JsonProperty("data")
    private Map<String, Object> data;

    // Response message, mainly returned on failure
    @JsonProperty("msg")
    private String msg;

    @Override
    public String toString() {
        return "ApiRespResult{" +
                "result=" + result +
                ", data=" + data +
                ", msg='" + msg + '\'' +
                '}';
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
