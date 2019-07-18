package com.pcauto.nlp.lucene_search;

import com.pcauto.utils.FileUtil;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;

/**
 *
 * 通过索引字段来读取文档
 *
 */
public class Searcher {

    public static void search(String indexDir,String q)throws Exception{

        //得到索引文件存储目录
        Directory dict = FSDirectory.open(FileSystems.getDefault().getPath(indexDir));
        //创建索引读取器
        IndexReader reader = DirectoryReader.open(dict);
        //创建索引搜索器
        IndexSearcher searcher = new IndexSearcher(reader);

        //设置相关性排序为TF/IDF 排序
        ClassicSimilarity tf_idf = new ClassicSimilarity();
        searcher.setSimilarity(tf_idf);

        //创建分析器，要跟前面创建索引用到的分析器相同，否则会出错
        Analyzer analyzer = new SmartChineseAnalyzer();

        //建立查询解析器，第一参数是查询的field, 第二个参数是分析器
        QueryParser parser = new QueryParser("contents", analyzer);

        //根据传进来的字符串q查找
        Query query = parser.parse(q);

        //检索开始时间
        long start = System.currentTimeMillis();

        //查询得到搜索命中的结果集，第一个参数是QueryParser生成的Parser，第二个参数是ScoreDos的最大文件数
        TopDocs hits = searcher.search(query, 10);

        //检索结束时间
        long end=System.currentTimeMillis();

        System.out.println("检索 " + q + " ，总共花费" + (end - start) + "毫秒" + "查询到" + hits.totalHits + "个记录");

        //遍历hits中的top-n文件集，也就是ScoreDoc
        for(ScoreDoc scoreDoc:hits.scoreDocs){
            //通过文件的序号找到文件所在路径
            Document doc = searcher.doc(scoreDoc.doc);
            System.out.println(doc.get(LuceneConstants.FILE_PATH) + " 相关性是：" + String.valueOf(scoreDoc.score));
        }

        //关闭reader
        reader.close();
    }

    public static SuggestWord[] checkWord(String queryWord){
        //索引目录
        String indexDir = FileUtil.ResourcePath +"index";

        //拼写检查
        try {
            //目录
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(indexDir));
            //创建拼写检查器，利用原本有的索引
            DirectSpellChecker checker = new DirectSpellChecker();
            //以下几步用来初始化索引
            IndexReader reader = DirectoryReader.open(directory);

            //从输入的string创建查询条目
            Term term = new Term(LuceneConstants.CONTENTS, queryWord);

            //获取最相近的前5个词
            int numSug = 5;
            SuggestWord[] suggestions = checker.suggestSimilar(term, numSug, reader);
            reader.close();
            directory.close();
            return suggestions;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    //测试
    public static void main(String[] args) throws IOException {
        String indexDir= FileUtil.ResourcePath +  "index";
        // 处理输入
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String str = null;
        System.out.println("请输入你要搜索的关键词:");
        try {
            str = br.readLine();
            System.out.println();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        // 拼写检查
        String temp = str;
        SuggestWord[] suggestions = checkWord(str);
        if (suggestions != null && suggestions.length != 0){
            System.out.println("你想输入的可能是:");
            for(int i = 0; i < suggestions.length; i++){
                System.out.println((i+1) + " : " + suggestions[i].string);
            }

            System.out.println("请选择上面的一个正确的关键词(输入 1 ~ 5)，或继续原词(输入0)进行搜索:");
            str = br.readLine();
            System.out.println();

            //判断如果输入0，就按原词搜索；如果输入其它，判断是否在1~5范围内
            if (str == "0"){
                str = temp;
            }
            else {
                boolean right = false;
                for (int i = 1; i <= 5; i++) {
                    if (str == String.valueOf(i)) {
                        right = true;
                    }
                }
                if (right) {
                    str = suggestions[str.charAt(0) - '1'].string;
                }
                else {
                    System.out.println("请输入 1 ~ 5之间的数字进行搜索");
                }
            }
        }

        try {
            search(indexDir, str);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
