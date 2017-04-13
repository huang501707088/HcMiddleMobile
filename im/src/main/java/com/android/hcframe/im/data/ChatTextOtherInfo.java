package com.android.hcframe.im.data;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcConfig;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.im.ChatActivity;
import com.android.hcframe.im.IMUtil;
import com.android.hcframe.im.R;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-8 16:16.
 */
public class ChatTextOtherInfo extends ChatMessageInfo {

    public ChatTextOtherInfo() {
        super();
        mType = 1;
        mIsOwn = false;
    }

    @Override
    public ViewHolderBase<ChatMessageInfo> createViewHolder(final boolean group) {
        return new ViewHolderBase<ChatMessageInfo>() {

            private TextView mDate;

            private TextView mName;

            private TextView mContent;

            private ImageView mIcon;

            private View mDivider;

            @Override
            public View createView(LayoutInflater inflater) {
                View item = inflater.inflate(R.layout.im_chatting_msg_item_other, null);
                mDate = (TextView) item.findViewById(R.id.chat_item_other_date);
                mName = (TextView) item.findViewById(R.id.chat_item_other_name);
                mContent = (TextView) item.findViewById(R.id.chat_item_other_content);
                mIcon = (ImageView) item.findViewById(R.id.chat_item_other_icon);
                mDivider = item.findViewById(R.id.chat_item_other_divider);
                return item;
            }

            @Override
            public void setItemData(int position, final ChatMessageInfo data) {
                if (data.isShowDate()) {
                    if (mDivider.getVisibility() != View.GONE)
                        mDivider.setVisibility(View.GONE);
                    if (mDate.getVisibility() != View.VISIBLE)
                        mDate.setVisibility(View.VISIBLE);
                    String formatDate = data.getFormatDate();
                    if (TextUtils.isEmpty(formatDate)) {
                        formatDate = IMUtil.getChatDate(HcApplication.getContext(), Long.valueOf(data.getDate()));
                        data.setFormatDate(formatDate);
                    }
                    mDate.setText(formatDate);
                } else {
                    if (mDate.getVisibility() != View.GONE)
                        mDate.setVisibility(View.GONE);
                    if (mDivider.getVisibility() != View.VISIBLE)
                        mDivider.setVisibility(View.VISIBLE);
                }
                if (group) {
                    mName.setVisibility(View.VISIBLE);
                    mName.setText(data.getName());
                }
                SpannableString spannable = IMUtil.getExpressionString(HcApplication.getContext(), data.getContent(), 20);
                mContent.setText(spannable);
//                mContent.setText(data.getContent());
                ImageLoader.getInstance().displayImage(HcUtil.getHeaderUri(data.getUserId()), mIcon, HcUtil.getAccountImageOptions());

                mContent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        Context context = v.getContext();
                        if (context instanceof ChatActivity) {
                            String[] list = HcConfig.getConfig().assertModule(HcConfig.Module.TASK) ? new String[] {"复制", "转发", "删除", "盯任务"} : new String[] {"复制", "转发", "删除"};
                            ((ChatActivity) context).showListDialog(list , data);
                            return true;
                        }
                        return false;
                    }
                });
            }
        };
    }

    @Override
    public int getState() {
        return 0;
    }
}
