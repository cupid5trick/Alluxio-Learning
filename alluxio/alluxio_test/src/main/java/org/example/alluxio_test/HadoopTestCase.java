package org.example.alluxio_test;

import alluxio.exception.AlluxioException;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HadoopTestCase extends AlluxioTestCaseBase {
    public static FileSystem fs = null;
    public static Boolean manageFileSystem = true;
    public static Boolean manageRecordFile = false;

    public HadoopTestCase() {}
    public HadoopTestCase(String fileName, String recordFileName) {
        fs = getFileSystem();
        this.fileName = fileName;
        this.recordFileName = recordFileName;
    }
    public HadoopTestCase(String fileName, FileOutputStream recordOutput) {
        fs = getFileSystem();
        this.fileName = fileName;
        this.recordOutput = recordOutput;
    }

    public static FileSystem getFileSystem() {
        if (fs == null) {
            Configuration conf = new Configuration();
            conf.set("fs.default.name", "hdfs://192.168.43.134:8020/");
            conf.set("fs.hdfs.impl", org.apache.hadoop.hdfs.DistributedFileSystem.class.getName());
            conf.set("fs.file.impl", org.apache.hadoop.fs.LocalFileSystem.class.getName());

            conf.set("dfs.replication", "1");

            try {
                return FileSystem.get(conf);
            }
            catch (IOException e) {
                System.out.println(String.format("Failed to connect hdfs: %s", e.getMessage()));
            }
        }
        return fs;
    }

    @Override
    public String getFileSize() {
        if (fileSize != null) {
            return fileSize;
        }
        if (fs == null) {
            fs = getFileSystem();
        }
        try {
            long size = fs.getFileStatus(new Path(fileName)).getLen();
            return String.format("%fGB", (double)size / (1024*1024*1024));
        }
        catch (IOException e) {
            System.out.println(String.format("Failed to get size of file %s: %s", fileName, e.getMessage()));
        }
        return null;
    }

    @Override
    public void run() throws IOException{
        FSDataInputStream inputStream = fs.open(new Path(fileName));
        BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
        String line = null;
        while (true)  {
            line = br.readLine();
            if (line == null) {
                break;
            }
        }
        inputStream.close();
        br.close();
    }

    @Override
    public void preRun() throws IOException {
        // only to maintain the status of fs when manageFileSystem=true
        if (manageFileSystem && fs == null) {
            fs = getFileSystem();
        }
        // only to maintain the status of recordFile and recordOutput when manageRecordFile=true
        if (manageRecordFile) {
            File recordFile = new File(recordFileName);
            if (! recordFile.exists()) {
                recordFile.createNewFile();
            }
            if (recordOutput == null) {
                recordOutput = new FileOutputStream(recordFile);
            }
            recordOutput.write((TestRecord.formatCsvHeader()+"\n").getBytes(StandardCharsets.UTF_8));
        }
    }

    @Override
    public void postRun() {
        // only to close fs when manageFileSystem=true
        if (manageFileSystem && fs != null) {
            try {
                fs.close();
            }
            catch (IOException e) {
                System.out.println(String.format("Failed to close AlluxioFileSystem: %s", e.getMessage()));
            }
        }
        // only to close the FileOutputStream when manageRecordFile=true
        if (manageRecordFile && recordOutput != null) {
            try {
                recordOutput.close();
            }
            catch (IOException e) {
                System.out.println(String.format("Failed to close File %s: %s", recordFileName, e.getMessage()));
            }
        }
    }

    public void setManageFileSystem(Boolean manage) {
        this.manageFileSystem = manage;
    }

    public void setManageRecordFile(Boolean manage) {
        this.manageRecordFile = manage;
    }

    public static void main(String[] args) throws IOException, AlluxioException {
        String fileName = "/alluxio/test-data/giga-1-5.csv";
        String recordFileName = "data/test_record_hdpapi.csv";

        if (args.length != 0 && args[0].equals("-n") && args[2].equals("-l")) {
//            Pattern p = Pattern.compile("[^/]*.csv]");
//            Matcher m = p.matcher(fileName);
            fileName = fileName.replaceAll("[^/]*.csv", args[1]);
//            Pattern p2 = Pattern.compile("[^/]*.csv]");
//            Matcher m2 = p2.matcher(recordFileName);
            recordFileName = recordFileName.replaceAll("[^/]*.csv", args[3]);
        }

        System.out.println(String.format("Will output to file %s", recordFileName));
        HadoopTestCase testCase = new HadoopTestCase(fileName, recordFileName);
        testCase.setManageRecordFile(true);
        testCase.run(10);
    }
}
