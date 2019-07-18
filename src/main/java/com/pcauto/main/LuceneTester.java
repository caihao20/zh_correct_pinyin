package com.pcauto.main;

import com.pcauto.nlp.lucene_search.Indexer;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

public class LuceneTester {
    String indexDir = "index";
    String dataDir = "data";
    Indexer indexer;

    public static void main(String[] args) {
        LuceneTester tester;
        try {
            tester = new LuceneTester();
            tester.createIndex();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void createIndex() throws IOException{
        indexer = new Indexer(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
//        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        numIndexed = indexer.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        indexer.close();
        System.out.println(numIndexed+" File indexed, time taken: "
                +(endTime-startTime)+" ms");
    }

    class TextFileFilter implements FileFilter{

        public boolean accept(File pathname) {

            return false;
        }
    }
}
