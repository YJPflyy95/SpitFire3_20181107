package org.astri.spitfire.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.astri.spitfire.R;

import java.util.List;

/**
 * <pre>
 *     author : HuGuoDong
 *     e-mail : guodong_hu@126.com
 *     time   : 2018/07/12
 *     desc   :
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class AlgorithmAdapter extends ArrayAdapter<Algorithm> {

    private int resourceId;

    public AlgorithmAdapter(@NonNull Context context, int textViewResourceId, @NonNull List<Algorithm> objects) {
        super(context, textViewResourceId, objects);
        resourceId = textViewResourceId;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Algorithm algorithm = getItem(position);

        View view;
        ViewHolder viewHolder;
        if(convertView == null){
            view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            viewHolder = new ViewHolder();

            // 算法文本
            viewHolder.algText = view.findViewById(R.id.algorithm_index);
            // 算法选择的check图标
            viewHolder.algImage = view.findViewById(R.id.algorithm_index_img);
            view.setTag(viewHolder); // 将viewHolder存在view中
        }else{
            view = convertView;
            viewHolder = (ViewHolder) view.getTag();
        }
//        viewHolder.algImage.setImageResource(R.drawable.checkmark);
        viewHolder.algText.setText(algorithm.getName());
        return view;
    }

    /**
     * 内部类，用于对控件的实例进行缓存
     */
    class ViewHolder{
        TextView algText;
        ImageView algImage;
    }
}
