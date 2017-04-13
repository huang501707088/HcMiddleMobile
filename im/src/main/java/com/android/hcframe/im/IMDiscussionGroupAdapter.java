package com.android.hcframe.im;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.ContainerCircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.List;

/**
 * Created by pc on 2016/9/30.
 */
public class IMDiscussionGroupAdapter extends HcBaseAdapter<IMDissionGroupValue> {

    public IMDiscussionGroupAdapter(Context context, List<IMDissionGroupValue> mIMDissionGroupValue) {
        super(context, mIMDissionGroupValue);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DiscussionGroupHolder mDiscussionGroupHolder = null;
        IMDissionGroupValue imDissionGroupValue = getItem(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.im_discussion_group_item,
                    parent, false);
            mDiscussionGroupHolder = new DiscussionGroupHolder();
            mDiscussionGroupHolder.discussionGroupName = (TextView) convertView.findViewById(R.id.discussion_group_name);
            mDiscussionGroupHolder.discussionGroupImg = (ContainerCircleImageView) convertView.findViewById(R.id.discussion_group_img);
            mDiscussionGroupHolder.discussionGroupIcon = (TextView) convertView.findViewById(R.id.discussion_group_icon);
            convertView.setTag(mDiscussionGroupHolder);
        } else {
            mDiscussionGroupHolder = (DiscussionGroupHolder) convertView.getTag();
        }
//        HcLog.D("IMDiscussionGroupAdapter#getView imDissionGroupValue = "+imDissionGroupValue + " position = "+position);
        if (("0").equals(imDissionGroupValue.getmIMDissionGroupUrl())) {
//            mDiscussionGroupHolder.discussionGroupImg.setImageResource(R.drawable.im_icon_add);
            mDiscussionGroupHolder.discussionGroupImg.setVisibility(View.GONE);
            mDiscussionGroupHolder.discussionGroupIcon.setVisibility(View.VISIBLE);
            mDiscussionGroupHolder.discussionGroupName.setText("");
        } else if (("1").equals(imDissionGroupValue.getmIMDissionGroupUrl())) {
//            mDiscussionGroupHolder.discussionGroupImg.setImageResource(R.drawable.im_icon_minus);
            mDiscussionGroupHolder.discussionGroupImg.setVisibility(View.GONE);
            mDiscussionGroupHolder.discussionGroupIcon.setVisibility(View.VISIBLE);
            mDiscussionGroupHolder.discussionGroupIcon.setText("—");
            mDiscussionGroupHolder.discussionGroupName.setText("");
        } else {
            mDiscussionGroupHolder.discussionGroupImg.setVisibility(View.VISIBLE);
            mDiscussionGroupHolder.discussionGroupIcon.setVisibility(View.GONE);
            mDiscussionGroupHolder.discussionGroupName.setText(imDissionGroupValue.getmIMDissionGroupName());
            String url = HcUtil.getHeaderUri(imDissionGroupValue.getmIMDissionGroupUrl());
            HcLog.D("url=" + url);
            //此处返回值要判断
            ImageLoader.getInstance().displayImage(url, mDiscussionGroupHolder.discussionGroupImg, HcUtil.getAccountImageOptions());
        }
        return convertView;
    }

    public static class DiscussionGroupHolder {
        public TextView discussionGroupName;
        public TextView discussionGroupIcon;
        public ContainerCircleImageView discussionGroupImg;
    }
}
