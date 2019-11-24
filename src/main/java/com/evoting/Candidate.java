package com.evoting;

public class Candidate {
    public Wallet wallet;
    public int index;
    public String name;

    public Candidate(int index){
        wallet = new Wallet();
        this.index = index;
    }
}
