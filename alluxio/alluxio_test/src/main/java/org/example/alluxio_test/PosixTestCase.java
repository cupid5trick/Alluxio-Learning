package org.example.alluxio_test;

import alluxio.client.file.FileSystem;
import alluxio.exception.AlluxioException;
import org.jline.utils.InputStreamReader;

import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class PosixTestCase extends AlluxioTestCaseBase {
// test the time of alluxio POSIX API, should be run on deployed fuse-client.

    public static FileInputStream inStream = null;
    public static BufferedReader br = null;
    public static Boolean manageRecordFile = false;

    public PosixTestCase() {}
    public PosixTestCase(String fileName, String recordFileName) {
        this.fileName = fileName;
        this.recordFileName = recordFileName;
    }
    public PosixTestCase(String fileName, FileOutputStream recordOutput) {
        this.fileName = fileName;
        this.recordOutput = recordOutput;
    }

    public void preRun() throws IOException {

        inStream = new FileInputStream(fileName);
        br = new BufferedReader(new InputStreamReader(inStream));
        if (manageRecordFile) {

            File recordFile = new File(recordFileName);
            if (! recordFile.exists()) {
                recordFile.createNewFile();
            }
            recordOutput = new FileOutputStream(recordFile);
        }
    }

    public void postRun() {
        if (br != null) {
            try {
                br.close();
            }
            catch (IOException e) {
                System.out.println(String.format("Failed to close BufferedReader: %s", e.getMessage()));
            }
        }
        if (manageRecordFile && recordOutput != null) {
            try {
                recordOutput.close();
            }
            catch (IOException e) {
                System.out.println(String.format("Failed to close file %s: %s", recordFileName, e.getMessage()));
            }
        }
    }

    public void run() throws IOException {

        String line = null;
        while (true) {
            line = br.readLine();
            if (line == null) {
                break;
            }
        }
    }

    public String getFileSize() {
        long size = new File(fileName).length();
        return String.format("%f GB", (double) size / (1024*1024*1024));
    }

    public void setManageRecordFile(Boolean manage) {
        this.manageRecordFile = manage;
    }


    public static void main(String[] args) throws IOException, AlluxioException {
        String fileName = "/mnt/alluxio-fuse/alluxio/test-data/giga-1-5.csv";
        String recordFileName = "data/test_record_posixapi.csv";

        if (args.length != 0 && args[0].equals("-n") && args[2].equals("-l")) {
//            Pattern p = Pattern.compile("[^/]*.csv]");
//            Matcher m = p.matcher(fileName);
            fileName = fileName.replaceAll("[^/]*.csv", args[1]);
//            Pattern p2 = Pattern.compile("[^/]*.csv]");
//            Matcher m2 = p2.matcher(recordFileName);
            recordFileName = recordFileName.replaceAll("[^/]*.csv", args[3]);
        }

        System.out.println(String.format("Will output to file %s", recordFileName));
        PosixTestCase testCase = new PosixTestCase(fileName, recordFileName);
        testCase.setManageRecordFile(true);
        testCase.run(10);

    }
}
