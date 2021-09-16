package org.example.alluxio_test;

import alluxio.AlluxioURI;
import alluxio.client.file.FileInStream;
import alluxio.client.file.FileSystem;
import alluxio.client.file.URIStatus;
import alluxio.conf.AlluxioConfiguration;
import alluxio.conf.AlluxioProperties;
import alluxio.conf.InstancedConfiguration;
import alluxio.conf.PropertyKey;
import alluxio.exception.AlluxioException;
import javassist.Loader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleTestCase extends AlluxioTestCaseBase {
    public static FileSystem fs = null;
    public static Boolean manageFileSystem = true;
    public static Boolean manageRecordFile = false;

    public SimpleTestCase() {}

    public SimpleTestCase(String fileName, String recordFileName) {
        this.fs = getFileSystem();
        this.fileName = fileName;
        this.recordFileName = recordFileName;
    }

    public SimpleTestCase(String fileName, FileOutputStream recordOutput) {
        this.fs = getFileSystem();
        this.fileName = fileName;
        this.recordOutput = recordOutput;
    }

    public static FileSystem getFileSystem() {
        if (fs == null) {
            AlluxioProperties prop = new AlluxioProperties();
            prop.set(PropertyKey.MASTER_HOSTNAME, "192.168.43.135");
            prop.set(PropertyKey.SECURITY_LOGIN_USERNAME, "alluxio");

            AlluxioConfiguration conf = new InstancedConfiguration(prop);
            return FileSystem.Factory.create(conf);
        }
        return fs;
    }

    public String getFileSize() {
        if (fileSize != null) {
            return fileSize;
        }
        if (fs == null) {
            fs = getFileSystem();
        }
        try {
            URIStatus status = fs.getStatus(new AlluxioURI(fileName));
            return String.format("%fGB", (double) status.getLength() / (1024*1024*1024));
        }
        catch (Exception e) {
            System.out.println(String.format("Failed to get fileSize: %s", e.getMessage()));
        }
        return null;
    }

    public void run() throws IOException, AlluxioException {
        FileInStream istream = fs.openFile(new AlluxioURI(this.fileName));

        BufferedReader br = new BufferedReader(new InputStreamReader(istream, StandardCharsets.UTF_8));
        String line = null;
        while (true) {
            line = br.readLine();
            if (line == null) {
                break;
            }
        }
        istream.close();
        br.close();
//        try {
//        } catch (Exception e) {
//            throw new IOException(String.format("Error when opening file %s: %s", fileName, e.getMessage()));
//        }
    }

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
        String recordFileName = "data/test_record_alluxioapi.csv";
        System.out.println(args.length);
        System.out.println(String.join(" ", args));

        if (args.length != 0 && args[0].equals("-n") && args[2].equals("-l")) {
//            Pattern p = Pattern.compile("[^/]*.csv]");
//            Matcher m = p.matcher(fileName);
            fileName = fileName.replaceAll("[^/]*.csv", args[1]);
//            Pattern p2 = Pattern.compile("[^/]*.csv]");
//            Matcher m2 = p2.matcher(recordFileName);
            recordFileName = recordFileName.replaceAll("[^/]*.csv", args[3]);
        }

        System.out.println(String.format("Will output to file %s", recordFileName));
        SimpleTestCase testCase = new SimpleTestCase(fileName, recordFileName);
        testCase.setManageRecordFile(true);
        testCase.run(10);

    }
}
