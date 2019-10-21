package com.evoting.blockchain;

import com.evoting.Voter;
import com.evoting.Wallet;
import com.google.gson.Gson;
import com.sun.crypto.provider.BlowfishKeyGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.security.Security;
import java.util.ArrayList;

import static spark.Spark.*;

public class Node {

    public static Transaction genesisTransaction;
    // Call the python server exposed for serving the json data
    public static void main(String args[]) throws IOException{
//        System.out.print(args[0]);
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        int nodePort = Integer.parseInt(args[0]);
        port(nodePort);

        //debug get
        get("/hello", ((request, response) -> "Hello World"));

        String chainBlockDir = Config.chainBlockDir;
        File file = new File(chainBlockDir);
        BlockChain blockChain = new BlockChain();
        ArrayList<Block> blocks = new ArrayList<Block>();
        BlockChain localChain;
        SyncBlock syncBlock = new SyncBlock();
        Gson gson = new Gson();

        Wallet coinbase = new Wallet();
        Wallet dealer = new Wallet();
        Block lastBlock;

        try {
            if (!file.exists()) {
                Block genesis = new Block().createGenesisBlock();

                Float coin = 1000f;

                genesisTransaction = new Transaction(coinbase.publicKey, dealer.publicKey, coin, null);
                genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
                genesisTransaction.transactionId = "0"; //manually set the transaction id
                genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
                blockChain.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
                genesis.addTransaction(genesisTransaction);
                blockChain.addBlock(genesis, true);
                blocks.add(genesis);
            }
            Wallet walletA = new Wallet();
            Block newBlock[] = new Block[10];
            System.out.println(dealer.publicKey);
            for (int j = 0; j < 10; j++) {
                localChain = syncBlock.syncLocal();
                System.out.println("size:----"+localChain.blocks.size());
                lastBlock = localChain.blocks.get(localChain.blocks.size() - 1);
                newBlock[j] = new Block(lastBlock.getHash(), lastBlock.getHeight());
                for (int i = 0; i < 5; i++) {
                    newBlock[j].addTransaction(dealer.sendFunds(walletA.publicKey, 1f, false));
                }
                blockChain.addBlock(newBlock[j], true);
                System.out.println("Funds added to : ------------------------------" + (j+1));
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void serveBlock() throws IOException {
        Runtime runtime =  Runtime.getRuntime();
        Process serveProc = runtime.exec("python3 app.py chaindata");
    }
}
