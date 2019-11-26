package com.evoting;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import spark.ModelAndView;
import spark.template.velocity.VelocityTemplateEngine;
import com.evoting.*;
import com.evoting.blockchain.*;

import java.io.*;
import java.security.Security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.*;

public class Home {
    static int electionid = 0;
    static int flag = 0;
    public static Transaction genesisTransaction;
    public static Dealer dealer;
    public static int index = 0;
    public static Boolean isValidCredentials = true;
    public static Boolean iskeyShareGenerated = false;
    public static String currentUuid = null;
    public static String currentEmail = null;
    public static Boolean isElectionStarted = false;
    public static Boolean isElectionEnded = false;
    public static Boolean isStart = true;




    public static int getid(){
        return electionid++;
    }

    public static void rollBack() throws IOException {
        index = 0;
        String path = System.getProperty("user.dir")+"/src/main/resources/Election/Election.json";
        File electionFile = new File(path);


        if(electionFile.exists()){
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, ArrayList<Map<String, Object>>> electionModel = gson.fromJson(bufferedReader, HashMap.class);
            if(electionModel.get("elections").size()!=0){
                BlockChain blockChain = new BlockChain();
                ArrayList<Block> blocks = new ArrayList<Block>();
                dealer = new Dealer();
                Wallet coinbase = new Wallet();
                dealer.noVoters = Integer.parseInt((String)electionModel.get("elections").get(0).get("numvoters"));

                try {
                    Block genesis = new Block().createGenesisBlock();
                    Float coin = Float.parseFloat((String)electionModel.get("elections").get(0).get("numvoters"));

                    generateGenesis(blockChain, blocks, coinbase, genesis, coin);


                }
                catch (Exception e) {
                    e.printStackTrace();
                }

                path = System.getProperty("user.dir")+"/src/main/resources/Election/Candidates.json";
                File candidateFile = new File(path);
                if(candidateFile.exists()){
                    bufferedReader = new BufferedReader(new FileReader(path));
                    HashMap<String, ArrayList<Map<String,String>>> candidateModel = gson.fromJson(bufferedReader, HashMap.class);
                    for(Map m: candidateModel.get("candidates")){
                        dealer.candidate.put((String) m.get("uuid"), new Candidate(index++));
                        dealer.candidate.get((String) m.get("uuid")).name = (String)m.get("firstname");
                    }


                    path = System.getProperty("user.dir") + "/src/main/resources/Election/Voters.json";
                    File voterFile = new File(path);

                    if(voterFile.exists()){
                        dealer.generateSecretShare();
                        iskeyShareGenerated = true;
                        bufferedReader = new BufferedReader(new FileReader(path));
                        SyncBlock syncBlock = new SyncBlock();
                        HashMap<String, Map<String,String>> voterModel = gson.fromJson(bufferedReader, HashMap.class);
                        Map<String,String> m;
                        for (String email: voterModel.keySet()){
                            m = voterModel.get(email);
                            voterModel.get(email).put("isVoted","false");
                            dealer.addVoter(m.get("uuid"));
                            BlockChain localChain = syncBlock.syncLocal();
                            Block lastBlock = localChain.blocks.get(localChain.blocks.size() - 1);
                            Block block = new Block(lastBlock.getHash(), lastBlock.getHeight());
                            block.addTransaction(dealer.wallet.sendFunds(dealer.voter.get(m.get("uuid")).wallet.publicKey, 1f, false));

                        }
                        try (Writer writer = new FileWriter(path)) {
                            gson = new GsonBuilder().setLenient().create();
                            gson.toJson(voterModel, writer);
                        }


                    }

                }
            };

        }

    }

    public static void flush(){
        String path = System.getProperty("user.dir")+"/src/main/resources/Election/Election.json";
        File file = new File(path);
        if(file.exists()){
            file.delete();
            path = System.getProperty("user.dir")+"/src/main/resources/Election/Candidates.json";
            file = new File(path);
            if(file.exists()){
                file.delete();
            }
            path = System.getProperty("user.dir") + "/src/main/resources/Election/Voters.json";
            file = new File(path);
            if(file.exists()){
                file.delete();
            }
        }
    }

    public static void generateGenesis(BlockChain blockChain, ArrayList<Block> blocks, Wallet coinbase, Block genesis, Float coin) throws IOException {
        genesisTransaction = new Transaction(coinbase.publicKey, dealer.wallet.publicKey, coin, null);
        genesisTransaction.generateSignature(coinbase.privateKey);	 //manually sign the genesis transaction
        genesisTransaction.transactionId = "0"; //manually set the transaction id
        genesisTransaction.outputs.add(new TransactionOutput(genesisTransaction.receiver, genesisTransaction.value, genesisTransaction.transactionId)); //manually add the Transactions Output
        blockChain.UTXOs.put(genesisTransaction.outputs.get(0).id, genesisTransaction.outputs.get(0));
        genesis.addTransaction(genesisTransaction);
        blockChain.addBlock(genesis, true);
        blocks.add(genesis);
    }


    public static void main(String[] args) {

        staticFileLocation("/public");
        SyncBlock syncBlock = new SyncBlock();

        ProcessBuilder process = new ProcessBuilder();
        Integer port;

        if (process.environment().get("PORT") != null) {
            port = Integer.parseInt(process.environment().get("PORT"));
        } else {
            port = 4567;
        }
        port(port);



        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());


        get("/index", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "index.vm");
        }, new VelocityTemplateEngine());


        get("/login", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "login.vm");
        }, new VelocityTemplateEngine());


        post("/login", (request, response) -> {
            String username = request.queryParams("inputEmail");
            String password = request.queryParams("inputPassword");
            System.out.println(username +  " " + password);
            Map<String, Object> model = new HashMap<>();
            if(username.equals("admin@admin.com") && password.equals("admin")){
                response.redirect("/home");
            }else {
                response.redirect("/vote");
            }
            return "ok";
        });

        get("/home", (request, response) -> {
            String path = System.getProperty("user.dir")+"/src/main/resources/Election/Election.json";

            Map<String, ArrayList<Map>> elections = new HashMap<>();

            File file = new File(path);
            if(!file.exists()){
                try (Writer writer = new FileWriter(path)) {
                    elections.put("elections", new ArrayList<>());
                    Gson gson = new GsonBuilder().setLenient().create();
                    gson.toJson(elections, writer);
                }
                System.out.println("File Created");
            }else {
                if(isStart==true){
                    rollBack();
                    isStart=false;
                }

                System.out.println("File  already exists in directory");
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            elections = gson.fromJson(bufferedReader, HashMap.class);
            return new ModelAndView(elections, "home.vm");
        }, new VelocityTemplateEngine());


        post("/addelection", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String title = request.queryParams("title");
            String numvoters = request.queryParams("numvoters");
            String description = request.queryParams("desc");

            model.put("electionid", getid());
            model.put("title", request.queryParams("title"));
            model.put("numvoters", request.queryParams("numvoters"));
            model.put("description", request.queryParams("desc"));


            String path = System.getProperty("user.dir")+"/src/main/resources/Election/Election.json";


            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, ArrayList<Map>> model1 = gson.fromJson(bufferedReader, HashMap.class);
            model1.get("elections").add(model);


            BlockChain blockChain = new BlockChain();
            ArrayList<Block> blocks = new ArrayList<Block>();
            dealer = new Dealer();
            Wallet coinbase = new Wallet();
            dealer.noVoters = Integer.parseInt(request.queryParams("numvoters"));

            try {
                Block genesis = new Block().createGenesisBlock();
                Float coin = Float.parseFloat(request.queryParams("numvoters"));

                generateGenesis(blockChain, blocks, coinbase, genesis, coin);


            }
            catch (Exception e) {
                e.printStackTrace();
            }

            System.out.println(dealer.wallet.getBalance());

            try (Writer writer = new FileWriter(path)) {
                gson = new GsonBuilder().setLenient().create();
                gson.toJson(model1, writer);
            }

            response.redirect("/home");
            return "ok";
        });

        get("/addelection", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            return new ModelAndView(model, "add_election.vm");
        }, new VelocityTemplateEngine());

        get("/addcandidate", (request, response) -> {
            String path = System.getProperty("user.dir")+"/src/main/resources/Election/Election.json";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, ArrayList<Map>> model = gson.fromJson(bufferedReader, HashMap.class);
            return new ModelAndView(model, "add_candidate.vm");
        }, new VelocityTemplateEngine());


        post("/addcandidate", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            String electionSelect = request.queryParams("election_select");

            model.put("firstname", request.queryParams("fname"));
            model.put("lastname", request.queryParams("lname"));
            model.put("election", request.queryParams("election_select"));

            String uuid = UUID.randomUUID().toString();

            model.put("uuid", uuid);
            System.out.println(uuid);

            String path = System.getProperty("user.dir")+"/src/main/resources/Election/Candidates.json";
            dealer.candidate.put(uuid, new Candidate(index++));
            dealer.candidate.get(uuid).name = request.queryParams("fname");
            System.out.println(dealer.candidate.keySet());
            File file = new File(path);
            if(!file.exists()){
                try (Writer writer = new FileWriter(path)) {
                    HashMap<String, ArrayList<Map>> candidates = new HashMap<>();
                    candidates.put("candidates", new ArrayList<>());
                    Gson gson = new GsonBuilder().setLenient().create();
                    gson.toJson(candidates, writer);
                }
                System.out.println("File Created");
            }else {
                System.out.println("File  already exists in directory");
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, ArrayList<Map>> candidates = gson.fromJson(bufferedReader, HashMap.class);
            candidates.get("candidates").add(model);

            try (Writer writer = new FileWriter(path)) {
                System.out.println(candidates);
                gson = new GsonBuilder().setLenient().create();
                gson.toJson(candidates, writer);
            }
            response.redirect("home");
            return "ok";
        });

        get("/election/:name", (request, response) -> {
            String path = System.getProperty("user.dir")+"/src/main/resources/Election/Candidates.json";

            File file = new File(path);
            if(file.exists()){
                BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
                Gson gson = new Gson();
                HashMap<String, Object> model = gson.fromJson(bufferedReader, HashMap.class);
                System.out.println(iskeyShareGenerated);
                model.put("iskeyShareGenerated", iskeyShareGenerated);
                model.put("isElectionStarted", isElectionStarted);
                model.put("isElectionEnded", isElectionEnded);
                model.remove("error");
                return new ModelAndView(model, "election_details.vm");
            }else{
                HashMap<String, Object> model = new HashMap<>();
                model.put("iskeyShareGenerated", iskeyShareGenerated);
                model.put("isElectionStarted", isElectionStarted);
                model.put("isElectionEnded", isElectionEnded);
                model.put("error", "No Candidates added");
                return new ModelAndView(model, "election_details.vm");
            }

        }, new VelocityTemplateEngine());



        get("/registervoter", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            System.out.println(iskeyShareGenerated);
            if(iskeyShareGenerated==false){
                return new ModelAndView(model, "wait.vm");
            }
            return new ModelAndView(model, "register_voter.vm");
        }, new VelocityTemplateEngine());



        get("/wait", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            return new ModelAndView(model, "wait.vm");
        }, new VelocityTemplateEngine());




        post("/registervoter", (request, response) -> {

            Map<String, Object> model = new HashMap<>();
            String email = request.queryParams("email");
            model.put("name", request.queryParams("fname"));
            model.put("email", email);
            model.put("password", request.queryParams("password"));
            model.put("isVoted", "false");

            String path = System.getProperty("user.dir") + "/src/main/resources/Election/Voters.json";
            String uuid = UUID.randomUUID().toString();
            model.put("uuid",uuid);
            System.out.println(uuid);
            System.out.println(dealer.candidateShare.length);
            dealer.addVoter(uuid);

            BlockChain localChain = syncBlock.syncLocal();
            Block lastBlock = localChain.blocks.get(localChain.blocks.size() - 1);
            Block block = new Block(lastBlock.getHash(), lastBlock.getHeight());
            block.addTransaction(dealer.wallet.sendFunds(dealer.voter.get(uuid).wallet.publicKey, 1f, false));
            System.out.println(dealer.voter.get(uuid).wallet.getBalance());

            File file = new File(path);
            if(!file.exists()){
                try (Writer writer = new FileWriter(path)) {
                    HashMap<String, Object> voters = new HashMap<>();
//                    voters.put("voters", new HashMap<String, String>());
                    Gson gson = new GsonBuilder().setLenient().create();
                    gson.toJson(voters, writer);
                }
                System.out.println("File Created");
            }else {
                System.out.println("File  already exists in directory");
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, Object> voters = gson.fromJson(bufferedReader, HashMap.class);
            voters.put(email, model);

            try (Writer writer = new FileWriter(path)) {
                System.out.println(voters);
                gson = new GsonBuilder().setLenient().create();
                gson.toJson(voters, writer);
            }

            System.out.println(voters);
//            return new ModelAndView(model, "voter_login.vm");
            response.redirect("/voterlogin");
            return "ook";
        });

        get("/vote", (request, response) -> {
            String path = System.getProperty("user.dir")+"/src/main/resources/Election/Candidates.json";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, Object> model = gson.fromJson(bufferedReader, HashMap.class);
            model.put("isElectionStarted", isElectionStarted);

            path = System.getProperty("user.dir") + "/src/main/resources/Election/Voters.json";
            bufferedReader = new BufferedReader(new FileReader(path));
            gson = new Gson();
            HashMap<String, Map<String, String>> voters = gson.fromJson(bufferedReader, HashMap.class);


            if(!voters.get(currentEmail).get("isVoted").equals("false")) {
                return new ModelAndView(model,"voting_complete.vm");
            }

            return new ModelAndView(model, "cast_vote.vm");
        }, new VelocityTemplateEngine());


        post("/vote", (request, response) -> {
            String candidate_uuid = request.queryParams("candidate_uuid");
            String voter_uuid = currentUuid;
            String path = System.getProperty("user.dir") + "/src/main/resources/Election/Voters.json";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, Map<String, String>> voters = gson.fromJson(bufferedReader, HashMap.class);

            System.out.println(currentEmail);
            System.out.println(voters);

            if(voters.get(currentEmail).get("isVoted").equals("false")) {
                voters.get(currentEmail).put("isVoted", "true");
                System.out.println(candidate_uuid+"-----------------");
                dealer.addVote(candidate_uuid,voter_uuid);
                System.out.println("voted--------------------");
                try (Writer writer = new FileWriter(path)) {
                    gson = new GsonBuilder().setLenient().create();
                    gson.toJson(voters, writer);
                }
            }
            System.out.println(dealer.displayKeyCount());
            return "ok";
        });

        get("/votecomplete", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            return new ModelAndView(model, "voting_complete.vm");
        }, new VelocityTemplateEngine());


        post("/generatekeyshare", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            System.out.println("Generating Keyshare...");
            dealer.generateSecretShare();
            iskeyShareGenerated = true;
            response.redirect("/home");
            return "ok";
        });

        post("/startelection", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            System.out.println("Starting election...");
            isElectionStarted = true;
            response.redirect("/home");
            return "ok";
        });

        post("/endelection", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            System.out.println("Ending election...");
            isElectionEnded = true;
            response.redirect("/home");
            return "ok";
        });

        get("/voterlogin", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
            model.put("isValidCredentials", isValidCredentials);
            return new ModelAndView(model, "voter_login.vm");
        }, new VelocityTemplateEngine());


        post("/voterlogin", (request, response) -> {
            Map<String, Object> model = new HashMap<>();

            String email = request.queryParams("inputEmail");
            String password = request.queryParams("inputPassword");

            String path = System.getProperty("user.dir") + "/src/main/resources/Election/Voters.json";
            BufferedReader bufferedReader = new BufferedReader(new FileReader(path));
            Gson gson = new Gson();
            HashMap<String, Map<String, String>> voters = gson.fromJson(bufferedReader, HashMap.class);

            Map<String, String> tempemail = voters.get(email);
            if(tempemail !=null){
                System.out.println(tempemail.get("password"));
                if(password.equals(tempemail.get("password"))){
                    isValidCredentials = true;
                    currentUuid = tempemail.get("uuid");
                    currentEmail = tempemail.get("email");
                    if(isElectionEnded){
                        response.redirect("/result");
                        return "ok";
                    }
                    response.redirect("/vote");
                }else{
                    isValidCredentials = false;
                    response.redirect("/voterlogin");
                }
            }
            return "ok";
        });


        get("/result", (request, response) -> {
            Map<String, Object> model = new HashMap<>();
//            model = dealer.displayResult();
            model.put("isElectionEnded", isElectionEnded);
            return new ModelAndView(model,"result.vm");
        }, new VelocityTemplateEngine());

        get("/resultdata", (request, response) -> {
            Map<String, Integer> model = new HashMap<>();
            model = dealer.displayResult();
//            model.put("ss", 30);
//            model.put("ss1", 30);
//            model.put("ss2", 30);
//            model.put("ss3", 30);
            Gson gson = new GsonBuilder().setLenient().create();
            return gson.toJson(model);
        });


        get("/rollback", (request, response) -> {
            try{
                rollBack();
                System.out.println("rollback.....");
            }
            catch(Exception e){
                System.out.println("rollback.....");
                response.redirect("/home");
                return "ok";
            }
            response.redirect("/home");
            return "ok";
        });

        get("/flush", (request, response) -> {
            try{
                flush();
                System.out.println("flushed...");
            }
            catch(Exception e){
                System.out.println("flushed...");
                response.redirect("/home");
                return "ok";
            }
            response.redirect("/home");
            return "ok";
        });
    }
}
