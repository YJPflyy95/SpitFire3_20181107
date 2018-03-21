package org.astri.spitfire.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SearchView;
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
import org.astri.spitfire.util.DataUtil;
import org.astri.spitfire.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/03/19
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class HistoryByExerciseFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "HistoryByExerciseFragment";

    private LineChart mHrHrvSpo2;
    private LineChart mGSR;
    private Typeface mTf;

    private Button backButton;

    public static float WIDTH = 3f;
    public static float CIRCLE_RADIUS = 9f;

    // 图表group
    private LinearLayout ll_pltgrp;

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.bt_history){
            LogUtil.d(TAG, "back to history.");
            replaceFragment(new HistoryFragment());
        }
    }

    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ll_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history_by_exercise, container, false);

        init(view);

        genPlts();

         return view;
    }

    private void init(View view){
        mTf = Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Regular.ttf");
        mHrHrvSpo2 = view.findViewById(R.id.ct_hrhrvspo2);
        mGSR = view.findViewById(R.id.ct_gsr);
        backButton = view.findViewById(R.id.bt_history);
        backButton.setOnClickListener(this);
    }



    /**
     * 生成曲线
     */
    private void genPlts(){


        LogUtil.d(TAG, "genPlts: ");

        List<History> historyList = DataUtil.genFakeHistoryExerciseData();
        String[] xlabels=searchHisDataDailyXlabels(historyList);


        LogUtil.d(TAG, "genPlts:  size:  " + historyList.size());

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

        // gen plts 显示图表数据
        genHrHrvSpo2Plt(genHrHrvSpo2Data(HR_val, HRV_val, SPO2_val, xlabels));
        genGsrPlt(genGsrData(GSR_val,xlabels));

    }


    /**
     * X坐标轴的label
     * @param historyList
     * @return
     */
    private String[] searchHisDataDailyXlabels(List<History> historyList){

        int size = historyList.size();
        String[] xlabels = new String[size];

        for(int i=0; i<size; i++){
            History h = historyList.get(i);
            xlabels[i] =  h.getTestTimeStr();
        }

        return xlabels;
    }

    /**
     * genHrHrvSpo2Plt
     * @param data
     */
    private void genHrHrvSpo2Plt(LineData data){

        LogUtil.d(TAG, "genHrHrvSpo2Plt");

        LineChart  chart = mHrHrvSpo2;
        chart.setData(data);
        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(false);


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

        chart.getAxisRight().setEnabled(false);

        chart.setDrawGridBackground(true);

        chart.getDescription().setEnabled(false);

        chart.animateX(750);
        // do not forget to refresh the chart
        chart.invalidate();
        LogUtil.d(TAG, "invalidate");
    }

    /**
     * genGsrPlt
     * @param data
     */
    private void genGsrPlt(LineData data){
        LineChart  chart = mGSR;
        chart.setData(data);
        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(false);

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

        chart.getAxisRight().setEnabled(false);

        chart.setDrawGridBackground(true);

        chart.getDescription().setEnabled(false);


        chart.animateX(750);
        // do not forget to refresh the chart
        chart.invalidate();
        LogUtil.d(TAG, "invalidate");
    }

    /**
     *
     * @param
     * @return
     */
    private LineData genHrHrvSpo2Data(List<Entry> HR_val , List<Entry> HRV_val, List<Entry> SPO2_val, final String[] xlabels){
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
            if ((int) value >= mValues.length || (int) value <0) {
                return "";
            }else{
                return mValues[(int) value];
            }
        }

    }
}
