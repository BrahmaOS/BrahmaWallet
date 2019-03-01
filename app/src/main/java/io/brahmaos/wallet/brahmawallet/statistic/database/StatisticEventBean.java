package io.brahmaos.wallet.brahmawallet.statistic.database;

public class StatisticEventBean {
    private long mTime = 0;
    private String mType = null;
    private String mCompName = null;
    private String mCompId = null;

    public StatisticEventBean(){
    }

    public void setTime(long time) {
        mTime = time;
    }

    public void setType(String type) {
        mType = type;
    }

    public void setCompName(String compName) {
        mCompName = compName;
    }

    public void setCompId(String compId) {
        mCompId = compId;
    }

    public long getTime() {
        return mTime;
    }

    public String getType() {
        return mType;
    }

    public String getCompName() {
        return mCompName;
    }

    public String getCompId() {
        return mCompId;
    }

    public String toString() {
        return "time:" + mTime + "; type:" + mType + "; compName:" + mCompName + "; compId:" + mCompId;
    }
}
