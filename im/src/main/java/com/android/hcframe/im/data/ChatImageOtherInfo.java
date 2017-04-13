package com.android.hcframe.im.data;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.hcframe.BigImageActivity;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.adapter.ViewHolderBase;
import com.android.hcframe.im.ChatActivity;
import com.android.hcframe.im.IMUtil;
import com.android.hcframe.im.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.ImageSize;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-10-9 16:43.
 */

public class ChatImageOtherInfo extends ChatMessageInfo {

    private String mFilePath;

    public ChatImageOtherInfo() {
        super();
        mType = 2;
        mIsOwn = false;
    }

    @Override
    public ViewHolderBase<ChatMessageInfo> createViewHolder(final boolean group) {
        return new ViewHolderBase<ChatMessageInfo>() {

            private TextView mDate;

            private TextView mName;

            private ImageView mContent;

            private ImageView mIcon;

            private View mDivider;

            @Override
            public View createView(LayoutInflater inflater) {
                View item = inflater.inflate(R.layout.im_chatting_image_item_other, null);
                mDate = (TextView) item.findViewById(R.id.chat_image_item_other_date);
                mName = (TextView) item.findViewById(R.id.chat_image_item_other_name);
                mContent = (ImageView) item.findViewById(R.id.chat_image_item_other_image);
                mIcon = (ImageView) item.findViewById(R.id.chat_image_item_other_icon);
                mDivider = item.findViewById(R.id.chat_image_item_other_divider);
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
                mContent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context context = mContent.getContext();
                        if (context instanceof ChatActivity) {
                            Intent intent = new Intent(context, BigImageActivity.class);
                            intent.putExtra("uri", "file://" + data.getFilePath());
                            context.startActivity(intent);
                            ((ChatActivity)context).overridePendingTransition(0, 0);
                        }

                    }
                });

                ImageLoader.getInstance().displayImage(HcUtil.getHeaderUri(data.getUserId()), mIcon, HcUtil.getAccountImageOptions());

                Bitmap imageSrc = IMUtil.getImage(data.getMessageId());
                if (imageSrc != null) {
                    mContent.setImageBitmap(imageSrc);
                    return;
                }

                File image = IMUtil.fileExist(data.getFilePath(), data.getChatId());
                if (!image.exists()) {
                    ImageLoader.getInstance().loadImage("file://" + data.getFilePath(), new ImageSize((int) (140 * HcUtil.getScreenDensity()), (int) (140 * HcUtil.getScreenDensity())),
                            IMUtil.getImageOptions(), new ImageLoadingListener() {
                                @Override
                                public void onLoadingStarted(String s, View view) {
                                    HcLog.D("ChatImageOtherInfo #onLoadingStarted s = "+s);
                                }

                                @Override
                                public void onLoadingFailed(String s, View view, FailReason failReason) {
                                    HcLog.D("ChatImageOtherInfo #onLoadingStarted s = "+s + " failReason type ="+failReason.getType());
                                }

                                @Override
                                public void onLoadingComplete(String s, View view, Bitmap bitmap) {
                                    HcLog.D("ChatImageOtherInfo #onLoadingComplete bitmap width= " + bitmap.getWidth() + " height = " + bitmap.getHeight() + " view = "+view);
                                    File image = IMUtil.fileExist(data.getFilePath(), data.getChatId());
                                    if (!image.exists()) {
                                        bitmap = IMUtil.saveImage(image, bitmap);
                                    }
                                    mContent.setImageBitmap(IMUtil.getRoundCornerImage(HcApplication.getContext(), R.drawable.im_chatting_msg_other_bg_normal, bitmap, data.getMessageId()));

                                }

                                @Override
                                public void onLoadingCancelled(String s, View view) {

                                }
                            });
                } else {
                    Bitmap src = BitmapFactory.decodeFile(image.getAbsolutePath());
                    if (src != null) {
                        HcLog.D("ChatImageOtherInfo 缩略图已经存在 bitmap width= " + src.getWidth() + " height = " + src.getHeight());
//                        Drawable drawable = mContent.getDrawable();
                        mContent.setImageBitmap(IMUtil.getRoundCornerImage(HcApplication.getContext(), R.drawable.im_chatting_msg_other_bg_normal, src, data.getMessageId()));

//                        if (drawable instanceof BitmapDrawable) {
//                            Bitmap old = ((BitmapDrawable) drawable).getBitmap();
//                            if (old != null && !old.isRecycled()) {
//                                old.recycle();
//                                old = null;
//                            }
//                        }
                    }
                }

                mContent.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (v.getContext() instanceof ChatActivity) {
                            ChatActivity activity = (ChatActivity) v.getContext();
                            activity.showListDialog(new String[] {"转发", "删除"}, data);
                        }
                        return true;
                    }
                });
            }
        };
    }

    public String getFilePath() {
        return mFilePath;
    }

    public void setFilePath(String filePath) {
        mFilePath = filePath;
    }

    @Override
    public int getState() {
        return 0;
    }
}
