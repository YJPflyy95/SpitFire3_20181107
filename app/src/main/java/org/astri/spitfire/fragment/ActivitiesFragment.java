package org.astri.spitfire.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Toast;

import org.astri.spitfire.ListViewSlideAdapter;
import org.astri.spitfire.R;
import org.astri.spitfire.component.SlideListView;

import java.util.ArrayList;
import java.util.List;


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
public class ActivitiesFragment extends Fragment {


    private static final String TAG = "ActivitiesFragment";

    @Nullable
    private SlideListView listView;
    private List<String> list=new ArrayList<String>();
    private ListViewSlideAdapter listViewSlideAdapter;

    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_activities, container, false);
        Button add = view.findViewById(R.id.Add_bt);
        add.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content,new NewActivityFragment())
                        .commit();
            }
        });
        initView(view);
        getData();
        return view;
    }

    private void initView(View view){


        listView=view.findViewById(R.id.activity_list);
        listViewSlideAdapter=new ListViewSlideAdapter(getContext(),list);
        listView.setAdapter(listViewSlideAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new NewActivityFragment())
                        .commit();
                Toast.makeText(getContext(), "Click item" + i, Toast.LENGTH_SHORT).show();
            }
        });
        listViewSlideAdapter.setOnClickListenerEditOrDelete(new ListViewSlideAdapter.OnClickListenerEditOrDelete() {

            public void OnClickListenerEdit(int position) {

                // go to ActivityDetailFragment
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivityDetailFragment())
                        .commit();
//                finish();// if you want to click button to go back, then do not use finish()
                Toast.makeText(getActivity(), "edit position: " + position, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnClickListenerDelete(int position) {
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivityDetailFragment())
                        .commit();
                Toast.makeText(getActivity(), "delete position: " + position, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * getData for list
     */
    private void getData(){
        list.clear();
        list.add("Jogging");
        list.add("Gym");
        list.add("Exam");
    }
}
