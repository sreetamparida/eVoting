package com.evoting.blockchain;

import com.evoting.util.CryptoUtil;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;

public class Transaction {
    public String transactionId;
    public PublicKey sender;
    public PublicKey receiver;
    public String sender_key;
    public String receiver_key;
    public float value;
    public byte[] signature;
    private static int sequence = 0;
    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();

    public Transaction(PublicKey from, PublicKey to, float value, ArrayList<TransactionInput> inputs){
        this.sender = from;
        this.receiver = to;
        this.value = value;
        this.inputs = inputs;
        this.sender_key = from.toString();
        this.receiver_key = to.toString();
    }

    public String calculateHash() {
        sequence++; //increase the sequence to avoid 2 identical transactions having the same hash
        return CryptoUtil.applySha256(
                CryptoUtil.getStringFromKey(sender) +
                        CryptoUtil.getStringFromKey(receiver) +
                        Float.toString(value) + sequence
        );
    }

    public void generateSignature(PrivateKey privateKey) {
        String data = CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(receiver) + Float.toString(value)	;
        this.signature = CryptoUtil.applyECDSASig(privateKey,data);
    }

    public boolean verifySignature() {
        String data = CryptoUtil.getStringFromKey(sender) + CryptoUtil.getStringFromKey(receiver) + Float.toString(value)	;
        return CryptoUtil.verifyECDSASig(sender, data, signature);
    }

    public float getInputsValue() {
        float total = 0;
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if com.evoting.blockchain.Transaction can't be found skip it
            total += i.UTXO.value;
        }
        return total;
    }

    public float getOutputsValue() {
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

    public boolean processTransaction() {

        if(!verifySignature()) {
            System.out.println("#com.evoting.blockchain.Transaction Signature failed to verify");
            return false;
        }

        //gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = BlockChain.UTXOs.get(i.transactionOutputId);
        }

        //check if transaction is valid:
        if(getInputsValue() < BlockChain.minimumTransaction) {
            System.out.println("#com.evoting.blockchain.Transaction Inputs to small: " + getInputsValue());
            return false;
        }

        //generate transaction outputs:
        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.receiver, value,transactionId)); //send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver,transactionId)); //send the left over 'change' back to sender

        //add outputs to Unspent list
        for(TransactionOutput o : outputs) {
            BlockChain.UTXOs.put(o.id , o);
        }

        //remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs) {
            if(i.UTXO == null) continue; //if com.evoting.blockchain.Transaction can't be found skip it
            BlockChain.UTXOs.remove(i.UTXO.id);
        }
        return true;
    }
}
