package com.evoting.blockchain;

import com.evoting.util.CryptoUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

public class Block {
    private BlockHeader blockHeader;
    private String hash;
    private int height;
    private ArrayList<Transaction> transactions = new ArrayList<Transaction>();

    public Block(){
        //required to instantiate the genesis block
        //required to persist block in BlockChain.addBlock()
    }

    public Block(String previousHash, int prevHeight) {
        if (previousHash != null) {
            //If the block is not a genesis block
            long tms = new Date().getTime();
            this.blockHeader = new BlockHeader(previousHash, tms);

            Random rand = new Random();
            //initialize nonce a random number between 0 to 1000(exclusive)
            blockHeader.setNonce(rand.nextInt(1000));

            this.height = prevHeight + 1;

//            this.hash = CryptoUtil.calculateHash(previousHash, blockHeader.getTimestamp(), blockHeader.getNonce());
        }
    }

    public Block createGenesisBlock(){
            //Settings for genesis block
            long tms = new Date().getTime();
            this.blockHeader = new BlockHeader("0", tms);

            blockHeader.setNonce(0);

            this.height = 1;

//            this.transactions = null;

            this.hash = CryptoUtil.calculateHash("0",
                    blockHeader.getTimestamp(), blockHeader.getNonce());


            return this;
        }

    public int getHeight(){
        return this.height;
    }

    public String getHash(){
        return this.hash;
    }

    public void setHash(String hash){
        this.hash = hash;
    }

    public ArrayList<Transaction> getTransactions(){
        return this.transactions;
    }

    public BlockHeader getBlockHeader(){
        return this.blockHeader;
    }

    public boolean persistBlock(Block block) throws IOException {
        String chainBlockDir = Config.chainBlockDir;
        File file = new File(chainBlockDir);
        Gson gson = new GsonBuilder().serializeNulls().create();

        try{
            file.mkdirs();
            if(file.exists()){
                System.out.println("Chaindata folder created");
                File fileTemp = new File(file,block.height+".json");
                System.out.print(fileTemp.toURI());
                FileWriter fw = new FileWriter(fileTemp);
                gson.toJson(block, fw);
                fw.flush();
                fw.close();
                System.out.print(gson.toJson(block));
                return true;
            }
            else {
                System.out.println("ERR:::Chaindata folder not created");
                return false;
            }
        }
        catch (Exception e){
            System.out.println("IOException occurred while creating chaindata folder");
        }
        return false;
    }

    public boolean addTransaction(Transaction transaction) {
        //process transaction and check if valid, unless block is genesis block then ignore.
        if(transaction == null) return false;
        if((!blockHeader.getPreviousBlockHash().equals("0"))) {
            if((!transaction.processTransaction())) {
                System.out.println("com.evoting.blockchain.Transaction failed to process. Discarded.");
                return false;
            }
        }
        transactions.add(transaction);
        System.out.println("com.evoting.blockchain.Transaction Successfully added to com.evoting.blockchain.Block");
        return true;
    }
}
