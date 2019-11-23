package com.evoting;

import com.evoting.blockchain.Block;
import com.evoting.blockchain.BlockChain;
import com.evoting.blockchain.Transaction;
import com.evoting.blockchain.TransactionOutput;

import java.io.IOException;
import java.math.BigInteger;
import java.security.PublicKey;
import java.security.Security;
import java.util.ArrayList;
import java.util.*;
public class Dealer {
    public HashMap<String, Voter> voter;
    public HashMap<String,Candidate> candidate;
    private ArrayList<BigInteger> keys;
    public BigInteger candidateShare[][];
    public Wallet wallet;

    public Dealer(){
        wallet = new Wallet();
        candidate = new HashMap<String, Candidate>();
        voter = new HashMap<String, Voter>();
        keys = new ArrayList<BigInteger>();
    }

    public void generateSecretShare(int noVoters){
        Shamir shamir = new Shamir(1,noVoters);
        candidateShare = new BigInteger[noVoters][this.candidate.size()];
        int i = 0;
        for(String s: candidate.keySet()){
            BigInteger secret = new BigInteger(candidate.get(s).wallet.publicKey.toString().getBytes());
            BigInteger share[] = shamir.split(secret);
            for(int j=0; j<noVoters; j++){
                candidateShare[j][i] = share[j];
            }
            i++;
        }

    }


    public void addVoter(String username){
        voter.put(username, new Voter());
        voter.get(username).setKeyShare(candidateShare[voter.size()-1]);
    }

    public void addVote(String r_uuid, String uuid){
        PublicKey receiver = candidate.get(r_uuid).wallet.publicKey;
        int index = candidate.get(r_uuid).index;
        keys.add(voter.get(uuid).vote(receiver,index));
    }

    public int displayKeyCount(){
        return keys.size();
    }

    public HashMap displayResult(){
        HashMap<String, Integer> votes = new HashMap<String, Integer>();
        for (String s: candidate.keySet()) {
            votes.put(s, (int) candidate.get(s).wallet.getBalance());
        }
        return votes;
    }


}
