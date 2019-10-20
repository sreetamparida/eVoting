package com.evoting;

import java.math.BigInteger;
import java.util.ArrayList;

public class Voter {
    public Wallet wallet;
    public String ID;
    private static ArrayList<String> keys = new ArrayList<String>();
    private BigInteger keyShare[];
   public Voter(){
        wallet = new Wallet();

    }

    public void setKeyShare(BigInteger share[]){
        keyShare = share;
    }

    public void vote() {

    }

}
