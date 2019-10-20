package com.evoting.blockchain;

import com.evoting.util.CryptoUtil;

import java.security.PublicKey;

public class TransactionOutput {
    public String id;
    public PublicKey receiver; //also known as the new owner of these coins.
    public float value; //the amount of coins they own
    public String parentTransactionId; //the id of the transaction this output was created in

    //Constructor
    public TransactionOutput(PublicKey receiver, float value, String parentTransactionId) {
        this.receiver = receiver;
        this.value = value;
        this.parentTransactionId = parentTransactionId;
        this.id = CryptoUtil.applySha256(CryptoUtil.getStringFromKey(receiver)+Float.toString(value)+parentTransactionId);
    }

    //Check if coin belongs to you
    public boolean isMine(PublicKey publicKey) {
        return (publicKey == receiver);
    }
}
