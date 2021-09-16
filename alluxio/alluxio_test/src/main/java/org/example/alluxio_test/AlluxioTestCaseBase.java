package org.example.alluxio_test;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileOutStream;
import alluxio.client.file.FileSystem;
import alluxio.client.file.URIStatus;
import alluxio.conf.AlluxioConfiguration;
import alluxio.conf.AlluxioProperties;
import alluxio.conf.InstancedConfiguration;
import alluxio.conf.PropertyKey;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


import alluxio.exception.AlluxioException;
import org.example.alluxio_test.TestRecord;

public abstract class AlluxioTestCaseBase {
    public static Integer n = 100;
    public static String fileSize = null;
    public static String recordFileName = "";
    public static OutputStream recordOutput = null;

    public String fileName;


    public String getFileName() {
        Pattern p = Pattern.compile("([^/]+)$");
        Matcher m = p.matcher(this.fileName);
        try {
            if (m.find()) {
                return m.group();
            }
            else {
                throw new IllegalArgumentException(String.format("Invalid fileName %s", fileName));
            }
        }
        catch (Exception e) {
            throw new IllegalArgumentException(String.format("Invalid fileName %s: %s", fileName, e.getMessage()));
        }
    }


    public void run(Integer n) throws IOException, AlluxioException{
        if (n > 0) {
            preRun();
            for (int i = 0; i < n; ++ i) {
                long t1 = System.currentTimeMillis();
                run();
                long t2 = System.currentTimeMillis();

                TestRecord record = new TestRecord(this, t1, t2);
                System.out.println(record.report());
                recordOutput.write((record.formatCsv()+"\n").getBytes(StandardCharsets.UTF_8));
            }
            postRun();
        }
    }

    public abstract void preRun() throws IOException;
    public abstract void postRun();
    public abstract String getFileSize();
    public abstract void run() throws IOException, AlluxioException;

}
