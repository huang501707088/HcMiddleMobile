package com.android.hcframe.internalservice.sign;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;

import com.android.hcframe.internalservice.signin.R;

public class StationInfoPopupWindow {
    private Context context;
    private PopupWindow popupWindow;

    public StationInfoPopupWindow(final Context context) {
        this.context = context;

        View view = LayoutInflater.from(context).inflate(R.layout.pop_window, null);

        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        // 这个是为了点击“返回Back”也能使其消失，而且并不会影响你的背景（非常奇妙的）
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources()));
    }

    // 下拉式 弹出 pop菜单 parent 右下角
    public void showAsDropDown(View parent) {
        // 保证尺寸是依据屏幕像素密度来的
//        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);
        popupWindow.showAsDropDown(parent);
        // 使其聚集
        popupWindow.setFocusable(false);
        // 设置同意在外点击消失
        popupWindow.setOutsideTouchable(false);
        // 刷新状态
        popupWindow.update();
    }


    // 隐藏菜单
    public void dismiss() {
        popupWindow.dismiss();
    }

    // 是否显示
    public boolean isShowing() {
        return popupWindow.isShowing();
    }

//    @Override
//    public void onClick(View v) {
//        if (v == goBtn) {
    //这里跳转到路径规划界面
//            Intent intent = new Intent(context, RouteActivity.class);
//            context.startActivity(intent);

//        }
//    }

}