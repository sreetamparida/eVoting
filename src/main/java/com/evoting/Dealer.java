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
    public static Voter voter[];
    public static Candidate candidate[];
    public static ArrayList<String> keys;
    public static Transaction genesisTransaction;
    public static BlockChain blockChain;
    public static BigInteger candidateShare[][];
    public Wallet wallet;

    public Dealer(){
        wallet = new Wallet();
    }


}
