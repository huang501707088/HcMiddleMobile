package com.android.hcframe.view.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.ListAdapter;

import com.android.hcframe.R;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-12-19 13:38.
 */

/**
 * 列表对话框
 */
public class ListDialog {

    private static AlertDialog mListDialog;

    /**
     * 显示列表对话框,右键菜单.
     * @param context
     * @param adapter ListView的适配器
     * @param listener ListView的监听器
     */
    public static void showListDialog(Context context, ListAdapter adapter, DialogInterface.OnClickListener listener) {
        showListDialog(context, adapter, listener, true);
    }

    /**
     *
     * @param context
     * @param items ListView的列表内容
     * @param listener ListView的监听器
     */
    public static void showListDialog(Context context, CharSequence[] items, DialogInterface.OnClickListener listener) {
        showListDialog(context, items, listener, true);
    }

    /**
     * 显示列表对话框,右键菜单.
     * @param context
     * @param adapter ListView的适配器
     * @param listener ListView的监听器
     * @param cancelable 点击返回按钮对话框是否消失
     */
    public static void showListDialog(Context context, ListAdapter adapter, DialogInterface.OnClickListener listener, boolean cancelable) {
        if (mListDialog == null) {
            mListDialog = new AlertDialog.Builder(context, R.style.HC_AlertDialog)
                    .setCancelable(cancelable)
                    .setAdapter(adapter, listener)
                    .create();
            mListDialog.show();
        } else {
            deleteListDialog();
            showListDialog(context, adapter, listener, cancelable);
        }

    }

    /**
     * 显示列表对话框,右键菜单.
     * @param context
     * @param items ListView的列表内容
     * @param listener ListView的监听器
     * @param cancelable 点击返回按钮对话框是否消失
     */
    public static void showListDialog(Context context, CharSequence[] items, DialogInterface.OnClickListener listener, boolean cancelable) {
        if (mListDialog == null) {
            mListDialog = new AlertDialog.Builder(context, R.style.HC_AlertDialog)
                    .setCancelable(cancelable)
                    .setItems(items, listener)
                    .create();
            mListDialog.show();
        } else {
            deleteListDialog();
            showListDialog(context, items, listener, cancelable);
        }

    }

    public static void deleteListDialog() {
        if (mListDialog != null) {
            mListDialog.cancel();
            mListDialog = null;
        }
    }
}
