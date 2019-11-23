package com.evoting;

import com.evoting.blockchain.Block;
import com.evoting.blockchain.BlockChain;
import com.evoting.blockchain.SyncBlock;

import java.math.BigInteger;
import java.security.PublicKey;
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

    public void vote(PublicKey receiver) {
        Block block;
        SyncBlock syncBlock = new SyncBlock();
        BlockChain localChain = syncBlock.syncLocal();
        Block lastBlock = localChain.blocks.get(localChain.blocks.size() - 1);
        block = new Block(lastBlock.getHash(), lastBlock.getHeight());
        block.addTransaction(wallet.sendFunds(receiver, 1f, false));
    }

}
