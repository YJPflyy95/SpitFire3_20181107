package org.astri.spitfire.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.astri.spitfire.R;
import org.astri.spitfire.ReadinessDetailActivity;
import org.astri.spitfire.ReadinessReadingActivity;
import org.astri.spitfire.ble.Fragment.BLEMainFragment;

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
public class HomeFragment extends Fragment {

    /**
     * Home View
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView TodayReadiness = view.findViewById(R.id.TodayReadiness_tv);
        TodayReadiness.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });
        ImageView todayReadiness = view.findViewById(R.id.todayReadiness_iv);
        todayReadiness.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });
        TextView Today_down = view.findViewById(R.id.Today_down_tv);
        Today_down.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });
        TextView HRVScores = view.findViewById(R.id.HRVScores_tv);
        HRVScores.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });
        TextView Today = view.findViewById(R.id.Today_tv);
        Today.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });
        TextView Yesterday = view.findViewById(R.id.Yesterday_tv);
        Yesterday.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });
        TextView Baseline = view.findViewById(R.id.Baseline_tv);
        Baseline.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessDetailActivity.class);
                startActivity(intent);
            }
        });

        Button test = (Button) view.findViewById(R.id.Test_bt);
        test.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v)
            {
                Intent intent = new Intent(getActivity(),ReadinessReadingActivity.class);
                startActivity(intent);
            }
        });

        Button AddNow = (Button) view.findViewById(R.id.AddNow_bt);
        AddNow.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new BLEMainFragment())
                        .commit();
            }
        });
        Button Completeyourprofilebt = (Button) view.findViewById(R.id.Completeyourprofilebt);
        Completeyourprofilebt.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new MeFragment())
                        .commit();
            }
        });
        TextView Addyourspitfire = (TextView) view.findViewById(R.id.Addyourspitfire_tv);
        Addyourspitfire.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new MeFragment())
                        .commit();
            }
        });
        return view;
    }
}
