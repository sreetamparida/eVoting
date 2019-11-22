package com.evoting;

import com.evoting.blockchain.Block;
import com.evoting.blockchain.BlockChain;
import com.evoting.blockchain.Transaction;
import com.evoting.blockchain.TransactionOutput;

import java.io.IOException;
import java.math.BigInteger;
import java.security.Security;
import java.util.ArrayList;
import java.util.*;
public class Dealer {
    public ArrayList<Voter> voter;
    public ArrayList<Candidate> candidate;
    public ArrayList<String> keys;
    public Transaction genesisTransaction;
    public BlockChain blockChain;
    public BigInteger candidateShare[][];
    public Wallet wallet;

    public Dealer(){
        wallet = new Wallet();
        candidate = new ArrayList<Candidate>();
        voter = new ArrayList<Voter>();
    }


}
