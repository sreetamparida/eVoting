package com.evoting.blockchain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SyncBlock {

    private ArrayList<String> synclocalPaths;
    private BlockChain bc = new BlockChain();;

    public SyncBlock(){
    }

    public ArrayList<String> getSyncPaths(){
        return this.synclocalPaths;
    }

    public BlockChain getBc(){
        return this.bc;
    }

    public Block getObjectBlocks(String path){
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(java.security.PublicKey.class, new IdInstanceCreator());
        Gson gson = builder.create();
//        Gson gson = new Gson();
        try {
            return gson.fromJson(new FileReader(path), Block.class);
        }
        catch (FileNotFoundException e){
            System.out.println(e);
        }
        return null;
    }

    public BlockChain syncLocal(){
        String chainBlockDir = Config.chainBlockDir;
        File file = new File(chainBlockDir);
        Gson gson = new Gson();

        try{
            if(file.exists()){
                System.out.println("Reading chaindata folder");
                try (Stream<Path> walks = Files.walk(Paths.get(chainBlockDir))) {
                    this.synclocalPaths = (ArrayList<String>) walks.filter(Files::isRegularFile)
                            .map(x -> x.toString())
                            .collect(Collectors.toList());
                    int len_path = synclocalPaths.size();
                    String path = "chaindata/"+len_path+".json";

                    bc.addBlock(getObjectBlocks(path), false);

//                    for (int i =1; i<=len_path;i++) {
//                        String path = "chaindata/"+i+".json";
//                        System.out.println(path);
//                        bc.addBlock(getObjectBlocks(path), false);
//                    }

                    return getBc();
                }
            }
            else {
                System.out.println("ERR:::Chaindata folder not present");
                return null;
            }
        }
        catch (Exception e){
            System.out.println("IOException occurred while reading the contents of chaindata folder");
            e.printStackTrace();
        }
        return null;
    }

    public boolean syncPeers(){

        if(syncLocal() != null){
            BlockChain mainChain = bc;
            for(String peers: Config.peers){
                //implement REST and then do this
            }
            return true;
        }
        else{
            System.out.println("Syncing from local failed");
            return false;
        }
    }

    public boolean sync() {
        return syncPeers();
    }
}

class IdInstanceCreator implements InstanceCreator<PublicKey> {
    public java.security.PublicKey createInstance(Type type) {
        return new PublicKey() {

            @Override
            public String getAlgorithm() {
                return null;
            }

            @Override
            public String getFormat() {
                return null;
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }
        };
    }
}
