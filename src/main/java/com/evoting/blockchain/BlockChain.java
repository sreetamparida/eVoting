package com.evoting.blockchain;

import com.evoting.util.CryptoUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BlockChain {
    public ArrayList<Block> blocks = new ArrayList<Block>();
    public static Transaction genesisTransaction;
    public static HashMap<String, TransactionOutput> UTXOs = new HashMap<String,TransactionOutput>();
    public static float minimumTransaction = 0.1f;

    public BlockChain(){
    }

    public static Boolean isChainValid() {
        Block currentBlock;
        Block previousBlock;
        String hashTarget = new String(new char[Config.difficulty]).replace('\0', '0');
        //a temporary working list of unspent transactions at a given block state.
        HashMap<String,TransactionOutput> tempUTXOs = new HashMap<String,TransactionOutput>();
        tempUTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        BlockChain b = new BlockChain();
        //loop through blockchain to check hashes:
        for(int i=1; i < b.blocks.size(); i++) {

            currentBlock = b.blocks.get(i);
            previousBlock = b.blocks.get(i-1);
            //compare registered hash and calculated hash:
            if(!currentBlock.getHash().equals(CryptoUtil.calculateHash(currentBlock.getBlockHeader().getPreviousBlockHash(),
                    currentBlock.getBlockHeader().getTimestamp(),
                    currentBlock.getBlockHeader().getNonce()))) {

                System.out.println("#Current Hashes not equal");
                return false;
            }
            //compare previous hash and registered previous hash
            if(!previousBlock.getHash().equals(currentBlock.getBlockHeader().getPreviousBlockHash()) ) {
                System.out.println("#Previous Hashes not equal");
                return false;
            }
            //check if hash is solved
            if(!currentBlock.getHash().substring(0, Config.difficulty).equals(hashTarget)) {
                System.out.println("#This block hasn't been mined");
                return false;
            }

            //loop through blockchains transactions:
            TransactionOutput tempOutput;
            for(int t=0; t <currentBlock.getTransactions().size(); t++) {
                Transaction currentTransaction = currentBlock.getTransactions().get(t);

                if(!currentTransaction.verifySignature()) {
                    System.out.println("#Signature on com.evoting.blockchain.Transaction(" + t + ") is Invalid");
                    return false;
                }
                if(currentTransaction.getInputsValue() != currentTransaction.getOutputsValue()) {
                    System.out.println("#Inputs are note equal to outputs on com.evoting.blockchain.Transaction(" + t + ")");
                    return false;
                }

                for(TransactionInput input: currentTransaction.inputs) {
                    tempOutput = tempUTXOs.get(input.transactionOutputId);

                    if(tempOutput == null) {
                        System.out.println("#Referenced input on com.evoting.blockchain.Transaction(" + t + ") is Missing");
                        return false;
                    }

                    if(input.UTXO.value != tempOutput.value) {
                        System.out.println("#Referenced input com.evoting.blockchain.Transaction(" + t + ") value is Invalid");
                        return false;
                    }

                    tempUTXOs.remove(input.transactionOutputId);
                }

                for(TransactionOutput output: currentTransaction.outputs) {
                    tempUTXOs.put(output.id, output);
                }

                if( currentTransaction.outputs.get(0).receiver != currentTransaction.receiver) {
                    System.out.println("#com.evoting.blockchain.Transaction(" + t + ") output recipient is not who it should be");
                    return false;
                }
                if( currentTransaction.outputs.get(1).receiver != currentTransaction.sender) {
                    System.out.println("#com.evoting.blockchain.Transaction(" + t + ") output 'change' is not sender.");
                    return false;
                }

            }
        }
        System.out.println("Blockchain is valid");
        return true;
    }

    public void addBlock(Block newBlock, boolean mine) throws IOException {
        if(mine) {
            MineBlock mineDriver = new MineBlock();
            mineDriver.mineBlock(newBlock, newBlock.getTransactions());
            blocks.add(newBlock);
            Block b = new Block();
            try {
                if (b.persistBlock(newBlock))
                    System.out.println(newBlock.getHeight() + " block persisted");
                else
                    System.out.println(newBlock.getHeight() + " block persist failed");
            } catch (Exception e) {
                System.out.println("IOException occurred");
            }
        }
        else{
            blocks.add(newBlock);
            Block b = new Block();
            if(newBlock.getHeight() == 1)
                System.out.println("Genesis block to be persisted");
                b.persistBlock(newBlock);
        }
    }
}
