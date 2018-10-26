package org.astri.spitfire.fragment;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
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
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.mancj.materialsearchbar.SimpleOnSearchActionListener;

import org.astri.spitfire.R;
import org.astri.spitfire.ReadinessDetailActivity;
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
import java.util.List;

import static org.astri.spitfire.util.DataUtil.findByDaily;
import static org.astri.spitfire.util.DataUtil.findByDailyExercise;
import static org.astri.spitfire.util.DataUtil.findByMonthly;
import static org.astri.spitfire.util.DataUtil.findByMonthlyExercise;
import static org.astri.spitfire.util.DataUtil.findByWeekly;
import static org.astri.spitfire.util.DataUtil.findByWeeklyExercise;

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
public class HistoryFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = HistoryFragment.class.getSimpleName();

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

    // search data by DAILY, WEEKLY or MONTHLY
    private static final int DAILY = 0;
    private static final int WEEKLY = 1;
    private static final int MONTHLY = 2;

    private SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");


    // search data by time or exercise type
    private TextView tvByTime;
    private TextView tvByExercise;

    private MaterialSearchBar searchBar; // search bar
    private LinearLayout byTimeBar; // time
    private LinearLayout legend; // chart legend


    // chart group
    private LinearLayout ll_pltgrp;

    // search by exercise group
    private LinearLayout ll_searchgrp;

    private BottomNavigationBar bottom_navigation_bar;

    // Sample data
    private final String[] exercises = {
            "Run",
            "Jogging",
            "Relax"
    };

    // exercise type
    private String[] mStrs = {"Jogging", "Gym", "Exam"};

    private SearchView mSearchView;

    private ListView mListView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_history, container, false);

        init(container, view);

        mSearchView = view.findViewById(R.id.searchView);
        mListView = view.findViewById(R.id.listView);
        mListView.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, mStrs));
        mListView.setTextFilterEnabled(true);

        // set query test listener
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            // when click submit trigger onQueryTextSubmit
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            // when text change trigger onQueryTextChange
            @Override
            public boolean onQueryTextChange(String newText) {
                if (!TextUtils.isEmpty(newText)){
                    mListView.setFilterText(newText);
                }else{
                    mListView.clearTextFilter();
                }
                return false;
            }
        });

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //positon为点击到的listView的索引
                String exercise = (String)mListView.getAdapter().getItem(position);
                Toast.makeText(getContext(), exercise, Toast.LENGTH_SHORT).show();
                //获取title的值
                replaceFragment(new HistoryByExerciseFragment());

            }
        });

        return view;
    }


    private void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.ll_content, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     * init views
     * @param container
     * @param view
     */
    private void init(@Nullable ViewGroup container, View view) {

        mTf = Typeface.createFromAsset(getContext().getAssets(), "OpenSans-Regular.ttf");
        mReadiness = view.findViewById(R.id.ct_readiness);
        mHrHrvSpo2 = view.findViewById(R.id.ct_hrhrvspo2);
        mGSR = view.findViewById(R.id.ct_gsr);

        Calendar ca = Calendar.getInstance();
        mYear = ca.get(Calendar.YEAR);
        mMonth = ca.get(Calendar.MONTH);
        mDay = ca.get(Calendar.DAY_OF_MONTH);

        // choose intervals: daily weekly monthly
        tvDate = view.findViewById(R.id.tv_date);
        ivDate = view.findViewById(R.id.iv_date);
        tvDate.setOnClickListener(this);
        ivDate.setOnClickListener(this);

        // choose start date
        tvStart = view.findViewById(R.id.tv_start);
        tvStartDate = view.findViewById(R.id.tv_start_date);
        tvStart.setOnClickListener(this);
        tvStartDate.setOnClickListener(this);

        // choose end date
        tvEnd = view.findViewById(R.id.tv_end);
        tvEndDate = view.findViewById(R.id.tv_end_date);
        tvEnd.setOnClickListener(this);
        tvEndDate.setOnClickListener(this);


        String startDate = TimeUtils.millis2String(TimeUtils.getNowMills()-1000*3600*24*6, Constants.FMT_MMddyyyy);
        String endDate = TimeUtils.millis2String(TimeUtils.getNowMills(), Constants.FMT_MMddyyyy);
        LogUtil.d(TAG, "startDate: "+startDate);
        LogUtil.d(TAG, "endDate: "+endDate);
        tvEndDate.setText(endDate);
        tvStartDate.setText(startDate);

        // search tag
        tvByExercise = view.findViewById(R.id.tv_byexercise);
        tvByExercise.setOnClickListener(this);
        tvByTime = view.findViewById(R.id.tv_bytime);
        tvByTime.setOnClickListener(this);

        // switch search
        searchBar = view.findViewById(R.id.searchBar);
        byTimeBar = view.findViewById(R.id.ll_byTime);

        // show legend
        legend = view.findViewById(R.id.ll_legend);

        // plots group(3 plots)
        ll_pltgrp = view.findViewById(R.id.ll_pltgrp);

        ll_searchgrp = view.findViewById(R.id.ll_searchgrp);

        // bottom navigation bar底部导航栏
        bottom_navigation_bar = container.getRootView().findViewById(R.id.bottom_navigation_bar);

        // init search bar
        initSearchBar();

        // generate fake data for testing
        DataUtil.genFakeData2DB();

        // show curves
        refreshView(Constants.DAILY);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        LogUtil.d(TAG, "onclick");

        // choose intervals
        if(v.getId() == R.id.tv_date
                || v.getId() == R.id.iv_date){

            if (!ShowUtils.isPopupWindowShowing()) {
                AnimationUtils.startModeSelectAnimation(ivDate, true);
                ShowUtils.showPopupWindow(getActivity(), tvDate, 90, 166, dateList,
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

            String data = tvDate.getText().toString();
            LogUtil.d(TAG, "search by : " + data);

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
            new DatePickerDialog(getContext(), onStartDateSetListener, mYear,mMonth,mDay).show();
        }

        // choose end date
        if(v.getId() == R.id.tv_end
                || v.getId() == R.id.tv_end_date){
            new DatePickerDialog(getContext(), onEndDateSetListener, mYear,mMonth,mDay).show();
        }

        // click by exercise
        if(v.getId() == R.id.tv_byexercise){
            byTimeBar.setVisibility(View.GONE);
            //searchBar.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.GONE);
            ll_pltgrp.setVisibility(View.GONE);
            legend.setVisibility(View.GONE);
            ll_searchgrp.setVisibility(View.VISIBLE);

            // FIXME a little bug
//            bottom_navigation_bar.setVisibility(View.GONE);
        }

        // click by time
        if(v.getId() == R.id.tv_bytime){
            byTimeBar.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.GONE);
            ll_pltgrp.setVisibility(View.VISIBLE);
            legend.setVisibility(View.VISIBLE);
            ll_searchgrp.setVisibility(View.GONE);
//            bottom_navigation_bar.setVisibility(View.VISIBLE);
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

            // 响应，按照何种方式显示数据：daily weekly monthly
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
     * Search bar settings
     */
    private void initSearchBar(){
        List<String> suggestions = new ArrayList();
        suggestions.add("Run");
        suggestions.add("Swim");
        suggestions.add("Bicycle");
        searchBar.setLastSuggestions(suggestions);
        searchBar.setOnSearchActionListener(new MyOnSearchActionConfirmed());

    }

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


    /**
     * ReadinessPlt
     * @param data
     */
    private void genReadinessPlt(LineData data){
        Log.d(TAG, "genReadinessPlt");

        LineChart  chart = mReadiness;
        chart.setData(data);
        XAxis xAxis = chart.getXAxis();
        xAxis.setGranularity(1f);

        // legend
        Legend legend = chart.getLegend();
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setForm(Legend.LegendForm.LINE);

        // don't show legend
        legend.setEnabled(false);


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
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                intent.putExtra("history_data", his);
                startActivity(intent);
            }

            @Override
            public void onNothingSelected()
            {

            }
        });
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
     * genReadinessData
     * @param readiness_Val
     * @param xlabels
     * @return
     */
    private LineData genReadinessData(List<Entry> readiness_Val, final String[] xlabels){

        Log.d(TAG, "genReadinessData");

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
        mReadiness.getXAxis().setValueFormatter(new HistoryFragment.MyXAxisValueFormatter(xlabels));

        return data;

    }

    /**
     * genHrHrvSpo2Data
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
        mHrHrvSpo2.getXAxis().setValueFormatter(new HistoryFragment.MyXAxisValueFormatter(xlabels));

        return data;

    }

    /**
     * genGsrData
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
        mGSR.getXAxis().setValueFormatter(new HistoryFragment.MyXAxisValueFormatter(xlabels));

        return data;
    }


    /**
     * genPlots
     * @param by
     */
    private void genPlts(int by){


        // show Legend
        legend.setVisibility(View.VISIBLE);

        LogUtil.d(TAG, "genPlts:  by " + by);

        List<History> historyList;
        String[] xlabels=null;

        if(by == DAILY){
            historyList = searchHisDataDaily();
            if(historyList.size()>0){
                xlabels = searchHisDataDailyXlabels(historyList);
            }
        }else if(by == WEEKLY){
            historyList = searchHisDataWeekly();
            if(historyList.size()>0) {
                xlabels = searchHisDataWeeklyXlabels(historyList);
            }
        }else{
            historyList = searchHisDataMonthly();
            if(historyList.size()>0){
                xlabels = searchHisDataMonthlyXlabels(historyList);
            }
        }

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

        if(checkDate() || historyList.size() == 0){ // start>end or list is empty
            // 搜索时间如果 开始>结束，那么不显示曲线数据
            // search time interval if start > end, then show nothing.
            mGSR.getData().clearValues();
            mGSR.invalidate();
            mReadiness.getData().clearValues();
            mReadiness.invalidate();
            mHrHrvSpo2.getData().clearValues();
            mHrHrvSpo2.invalidate();
        }else{
            // gen plots to show data  显示图表数据
            genReadinessPlt(genReadinessData(readiness_Val, xlabels)); // readiness
            genHrHrvSpo2Plt(genHrHrvSpo2Data(HR_val, HRV_val, SPO2_val, xlabels)); // hr, hrv spo2
            genGsrPlt(genGsrData(GSR_val,xlabels)); // gsr
        }

    }

    /**
     * unused
     */
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

    /**
     * unused
     */
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
     * check if start > end
     * @return
     */
    private boolean checkDate(){

        boolean result = false;

        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        if(start > end){
            result = true;
        }

        return result;
    }


    /**
     * searchHisDataDaily
     * @return
     */
    private List<History> searchHisDataDaily() {


        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        LogUtil.d(TAG, "StartDate: "+ txStarDate);
        LogUtil.d(TAG, "EndDate: "+txEndDate);

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        List<History> historyList;

        String byExercise = searchBar.getText();
        LogUtil.d(TAG, "searchBar's text is : " + byExercise);

        if(byExercise.equals("")){
            historyList = findByDaily(start, end);
        }else{
            historyList = findByDailyExercise(start, end, byExercise);
        }


        return historyList;
    }

    /**
     * X axis label
     * @param historyList
     * @return
     */
    private String[] searchHisDataDailyXlabels(List<History> historyList){

        int size = historyList.size();
        String[] xlabels = new String[size];

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


    /**
     * searchHisDataWeekly
     * @return
     */
    private List<History> searchHisDataWeekly() {

        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        LogUtil.d(TAG, "StartDate: "+ txStarDate);
        LogUtil.d(TAG, "EndDate: "+txEndDate);

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        List<History> historyList;

        String byExercise = searchBar.getText();
        LogUtil.d(TAG, "searchBar's text is : " + byExercise);

        if(byExercise.equals("")){
            historyList = findByWeekly(start, end);
        }else{
            historyList = findByWeeklyExercise(start, end, byExercise);
        }

        return historyList;
    }

    /**
     * searchHisDataWeeklyXlabels
     * @param historyList
     * @return
     */
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

    /**
     * searchHisDataMonthly
     * @return
     */
    private List<History> searchHisDataMonthly() {

        String txStarDate = tvStartDate.getText().toString();
        String txEndDate = tvEndDate.getText().toString();
        LogUtil.d(TAG, "StartDate: "+ txStarDate);
        LogUtil.d(TAG, "EndDate: "+txEndDate);

        long start = TimeUtils.string2Millis(txStarDate, sdf);
        long end = TimeUtils.string2Millis(txEndDate, sdf);

        List<History> historyList;

        String byExercise = searchBar.getText();
        LogUtil.d(TAG, "searchBar's text is : " + byExercise);
        if(byExercise.equals("")){
            historyList = findByMonthly(start, end);
        }else{
            historyList = findByMonthlyExercise(start, end, byExercise);
        }

        return historyList;
    }

    /**
     * searchHisDataMonthlyXlabels
     * @param historyList
     * @return
     */
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

    /**
     * search bar action
     */
    class MyOnSearchActionConfirmed extends SimpleOnSearchActionListener {

        private static final String TAG = "MySearchActionConfirmed";

        @Override
        public void onSearchStateChanged(boolean enabled) {
            super.onSearchStateChanged(enabled);
        }

        @Override
        public void onSearchConfirmed(CharSequence searchText) {
            LogUtil.d(TAG, "onSearchConfirmed: "+searchText.toString());

            // 隐藏输入框
            InputMethodManager imm = ( InputMethodManager) getView().getContext( ).getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm.isActive()) {
                imm.hideSoftInputFromWindow( getView().getApplicationWindowToken() , 0 );
            }

            // daily weekly monthly
            String by = tvDate.getText().toString();

            // show plots
            ll_pltgrp.setVisibility(View.VISIBLE);

            if(dateList.get(0).equals(by)){
                refreshView(Constants.DAILY);
            }else if(dateList.get(1).equals(by)){
                refreshView(Constants.WEEKLY);
            }else {
                refreshView(Constants.MONTHLY);
            }


            super.onSearchConfirmed(searchText);
        }

        @Override
        public void onButtonClicked(int buttonCode) {
            super.onButtonClicked(buttonCode);
        }
    }

}
