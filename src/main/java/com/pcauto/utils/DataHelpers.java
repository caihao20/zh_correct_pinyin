package com.pcauto.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class DataHelpers {

    public static Set<String> getDictionary() throws IOException{
        Set<String> dictionary = new HashSet<>();
        BufferedReader bufferedReader = new BufferedReader(new FileReader(new File("bktree/src/words")));
        String temp;
        while ((temp = bufferedReader.readLine()) != null){
            dictionary.add(temp);
        }
        bufferedReader.close();
        return dictionary;
    }



}
