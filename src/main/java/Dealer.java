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

    public static void main(String[] args) {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider()); //Setup Bouncey castle as a Security Provider

        Block curr;
        Block prev;
        ArrayList<Block> blocks = new ArrayList<Block>();

        blockChain = new BlockChain();

        System.out.println("Enter number of Candidates");
        Scanner s = new Scanner(System.in);
        int no_candidate = s.nextInt();

        System.out.println("Enter number of Voters");
        int no_voter = s.nextInt();
        voter = new Voter[no_voter];

        Shamir shamir = new Shamir(11,20);
        candidate = new Candidate[no_candidate];
        candidateShare = new BigInteger[no_candidate][];
        for(int i= 0; i< no_candidate; i++){
            candidate[i] = new Candidate();
            candidateShare[i] = new BigInteger[20];

            candidateShare[i] = shamir.split(new BigInteger(candidate[i].wallet.publicKey.toString().getBytes()));

            System.out.println("Key share stored for candidate :" + i);
        }

        Wallet coinbase = new Wallet();
        Wallet dealer = new Wallet();
        Float coin = (float)no_voter;

        genesisTransaction = new Transaction(coinbase.publicKey, dealer.publicKey, coin, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        blockChain.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        Block genesis = new Block("0");
        genesis.addTransaction(genesisTransaction);
        blockChain.addBlock(genesis);
        blocks.add(genesis);

        for(int j=0; j<no_voter;j++){
            voter[j] = new Voter();
            prev = blocks.get(j);
            blocks.add(new Block(prev.hash));
            curr = blocks.get(j+1);
            curr.addTransaction(dealer.sendFunds(voter[j].wallet.publicKey,1f));
            blockChain.addBlock(curr);
            System.out.println("Funds added to voter: " + (j+1));
        }




    }
}
