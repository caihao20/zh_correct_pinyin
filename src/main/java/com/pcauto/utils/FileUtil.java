package com.pcauto.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class FileUtil {
    public static final String ResourcePath = "./src/main/resources/";

    public static List<String> getFileContext(String path) throws Exception {
        path = ResourcePath + path;
        FileReader fileReader = new FileReader(path);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        List<String> list = new ArrayList<String>();
        String str = null;
        while ((str = bufferedReader.readLine()) != null) {
            if (str.trim().length() > 0) {
                list.add(str.replace("#",""));
            }
        }
        return list;
    }

    /**
     * 读取文件内容，作为字符串返回
     */
    public static String readFileAsString(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        }

        if (file.length() > 1024 * 1024 * 1024) {
            throw new IOException("File is too large");
        }

        StringBuilder sb = new StringBuilder((int) (file.length()));
        // 创建字节输入流
        FileInputStream fis = new FileInputStream(filePath);
        // 创建一个长度为10240的Buffer
        byte[] bbuf = new byte[10240];
        // 用于保存实际读取的字节数
        int hasRead = 0;
        while ((hasRead = fis.read(bbuf)) > 0) {
            sb.append(new String(bbuf, 0, hasRead));
        }
        fis.close();
        return sb.toString();
    }

    /**
     * 根据文件路径读取byte[] 数组
     */
    public static byte[] readFileByBytes(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException(filePath);
        } else {
            ByteArrayOutputStream bos = new ByteArrayOutputStream((int) file.length());
            BufferedInputStream in = null;

            try {
                in = new BufferedInputStream(new FileInputStream(file));
                short bufSize = 1024;
                byte[] buffer = new byte[bufSize];
                int len1;
                while (-1 != (len1 = in.read(buffer, 0, bufSize))) {
                    bos.write(buffer, 0, len1);
                }

                byte[] var7 = bos.toByteArray();
                return var7;
            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException var14) {
                    var14.printStackTrace();
                }

                bos.close();
            }
        }
    }

    public static void writeToText(String dir, String name, List<String> contents) {
        try {
            File csv = createFile(dir, "_" + name);

            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(csv, true), "UTF-8"));
            for (int i = 0; i < contents.size(); i++) {
                String cc = contents.get(i);
                if (null == cc || "".equals(cc) || "".equals(cc.trim())) {
                    continue;
                }
                bufferedWriter.write(cc);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            // TODO: handle exception
        }
    }

    private static File createFile(String dir, String name) {
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHH");
        String folderName = format.format(date);
        File folder = new File(dir + folderName);
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        }
        SimpleDateFormat format2 = new SimpleDateFormat("yyyyMMddHHmm");
        String fileName = format2.format(date) + name + ".txt";
        System.out.println(folder.getAbsolutePath() + "/" + fileName);
        File csv = new File(folder.getAbsolutePath() + "/" + fileName);
        if (!csv.exists()) {
            try {
                csv.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return csv;
    }
}
