package org.astri.spitfire;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.TextView;


import com.blankj.utilcode.util.TimeUtils;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.astri.spitfire.chart.MyColor;
import org.astri.spitfire.entities.History;
import org.astri.spitfire.util.AnimationUtils;
import org.astri.spitfire.util.Constants;
import org.astri.spitfire.util.DataUtil;
import org.astri.spitfire.util.LogUtil;
import org.astri.spitfire.util.MyTimeUtils;
import org.astri.spitfire.util.ShowUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.astri.spitfire.util.DataUtil.find;
import static org.astri.spitfire.util.DataUtil.findByDaily;
import static org.astri.spitfire.util.DataUtil.findByMonthly;
import static org.astri.spitfire.util.DataUtil.findByWeekly;

/**
 * <pre>
 *     author : ghf
 *     e-mail : 869862783@qq.com
 *     time   : 2018/1/23
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class HistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "HistoryActivity";

    public static float WIDTH = 3f;
    public static float CIRCLE_RADIUS = 9f;

    private Typeface mTf;

    private static final String[] dates = new String[]{"Daily", "Weekly", "Monthly"};
    private List<String> dateList = Arrays.asList(dates);

    private LineChart mReadiness;
    private LineChart mHrHrvSpo2;
    private LineChart mGSR;


    private TextView tvDate;
    private ImageView ivDate;
    private TextView tvStart;
    private TextView tvStartDate;
    private int mYear, mMonth, mDay;

    private TextView tvEnd;
    private TextView tvEndDate;

    // 搜索时间间隔
    private static final int DAILY = 0;
    private static final int WEEKLY = 1;
    private static final int MONTHLY = 2;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        initView();

    }

    @Override
    public void onClick(View v) {
        LogUtil.d(TAG, "onclick");

        // choose intervals
        if(v.getId() == R.id.tv_date
        || v.getId() == R.id.iv_date){
            String data = tvDate.getText().toString();
            LogUtil.d(TAG, "search by : " + data);
            if (!ShowUtils.isPopupWindowShowing()) {
                AnimationUtils.startModeSelectAnimation(ivDate, true);
                ShowUtils.showPopupWindow(HistoryActivity.this, tvDate, 90, 166, dateList,
                        new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view,
                                                    int position, long id) {
                                ShowUtils.updatePopupWindow(position);
                                AnimationUtils.startModeSelectAnimation(ivDate, false);
                                ShowUtils.popupWindowDismiss();
                                tvDate.setText(dateList.get(position));
                                // 更新图表
                                // ChartUtils.notifyDataSetChanged(chart, getData(), position);
                                String data = tvDate.getText().toString();
                                LogUtil.d(TAG, "search by : " + data);
                                if(dateList.get(0).equals(data)){
                                    refreshView(Constants.DAILY);
                                }else if(dateList.get(1).equals(data)){
                                    refreshView(Constants.WEEKLY);
                                }else {
                                    refreshView(Constants.MONTHLY);
                                }

                            }
                        });
            } else {
                AnimationUtils.startModeSelectAnimation(ivDate, false);
                ShowUtils.popupWindowDismiss();
            }

            if (dateList.get(0).equals(data)) {
                ShowUtils.updatePopupWindow(0);
            } else if (dateList.get(1).equals(data)) {
                ShowUtils.updatePopupWindow(1);
            } else if (dateList.get(2).equals(data)) {
                ShowUtils.updatePopupWindow(2);
            }
        }

        // choose start date
        if(v.getId() == R.id.tv_start
                || v.getId() == R.id.tv_start_date){
            new DatePickerDialog(this, onStartDateSetListener, mYear,mMonth,mDay).show();
        }

        // choose end date
        if(v.getId() == R.id.tv_end
                || v.getId() == R.id.tv_end_date){
            new DatePickerDialog(this, onEndDateSetListener, mYear,mMonth,mDay).show();
        }

    }

    /**
     * choose start date
     */
    private DatePickerDialog.OnDateSetListener onStartDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String date;
            if (mMonth + 1 < 10) {
                if (mDay < 10) {
                    date = new StringBuffer().append("0").append(mMonth + 1).append("/")
                            .append("0").append(mDay).append("/")
                            .append(mYear).toString();
                } else {
                    date = new StringBuffer().append("0").append(mMonth + 1).append("/")
                            .append(mDay).append("/")
                            .append(mYear).toString();
                }

            } else {
                if (mDay < 10) {
                    date = new StringBuffer().append(mMonth + 1).append("/")
                            .append("0").append(mDay).append("/")
                            .append(mYear).toString();
                } else {
                    date = new StringBuffer().append("0").append(mMonth + 1).append("/")
                            .append("0").append(mDay).append("/")
                            .append(mYear).toString();
                }

            }
            tvStartDate.setText(date);

            // 响应
            String data = tvDate.getText().toString();

            if(dateList.get(0).equals(data)){
                refreshView(Constants.DAILY);
            }else if(dateList.get(1).equals(data)){
                refreshView(Constants.WEEKLY);
            }else {
                refreshView(Constants.MONTHLY);
            }
        }
    };

    /**
     * choose end date
     */
    private DatePickerDialog.OnDateSetListener onEndDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mYear = year;
            mMonth = monthOfYear;
            mDay = dayOfMonth;
            String date;
            if (mMonth + 1 < 10) {
                if (mDay < 10) {
                    date = new StringBuffer().append("0").append(mMonth + 1).append("/")
                            .append("0").append(mDay).append("/")
                            .append(mYear).toString();
                } else {
                    date = new StringBuffer().append("0").append(mMonth + 1).append("/")
                            .append(mDay).append("/")
                            .append(mYear).toString();
                }

            } else {
                if (mDay < 10) {
                    date = new StringBuffer().append(mMonth + 1).append("/")
                            .append("0").append(mDay).append("/")
                            .append(mYear).toString();
                } else {
                    date = new StringBuffer().append("0").append(mMonth + 1).append("/")
                            .append("0").append(mDay).append("/")
                            .append(mYear).toString();
                }

            }
            tvEndDate.setText(date);

            String data = tvDate.getText().toString();

            if(dateList.get(0).equals(data)){
                refreshView(Constants.DAILY);
            }else if(dateList.get(1).equals(data)){
                refreshView(Constants.WEEKLY);
            }else {
                refreshView(Constants.MONTHLY);
            }
        }
    };

    /**
     * 初始化视图
     */
    private void initView(){

        // 生成测试数据
        DataUtil.genFakeData2DB();

        mTf = Typeface.createFromAsset(getApplicationContext().getAssets(), "OpenSans-Regular.ttf");
        mReadiness = findViewById(R.id.ct_readiness);
        mHrHrvSpo2 = findViewById(R.id.ct_hrhrvspo2);
        mGSR = findViewById(R.id.ct_gsr);

        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);

        // choose intervals: daily weekly monthly
        tvDate = findViewById(R.id.tv_date);
        ivDate = findViewById(R.id.iv_date);
        tvDate.setOnClickListener(this);
        ivDate.setOnClickListener(this);

        // choose start date
        tvStart = findViewById(R.id.tv_start);
        tvStartDate = findViewById(R.id.tv_start_date);
        tvStart.setOnClickListener(this);
        tvStartDate.setOnClickListener(this);

        // choose end date
        tvEnd = findViewById(R.id.tv_end);
        tvEndDate = findViewById(R.id.tv_end_date);
        tvEnd.setOnClickListener(this);
        tvEndDate.setOnClickListener(this);

        // 显示曲线
        refreshView(Constants.DAILY);


    }

    /**
     * refresh chart
     */
    private void refreshView(int by){
        genPlts(by);
    }


    /**
     * show chart with fake data
     * @param chart
     */
    private void setFakeData(LineChart chart){
        List<History> historyList = DataUtil.genFakeHistoryData();
        List<Entry> readiness_Val = new ArrayList<Entry>();
        List<Entry> HR_val = new ArrayList<Entry>();
        List<Entry> HRV_val = new ArrayList<Entry>();
        List<Entry> SPO2_val = new ArrayList<Entry>();
        List<Entry> GSR_val = new ArrayList<Entry>();
        final String[] xlabels = new String[10];

        for(int i=0; i<10; i++){
            History h = historyList.get(i);

            Entry readiness = new Entry(i, h.getReadiness());
            readiness_Val.add(readiness);

            Entry hr = new Entry(i, h.getHR());
            HR_val.add(hr);

            Entry hrv = new Entry(i, h.getHRV());
            HRV_val.add(hrv);

            Entry spo2 = new Entry(i, h.getSPO2());
            SPO2_val.add(spo2);

            xlabels[i] =  "" + i;
        }
        xlabels[0] = "01/11/2018";
        xlabels[9] = "01/20/2018";

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        LineDataSet readiness = new LineDataSet(readiness_Val, "Readiness");
        dataSets.add(readiness);

        LineDataSet hr = new LineDataSet(HR_val, "HR");
        dataSets.add(hr);

        LineDataSet hrv = new LineDataSet(HRV_val, "HRV");
        dataSets.add(hrv);

        LineDataSet spo2 = new LineDataSet(SPO2_val, "SPO2");
        dataSets.add(spo2);

        LineDataSet gsr = new LineDataSet(GSR_val, "GSR");
        dataSets.add(gsr);

        LineData data = new LineData(dataSets);
        chart.setData(data);
        chart.invalidate();

        IAxisValueFormatter formatter = new IAxisValueFormatter() {

            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return xlabels[(int) value];
            }

            // we don't draw numbers, so no decimal digits needed
//            @Override
//            public int getDecimalDigits() {  return 0; }
        };
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f); // minimum axis-step (interval) is 1
        xAxis.setValueFormatter(formatter);


    }



    private void genReadinessPlt(LineData data){
        LineChart  chart = mReadiness;
        chart.setData(data);

//        List<ILineDataSet> dataSets = data.getDataSets();
//        ILineDataSet readiness = dataSets.get(0);
//        int size = readiness.getEntryCount();
//
//        final String[] xlabels = new String[size];
//
//        for(int i=0; i<size; i++){
//            Entry e = readiness.getEntryForIndex(i);
//            History h = (History) e.getData();
//            xlabels[i] =  "" + TimeUtils.date2String(h.getTestDate(), Constants.FMT_d);
//        }
//        xlabels[0] = ((History)readiness.getEntryForIndex(0).getData()).getDateStr();
//        xlabels[size-1] = ((History)readiness.getEntryForIndex(0).getData()).getDateStr();

        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);
        //xAxis.setValueFormatter(new MyXAxisValueFormatter(xlabels));
        //mReadiness.getXAxis().setValueFormatter(formatter);


        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(true);


        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        //xAxis.setLabelCount(10, true);


        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setTypeface(mTf);
        leftYAxis.setLabelCount(10, false);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftYAxis.setAxisMaximum(10f);


        chart.getAxisRight().setEnabled(false);
        chart.setDrawGridBackground(true);
        chart.getDescription().setEnabled(false);
        chart.animateX(750);

        // do not forget to refresh the chart
        chart.invalidate();

        // add setOnChartValueSelectedListener
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener()
        {
            @Override
            public void onValueSelected(Entry e, Highlight h)
            {
                float x=e.getX();
                float y=e.getY();
                History his = (History) e.getData();
                LogUtil.d(TAG, "X: "+x +",Y: "+y +", history:" + his);

                // TODO: add a new intent to show history detail data
                // FIXME: high light selected value ?
                Intent intent = new Intent(HistoryActivity.this,HomeActivity.class);
                intent.putExtra("history_data", his);
                startActivity(intent);

            }

            @Override
            public void onNothingSelected()
            {

            }
        });


    }

    private void genHrHrvSpo2Plt(LineData data){
        LineChart  chart = mHrHrvSpo2;
        chart.setData(data);
        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(true);


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        //xAxis.setLabelCount(10, false);

        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setTypeface(mTf);
        leftYAxis.setLabelCount(10, true);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftYAxis.setAxisMaximum(100f);

//        YAxis rightYAxis = chart.getAxisRight();
//        rightYAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        rightYAxis.setAxisMaximum(120f);

        chart.getAxisRight().setEnabled(false);

        chart.setDrawGridBackground(true);

        chart.getDescription().setEnabled(false);

        chart.animateX(750);
        // do not forget to refresh the chart
        chart.invalidate();
        LogUtil.d(TAG, "invalidate");


    }

    private void genGsrPlt(LineData data){
        LineChart  chart = mGSR;
        chart.setData(data);
        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(true);


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(mTf);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        //xAxis.setLabelCount(10, true);

        YAxis leftYAxis = chart.getAxisLeft();

        leftYAxis.setTypeface(mTf);
        leftYAxis.setLabelCount(10, false);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftYAxis.setAxisMaximum(20f);

//        YAxis rightYAxis = chart.getAxisRight();
//        rightYAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
//        rightYAxis.setAxisMaximum(12f);

        chart.getAxisRight().setEnabled(false);

        chart.setDrawGridBackground(true);

        chart.getDescription().setEnabled(false);


        chart.animateX(750);
        // do not forget to refresh the chart
        chart.invalidate();
        LogUtil.d(TAG, "invalidate");
    }

    private LineData genReadinessData(List<Entry> readiness_Val, final String[] xlabels){

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        LineDataSet readiness = new LineDataSet(readiness_Val, "Readiness");
        readiness.setLineWidth(WIDTH);
        readiness.setCircleRadius(CIRCLE_RADIUS);
        readiness.setHighLightColor(MyColor.SEA_GREEN);
        readiness.setColor(MyColor.SEA_GREEN);
        readiness.setCircleColor(MyColor.SEA_GREEN);
        readiness.setCircleColorHole(MyColor.SEA_GREEN);
        readiness.setDrawValues(false);
        readiness.setAxisDependency(YAxis.AxisDependency.LEFT);
        dataSets.add(readiness);
        LineData data = new LineData(dataSets);

        mReadiness.getXAxis().setGranularity(1f);
        mReadiness.getXAxis().setValueFormatter(new MyXAxisValueFormatter(xlabels));


        return data;

    }

    /**
     *
     * @param
     * @return
     */
    private LineData genHrHrvSpo2Data(List<Entry> HR_val , List<Entry> HRV_val,List<Entry> SPO2_val,final String[] xlabels){
        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        LineDataSet hr = new LineDataSet(HR_val, "HR");
        hr.setLineWidth(WIDTH);
        hr.setDrawCircles(false);
        hr.setDrawValues(false);
        hr.setColor(MyColor.YELLOW);
        dataSets.add(hr);

        LineDataSet hrv = new LineDataSet(HRV_val, "HRV");
        hrv.setLineWidth(WIDTH);
        hrv.setDrawCircles(false);
        hrv.setDrawValues(false);
        hrv.setColor(MyColor.DARK_ORANGE);
        dataSets.add(hrv);

        LineDataSet spo2 = new LineDataSet(SPO2_val, "SPO2");
        spo2.setLineWidth(WIDTH);
        spo2.setDrawCircles(false);
        spo2.setDrawValues(false);
        spo2.setColor(MyColor.DEEP_SKY_BLUE2);
        dataSets.add(spo2);

        LineData data = new LineData(dataSets);

        // set x axis
        mHrHrvSpo2.getXAxis().setGranularity(1f);
        mHrHrvSpo2.getXAxis().setValueFormatter(new MyXAxisValueFormatter(xlabels));

        return data;


    }

    /**
     *
     * @param GSR_val
     * @param xlabels
     * @return
     */
    private LineData genGsrData(List<Entry> GSR_val,final String[] xlabels){

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();

        LineDataSet gsr = new LineDataSet(GSR_val, "GSR");
        gsr.setLineWidth(WIDTH);
        gsr.setDrawCircles(false);
        gsr.setDrawValues(false);
        gsr.setColor(MyColor.MAGENTA);

        dataSets.add(gsr);

        LineData data = new LineData(dataSets);

        mGSR.getXAxis().setGranularity(1f);
        mGSR.getXAxis().setValueFormatter(new MyXAxisValueFormatter(xlabels));

        return data;
    }


    /**
     * 生成曲线
     * @param by
     */
    private void genPlts(int by){

        List<History> historyList;
        final String[] xlabels;

        if(by == DAILY){
            historyList = searchHisDataDaily();
            xlabels = searchHisDataDailyXlabels(historyList);
        }else if(by == WEEKLY){
            historyList = searchHisDataWeekly();
            xlabels = searchHisDataWeeklyXlabels(historyList);
        }else{
            historyList = searchHisDataMonthly();
            xlabels = searchHisDataMonthlyXlabels(historyList);
        }

        // plt1
        List<Entry> readiness_Val = new ArrayList<Entry>();

        // plt2
        List<Entry> HR_val = new ArrayList<Entry>();
        List<Entry> HRV_val = new ArrayList<Entry>();
        List<Entry> SPO2_val = new ArrayList<Entry>();

        // plt3
        List<Entry> GSR_val = new ArrayList<Entry>();

        int size = historyList.size();

        for(int i=0; i<size; i++){
            History h = historyList.get(i);

            // plt1
            Entry readiness = new Entry(i, h.getReadiness(), h);
            readiness_Val.add(readiness);

            // plt2
            Entry hr = new Entry(i, h.getHR());
            HR_val.add(hr);
            Entry hrv = new Entry(i, h.getHRV());
            HRV_val.add(hrv);
            Entry spo2 = new Entry(i, h.getSPO2());
            SPO2_val.add(spo2);

            // plt3
            Entry gsr = new Entry(i, h.getGSR());
            GSR_val.add(gsr);
        }

        // gen plts
        genReadinessPlt(genReadinessData(readiness_Val, xlabels));
        genHrHrvSpo2Plt(genHrHrvSpo2Data(HR_val, HRV_val, SPO2_val, xlabels));
        genGsrPlt(genGsrData(GSR_val,xlabels));

    }


    private void testDate(){

        int week;
        week = TimeUtils.getWeekOfYear("01/17/2018",sdf);
        LogUtil.d(TAG, "week: " + week);
        week = TimeUtils.getWeekOfYear("01/01/2018",sdf);
        LogUtil.d(TAG, "week: " + week);
        week = TimeUtils.getWeekOfYear("01/07/2018",sdf);
        LogUtil.d(TAG, "week: " + week);

        testWeekIntervals();
    }

    private void testWeekIntervals(){
        int[] intevals = new int[53];


        List<History> historyList = DataUtil.genFakeHistoryData();

        for (History h:
                historyList) {
            int testWeek = MyTimeUtils.getWeekOfYearCN(h.getTestDate());
            Log.d(TAG, "testWeekIntervals: " + TimeUtils.date2String(h.getTestDate()) +", week: " + testWeek);
            intevals[testWeek]++;
        }

        for(int i=0; i<intevals.length; i++){
            if(intevals[i]!=0){
                LogUtil.d(TAG,"testWeekIntervals ["+i +"]: "+intevals[i]);

            }
        }

    }



    /**
     *
     * @return
     */
    private List<History> searchHisDataDaily() {

        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        LogUtil.d(TAG, "StartDate: "+ txStarDate);
        LogUtil.d(TAG, "EndDate: "+txEndDate);

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        List<History> historyList = findByDaily(start, end);

        return historyList;
    }

    private String[] searchHisDataDailyXlabels(List<History> historyList){
        int size = historyList.size();
        final String[] xlabels = new String[size];

        for(int i=0; i<size; i++){
            History h = historyList.get(i);
            xlabels[i] =  "" + TimeUtils.date2String(h.getTestDate(), Constants.FMT_d);
        }

        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        xlabels[0] = txStarDate;
        xlabels[size-1] = txEndDate;
        return xlabels;
    }


    private List<History> searchHisDataWeekly() {

        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        LogUtil.d(TAG, "StartDate: "+ txStarDate);
        LogUtil.d(TAG, "EndDate: "+txEndDate);

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        List<History> historyList = findByWeekly(start, end);

        return historyList;
    }

    private String[] searchHisDataWeeklyXlabels(List<History> historyList){
        int size = historyList.size();
        final String[] xlabels = new String[size];

        for(int i=0; i<size; i++){
            History h = historyList.get(i);
            if(h.getWeek()<10){
                xlabels[i] =  "0" + h.getWeek();
            }else{
                xlabels[i] =  "" + h.getWeek();
            }
        }
        xlabels[0] = "2018"+ xlabels[0];
        xlabels[size-1] = "2018"+ xlabels[size-1];
        return xlabels;
    }


    private List<History> searchHisDataMonthly() {
        LogUtil.d(TAG, "searchHisDataMonthly: ");


        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        LogUtil.d(TAG, "StartDate: "+ txStarDate);
        LogUtil.d(TAG, "EndDate: "+txEndDate);

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        List<History> historyList = findByMonthly(start, end);

        return historyList;
    }

    private String[] searchHisDataMonthlyXlabels(List<History> historyList){
        int size = historyList.size();
        final String[] xlabels = new String[size];
        DateFormat df = new SimpleDateFormat("MMM");
        for(int i=0; i<size; i++){
            History h = historyList.get(i);
            xlabels[i] =  "" + TimeUtils.date2String(h.getTestDate(),df);
        }
        return xlabels;
    }



    /**
     * 产品原型。暂时只处理最多2018年以来的数据
     * @return
     */
    private Map<Integer,Integer> getTotalWeeks(){
        int[] totalWeeks = new int[53];
        Map<Integer,Integer> week = new HashMap<>();
        List<History> historyList = searchHisDataDaily(); // 得到所有的按天的数据

        int size = historyList.size();

        for(int i=0; i<size; i++){

            int lastTestWeek = -1;
            if((i-1) > 0){
                History lastHistory = historyList.get(i-1); // 前一天的数据
                lastTestWeek = MyTimeUtils.getWeekOfYearCN(lastHistory.getTestDate());
            }

            History history = historyList.get(i);

            int testWeek = MyTimeUtils.getWeekOfYearCN(history.getTestDate()); // 本条数据在一年中的第几周

        }

        for (History h:
            historyList) {

            int testWeek = MyTimeUtils.getWeekOfYearCN(h.getTestDate()); // 本条数据在一年中的第几周

            Log.d(TAG, "testWeekIntervals: " + TimeUtils.date2String(h.getTestDate()) +", week: " + testWeek);
            totalWeeks[testWeek]++;
        }
        for(int i=0; i<totalWeeks.length; i++){
            if(totalWeeks[i]!=0){
                week.put(i, totalWeeks[i]); // key: 第几周 value：有几天
                LogUtil.d(TAG,"testWeekIntervals ["+i +"]: "+totalWeeks[i]);
            }
        }
        return week;
    }


    /**
     * 格式化X轴
     */
    class MyXAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues;

        public MyXAxisValueFormatter(String[] values) {
            this.mValues = values;
        }


        @Override
        public String getFormattedValue(float value, AxisBase axis) {

            LogUtil.d(TAG, "getFormattedValue: " + value);
            LogUtil.d(TAG, "getFormattedValue： " + mValues.length);
            if ((int) value >= mValues.length) {
                return "";
            }else{
                return mValues[(int) value];
            }
        }



    }

}
