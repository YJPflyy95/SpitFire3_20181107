package org.astri.spitfire.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import org.astri.spitfire.R;
import org.astri.spitfire.util.DensityUtils;

import java.util.List;

/**
 * <pre>
 *     author : ghf
 *     e-mail : 869862783@qq.com
 *     time   : 2018/1/31
 *     desc   : PopupWindow选择提示框列表适配器
 *     modified by :
 *     e-mail : xx_xxx@xx
 *     time   :
 *     desc   :
 *     version: 1.0
 * </pre>
 */
public class PopupWindowAdapter extends BaseAdapter {

    private Context context;
    private List<String> stringList;
    private int selectedPosition;

    public PopupWindowAdapter(Context context, List<String> stringList) {
        this.context = context;
        this.stringList = stringList;
    }

    @Override
    public int getCount() {
        return stringList.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_popup_window, parent, false);
            holder = new ViewHolder();
            holder.tv_item = (TextView) convertView.findViewById(R.id.tv_item);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (position == 0) {
            holder.tv_item.setPadding(DensityUtils.dp2px(context, 18),
                    DensityUtils.dp2px(context, 25), 12, DensityUtils.dp2px(context, 10));
        } else if (position == 1) {
            holder.tv_item.setPadding(DensityUtils.dp2px(context, 18),
                    DensityUtils.dp2px(context, 15), 12, DensityUtils.dp2px(context, 10));
        } else if (position == 2) {
            holder.tv_item.setPadding(DensityUtils.dp2px(context, 18),
                    DensityUtils.dp2px(context, 15), 12, DensityUtils.dp2px(context, 25));
        }

        holder.tv_item.setText(stringList.get(position));
        if (selectedPosition == position) {
            holder.tv_item.setTextColor(Color.parseColor("#3CB7EA"));
        } else {
            holder.tv_item.setTextColor(Color.parseColor("#3B434E"));
        }
        return convertView;
    }

    public class ViewHolder {
        TextView tv_item;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
    }
}
