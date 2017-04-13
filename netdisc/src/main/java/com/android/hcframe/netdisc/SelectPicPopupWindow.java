package com.android.hcframe.netdisc;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.android.hcframe.netdisc.R;

public class SelectPicPopupWindow extends PopupWindow {

	private LinearLayout mNetdiscPictureLinear;
	private LinearLayout mNetdiscMusicLinear;
	private LinearLayout mNetdiscVideoLinear;
	private LinearLayout mNetdiscFileLinear;
	private TextView mCancelText;
	private View mMenuView;

	public SelectPicPopupWindow(Activity context,OnClickListener itemsOnClick) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.netdisc_expend_footer_popup, null);
        mNetdiscPictureLinear = (LinearLayout) mMenuView.findViewById(R.id.netdisc_picture_linear);
		mNetdiscMusicLinear= (LinearLayout) mMenuView.findViewById(R.id.netdisc_music_linear);
		mNetdiscVideoLinear= (LinearLayout) mMenuView.findViewById(R.id.netdisc_video_linear);
		mNetdiscFileLinear= (LinearLayout) mMenuView.findViewById(R.id.netdisc_file_linear);
		mCancelText	= (TextView) mMenuView.findViewById(R.id.cancel_text);
		//取消按钮
		mCancelText.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
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
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}
				return true;
			}
		});

	}

}
