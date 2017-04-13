package com.android.hcframe.im;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.HcBaseAdapter;
import com.android.hcframe.container.ContainerCircleImageView;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by pc on 2016/9/30.
 */
public class IMDiscussionGroupAllAdapter extends HcBaseAdapter<IMDissionGroupValue> {
    private List<IMDissionGroupValue> mIMDiscussionGroupItem;
    private LayoutInflater mInflater;

    public IMDiscussionGroupAllAdapter(Context context, List<IMDissionGroupValue> mIMDissionGroupValue) {
        super(context, mIMDissionGroupValue);
        mIMDiscussionGroupItem = mIMDissionGroupValue;
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DiscussionGroupHolder mDiscussionGroupHolder = null;
        IMDissionGroupValue imDissionGroupValue = mIMDiscussionGroupItem.get(position);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.im_discussion_group_all_item,
                    parent, false);
            mDiscussionGroupHolder = new DiscussionGroupHolder();
            mDiscussionGroupHolder.discussionGroupAllName = (TextView) convertView.findViewById(R.id.discussion_group_all_name);
            mDiscussionGroupHolder.discussionGroupAllImg = (ContainerCircleImageView) convertView.findViewById(R.id.discussion_group_all_img);
            convertView.setTag(mDiscussionGroupHolder);
        } else {
            mDiscussionGroupHolder = (DiscussionGroupHolder) convertView.getTag();
        }

        mDiscussionGroupHolder.discussionGroupAllName.setText(imDissionGroupValue.getmIMDissionGroupName());
        String url = HcUtil.getScheme() + "/terminalServer/file/getfile?fileid=" + imDissionGroupValue.getmIMDissionGroupUrl() + "&type=common";
        ImageLoader.getInstance().displayImage(url, mDiscussionGroupHolder.discussionGroupAllImg, HcUtil.getAccountImageOptions());
        return convertView;
    }

    public static class DiscussionGroupHolder {
        public TextView discussionGroupAllName;
        public ContainerCircleImageView discussionGroupAllImg;
    }
}
