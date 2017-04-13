package com.android.hcmail;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.hcframe.email.R;


public class DeleteSelectPicPopupWindow extends PopupWindow {

    private TextView mHcmailReceiveBox;
    private TextView mHcmailSendBox;
    private TextView mHcmailDraftBox;
    private TextView mCancelText;
    private View mMenuView;

    public DeleteSelectPicPopupWindow(Activity context, OnClickListener itemsOnClick) {
        super(context);
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.hcmail_delete_expend_footer_popup, null);
        mHcmailReceiveBox = (TextView) mMenuView.findViewById(R.id.hcmail_receive_box);
        mHcmailSendBox = (TextView) mMenuView.findViewById(R.id.hcmail_send_box);
        mHcmailDraftBox = (TextView) mMenuView.findViewById(R.id.hcmail_draft_box);
        mCancelText = (TextView) mMenuView.findViewById(R.id.cancel_text);
        mMenuView.findViewById(R.id.email_popwindow).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });
        //取消按钮
        mCancelText.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                //销毁弹出框
                dismiss();
            }
        });
        //设置按钮监听
        mHcmailReceiveBox.setOnClickListener(itemsOnClick);
        mHcmailSendBox.setOnClickListener(itemsOnClick);
        mHcmailDraftBox.setOnClickListener(itemsOnClick);
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.MATCH_PARENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.AnimBottom);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0xb0000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
        //mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
        mMenuView.setOnTouchListener(new OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {

                int height = mMenuView.findViewById(R.id.pop_layout).getTop();
                int y = (int) event.getY();
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (y < height) {
                        dismiss();
                    }
                }
                return true;
            }
        });

    }

}
