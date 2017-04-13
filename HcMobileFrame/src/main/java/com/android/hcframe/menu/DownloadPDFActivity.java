/*
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com 
 * @author jinjr
 * @data 2015-6-12 下午6:13:40
 */
package com.android.hcframe.menu;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.R.string;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.push.HcAppState;
import com.artifex.mupdfdemo.HcPDFActivity;

public class DownloadPDFActivity extends HcBaseActivity implements IHttpResponse {
	private String url;
	private String filename = null;
	private File file;
	private Handler handler;
	private String mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		HcLog.D("DownloadPDFActivity# " + HcLog.TAG_PDF + " it is onCreate!");
		Intent intent = getIntent();
		url = intent.getExtras().getString("url");
		mTitle = intent.getExtras().getString("title");
		
		if (TextUtils.isEmpty(url)) {
			HcUtil.showToast(this, "文件下载失败！");
			finishActivity();
			return;
		}
		HcLog.D(" DownloadPDFActivity onCreate url = "+url);
		filename = HcUtil.getMD5String(url);
		file = new File(HcApplication.getPdfDir(), filename + ".pdf");
		// 判断本地是否有pdf
		if (file.exists()) {
			startPDFActicity(file);
		} else {
			HcLog.Sysout("5");
			HcDialog.showProgressDialog(this, "正在下载...");
			// 发送pdf下载命令
			if (HcUtil.CHANDED) {
				url = HcUtil.mappedUrl(url);
			}
			HcHttpRequest.getRequest().sendDownPdfCommand(url, this);
		}
		handler=new Handler();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	private void startPDFActicity(File file) {
		HcLog.Sysout("4");
		Uri uri = Uri.parse(file.getAbsolutePath());
		Intent intent = new Intent(this, HcPDFActivity.class);
		intent.setAction(Intent.ACTION_VIEW);
		intent.setData(uri);
		intent.putExtra("title", mTitle);
		HcAppState.getInstance().removeActivity(this);
		startActivity(intent);
		finish();
		overridePendingTransition(0, 0);

	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory category) {
		// TODO Auto-generated method stub		
		if (request != null && request == RequestCategory.DOWNLOAD_PDF) {
			if (data != null && data instanceof InputStream) {
				InputStream stream = (InputStream) data;
				String filename=createPdf(this.filename, stream);
				HcDialog.deleteProgressDialog();
				if(!HcUtil.isEmpty(filename))
				{
					handler.post(new Runnable() {
						
						@Override
						public void run() {
							// 打开pdf文件
							startPDFActicity(file);
						}
					});

				} else {
					HcUtil.showToast(this, "文件下载失败！");
					finishActivity();
				}
			} else {
				HcDialog.deleteProgressDialog();
				HcUtil.showToast(this, "文件下载失败！");
				finishActivity();
			}
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	private String createPdf(String pdf, InputStream is) {
		if (is != null) {
			OutputStream outputStream = null;
			try {
				String directory = HcApplication.getPdfDir().getAbsolutePath();
				File dir = new File(directory);
	            if (!dir.exists()) dir.mkdirs();
	            File file = new File(directory, pdf+".pdf");
	            outputStream = new FileOutputStream(file);
	            byte[] b = new byte[1024];
    			int len;
    			while ((len = is.read(b)) > 0) {
    				HcLog.D(" DownloadPDFActivity createPdf len = "+len);
    				outputStream.write(b, 0, len);
    			}
    			return file.getAbsolutePath();		
			} catch (Exception e) {
				// TODO: handle exception
				return null;
			} finally {
				try {
					if (outputStream != null) 
						outputStream.close();
					is.close();
					is = null;
				} catch (Exception e2) {
					// TODO: handle exception
				}
			}
		}
		
		return null;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		HcLog.D("DownloadPDFActivity# " + HcLog.TAG_PDF + " it is onResume!");
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		HcLog.D("DownloadPDFActivity# " + HcLog.TAG_PDF + " it is onStop!");
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}
	
	private void finishActivity() {
		HcAppState.getInstance().removeActivity(this);
		finish();
	}
}
