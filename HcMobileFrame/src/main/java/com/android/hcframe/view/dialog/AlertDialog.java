package com.android.hcframe.view.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.hcframe.R;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-3-21 14:10.
 */

public class AlertDialog {

    private static TextView mTitle;

    private static EditText mEditContent;

    private static TextView mTextContent;

    private static TextView mCanel;

    private static TextView mOk;

    private static TextView mSingBtn;

    private static LinearLayout mBtnParent;

    private static OnClickListener mListener;

    private static android.app.AlertDialog mAlertDialog;

    private AlertDialog() {};

    /**
     * 显示一个有标题并且带有两个按钮可以编辑内容的对话框
     * @param context
     * @param title 对话框标题
     * @param editContent 编辑框的初始显示,可以为空
     * @param listener 按钮的监听器
     */
    public static void showEditableDialog(Context context, String title, String editContent, OnClickListener listener) {
        showDialog(context, title, "", editContent, listener, true, false, true);
    }

    /**
     * 显示一个有标题并且带有两个按钮可以编辑内容的对话框
     * @param context
     * @param title 对话框标题
     * @param hintContent 编辑框提示的内容
     * @param editContent 编辑框的初始显示,可以为空
     * @param listener 按钮的监听器
     */
    public static void showEditableDialog(Context context, String title, String hintContent, String editContent, OnClickListener listener) {
        showDialog(context, title, hintContent, editContent, listener, true, false, true);
    }

    /**
     * 显示一个没有标题但有两个按钮可以编辑内容的对话框
     * @param context
     * @param hintContent 编辑框提示的内容
     * @param listener 按钮的监听器
     */
    public static void showUntitledEditableDialog(Context context, String hintContent, OnClickListener listener) {
        showDialog(context, "", hintContent, "", listener, false, false, true);
    }

    /**
     * 显示一个没有标题但有两个按钮可以编辑内容的对话框
     * @param context
     * @param hintContent 编辑框提示的内容
     * @param editContent 编辑框的初始显示,可以为空
     * @param listener 按钮的监听器
     */
    public static void showUntitledEditableDialog(Context context, String hintContent, String editContent, OnClickListener listener) {
        showDialog(context, "", hintContent, editContent, listener, false, false, true);
    }

    /**
     * 显示一个有标题但带有两个按钮的不可以编辑内容的对话框
     * @param context
     * @param title 对话框的标题
     * @param content 对话框的内容
     * @param listener 按钮的监听器
     */
    public static void showUneditableDialog(Context context, String title, String content, OnClickListener listener) {
        showDialog(context, title, "", content, listener, true, false, false);
    }

    /**
     * 显示一个没有标题且带有两个按钮的不可以编辑内容的对话框
     * @param context
     * @param content 对话框的内容
     * @param listener 按钮的监听器
     */
    public static void showUntitledUneditableDialog(Context context, String content, OnClickListener listener) {
        showDialog(context, "", "", content, listener, false, false, false);
    }

    //////////////////////////////////// 单按钮对话框

    /**
     * 显示一个有标题并且只有一个按钮可以编辑内容的对话框
     * @param context
     * @param title 对话框标题
     * @param hintContent 编辑框提示的内容
     * @param listener 按钮的监听器
     */
    public static void showEditableSingleDialog(Context context, String title, String hintContent, OnClickListener listener) {
        showDialog(context, title, hintContent, "", listener, true, true, true);
    }

    /**
     * 显示一个有标题并且只有一个按钮可以编辑内容的对话框
     * @param context
     * @param title 对话框标题
     * @param hintContent 编辑框提示的内容
     * @param editContent 编辑框的初始显示,可以为空
     * @param listener 按钮的监听器
     */
    public static void showEditableSingleDialog(Context context, String title, String hintContent, String editContent, OnClickListener listener) {
        showDialog(context, title, hintContent, editContent, listener, true, true, true);
    }

    /**
     * 显示一个没有标题但只有一个按钮可以编辑内容的对话框
     * @param context
     * @param hintContent 编辑框提示的内容
     * @param listener 按钮的监听器
     */
    public static void showUntitledEditableSingleDialog(Context context, String hintContent, OnClickListener listener) {
        showDialog(context, "", hintContent, "", listener, false, true, true);
    }

    /**
     * 显示一个没有标题但只有一个按钮可以编辑内容的对话框
     * @param context
     * @param hintContent 编辑框提示的内容
     * @param editContent 编辑框的初始显示,可以为空
     * @param listener 按钮的监听器
     */
    public static void showUntitledEditableSingleDialog(Context context, String hintContent, String editContent, OnClickListener listener) {
        showDialog(context, "", hintContent, editContent, listener, false, true, true);
    }

    /**
     * 显示一个有标题但只有一个按钮的不可以编辑内容的对话框
     * @param context
     * @param title 对话框的标题
     * @param content 对话框的内容
     * @param listener 按钮的监听器
     */
    public static void showUneditableSingleDialog(Context context, String title, String content, OnClickListener listener) {
        showDialog(context, title, "", content, listener, true, true, false);
    }

    /**
     * 显示一个没有标题且只有一个按钮的不可以编辑内容的对话框
     * @param context
     * @param content 对话框的内容
     * @param listener 按钮的监听器
     */
    public static void showUntitledUneditableSingleDialog(Context context, String content, OnClickListener listener) {
        showDialog(context, "", "", content, listener, false, true, false);
    }

    /**
     *
     * @param context
     * @param title 对话框标题
     * @param hintContent 可编辑对话框的默认提示内容
     * @param content 对话框的内容,可编辑对话框时,为默认的初始值
     * @param listener 监听器
     * @param showTitle 是否显示标题
     * @param single 是否只显示单个按钮
     * @param editable 对话框是否可编辑
     */
    public static void showDialog(Context context, String title, String hintContent, String content, OnClickListener listener, boolean showTitle, boolean single, boolean editable) {
        deleteDialog();
        initViews(context, editable);
        initListener(listener);
        if (showTitle) {
            mTitle.setText(title);
        } else {
            mTitle.setVisibility(View.GONE);
        }
        if (!editable) {
            mEditContent.setVisibility(View.GONE);
            mTextContent.setVisibility(View.VISIBLE);
            mTextContent.setText(content);
        } else {
            mEditContent.setHint(hintContent);
            if (!TextUtils.isEmpty(content)) {
                mEditContent.setText(content);
                mEditContent.setSelection(content.length());
            }
        }

        if (single) {
            mBtnParent.setVisibility(View.GONE);
            mSingBtn.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 删除弹出的对话框
     */
    public static void deleteDialog() {
        if (mAlertDialog != null) {
            mAlertDialog.cancel();
            mAlertDialog = null;
            mTitle = null;
            mEditContent = null;
            mTextContent = null;
            mCanel = null;
            mOk = null;
            mSingBtn = null;
            mBtnParent = null;
            mListener = null;
        }
    }

    private static void initViews(Context context, boolean editable) {
        mAlertDialog = new android.app.AlertDialog.Builder(context).create();
        mAlertDialog.setCancelable(false);
        mAlertDialog.show();
        Window w = mAlertDialog.getWindow();
        w.setContentView(R.layout.framework_alert_dialog_layout);
        if (editable)
            w.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        mTitle = (TextView) w.findViewById(R.id.framework_alert_dialog_title);
        mEditContent = (EditText) w.findViewById(R.id.framework_alert_dialog__edit_content);
        mTextContent = (TextView) w.findViewById(R.id.framework_alert_dialog_text_content);
        mBtnParent = (LinearLayout) w.findViewById(R.id.framework_alert_dialog_btn_parent);
        mCanel = (TextView) w.findViewById(R.id.framework_alert_dialog_canel);
        mOk = (TextView) w.findViewById(R.id.framework_alert_dialog_ok);
        mSingBtn = (TextView) w.findViewById(R.id.framework_alert_dialog_single_btn);
    }

    private static void initListener(OnClickListener listener) {
        mListener = listener;
        mCanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onClick(DialogInterface.BUTTON_NEGATIVE, null);
                }
                deleteDialog();
            }
        });
        mOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditContent.getText().toString();
                if (mListener != null) {
                    mListener.onClick(DialogInterface.BUTTON_POSITIVE, content);
                }
                deleteDialog();
            }
        });
        mSingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = mEditContent.getText().toString();
                if (mListener != null) {
                    mListener.onClick(DialogInterface.BUTTON_NEUTRAL, content);
                }
                deleteDialog();
            }
        });
    }

    public interface OnClickListener {

        /**
         * 对话框按钮点击事件
         * @param which 按钮的标识 {@link DialogInterface#BUTTON_POSITIVE};
         *            {@link DialogInterface#BUTTON_NEGATIVE};
         *              {@link DialogInterface#BUTTON_NEUTRAL}说明点击单一按钮.
         * @param content EditText的内容
         */
        public void onClick(int which, String content);
    }
}
