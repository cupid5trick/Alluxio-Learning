package org.example.alluxio_test;

import java.util.Date;
import java.time.Duration;
import java.text.SimpleDateFormat;

import org.example.alluxio_test.AlluxioTestCaseBase;


public class TestRecord {
    public String fileName;
    public String size;
    public String timeStamp;
    public String duration;
    public String caseName;

    public TestRecord() {}
    public TestRecord(AlluxioTestCaseBase caseObject, long beginTimeMillis, long endTimeMillis) {
        this.fileName = caseObject.getFileName();
        this.size = caseObject.getFileSize();
        this.setTimeStamp(beginTimeMillis);
        this.setDuration(beginTimeMillis, endTimeMillis);
        this.setCaseName(caseObject);
    }

    public void setTimeStamp(long timeMillis) {
        this.timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS").format(new Date(timeMillis));
    }

    public void setDuration(long beginTimeMillis, long endTimeMillis) {
        double seconds = (double)(endTimeMillis - beginTimeMillis) / 1000;
        this.duration = String.format("%fseconds", seconds);
    }

    public void setCaseName(AlluxioTestCaseBase caseObject) {
        this.caseName = caseObject.getClass().getName();
    }

    public String report() {
        return String.format("[%s] %s %s %s %s", this.timeStamp, this.caseName, this.duration, this.size, this.fileName);
    }

    public String formatCsv() {
        return String.format("%s,%s,%s,%s,%s", this.timeStamp, this.caseName, this.duration, this.size, this.fileName);
    }

    public static String formatCsvHeader() {
        return String.format("%s,%s,%s,%s,%s", "TimeStamp", "CaseName", "Duration", "Size", "FileName");
    }
}
