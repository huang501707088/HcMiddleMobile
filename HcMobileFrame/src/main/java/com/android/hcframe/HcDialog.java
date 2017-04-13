/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-4-13 上午10:58:35
*/
package com.android.hcframe;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class HcDialog {

	private static TextView mContent;
	private static AlertDialog mAlertDialog;
	private static ProgressBar mProgressBar;
	private static ImageView mImageView;
	
	/**
	 * 显示对话框,且对话框不可被关闭
	 * @author jrjin
	 * @time 2015-9-24 下午4:15:47
	 * @param context 
	 * @param content 对话框的内容
	 */
	public static void showProgressDialog(Context context, String content) {
		/*if (mAlertDialog == null) {
			mAlertDialog = new AlertDialog.Builder(context).create();
			mAlertDialog.setCancelable(false);
			mAlertDialog.show();
			Window window = mAlertDialog.getWindow();
			window.setContentView(R.layout.dialog_progress_style);
			TextView t = (TextView) window.findViewById(R.id.progress_content);
			t.setText(content);
			mContent = t;
		} else {
			mContent.setText(content);
			mAlertDialog.show();
		}*/
		showProgressDialog(context, content, false);
	}
	
	/**
	 * 显示对话框,且对话框不可被关闭
	 * @author jrjin
	 * @time 2015-9-24 下午4:15:47
	 * @param context
	 * @param resId 需要显示对话框内容的资源id
	 */
	public static void showProgressDialog(Context context, int resId) {
		/*if (mAlertDialog == null) {
			mAlertDialog = new AlertDialog.Builder(context).create();
			mAlertDialog.setCancelable(false);
			mAlertDialog.show();
			Window window = mAlertDialog.getWindow();
			window.setContentView(R.layout.dialog_progress_style);
			TextView t = (TextView) window.findViewById(R.id.progress_content);
			t.setText(context.getResources().getString(resId));
			mContent = t;
		} else {
			mContent.setText(context.getResources().getString(resId));
			mAlertDialog.show();
		}*/
		showProgressDialog(context, resId, false);
	}
	
//	public static void canelProgressDialog() {
//		if (mAlertDialog != null) {
//			mAlertDialog.cancel();
//		}
//	}
	
	/**
	 * 删除对话框
	 * @author jrjin
	 * @time 2015-9-24 下午4:22:09
	 */
	public static void deleteProgressDialog() {
		if (mAlertDialog != null) {
			mAlertDialog.cancel();
			mContent = null;
			mProgressBar = null;
			mImageView = null;
			mAlertDialog = null;
		}
	}
	
	/**
	 * 显示对话框
	 * @author jrjin
	 * @time 2015-9-24 下午4:15:47
	 * @param context
	 * @param content 对话框的内容
	 * @param cancel  是否可以关闭对话框 true:可以；false 不可以
	 */
	public static void showProgressDialog(Context context, String content, boolean cancel) {
		deleteProgressDialog();
		if (mAlertDialog == null) {
			mAlertDialog = new AlertDialog.Builder(context).create();
			mAlertDialog.setCancelable(cancel);
			mAlertDialog.show();
			Window window = mAlertDialog.getWindow();
			window.setContentView(R.layout.dialog_progress_style);
			TextView t = (TextView) window.findViewById(R.id.progress_content);
			mProgressBar = (ProgressBar) window.findViewById(R.id.dialog_progressbar);
			mImageView = (ImageView) window.findViewById(R.id.dialog_image);
			t.setText(content);
			mContent = t;
		} else {
			if (mProgressBar.getVisibility() != View.VISIBLE)
				mProgressBar.setVisibility(View.VISIBLE);
			if (mImageView.getVisibility() != View.GONE)
				mImageView.setVisibility(View.GONE);
			mContent.setText(content);
			mAlertDialog.setCancelable(cancel);
			mAlertDialog.show();
		}
	}
	
	/**
	 * 显示对话框
	 * @author jrjin
	 * @time 2015-9-24 下午4:15:47
	 * @param context
	 * @param resId 需要显示对话框内容的资源id
	 * @param cancel  是否可以关闭对话框 true:可以；false 不可以
	 */
	public static void showProgressDialog(Context context, int resId, boolean cancel) {
		deleteProgressDialog();
		if (mAlertDialog == null) {
			mAlertDialog = new AlertDialog.Builder(context).create();
			mAlertDialog.setCancelable(cancel);
			mAlertDialog.show();
			Window window = mAlertDialog.getWindow();
			window.setContentView(R.layout.dialog_progress_style);
			TextView t = (TextView) window.findViewById(R.id.progress_content);
			mProgressBar = (ProgressBar) window.findViewById(R.id.dialog_progressbar);
			mImageView = (ImageView) window.findViewById(R.id.dialog_image);
			t.setText(context.getResources().getString(resId));
			mContent = t;
		} else {
			if (mProgressBar.getVisibility() != View.VISIBLE)
				mProgressBar.setVisibility(View.VISIBLE);
			if (mImageView.getVisibility() != View.GONE)
				mImageView.setVisibility(View.GONE);
			mContent.setText(context.getResources().getString(resId));
			mAlertDialog.setCancelable(cancel);
			mAlertDialog.show();
		}
	}
	
	public static void showSuccessDialog(Context context, String content) {
		deleteProgressDialog();
		if (mAlertDialog == null) {
			mAlertDialog = new AlertDialog.Builder(context).create();
			mAlertDialog.show();
			Window window = mAlertDialog.getWindow();
			window.setContentView(R.layout.dialog_progress_style);
			TextView t = (TextView) window.findViewById(R.id.progress_content);
			mProgressBar = (ProgressBar) window.findViewById(R.id.dialog_progressbar);
			mImageView = (ImageView) window.findViewById(R.id.dialog_image);
			mProgressBar.setVisibility(View.GONE);
			mImageView.setVisibility(View.VISIBLE);
			t.setText(content);
			mContent = t;
			AnimationDrawable anim = (AnimationDrawable) mImageView.getBackground();
			anim.start();
		} else {
			if (mProgressBar.getVisibility() != View.GONE)
				mProgressBar.setVisibility(View.GONE);
			if (mImageView.getVisibility() != View.VISIBLE)
				mImageView.setVisibility(View.VISIBLE);
			mContent.setText(content);
			mAlertDialog.setCancelable(true);
			mAlertDialog.show();
			AnimationDrawable anim = (AnimationDrawable) mImageView.getBackground();
			anim.start();
		}
	}
}
