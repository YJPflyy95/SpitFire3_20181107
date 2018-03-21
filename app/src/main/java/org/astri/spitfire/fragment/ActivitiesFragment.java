package org.astri.spitfire.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.astri.spitfire.Activities;
import org.astri.spitfire.ListViewSlideAdapter;
import org.astri.spitfire.NewActivity;
import org.astri.spitfire.R;
import org.astri.spitfire.component.SlideListView;
import org.astri.spitfire.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

import static org.astri.spitfire.util.ShowUtils.view;

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
        Button add = (Button) view.findViewById(R.id.Add_bt);
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

//        LogUtil.d("ActivitiesFragment","initView...");

        listView=view.findViewById(R.id.activity_list);
//        LogUtil.d(TAG, "initView: " + list.size());
        listViewSlideAdapter=new ListViewSlideAdapter(getContext(),list);
        listView.setAdapter(listViewSlideAdapter);
        //ListView item的点击事件
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
//                Intent intent = new Intent(getActivity(),ActivityDetailFragment);
//                startActivity(intent);
                FragmentManager fm = getActivity().getSupportFragmentManager();
                fm.beginTransaction()
                        .replace(R.id.ll_content, new ActivityDetailFragment())
                        .commit();
//                finish();//看你需不需要返回当前界面，如果点返回需要返回到当前界面，就不用这个
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
    private void getData(){
        list.clear();
        list.add("Jogging");
        list.add("Gym");
        list.add("Exam");
    }
}
