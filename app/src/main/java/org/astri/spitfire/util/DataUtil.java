package org.astri.spitfire.util;


import android.util.Log;

import com.blankj.utilcode.util.TimeUtils;

import org.astri.spitfire.entities.History;
import org.astri.spitfire.entities.User;
import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.astri.spitfire.util.Constants.FMT_HM;
import static org.astri.spitfire.util.Constants.FMT_HMS;

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
public class DataUtil {

    private static final String TAG = "DataUtil";




    /**
     * according to prototype, we generate some fake data.
     * generate History data
     * @return historyList
     */
    public static List<History> genFakeHistoryData(){
        List<History> historyList = new ArrayList<>();
        Calendar c  = Calendar.getInstance();

        Date date = TimeUtils.string2Date("01/01/2018 09:00:30", Constants.DATE_TIME_FORMAT);
        c.setTime(date);

        Random r = new Random();
        r.nextInt();
        for(int i=0; i<365; i++){
            History history = new History();
            history.setReadiness(5 + r.nextInt(5));
            history.setSPO2(90 + r.nextInt(10));
            history.setHR(60+r.nextInt(5));
            history.setHRV(55 + r.nextInt(10));
            history.setGSR(10 + r.nextInt(2));
            if(i%3==0){
                history.setExercise("Run");
            }else if(i%3==1){
                history.setExercise("Swim");
            }else{
                history.setExercise("Bicycle");
            }
            c.add(Calendar.DAY_OF_YEAR, 1);
            history.setTestDate(new Date(c.getTimeInMillis()));
            history.setAvgHR(55+r.nextInt(9));
            history.setMaxHR(65);
            history.setMinHR(55);
            history.setDuration(2);

            history.setTestTime(c.getTimeInMillis());
            history.setWeek(MyTimeUtils.getWeekOfYearCN(history.getTestDate()));
            history.setMonth(c.get(Calendar.MONTH)); // jan -> 0

            //history.save(); // save data to db

            historyList.add(history);
            LogUtil.d(TAG, history.toString());
        }

        return historyList;
    }


    /**
     * 生成HistoryExercise显示需要的数据
     * @return
     */
    public static List<History> genFakeHistoryExerciseData(){
        List<History> dataList = new ArrayList<>();

        Calendar c  = Calendar.getInstance();
        Random r = new Random();
        long time = c.getTimeInMillis();
        for(int i=0; i<20; i++){
            time+=1000*60*10;

            History h = new History();
            h.setReadiness(5 + r.nextInt(5));
            h.setSPO2(90 + r.nextInt(10));
            h.setHR(60+r.nextInt(5));
            h.setHRV(55 + r.nextInt(10));
            h.setGSR(10 + r.nextInt(5));
            h.setExercise("Run");
            h.setTestDate(new Date(c.getTimeInMillis()));
            h.setMaxHR(65);
            h.setMaxHR(55);
            h.setTestTime(c.getTimeInMillis());
            h.setWeek(MyTimeUtils.getWeekOfYearCN(h.getTestDate()));
            h.setMonth(c.get(Calendar.MONTH)); // jan -> 0

            // HH:mm
            String curTime = TimeUtils.millis2String(time, FMT_HM);
            h.setTestTimeStr(curTime);

            LogUtil.d(TAG, h.toString());
            dataList.add(h);
        }

        return dataList;
    }

    /**
     * save to db
     */
    public static void saveData(){
        List<History> historyList = genFakeHistoryData();
        for(int i=0; i<historyList.size(); i++){
            historyList.get(i).save();
        }
    }

    public static void find(){
        //saveData();
        List<History> historyList =  DataSupport.where("month =  ? group by week order by week", "0").find(History.class);
        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
    }


    public static List<History> findByDaily(long start, long end){
        //saveData();
        List<History> historyList =  DataSupport.where("testTime between  ? and ? order by testTime", ""+start, ""+end).find(History.class);
        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
        return historyList;
    }


    public static List<History> findByDailyExercise(long start, long end, String byExercise){
        //saveData();
        List<History> historyList =  DataSupport.where("exercise = ? and (testTime between  ? and ?) order by testTime",byExercise, ""+start, ""+end).find(History.class);

        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
        return historyList;
    }

    public static List<History> findByWeekly(long start, long end){
        //saveData();
        List<History> historyList =  DataSupport.where("testTime between  ? and ? group by week order by week", ""+start, ""+end).find(History.class);
        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
        return historyList;
    }
    public static List<History> findByWeeklyExercise(long start, long end, String byExercise){
        //saveData();
        List<History> historyList =  DataSupport.where("exercise = ? and (testTime between  ? and ? ) group by week order by week", byExercise,""+start, ""+end).find(History.class);
        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
        return historyList;
    }

    public static List<History> findByMonthly(long start, long end){
        //saveData();
        List<History> historyList =  DataSupport.where("testTime between  ? and ? group by month order by month", ""+start, ""+end).find(History.class);
        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
        return historyList;
    }

    public static List<History> findByMonthlyExercise(long start, long end, String exercise){
        List<History> historyList =  DataSupport.where("exercise = ? and (testTime between  ? and ?) group by month order by testDate",exercise, ""+start, ""+end).find(History.class);
        for(int i=0; i<historyList.size(); i++){
            History  h =historyList.get(i);
            Log.d(TAG, "find: " + h);
        }
        return historyList;
    }

    /**
     * 生成测试数据
     */
    public static void genFakeData2DB(){
        //saveData();
        int count =  DataSupport.count(History.class);
        if(! (count>1) ){
            Log.d(TAG, "genFakeData2DB: true");
            saveData();
        }else{
            Log.d(TAG, "genFakeData2DB: false");
        }
    }



    public static void genUsers(){
        Log.d(TAG, "genUsers: ....");
        List<User> list = new ArrayList<>();
        User u = new User();
        u.setName("海峰");
        u.setName("");
        u.save();
        list.add(u);
        u = new User();
        u.setName("思宏");
        u.setName("");
        u.save();

        List<User> users = DataSupport.where("name = '思宏'").find(User.class);
        for (User ur:
                users) {
            Log.d(TAG, "genUsers: .... " + ur);
        }


    }


}
