package com.android.hcframe.view.selector;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-8 14:14.
 */
public class PersonnelChooseAdapter extends HcBaseAdapter<ItemInfo> {

    public PersonnelChooseAdapter(Context context, List<ItemInfo> infos) {
        super(context, infos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ItemInfo itemInfo = getItem(position);
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.choose_personnel_item_layout, parent, false);
            holder.mBox = (CheckBox) convertView.findViewById(R.id.choose_personnel_item_checkbox);
            holder.mGoBtn = (ImageView) convertView.findViewById(R.id.choose_personnel_item_go_btn);
            holder.mIcon = (ImageView) convertView.findViewById(R.id.choose_personnel_item_icon);
            holder.mName = (TextView) convertView.findViewById(R.id.choose_personnel_item_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (itemInfo instanceof StaffInfo) { // 员工
            if (holder.mGoBtn.getVisibility() != View.GONE)
                holder.mGoBtn.setVisibility(View.GONE);
            if (itemInfo.isMultipled()) {
                if (holder.mBox.getVisibility() != View.VISIBLE)
                    holder.mBox.setVisibility(View.VISIBLE);
                holder.mBox.setChecked(itemInfo.isSelected());
            } else {
                if (holder.mBox.getVisibility() != View.GONE)
                    holder.mBox.setVisibility(View.GONE);
            }

        } else {
            if (holder.mBox.getVisibility() != View.GONE)
                holder.mBox.setVisibility(View.GONE);
            if (holder.mGoBtn.getVisibility() != View.VISIBLE)
                holder.mGoBtn.setVisibility(View.VISIBLE);
        }

        holder.mName.setText(itemInfo.getItemValue());
        ImageLoader.getInstance().displayImage(itemInfo.getIconUrl(), holder.mIcon, HcUtil.getAccountImageOptions());

        return convertView;
    }

    private class ViewHolder {

        ImageView mIcon;

        TextView mName;

        ImageView mGoBtn;

        CheckBox mBox;
    }
}
