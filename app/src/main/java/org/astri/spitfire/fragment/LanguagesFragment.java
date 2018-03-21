package org.astri.spitfire.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.astri.spitfire.Activities;
import org.astri.spitfire.HistoryActivity;
import org.astri.spitfire.HomeActivity;
import org.astri.spitfire.MeActivity;
import org.astri.spitfire.R;

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
public class LanguagesFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_languages, container, false);
        Button back = (Button) view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new MeFragment())
                        .commit();
            }
        });
        //        mBackButton.setOnClickListener(new View.OnClickListener(){
//            public void onClick(View v) {
//
//                FragmentManager fm = getActivity().getSupportFragmentManager();
//                fm.beginTransaction()
//                        .replace(R.id.ll_content,new MeFragment())
//                        .commit();
//            }
//        });
        Button save = (Button) view.findViewById(R.id.Save_bt);
        save.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new MeFragment())
                        .commit();
            }
        });
        return view;
    }
}
