package com.evoting.blockchain;

import java.util.Date;

public class BlockHeader {
    public static String version = "0.1";
    private String previousBlockHash;
    private String merkleRoot;
    private long timestamp;
    public static int difficulty = Config.difficulty;
    private int nonce;

    public BlockHeader(String previousBlockHash, long timestamp){
        this.previousBlockHash = previousBlockHash;
        this.timestamp = timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public String getMerkleRoot() {
        return merkleRoot;
    }

    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    public long getTimestamp(){
        return timestamp;
    }
}


