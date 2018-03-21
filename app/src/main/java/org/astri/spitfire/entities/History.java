package org.astri.spitfire.entities;

import org.litepal.crud.DataSupport;

import java.io.Serializable;
import java.util.Date;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/01/23
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class History extends DataSupport implements Serializable{

    private int readiness;
    private int HR;  // heart rate
    private int HRV; // heart rate variance
    private int SPO2;
    private int GSR;
    private String exercise; //
    private Date testDate; // ?
    private int stimulation;

    private int minHR;
    private int maxHR;
    private int avgHR;
    private int duration; // mins

    private String dateStr;

    private int week; // hist 对应的第几周
    private int month; // hist 对应在第几个月份
    private long testTime; // 测试时间戳

    private String testTimeStr;

    public History() {
    }


    public History(int readiness, int HR, int HRV, int SPO2, int GSR, String exercise, Date testDate) {
        this.readiness = readiness;
        this.HR = HR;
        this.HRV = HRV;

        this.SPO2 = SPO2;
        this.GSR = GSR;
        this.exercise = exercise;
        this.testDate = testDate;
    }

    public int getReadiness() {
        return readiness;
    }

    public void setReadiness(int readiness) {
        this.readiness = readiness;
    }

    public int getHR() {
        return HR;
    }

    public void setHR(int HR) {
        this.HR = HR;
    }

    public int getHRV() {
        return HRV;
    }

    public void setHRV(int HRV) {
        this.HRV = HRV;
    }

    public int getSPO2() {
        return SPO2;
    }

    public void setSPO2(int SPO2) {
        this.SPO2 = SPO2;
    }

    public int getGSR() {
        return GSR;
    }

    public void setGSR(int GSR) {
        this.GSR = GSR;
    }

    public String getExercise() {
        return exercise;
    }

    public void setExercise(String exercise) {
        this.exercise = exercise;
    }

    public Date getTestDate() {
        return testDate;
    }

    public void setTestDate(Date testDate) {
        this.testDate = testDate;
    }
    public int getMinHR() {
        return minHR;
    }

    public void setMinHR(int minHR) {
        this.minHR = minHR;
    }

    public int getMaxHR() {
        return maxHR;
    }

    public void setMaxHR(int maxHR) {
        this.maxHR = maxHR;
    }

    public int getAvgHR() {
        return avgHR;
    }

    public void setAvgHR(int avgHR) {
        this.avgHR = avgHR;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public int getWeek() {
        return week;
    }

    public void setWeek(int week) {
        this.week = week;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public long getTestTime() {
        return testTime;
    }

    public void setTestTime(long testTime) {
        this.testTime = testTime;
    }

    public int getStimulation() {
        return stimulation;
    }

    public void setStimulation(int stimulation) {
        this.stimulation = stimulation;
    }

    public String getTestTimeStr() {
        return testTimeStr;
    }

    public void setTestTimeStr(String testTimeStr) {
        this.testTimeStr = testTimeStr;
    }

    @Override
    public String toString() {
        return "History{" +
                "readiness=" + readiness +
                ", HR=" + HR +
                ", HRV=" + HRV +
                ", SPO2=" + SPO2 +
                ", GSR=" + GSR +
                ", exercise='" + exercise + '\'' +
                ", testDate=" + testDate +
                ", stimulation=" + stimulation +
                ", minHR=" + minHR +
                ", maxHR=" + maxHR +
                ", avgHR=" + avgHR +
                ", duration=" + duration +
                ", dateStr='" + dateStr + '\'' +
                ", week=" + week +
                ", month=" + month +
                ", testTime=" + testTime +
                ", testTimeStr='" + testTimeStr + '\'' +
                '}';
    }
}
