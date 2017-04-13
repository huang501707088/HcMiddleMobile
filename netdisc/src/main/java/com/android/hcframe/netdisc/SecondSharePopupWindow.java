package com.android.hcframe.netdisc;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class SecondSharePopupWindow extends PopupWindow {

    private LinearLayout mNetdiscPictureLinear;
    private LinearLayout mNetdiscMusicLinear;
    private LinearLayout mNetdiscVideoLinear;
    private LinearLayout mNetdiscFileLinear;
    private TextView mCancelText, netdisc_id_tv_name, netdisc_id_tv_link, netdisc_id_tv_code;
    private View mMenuView;
    ImageView netdosc_id_iv_lock;
    Activity mContext;

    public SecondSharePopupWindow(Activity context, OnClickListener itemsOnClick, String link, String code, String name) {
        super(context);
        mContext = context;
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.netdisc_second_share_popup, null);
        mNetdiscPictureLinear = (LinearLayout) mMenuView.findViewById(R.id.netdisc_picture_linear);
        mNetdiscMusicLinear = (LinearLayout) mMenuView.findViewById(R.id.netdisc_music_linear);
        mNetdiscVideoLinear = (LinearLayout) mMenuView.findViewById(R.id.netdisc_video_linear);
        mNetdiscFileLinear = (LinearLayout) mMenuView.findViewById(R.id.netdisc_file_linear);
        netdisc_id_tv_name = (TextView) mMenuView.findViewById(R.id.netdisc_id_tv_name);
        netdisc_id_tv_name.setText(name);
        netdosc_id_iv_lock = (ImageView) mMenuView.findViewById(R.id.netdosc_id_iv_lock);
        netdisc_id_tv_link = (TextView) mMenuView.findViewById(R.id.netdisc_id_tv_link);
        netdisc_id_tv_code = (TextView) mMenuView.findViewById(R.id.netdisc_id_tv_code);
        netdisc_id_tv_link.setText(link);
        if (TextUtils.isEmpty(code)) {
            netdosc_id_iv_lock.setVisibility(View.INVISIBLE);
        } else {
            netdosc_id_iv_lock.setVisibility(View.VISIBLE);
            netdisc_id_tv_code.setText(code);
        }
        mCancelText = (TextView) mMenuView.findViewById(R.id.cancel_text);
        //取消按钮
        mCancelText.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                backgroundAlpha(1);
                //销毁弹出框
                dismiss();
            }
        });
        //设置按钮监听
        mNetdiscPictureLinear.setOnClickListener(itemsOnClick);
        mNetdiscMusicLinear.setOnClickListener(itemsOnClick);
        mNetdiscVideoLinear.setOnClickListener(itemsOnClick);
        mNetdiscFileLinear.setOnClickListener(itemsOnClick);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        this.setTouchable(true);
        this.setBackgroundDrawable(new BitmapDrawable());
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1);
            }
        });
        this.setOutsideTouchable(false);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        backgroundAlpha(1);
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = mContext.getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        mContext.getWindow().setAttributes(lp);
    }

}
