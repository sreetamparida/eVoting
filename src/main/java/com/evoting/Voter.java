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
    public static BigInteger key;
    private BigInteger keyShare[];
   public Voter(){
        wallet = new Wallet();

    }

    public void setKeyShare(BigInteger share[]){
        keyShare = share;
    }

    public BigInteger vote(PublicKey receiver, int index) {
        Block block;
        SyncBlock syncBlock = new SyncBlock();
        BlockChain localChain = syncBlock.syncLocal();
        Block lastBlock = localChain.blocks.get(localChain.blocks.size() - 1);
        block = new Block(lastBlock.getHash(), lastBlock.getHeight());
        block.addTransaction(wallet.sendFunds(receiver, 1f, false));
        this.key = keyShare[index];
        return key;
    }

}
