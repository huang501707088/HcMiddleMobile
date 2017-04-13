package com.android.hcframe.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.EditText;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-12-21 10:20.
 */

/**
 * 可编辑的对话框
 */
public class EditDialog {

    private static AlertDialog mListDialog;

    private static EditText mEdit;

    private static DialogInterface.OnClickListener mListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (mClickListener != null) {
                mClickListener.onClick(which, mEdit != null ? mEdit.getText().toString() : "");
            }
         }
    };

    private static OnClickListener mClickListener;

    /**
     *
     * @param context
     * @param content EditText默认的内容
     * @param listener 按钮点击的监听器
     */
    public static void showEditDialog(Context context, String content, OnClickListener listener) {
        showEditDialog(context, null, content, true, "确认", "取消", listener);
    }

    /**
     *
     * @param context
     * @param content EditText默认的内容
     * @param listener 按钮点击的监听器
     */
    public static void showEditDialog(Context context, String title, String content, OnClickListener listener) {
        showEditDialog(context, title, content, true, "确认", "取消", listener);
    }

    /**
     *
     * @param context
     * @param content EditText默认的内容
     * @param cancelable 按返回键是否可以隐藏对话框
     * @param ok 确认按钮的文字显示
     * @param canel 取消按钮的文字显示
     * @param listener 按钮点击的监听器
     */
    public static void showEditDialog(Context context, String title, String content, boolean cancelable, String ok, String canel, OnClickListener listener) {
        if (content == null)
            content = "";
        if (mEdit == null) {
            mEdit = new EditText(context);
//            mEdit.setTextSize(16 * HcUtil.getScreenDensity());
            mEdit.setTextColor(Color.parseColor("#333333"));
            mEdit.setBackgroundColor(Color.TRANSPARENT);
            mEdit.setMaxLines(1);
            mEdit.setPadding(30,10,30,10);
            mEdit.setGravity(Gravity.CENTER_VERTICAL);
            mEdit.setHeight(200);
        }
        mEdit.setText(content);
        mEdit.setSelection(content.length());
        mClickListener = listener;
        if (mListDialog == null) {
            mListDialog = new AlertDialog.Builder(context, R.style.HC_AlertDialog)
                    .setCancelable(cancelable)
                    .setView(mEdit)
                    .setTitle(title)
                    .create();
            if (!TextUtils.isEmpty(ok)) {
                mListDialog.setButton(DialogInterface.BUTTON_POSITIVE, ok, mListener);
            }
            if (!TextUtils.isEmpty(canel)) {
                mListDialog.setButton(DialogInterface.BUTTON_NEGATIVE, canel, mListener);
            }
            mListDialog.show();
        } else {
            deleteEditDialog();
            showEditDialog(context, title, content, cancelable, ok, canel, listener);
        }

    }

    public static void deleteEditDialog() {
        if (mListDialog != null) {
            mListDialog.cancel();
            mListDialog = null;
        }
        mEdit = null;
        mClickListener = null;
    }

    public interface OnClickListener {

        /**
         * 对话框按钮点击事件
         * @param which 按钮的标识 {@link DialogInterface#BUTTON_POSITIVE},
         *            {@link DialogInterface#BUTTON_NEGATIVE}.
         * @param content EditText的内容
         */
        public void onClick(int which, String content);
    }


}
