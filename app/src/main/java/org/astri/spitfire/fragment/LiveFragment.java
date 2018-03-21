package org.astri.spitfire.fragment;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.astri.spitfire.R;
import org.astri.spitfire.chart.MyColor;
import org.astri.spitfire.entities.History;
import org.astri.spitfire.util.Constants;
import org.astri.spitfire.util.LogUtil;
import org.astri.spitfire.util.MyTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static com.blankj.utilcode.util.TimeUtils.millis2String;
import static com.vise.utils.handler.HandlerUtil.runOnUiThread;
import static org.astri.spitfire.util.Constants.FMT_HMS;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/02/03
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class LiveFragment extends Fragment {

    private LineChart mHrHrvSpo2;
    private LineChart mGSR;
    private LineChart mStimulation;
    private Timer timer;
    private volatile boolean isVisible; // fragment isVisible?

    public static float WIDTH = 3f;
    public static float CIRCLE_RADIUS = 9f;
    public static int MAX = 20; // max number of points

    private static int count;

    private static final String TAG = "LiveFragment";

    private TextView txHeartRate;
    private TextView txHRV;
    private TextView txSPO2;
    private TextView txGSR;
    private TextView txStimulation;

    private static List<String> xValueFormatterList = new ArrayList<>();

    private static LiveFragment singleLiveFragment = null;

    public static LiveFragment getInstance(){
        if(singleLiveFragment == null){
            return new LiveFragment();
        }else{
            return singleLiveFragment;
        }
    }





    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        init(view);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void init(View view) {

        mHrHrvSpo2 = view.findViewById(R.id.ct_hrhrvspo2);
        mGSR = view.findViewById(R.id.ct_gsr);
        mStimulation = view.findViewById(R.id.ct_stimulation);

        setPltParams(mHrHrvSpo2,0f,100f);
        setPltParams(mGSR, 0f, 30f);
        setPltParams(mStimulation,0f,180f);

        mHrHrvSpo2.setData(new LineData());
        mGSR.setData(new LineData());
        mStimulation.setData(new LineData());

        LogUtil.d(TAG, "init: data set");

        mHrHrvSpo2.invalidate();
        mGSR.invalidate();
        mStimulation.invalidate();

//        if(timer == null){
//            timer = new Timer();
//        }
//
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                if(count>=240){ // s
//                    mHrHrvSpo2.getData().clearValues();
//                    mGSR.getData().clearValues();
//                    mStimulation.getData().clearValues();
//                    count = 0;
//                }
//                addEntry();
//            }
//        },1000, 200);
        txHeartRate = view.findViewById(R.id.tx_heartrate);
        txHRV = view.findViewById(R.id.tx_hrv);
        txSPO2 = view.findViewById(R.id.tx_spo2);
        txGSR = view.findViewById(R.id.tx_gsr);
        txStimulation = view.findViewById(R.id.tx_stimulation);

        feedMultiple();
    }

    /**
     * 根据大小显示字体
     * @param pre
     * @param data
     * @return
     */
    private static SpannableString setCircleViewText(String pre, String data){
        SpannableString sp = new SpannableString(pre+data) ;
        sp.setSpan(new AbsoluteSizeSpan(10,true),0,pre.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sp.setSpan(new AbsoluteSizeSpan(20,true),pre.length(),pre.length()+data.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sp;
    }


    /**
     * 曲线中加入点
     */
    private void addEntry(){

        int mHrHrvSpo2Sets = mHrHrvSpo2.getLineData().getDataSetCount();
        if(mHrHrvSpo2Sets == 0){
            // add data set
            mHrHrvSpo2.getData().addDataSet(createHrDataSet());
            mHrHrvSpo2.getData().addDataSet(createHrvDataSet());
            mHrHrvSpo2.getData().addDataSet(createSpo2DataSet());
        }

        int mGSRSets = mGSR.getLineData().getDataSetCount();
        if(mGSRSets == 0){
            // add data set
            mGSR.getData().addDataSet(createGsrDataSet());
        }

        int mStimulationSets = mStimulation.getLineData().getDataSetCount();
        if(mStimulationSets == 0){
            // add data set
            mStimulation.getData().addDataSet(createStimulationDataSet());
        }


        LineData HrHrvSpo2 = mHrHrvSpo2.getData();
        ILineDataSet ds_hr = HrHrvSpo2.getDataSetByIndex(0);
        ILineDataSet ds_hrv = HrHrvSpo2.getDataSetByIndex(1);
        ILineDataSet ds_spo2 = HrHrvSpo2.getDataSetByIndex(2);

        LineData GSR = mGSR.getData();
        ILineDataSet ds_gsr = GSR.getDataSetByIndex(0);

        LineData Stimulation = mStimulation.getData();
        ILineDataSet ds_simulation = Stimulation.getDataSetByIndex(0);

        History h = genData();
        int x = count++;

        ds_hr.addEntry(new Entry(x, h.getHR()));
        ds_hrv.addEntry(new Entry(x, h.getHRV()));
        ds_spo2.addEntry(new Entry(x, h.getSPO2()));

        ds_gsr.addEntry(new Entry(x, h.getGSR()));

//        if(h.getStimulation()!=0){
//            float xVal = (float) (count);
//            if(x%10 == 8){
//                xVal = xVal - 1.99f;
//                LogUtil.d(TAG, "XVAL = " +xVal);
//                float yVal = 0f;
//                ds_simulation.addEntry(new Entry(xVal, h.getStimulation()));
//            }else{
//                xVal = xVal -0.1f;
//                LogUtil.d(TAG, "XVAL = " +xVal);
//                float yVal = 0f;
//                ds_simulation.addEntry(new Entry(xVal, h.getStimulation()));
//            }
//        }else{
//            ds_simulation.addEntry(new Entry(x, h.getStimulation()));
//        }

        ds_simulation.addEntry(new Entry(x, h.getStimulation()));

        if(txHeartRate!=null && txSPO2!=null&& txHRV!=null){
            // set circle text view content
            txHeartRate.setText(setCircleViewText("Heart Rate\n",""+h.getHR()));
            txSPO2.setText(setCircleViewText("SPO2\n",""+h.getSPO2()));
            txHRV.setText(setCircleViewText("HRV\n",""+h.getHRV()));
            txGSR.setText(setCircleViewText("GSR\n", ""+h.getGSR()));
            txStimulation.setText(setCircleViewText("Stimulation\n", h.getStimulation()+" mV"));
        }

        xValueFormatterList.add(h.getTestTimeStr());

        HrHrvSpo2.notifyDataChanged();
        mHrHrvSpo2.notifyDataSetChanged();
        setXValueFormatt(mHrHrvSpo2);
        mHrHrvSpo2.setVisibleXRangeMaximum(MAX);
        //mHrHrvSpo2.invalidate();
        mHrHrvSpo2.moveViewTo(HrHrvSpo2.getDataSetByIndex(0).getEntryCount() - (MAX+1), 50f, YAxis.AxisDependency.RIGHT);

        GSR.notifyDataChanged();
        mGSR.notifyDataSetChanged();
        setXValueFormatt(mGSR);
        mGSR.setVisibleXRangeMaximum(MAX);
        mGSR.moveViewTo(GSR.getDataSetByIndex(0).getEntryCount() - (MAX+1), 50f, YAxis.AxisDependency.RIGHT);
        //mGSR.invalidate();


        Stimulation.notifyDataChanged();
        mStimulation.notifyDataSetChanged();
        setXValueFormatt(mStimulation);
        mStimulation.setVisibleXRangeMaximum(MAX);
        mStimulation.moveViewTo(Stimulation.getDataSetByIndex(0).getEntryCount() - (MAX+1), 50f, YAxis.AxisDependency.RIGHT);
        //mStimulation.invalidate();
    }

    private void setXValueFormatt(LineChart lchart){
        lchart.getXAxis().setValueFormatter(new MyXAxisValueFormatter(xValueFormatterList.toArray(new String[xValueFormatterList.size()])));
    }


    private LineDataSet createHrDataSet(){
        LineDataSet set = new LineDataSet(null, "Heart Rate");
        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(MyColor.SEA_GREEN);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }

    private LineDataSet createHrvDataSet(){
        LineDataSet set = new LineDataSet(null, "HRV");
        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(MyColor.DARK_ORANGE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }

    private LineDataSet createSpo2DataSet(){
        LineDataSet set = new LineDataSet(null, "SPO2");
        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(MyColor.DEEP_SKY_BLUE2);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }

    private LineDataSet createGsrDataSet(){
        LineDataSet set = new LineDataSet(null, "GSR");

        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(MyColor.MAGENTA);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }

    private LineDataSet createStimulationDataSet(){
        LineDataSet set = new LineDataSet(null, "Stimulation");
        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(MyColor.SEA_GREEN);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }


    /**
     * 生成图表上的实时数据
     * @return
     */
    private History genData(){

        Calendar c  = Calendar.getInstance();
        Random r = new Random();

        History h = new History();
        h.setReadiness(5 + r.nextInt(5));
        h.setSPO2(90 + r.nextInt(10));
        h.setHR(60+r.nextInt(5));
        h.setHRV(55 + r.nextInt(10));
        h.setGSR(10 + r.nextInt(5));
        h.setExercise("Live");
        h.setTestDate(new Date(c.getTimeInMillis()));
        h.setMaxHR(65);
        h.setMaxHR(55);
        h.setDuration(2);
        h.setTestTime(c.getTimeInMillis());
        h.setWeek(MyTimeUtils.getWeekOfYearCN(h.getTestDate()));
        h.setMonth(c.get(Calendar.MONTH)); // jan -> 0
//        if(count%10==8 || count%10==9){
//            h.setStimulation(75);
        if(count%10==2){
            h.setStimulation(100);
        }else {
            h.setStimulation(0);
        }

        // HH:mm:ss
        String curTime = TimeUtils.millis2String(h.getTestTime(), FMT_HMS);
        h.setTestTimeStr(curTime);

        return h;
    }

    /**
     * 设置图表的参数
     * @param chart
     * @param min Y min value
     * @param max Y max value
     */
    private void setPltParams(LineChart chart, float min, float max){
        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(false);


        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);

        //xAxis.setLabelCount(10, true);
        YAxis leftYAxis = chart.getAxisLeft();
        leftYAxis.setLabelCount(10, false);
        leftYAxis.setAxisMinimum(min); // this replaces setStartAtZero(true)
        leftYAxis.setAxisMaximum(max);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis.setDrawLabels(true);

        chart.getAxisRight().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.getDescription().setEnabled(false);
        chart.animateX(750);
        // do not forget to refresh the chart
        LogUtil.d(TAG, "invalidate");
    }

    /**
     * format X axis
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
            if ((int) value >= mValues.length || (int)value <0) {
                return "";
            }else{
                return mValues[(int) value];
            }
        }

    }


    @Override
    public void onDestroyView() {
        if (thread != null)
            thread.interrupt();
        super.onDestroyView();
    }


    private Thread thread;

    private void feedMultiple() {

        if (thread != null){
            thread.interrupt();
        }

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry();
            }
        };

        thread = new Thread(new Runnable() {

            @Override
            public void run() {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    getActivity().runOnUiThread(runnable);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {

                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (thread != null) {
            thread.interrupt();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

}
