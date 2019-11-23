package com.evoting;

public class Candidate {
    public Wallet wallet;
    public int index;

    public Candidate(int index){
        wallet = new Wallet();
        this.index = index;
    }
}
