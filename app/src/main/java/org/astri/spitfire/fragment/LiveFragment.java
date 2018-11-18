package org.astri.spitfire.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.astri.spitfire.R;
import org.astri.spitfire.ble.common.AddEntryService;
import org.astri.spitfire.chart.MyColor;
import org.astri.spitfire.entities.History;
import org.astri.spitfire.util.LogUtil;
import org.astri.spitfire.util.MyTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Timer;
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

    private static final String TAG = LiveFragment.class.getSimpleName( );

    private LineChart mHrHrvSpo2;
    private LineChart mGSR;
    private LineChart mStimulation;
    private Timer timer;
    private volatile boolean isVisible; // fragment isVisible?

    public static float WIDTH = 3f;
    public static float CIRCLE_RADIUS = 9f;
    public static int MAX = 10; // max number of points

    private static int count;
    private TextView txHeartRate;
    private TextView txHRV;
    private TextView txSPO2;
    private TextView txGSR;
    private TextView txStimulation;

    private static List<String> xValueFormatterList = new ArrayList<>();

    private static LiveFragment singleLiveFragment = null;

    private AddEntryService mAddEntryService;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        init(view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAddEntryService.startPolling(runnable, 500);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }

    /**
     * init views
     * @param view
     */
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

        txHeartRate = view.findViewById(R.id.tx_heartrate);
        txHRV = view.findViewById(R.id.tx_hrv);
        txSPO2 = view.findViewById(R.id.tx_spo2);
        txGSR = view.findViewById(R.id.tx_gsr);
        txStimulation = view.findViewById(R.id.tx_stimulation);

//        feedMultiple();
        mAddEntryService = new AddEntryService(new Handler());

    } //init( )

    /**
     * setCircleViewText
     * @param pre
     * @param data
     * @return
     */
    private static SpannableString setCircleViewText(String pre, String data)
    {
        SpannableString sp = new SpannableString(pre+data);
        sp.setSpan(new AbsoluteSizeSpan(10,true),0,pre.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sp.setSpan(new AbsoluteSizeSpan(20,true),pre.length(),pre.length()+data.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sp;
    }


    /**
     * addEntry to plot
     */
    private void addEntry( )
    {
        int mHrHrvSpo2Sets = mHrHrvSpo2.getLineData().getDataSetCount( );
        if(mHrHrvSpo2Sets == 0)
        {
            // add data set
            mHrHrvSpo2.getData().addDataSet(createHrDataSet());
            mHrHrvSpo2.getData().addDataSet(createHrvDataSet());
            mHrHrvSpo2.getData().addDataSet(createSpo2DataSet());
        }

        int mGSRSets = mGSR.getLineData().getDataSetCount();
        if(mGSRSets == 0)
        {
            // add data set
            mGSR.getData().addDataSet(createGsrDataSet());
        }

        int mStimulationSets = mStimulation.getLineData().getDataSetCount();
        if(mStimulationSets == 0)
        {
            // add data set
            mStimulation.getData().addDataSet(createStimulationDataSet());
        }

        LineData HrHrvSpo2 = mHrHrvSpo2.getData( );
        ILineDataSet dataset_hr = HrHrvSpo2.getDataSetByIndex(0);
        ILineDataSet dataset_hrv = HrHrvSpo2.getDataSetByIndex(1);
        ILineDataSet dataset_spo2 = HrHrvSpo2.getDataSetByIndex(2);

        LineData GSR = mGSR.getData( );
        ILineDataSet ds_gsr = GSR.getDataSetByIndex(0);

        LineData Stimulation = mStimulation.getData( );
        ILineDataSet ds_simulation = Stimulation.getDataSetByIndex(0);

        History h = generateData( );
        int x = count++;

        dataset_hr.addEntry(new Entry(x, h.getHR()));   //添加Heart_Rate数据点
        dataset_hrv.addEntry(new Entry(x, h.getHRV()));  //添加HRV数据点
        dataset_spo2.addEntry(new Entry(x, h.getSPO2())); //添加SPO2数据点

        ds_gsr.addEntry(new Entry(x, h.getGSR()));

        ds_simulation.addEntry(new Entry(x, h.getStimulation()));

        if(txHeartRate!=null && txHRV!=null&& txSPO2!=null)
        {
            // set circle text view content
            txHeartRate.setText(setCircleViewText("Heart_Rate\n",""+h.getHR()));    //更新TextView显示的数据
            txSPO2.setText(setCircleViewText("SPO2\n",""+h.getSPO2()));
            txHRV.setText(setCircleViewText("HRV\n",""+h.getHRV()));
            txGSR.setText(setCircleViewText("GSR\n", ""+h.getGSR()));
            txStimulation.setText(setCircleViewText("Stimulation\n", h.getStimulation()+" mV"));
        }

        xValueFormatterList.add(h.getTestTimeStr( ));

        HrHrvSpo2.notifyDataChanged( );
        mHrHrvSpo2.notifyDataSetChanged( );
        setXValueFormat(mHrHrvSpo2);
        mHrHrvSpo2.setVisibleXRangeMaximum(MAX);
        //mHrHrvSpo2.invalidate();
        mHrHrvSpo2.moveViewTo(HrHrvSpo2.getDataSetByIndex(0).getEntryCount() - (MAX+1), 50f, YAxis.AxisDependency.RIGHT);

        GSR.notifyDataChanged( );
        mGSR.notifyDataSetChanged( );
        setXValueFormat(mGSR);
        mGSR.setVisibleXRangeMaximum(MAX);
        mGSR.moveViewTo(GSR.getDataSetByIndex(0).getEntryCount() - (MAX+1), 50f, YAxis.AxisDependency.RIGHT);
        //mGSR.invalidate();


        Stimulation.notifyDataChanged();
        mStimulation.notifyDataSetChanged();
        setXValueFormat(mStimulation);
        mStimulation.setVisibleXRangeMaximum(MAX);
        mStimulation.moveViewTo(Stimulation.getDataSetByIndex(0).getEntryCount() - (MAX+1), 50f, YAxis.AxisDependency.RIGHT);
        //mStimulation.invalidate();

    }  //addEntry( )

    /**
     * setXValueFormat
     * @param lineChart
     */
    private void setXValueFormat(LineChart lineChart)  //设置某个图表的X坐标格式
    {
        lineChart.getXAxis().setValueFormatter(new MyXAxisValueFormatter(xValueFormatterList.toArray(new String[xValueFormatterList.size()]) ) );
    }


    /**
     * createHrDataSet
     * @return
     */
    private LineDataSet createHrDataSet()
    {
        LineDataSet set = new LineDataSet(null, "Heart Rate");
        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);  //点的圆圈
        set.setDrawValues(false);   //点的数值
        set.setColor(MyColor.SEA_GREEN);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }

    /**
     * createHrvDataSet
     * @return
     */
    private LineDataSet createHrvDataSet()
    {
        LineDataSet set = new LineDataSet(null, "HRV");
        set.setLineWidth(WIDTH);
        set.setDrawCircles(false);
        set.setDrawValues(false);
        set.setColor(MyColor.DARK_ORANGE);
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setValueTextSize(10f);
        return set;
    }

    /**
     * createSpo2DataSet
     * @return
     */
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

    /**
     * createGsrDataSet
     * @return
     */
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

    /**
     * createStimulationDataSet
     * @return
     */
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
     * generate real time data
     * @return
     */
    private History generateData( )
    {

        Calendar c  = Calendar.getInstance();
        Random r = new Random();

        History h = new History( );
        h.setReadiness(5 + r.nextInt(5));
        h.setSPO2(90 + r.nextInt(10));
        h.setHR(60+r.nextInt(5));
        h.setHRV(55 + r.nextInt(25));
        h.setGSR(11 + r.nextInt(5));
        h.setExercise("Live");
        h.setTestDate(new Date(c.getTimeInMillis()));
        h.setMaxHR(65);
        h.setMaxHR(55);
        h.setDuration(2);
        h.setTestTime(c.getTimeInMillis());
        h.setWeek(MyTimeUtils.getWeekOfYearCN(h.getTestDate()));
        h.setMonth(c.get(Calendar.MONTH)); // jan -> 0

        if(count%10==2){
            h.setStimulation(100);
        }else {
            h.setStimulation(0);
        }

        // HH:mm:ss
        String curTime = TimeUtils.millis2String(h.getTestTime(), FMT_HMS);
        h.setTestTimeStr(curTime);

       Log.d("LiveFragment","abc");
        return h;
    }

    /**
     * set plots parameter
     * @param lineChart
     * @param min Y min value
     * @param max Y max value
     */
    private void setPltParams(LineChart lineChart, float min, float max)     //设置图表的参数
    {
        // legend
        Legend legend = lineChart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(false);


        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(true);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawLabels(true);

        //xAxis.setLabelCount(10, true);
        YAxis leftYAxis = lineChart.getAxisLeft();
        leftYAxis.setLabelCount(10, false);
        leftYAxis.setAxisMinimum(min); // this replaces setStartAtZero(true)
        leftYAxis.setAxisMaximum(max);
        leftYAxis.setDrawGridLines(true);
        leftYAxis.setDrawAxisLine(true);
        leftYAxis.setDrawLabels(true);

         lineChart.getAxisRight().setEnabled(false);
         lineChart.setDrawGridBackground(false);
         lineChart.getDescription().setEnabled(false);

         lineChart.animateX(2000);
        // do not forget to refresh the chart
        LogUtil.d(TAG, "invalidate");
    } //setPltParams( )

    /**
     * format X axis
     */
    class MyXAxisValueFormatter implements IAxisValueFormatter {

        private String[] mValues;
        public MyXAxisValueFormatter(String[] values)
        {
            this.mValues = values;
        }

        @Override
        public String getFormattedValue(float value, AxisBase axis)
        {
            LogUtil.d(TAG, "getFormattedValue: " + value);
            LogUtil.d(TAG, "getFormattedValue： " + mValues.length);
            if ((int) value >= mValues.length || (int)value <0)
            {      return "";                   }
            else
            {   return mValues[(int) value];   }
        }

    }


    @Override
    public void onDestroyView()
    {
        if (thread != null)
        { thread.interrupt();}

        super.onDestroyView( );
    }


    private Thread thread;

    /**
     * real time data
     */
    private void feedMultiple( )
    {
        if (thread != null){
            thread.interrupt();
        }

        final Runnable runnable = new Runnable() {

            @Override
            public void run() {
                addEntry( );
            }
        };

        thread = new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                for (int i = 0; i < 1000; i++) {

                    // Don't generate garbage runnables inside the loop.
                    getActivity().runOnUiThread(runnable);

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e)
                    {

                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start( );
    } //feedMultiple( )

    @Override
    public void onPause()
    {
        mAddEntryService.endPolling(runnable);
        super.onPause();

    }

    @Override
    public void onDestroy( )
    {
        super.onDestroy( );
    }

    /**
     * a runnable thread
     * run to read data from device
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            getActivity().runOnUiThread(addEntryRunnable);
        }
    };

    final Runnable addEntryRunnable = new Runnable() {

        @Override
        public void run() {
            addEntry( );
        }
    };

}
