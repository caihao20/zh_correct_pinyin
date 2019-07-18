package com.pcauto.nlp.lucene_search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private IndexWriter indexWriter;

    public Indexer(String indexDirectoryPath) throws IOException {
        //创建储存索引的类，储存在磁盘上
        Directory indexDirectory = FSDirectory.open(FileSystems.getDefault().getPath(indexDirectoryPath));
        //中文分析器
        Analyzer analyzer = new StandardAnalyzer();
        //配置IndexWriter
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        //创建IndexWriter，可以对索引进行写操作，负责创建索引和打开已经存在的索引
        indexWriter = new IndexWriter(indexDirectory, config);
    }

    public void close() throws CorruptIndexException, IOException{
        indexWriter.close();
    }

    private Document getDocument(File file) throws IOException{
        //把file转化为string，读取文本，把文本一行行读取到buffer
        StringBuffer buffer = new StringBuffer();
        BufferedReader bf= new BufferedReader(new FileReader(file));
        String s = null;
        while((s = bf.readLine())!=null){
            buffer.append(s.trim());
        }
        String content = buffer.toString();
        //读取完成记得把缓存读取关掉
        bf.close();

        //创建document
        Document document = new Document();
        //配置document中fieldType的参数
        FieldType fieldType = new FieldType();
        //表示要存储到索引中,因为比较短所以将内容，文件名和绝对路径都存储到索引里了
        fieldType.setStored(true);
        //表示文档、词频和 位置都被索引
        fieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        //向document中添加内容，文件名和绝对路径这三个域，才能创建这三者的索引
        document.add(new Field(LuceneConstants.CONTENTS, content,fieldType));
        document.add(new Field(LuceneConstants.FILE_NAME,file.getName(),fieldType));
        document.add(new Field(LuceneConstants.FILE_PATH,file.getCanonicalPath(),fieldType));

        return document;
    }

    private void indexFile(File file) throws IOException{
        System.out.println("Indexing " + file.getCanonicalPath());
        Document document = getDocument(file);
        indexWriter.addDocument(document);
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws IOException{
        //获取data文件夹中的所有文件
        File[] files = new File(dataDirPath).listFiles();

        int count = 0;
        //对每个file创建索引
        for (File file : files) {
            //判断这个file不是目录名，存在，可读且是txt文件
            if(!file.isDirectory() && !file.isHidden() && file.exists() && file.canRead() && filter.accept(file)) {
                indexFile(file);
                count++;
            }
        }
        return count;
    }
}
