package com.pcauto.main;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.dictionary.py.Pinyin;
import com.pcauto.bktree.BKTree;
import com.pcauto.bktree.DistanceFunction;
import com.pcauto.bktree.DistanceFunctions;
import com.pcauto.component.BKTreeAlgo;
import com.pcauto.component.StringEditMetric;
import com.pcauto.utils.DataHelpers;
import com.pcauto.utils.FileUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {
    private final static String TreeType_HAN2PIN = "TreeType_HAN2PIN";
    private final static String TreeType_PIN2HAN = "TreeType_PIN2HAN";
    private final static String TreeType_HAN = "TreeType_HAN";

    private static DistanceFunction<CharSequence> distFunc = DistanceFunctions.levenshteinDistance();
    private static HashMap<String, String> mapHanToPin = new HashMap<>();
    private static BKTree<CharSequence> tree = null;
    private static String treeType = TreeType_HAN2PIN;

    public static void main(String[] args) {
        System.out.println("Hello World!");

//        test1();

        try {
//            test2();
            testPinyin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void test1() {
        Scanner scanner = new Scanner(System.in);
        BKTreeAlgo<String> bkTree = new BKTreeAlgo<String>(1, new StringEditMetric());
        try {
            bkTree.addAll(DataHelpers.getDictionary());
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (scanner.hasNext()) {
            String word = scanner.next().trim();
            System.out.printf("matching set: %s\n", bkTree.search(word));
        }
    }

    private static void testHan() throws Exception {
        genWordTree();
        long startTime = System.currentTimeMillis();

        String[] testWords = new String[]{
                "探月",
                "宝马擦三",
                "宝妈三系"
        };

        for (String testWord : testWords) {
            search(tree, testWord);
        }
        System.out.println("测试耗时：" + (System.currentTimeMillis() - startTime) + "ms");
    }



    private static void testPinyin() throws Exception {
        genWordTree();

        long startTime = System.currentTimeMillis();
        String[] testWords = new String[]{
                "卡若拉",
                "宝马擦三",
                "宝妈三系",
                "陕西宝马",
                "三系宝妈",
                "本次",
                "帅哥",
                "山西",
                "探月",
                "若拉"
        };

        for (String testWord : testWords) {
            search(tree, testWord);
        }
        System.out.println("测试耗时：" + (System.currentTimeMillis() - startTime) + "ms");
    }


    private static void search(BKTree<CharSequence> tree, String word) {
        System.out.println("'" + word + "'的最相近结果：");
        List<com.pcauto.bktree.BKTree.SearchResult<CharSequence>> results =
                tree.search(getPinyin(word), Math.max(1, word.length() / 4));

        if (results == null || results.size() == 0) {
            System.out.println("没有找到对应的关键词！");
            return;
        }
//        for(com.pcauto.bktree.BKTree.SearchResult<CharSequence> item : results){
        com.pcauto.bktree.BKTree.SearchResult<CharSequence> item = results.get(0);
        if (treeType.equals(TreeType_PIN2HAN)) {
            System.out.println(mapHanToPin.get(item.getItem()));
        } else if (treeType.equals(TreeType_HAN2PIN)) {
            for (String key : mapHanToPin.keySet()) {
                if (mapHanToPin.get(key) == item.getItem()) {
                    System.out.println(key);
                }
            }
        } else {
            System.out.println(mapHanToPin.get(item.getItem()));
        }
    }




    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static void genWordTree() throws Exception {
        tree = new BKTree(distFunc);
        List<String> testStrings = FileUtil.getFileContext("car_dict/user_brand.txt");
        testStrings.addAll(FileUtil.getFileContext("car_dict/user_series.txt"));
        System.out.println("词典条数：" + testStrings.size());
        mapHanToPin = new HashMap<>();
        for (String str : testStrings) {
            if (treeType.equals(TreeType_PIN2HAN)) {
                mapHanToPin.put(getPinyin(str), str);
            } else if (treeType.equals(TreeType_HAN2PIN)) {
                mapHanToPin.put(str, getPinyin(str));
            } else {
                tree.add(str.replace(".", ""));
            }
        }
        System.out.println("拼音词典条数：" + mapHanToPin.size());
        long startTime = System.currentTimeMillis();
        if (treeType.equals(TreeType_PIN2HAN)) {
            for (String testStr : mapHanToPin.keySet()) {
                tree.add(testStr.replace(".", ""));
            }
        } else if (treeType.equals(TreeType_HAN2PIN)) {
            for (String testStr : mapHanToPin.values()) {
                tree.add(testStr.replace(".", ""));
            }
        }
        System.out.println("建树耗时：" + (System.currentTimeMillis() - startTime) + "ms");
    }

    private static String getPinyin(String text) {
        List<Pinyin> pinyinList = HanLP.convertToPinyinList(text);
        StringBuilder stringBuilder = new StringBuilder();
        for (Pinyin pinyin : pinyinList) {
            String py = pinyin.getPinyinWithoutTone();
            if (py == null || py.trim().length() == 0 || "none".equalsIgnoreCase(py)) {
                continue;
            }
            stringBuilder.append(pinyin.getPinyinWithoutTone());
        }
        return stringBuilder.toString();
    }
}
