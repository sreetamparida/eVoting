package com.evoting.blockchain;

import com.evoting.util.CryptoUtil;

import java.util.ArrayList;

public class MineBlock {

    public MineBlock(){
    }

    public void mineBlock(Block block, ArrayList<Transaction> transactions) {
        block.getBlockHeader().setMerkleRoot(CryptoUtil.getMerkleRoot(transactions));
        String target = CryptoUtil.getDificultyString(BlockHeader.difficulty);
        int n = block.getBlockHeader().getNonce();
        System.out.println("Inside Mine Block");
        while(!block.getHash().substring( 0, BlockHeader.difficulty).equals(target)) {
            n++;
            block.setHash(CryptoUtil.calculateHash(block.getBlockHeader().getPreviousBlockHash(),
                    block.getBlockHeader().getTimestamp(), n));
        }
        block.getBlockHeader().setNonce(n);
        System.out.println("com.evoting.blockchain.Block Mined!!! : " + block.getHash());
    }
}
