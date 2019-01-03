package io.brahmaos.wallet.brahmawallet.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Dapp implements Serializable {
    private int icon;
    private String name;
    private String desc;
    private String url;

    public Dapp(int icon, String name, String desc, String url) {
        this.icon = icon;
        this.name = name;
        this.desc = desc;
        this.url = url;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Dapp{" +
                "icon=" + icon +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
