package com.android.hcframe.container.data;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.ContainerImageView;
import com.android.hcframe.container.ContainerRoundedImageView;
import com.android.hcframe.container.ContainerTextView;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-6-6 14:33.
 */
public class MultipleAppAdapter05 extends HcBaseAdapter<ViewInfo> {

    private ViewGroup.LayoutParams mParams;

    public MultipleAppAdapter05(Context context, List<ViewInfo> infos) {
        super(context, infos);
        // TODO Auto-generated constructor stub
        int width = HcUtil.getScreenWidth() / 3;
        mParams = new ViewGroup.LayoutParams(width, (int) (90 * HcUtil.getScreenDensity()));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        ViewInfo appInfo = getItem(position);
        ViewHolder mHolder;
        if (convertView == null) {
            mHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.container_item_multiple_grid_layout05, parent, false);
            convertView.setLayoutParams(mParams);
            mHolder.mIcon = (ContainerImageView) convertView.findViewById(R.id.container_item_grid05_img01);
            mHolder.mTitle = (ContainerTextView) convertView.findViewById(R.id.container_item_grid05_text01);
            mHolder.mSubTitle = (ContainerTextView) convertView.findViewById(R.id.container_item_grid05_text02);
            convertView.setTag(mHolder);
        } else {
            mHolder = (ViewHolder) convertView.getTag();
        }

//        ((ContainerRoundedImageView) mHolder.mIcon).setRadius(30);
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
