package io.brahmaos.wallet.brahmawallet.model;

import org.bitcoinj.core.Peer;

import java.io.Serializable;
import java.util.Date;

public class BitcoinDownloadProgress implements Serializable {
    private Peer peer;
    private int blocksLeft;
    private double progressPercentage;
    private Date currentBlockDate;
    private boolean downloaded;

    public Peer getPeer() {
        return peer;
    }

    public void setPeer(Peer peer) {
        this.peer = peer;
    }

    public int getBlocksLeft() {
        return blocksLeft;
    }

    public void setBlocksLeft(int blocksLeft) {
        this.blocksLeft = blocksLeft;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public Date getCurrentBlockDate() {
        return currentBlockDate;
    }

    public void setCurrentBlockDate(Date currentBlockDate) {
        this.currentBlockDate = currentBlockDate;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    @Override
    public String toString() {
        return "BitcoinDownloadProgress{" +
                "peer=" + peer +
                ", blocksLeft=" + blocksLeft +
                ", progressPercentage=" + progressPercentage +
                ", currentBlockDate=" + currentBlockDate +
                ", downloaded=" + downloaded +
                '}';
    }
}
