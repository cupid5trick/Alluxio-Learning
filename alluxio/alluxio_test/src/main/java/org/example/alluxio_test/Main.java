package org.example.alluxio_test;

import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import java.text.SimpleDateFormat;
import java.util.Date;


public class Main {
//    public static String recordFileName = String.format("data/test-records-%s.csv",
//            new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date()));
    public static String testFileName = "/alluxio/test-data/giga-1-5.csv";
    public static String posixTestFileName = "/mnt/alluxio-fuse/alluxio/test-data/giga-1-5.csv";
    public static String recordFileName = "data/test-records.csv";
//    public static String testFileName = "/LICENSE";
//    public static String posixTestFileName = "/mnt/alluxio-fuse/LICENSE";
    public static Integer n = 100;
    public static void main(String[] args) throws Exception {
        System.out.println(String.format("Will write to file %s", recordFileName));

        FileOutputStream outStream = new FileOutputStream(recordFileName);
        outStream.write((TestRecord.formatCsvHeader()+"\n").getBytes(StandardCharsets.UTF_8));

        SimpleTestCase testCase = new SimpleTestCase(testFileName, outStream);
        testCase.run(n);
//        PosixTestCase posixTestCase = new PosixTestCase(posixTestFileName, outStream);
//        posixTestCase.run(n);
        HadoopTestCase hadoopTestCase = new HadoopTestCase(testFileName, outStream);
        hadoopTestCase.run(n);

        // 关闭测试占用的资源
        testCase.setManageRecordFile(true);
        testCase.postRun();

    }

}
