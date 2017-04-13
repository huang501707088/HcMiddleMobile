/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-11-24 下午7:23:25
*/
package com.android.hcframe.container.data;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.ContainerImageView;
import com.android.hcframe.container.ContainerTextView;

public class MultipleAppAdapter01 extends HcBaseAdapter<ViewInfo> {

    public MultipleAppAdapter01(Context context, List<ViewInfo> infos) {
        super(context, infos);
        // TODO Auto-generated constructor stub
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewInfo appInfo = getItem(position);
        ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.container_item_multiple_grid_layout01, parent, false);
            mHolder.mIcon = (ContainerImageView) convertView.findViewById(R.id.container_item_grid01_img01);
            mHolder.mTitle = (ContainerTextView) convertView.findViewById(R.id.container_item_grid01_text01);
            mHolder.mSubTitle = (ContainerTextView) convertView.findViewById(R.id.container_item_grid01_text02);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

        mHolder.mIcon.setValue(appInfo);
        mHolder.mTitle.setValue(appInfo);
        mHolder.mSubTitle.setValue(appInfo);
        return convertView;
    }

    private class ViewHolder {

        private ContainerImageView mIcon;
        private ContainerTextView mTitle;
        private ContainerTextView mSubTitle;
    }
}
