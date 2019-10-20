package com.evoting.blockchain;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class Config {
    public static String chainBlockDir = "chaindata";
    public static String sharedBlockDir = "bblocs";
    public static int difficulty = 4;
    public static ArrayList<String> peers = (ArrayList<String>) Stream.of(
            "http://localhost:6000/",
            "http://localhost:6001/",
            "http://localhost:6002/",
            "http://localhost:6003/").collect(Collectors.toList());

}
