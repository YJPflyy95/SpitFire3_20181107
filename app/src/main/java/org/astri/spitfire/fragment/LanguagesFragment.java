package org.astri.spitfire.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
        Button back = view.findViewById(R.id.Back_bt);
        back.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new MeFragment())
                        .commit();
            }
        });
        Button save = view.findViewById(R.id.Save_bt);
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
