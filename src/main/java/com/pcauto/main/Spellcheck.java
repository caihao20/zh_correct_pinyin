package com.pcauto.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.util.Iterator;

import com.pcauto.utils.FileUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.PlainTextDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.suggest.InputIterator;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Spellcheck {
    public static String directorypath;
    public static String origindirectorypath;
    public SpellChecker spellcheck;
    public LuceneDictionary dict;

    /**
     * 创建索引
     * a
     * @return
     * @throws IOException
     * boolean
     */
    public static void createIndex(String directorypath,String origindirectorypath) throws IOException
    {
        Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(directorypath));
        //中文分析器
        Analyzer analyzer = new SmartChineseAnalyzer();
        SpellChecker spellchecker = new SpellChecker(directory);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        PlainTextDictionary pdic = new PlainTextDictionary(new InputStreamReader(new FileInputStream(new File(origindirectorypath)),"utf-8"));
        spellchecker.indexDictionary(new PlainTextDictionary(FileSystems.getDefault().getPath(origindirectorypath)), config, false);
        directory.close();
        spellchecker.close();
    }
    public Spellcheck(String opath ,String path)
    {
        origindirectorypath = opath;
        directorypath = path;
        Directory directory;
        try {
            directory = FSDirectory.open(FileSystems.getDefault().getPath(directorypath));
            spellcheck = new SpellChecker(directory);
            IndexReader oriIndex = DirectoryReader.open(directory);
            dict = new LuceneDictionary(oriIndex,"name");
        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void setAccuracy(float v)
    {
        spellcheck.setAccuracy(v);
    }


    public String[]search(String queryString, int suggestionsNumber)
    {
        String[]suggestions = null;
        try {
            if (exist(queryString))
                return null;
            suggestions = spellcheck.suggestSimilar(queryString,suggestionsNumber);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return suggestions;
    }

    private boolean exist(String queryString) throws IOException {
        InputIterator ite =  dict.getEntryIterator();
        while (ite.hasContexts())
        {
            if (ite.next().equals(queryString))
                return true;
        }
        return false;
    }


    public static void main(String[] args) throws IOException {
        String opath = FileUtil.ResourcePath+ "car_dict/user_series.txt";
        String ipath = FileUtil.ResourcePath+"index";
        Spellcheck.createIndex(ipath, opath);
        Spellcheck spellcheck = new Spellcheck(opath,ipath);
        //spellcheck.createSpellIndex();

        spellcheck.setAccuracy((float) 0.5);
        String [] result = spellcheck.search("宝妈三溪", 15);
        if(result.length==0||null==result)
        {
            System.out.println("未发现错误");
        }
        else
        {
            System.out.println("你是不是要找:");
            for(String hit:result)
            {
                System.out.println(hit);
            }
        }
    }
}
