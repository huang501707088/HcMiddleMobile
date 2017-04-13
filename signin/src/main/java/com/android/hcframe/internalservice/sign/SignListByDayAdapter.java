package com.android.hcframe.internalservice.sign;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.BigImageActivity;
import com.android.hcframe.HcLog;
import com.android.hcframe.internalservice.signcls.SignImgList;
import com.android.hcframe.internalservice.signcls.SignListByDay;
import com.android.hcframe.internalservice.signin.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

import java.util.List;

/**
 * Created by pc on 2016/5/27.
 */
class SignListByDayAdapter extends BaseAdapter {
    private List<SignListByDay> signListByDay;
    private Context mContext;
    private final int textType = 0;
    private final int imgType = 1;
    private ImageLoader mImageLoader;

    private DisplayImageOptions mOptions;

    public SignListByDayAdapter(Context context, List<SignListByDay> list) {
        mContext = context;
        signListByDay = list;
        mOptions = new DisplayImageOptions.Builder()
                .imageScaleType(ImageScaleType.EXACTLY)
                .showImageOnLoading(R.drawable.default_icon)
                .showImageForEmptyUri(R.drawable.default_icon)
                .showImageOnFail(R.drawable.default_icon).cacheInMemory(true)
                .cacheOnDisk(true).considerExifParams(true)
                .bitmapConfig(Bitmap.Config.ARGB_8888).build();
        mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return signListByDay.size();
    }

    @Override
    public Object getItem(int position) {
        return signListByDay.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public int getItemViewType(int position) {
        if (signListByDay.get(position).getSignImgList() != null && !signListByDay.get(position).getSignImgList().isEmpty()) {
            return imgType;
        } else {
            return textType;
        }
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SignListByDayViewHolder signListByDayViewHolder = null;
        SignListByDayOtherViewHolder signListByDayOtherViewHolder = null;
        int type = getItemViewType(position);
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            // 按当前所需的样式，确定new的布局
            switch (type) {
                case textType:
                    if (!TextUtils.isEmpty(signListByDay.get(position).getSignInTime()) && !TextUtils.isEmpty(signListByDay.get(position).getSignOutTime())) {
                        convertView = inflater.inflate(R.layout.sign_list_by_day_item,
                                parent, false);
                        signListByDayViewHolder = new SignListByDayViewHolder();
                        signListByDayViewHolder.time = (TextView) convertView
                                .findViewById(R.id.time);
                        signListByDayViewHolder.outTime = (TextView) convertView
                                .findViewById(R.id.out_time);
                        signListByDayViewHolder.address = (TextView) convertView
                                .findViewById(R.id.address);
                        signListByDayViewHolder.remark = (TextView) convertView
                                .findViewById(R.id.remark);
                        signListByDayViewHolder.outAddress = (TextView) convertView
                                .findViewById(R.id.out_address);
                        signListByDayViewHolder.outRemark = (TextView) convertView
                                .findViewById(R.id.out_remark);
                        signListByDayViewHolder.item = (LinearLayout) convertView
                                .findViewById(R.id.item);
                        signListByDayViewHolder.outItem = (LinearLayout) convertView
                                .findViewById(R.id.out_item);
                    } else {
                        convertView = inflater.inflate(R.layout.sign_list_by_day_one_item,
                                parent, false);
                        signListByDayViewHolder = new SignListByDayViewHolder();
                        signListByDayViewHolder.item = (LinearLayout) convertView
                                .findViewById(R.id.item);
                        signListByDayViewHolder.oneTime = (TextView) convertView
                                .findViewById(R.id.time);
                        signListByDayViewHolder.remark = (TextView) convertView
                                .findViewById(R.id.remark);
                        signListByDayViewHolder.address = (TextView) convertView
                                .findViewById(R.id.address);
                    }
                    convertView.setTag(signListByDayViewHolder);
                    break;
                case imgType:
                    convertView = inflater.inflate(R.layout.sign_list_by_day_other_item,
                            parent, false);
                    signListByDayOtherViewHolder = new SignListByDayOtherViewHolder();
                    signListByDayOtherViewHolder.time = (TextView) convertView
                            .findViewById(R.id.time);
                    signListByDayOtherViewHolder.addressText = (TextView) convertView
                            .findViewById(R.id.address_text);
                    signListByDayOtherViewHolder.signText = (TextView) convertView
                            .findViewById(R.id.sign_text);
                    signListByDayOtherViewHolder.addressImg1 = (ImageView) convertView
                            .findViewById(R.id.address_img1);
                    signListByDayOtherViewHolder.addressImg2 = (ImageView) convertView
                            .findViewById(R.id.address_img2);
                    signListByDayOtherViewHolder.addressImg3 = (ImageView) convertView
                            .findViewById(R.id.address_img3);
                    signListByDayOtherViewHolder.addressImg4 = (ImageView) convertView
                            .findViewById(R.id.address_img4);
                    convertView.setTag(signListByDayOtherViewHolder);
                    break;
                default:
                    break;
            }
        } else {
            switch (type) {
                case textType:
                    signListByDayViewHolder = (SignListByDayViewHolder) convertView.getTag();
                    break;
                case imgType:
                    signListByDayOtherViewHolder = (SignListByDayOtherViewHolder) convertView.getTag();
                    break;
                default:
                    break;
            }
        }
        // 设置资源
        switch (type) {
            case textType:
                if (!TextUtils.isEmpty(signListByDay.get(position).getSignInTime()) && TextUtils.isEmpty(signListByDay.get(position).getSignOutTime())) {
                    signListByDayViewHolder.oneTime.setText(signListByDay.get(position).getSignInTime());
                    signListByDayViewHolder.address.setText(signListByDay.get(position).getAddressName());
                    String remark = signListByDay.get(position).getRemark();
                    if (remark != null) {
                        HcLog.D("remark =" + remark);
                        signListByDayViewHolder.remark.setText(remark);
                    } else {
                        //remove该布局，使整体居中
                        //                         signListByDayViewHolder.item.removeViewAt(1);
                        signListByDayViewHolder.item.getChildAt(1).setVisibility(View.GONE);
                    }
                } else if (TextUtils.isEmpty(signListByDay.get(position).getSignInTime()) && !TextUtils.isEmpty(signListByDay.get(position).getSignOutTime())) {
                    signListByDayViewHolder.oneTime.setText(signListByDay.get(position).getSignOutTime());
                    signListByDayViewHolder.address.setText(signListByDay.get(position).getAddressName());
                    String remark = signListByDay.get(position).getRemark();
                    if (remark != null) {
                        HcLog.D("remark =" + remark);
                        signListByDayViewHolder.remark.setText(remark);
                    } else {
                        //remove该布局，使整体居中
                        signListByDayViewHolder.item.getChildAt(1).setVisibility(View.GONE);
                    }
                } else if (!TextUtils.isEmpty(signListByDay.get(position).getSignInTime()) && !TextUtils.isEmpty(signListByDay.get(position).getSignOutTime())) {
                    signListByDayViewHolder.time.setText(signListByDay.get(position).getSignInTime());
                    signListByDayViewHolder.address.setText(signListByDay.get(position).getAddressName());
                    String remark = signListByDay.get(position).getRemark();
                    if (remark != null) {
                        HcLog.D("remark =" + remark);
                        signListByDayViewHolder.remark.setText(remark);
                    } else {
                        //remove该布局，使整体居中
                        signListByDayViewHolder.item.getChildAt(1).setVisibility(View.GONE);
                    }
                    signListByDayViewHolder.outTime.setText(signListByDay.get(position).getSignOutTime());
                    signListByDayViewHolder.outAddress.setText(signListByDay.get(position).getAddressName());
                    if (remark != null) {
                        HcLog.D("remark =" + remark);
                        signListByDayViewHolder.outRemark.setText(remark);
                    } else {
                        //remove该布局，使整体居中
                        signListByDayViewHolder.item.getChildAt(1).setVisibility(View.GONE);
                    }
                }
                break;
            case imgType:
                signListByDayOtherViewHolder.time.setText(signListByDay.get(position).getSignInTime());
                signListByDayOtherViewHolder.addressText.setText(signListByDay.get(position).getAddressName());
                if (TextUtils.isEmpty(signListByDay.get(position).getRemark())) {
                    signListByDayOtherViewHolder.signText.setVisibility(View.GONE);
                } else {
                    signListByDayOtherViewHolder.signText.setText(signListByDay.get(position).getRemark());
                }
                final List<SignImgList> signImgLists = signListByDay.get(position).getSignImgList();
                if (signImgLists != null && signImgLists.size() > 0) {
                    switch (signImgLists.size()) {
                        case 1:
                            signListByDayOtherViewHolder.addressImg1.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg2.setVisibility(View.GONE);
                            signListByDayOtherViewHolder.addressImg3.setVisibility(View.GONE);
                            signListByDayOtherViewHolder.addressImg4.setVisibility(View.GONE);
                            mImageLoader.displayImage(signImgLists.get(0).getFilePath(), signListByDayOtherViewHolder.addressImg1, mOptions);
                            break;
                        case 2:
                            signListByDayOtherViewHolder.addressImg1.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg2.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg3.setVisibility(View.GONE);
                            signListByDayOtherViewHolder.addressImg4.setVisibility(View.GONE);
                            mImageLoader.displayImage(signImgLists.get(0).getFilePath(), signListByDayOtherViewHolder.addressImg1, mOptions);
                            mImageLoader.displayImage(signImgLists.get(1).getFilePath1(), signListByDayOtherViewHolder.addressImg2, mOptions);
                            break;
                        case 3:
                            signListByDayOtherViewHolder.addressImg1.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg2.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg3.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg4.setVisibility(View.GONE);
                            mImageLoader.displayImage(signImgLists.get(0).getFilePath(), signListByDayOtherViewHolder.addressImg1, mOptions);
                            mImageLoader.displayImage(signImgLists.get(1).getFilePath1(), signListByDayOtherViewHolder.addressImg2, mOptions);
                            mImageLoader.displayImage(signImgLists.get(2).getFilePath2(), signListByDayOtherViewHolder.addressImg3, mOptions);
                            break;
                        case 4:
                            signListByDayOtherViewHolder.addressImg1.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg2.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg3.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg4.setVisibility(View.VISIBLE);
                            mImageLoader.displayImage(signImgLists.get(0).getFilePath(), signListByDayOtherViewHolder.addressImg1, mOptions);
                            mImageLoader.displayImage(signImgLists.get(1).getFilePath1(), signListByDayOtherViewHolder.addressImg2, mOptions);
                            mImageLoader.displayImage(signImgLists.get(2).getFilePath2(), signListByDayOtherViewHolder.addressImg3, mOptions);
                            mImageLoader.displayImage(signImgLists.get(3).getFilePath3(), signListByDayOtherViewHolder.addressImg4, mOptions);
                            break;
                        default:
                            signListByDayOtherViewHolder.addressImg1.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg2.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg3.setVisibility(View.VISIBLE);
                            signListByDayOtherViewHolder.addressImg4.setVisibility(View.VISIBLE);
                            mImageLoader.displayImage(signImgLists.get(0).getFilePath(), signListByDayOtherViewHolder.addressImg1, mOptions);
                            mImageLoader.displayImage(signImgLists.get(1).getFilePath(), signListByDayOtherViewHolder.addressImg2, mOptions);
                            mImageLoader.displayImage(signImgLists.get(2).getFilePath(), signListByDayOtherViewHolder.addressImg3, mOptions);
                            mImageLoader.displayImage(signImgLists.get(3).getFilePath(), signListByDayOtherViewHolder.addressImg4, mOptions);
                            break;
                    }
                    signListByDayOtherViewHolder.addressImg1.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startBigImageActivity(signImgLists.get(0).getFilePath());
                        }
                    });
                    signListByDayOtherViewHolder.addressImg2.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startBigImageActivity(signImgLists.get(1).getFilePath1());
                        }
                    });
                    signListByDayOtherViewHolder.addressImg3.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startBigImageActivity(signImgLists.get(2).getFilePath2());
                        }
                    });
                    signListByDayOtherViewHolder.addressImg4.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startBigImageActivity(signImgLists.get(3).getFilePath3());
                        }
                    });

                }
                break;
            default:
                break;
        }

        return convertView;
    }

    private void startBigImageActivity(String uri) {
        Intent intent = new Intent(mContext, BigImageActivity.class);
        intent.putExtra("uri", uri);
        mContext.startActivity(intent);
        ((Activity) mContext).overridePendingTransition(0, 0);
    }

    public class SignListByDayViewHolder {
        private TextView time;
        private TextView remark;
        private TextView outTime;
        private TextView oneTime;
        private TextView address;
        private TextView outAddress;
        private TextView outRemark;
        private LinearLayout outItem;
        private LinearLayout item;

    }

    public class SignListByDayOtherViewHolder {
        private TextView time;
        private TextView addressText;
        private TextView signText;
        private ImageView addressImg1;
        private ImageView addressImg2;
        private ImageView addressImg3;
        private ImageView addressImg4;
    }
}