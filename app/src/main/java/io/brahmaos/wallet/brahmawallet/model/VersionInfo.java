package io.brahmaos.wallet.brahmawallet.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class VersionInfo implements Serializable {
    private int id;
    @JsonProperty("app_id")
    public int appId;
    public int code;
    private String desc;
    public String name;
    @JsonProperty("need_update")
    private int needUpdate;
    public int os;
    @JsonProperty("pkg_sha1")
    private String pkgSHA1;
    @JsonProperty("pkg_size")
    private int pkgSize;
    @JsonProperty("pkg_url")
    private String pkgUrl;
    @JsonProperty("release_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date releaseTime;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAppId() {
        return appId;
    }

    public void setAppId(int appId) {
        this.appId = appId;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNeedUpdate() {
        return needUpdate;
    }

    public void setNeedUpdate(int needUpdate) {
        this.needUpdate = needUpdate;
    }

    public int getOs() {
        return os;
    }

    public void setOs(int os) {
        this.os = os;
    }

    public String getPkgSHA1() {
        return pkgSHA1;
    }

    public void setPkgSHA1(String pkgSHA1) {
        this.pkgSHA1 = pkgSHA1;
    }

    public int getPkgSize() {
        return pkgSize;
    }

    public void setPkgSize(int pkgSize) {
        this.pkgSize = pkgSize;
    }

    public String getPkgUrl() {
        return pkgUrl;
    }

    public void setPkgUrl(String pkgUrl) {
        this.pkgUrl = pkgUrl;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "id=" + id +
                ", appId=" + appId +
                ", code=" + code +
                ", desc='" + desc + '\'' +
                ", name='" + name + '\'' +
                ", needUpdate=" + needUpdate +
                ", os=" + os +
                ", pkgSHA1='" + pkgSHA1 + '\'' +
                ", pkgSize=" + pkgSize +
                ", pkgUrl='" + pkgUrl + '\'' +
                ", releaseTime=" + releaseTime +
                '}';
    }
}
