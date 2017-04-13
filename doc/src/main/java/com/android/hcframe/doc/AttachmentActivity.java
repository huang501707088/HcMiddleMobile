package com.android.hcframe.doc;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcBaseActivity;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.doc.data.DocGetPdfUri;
import com.android.hcframe.push.HcAppState;

import java.io.File;

public class AttachmentActivity extends HcBaseActivity implements OnClickListener {

	private String url;

	private String mTitle;

	private int flag;

	// fix
	private RelativeLayout att_header_fix;

	private ImageView att_close_fix;

	private LinearLayout details_pdf_show_attr;

	private LinearLayout att_foot_fix;

	private TextView doc_file_name_fix;

	private ImageView switch_icon_fix;

	// float
	private RelativeLayout att_header_float;

	private ImageView att_close_float;

	private LinearLayout att_foot_float;

	private TextView doc_file_name_float;

	private ImageView switch_icon_float;

	private boolean isFull = false;

	private Handler mHandler;

	private String filename;

	private File file;

	private ShowPdfView showPdfView;

	private DocGetPdfUri docGetPdfUri;

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_attachment);
		Intent intent = getIntent();
		url = intent.getExtras().getString("url");
		mTitle = intent.getExtras().getString("title");
		flag = intent.getExtras().getInt("flag");

		initView();

		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (showPdfView != null) {
			showPdfView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (showPdfView != null) {
			showPdfView.onPause();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (showPdfView != null) {
			showPdfView.release();
		}
	}

	private void initView() {
		att_header_fix = (RelativeLayout) findViewById(R.id.att_header_fix);
		att_close_fix = (ImageView) findViewById(R.id.att_close_fix);
		details_pdf_show_attr = (LinearLayout) findViewById(R.id.details_pdf_show_attr);
		att_foot_fix = (LinearLayout) findViewById(R.id.att_foot_fix);
		doc_file_name_fix = (TextView) findViewById(R.id.doc_file_name_fix);
		switch_icon_fix = (ImageView) findViewById(R.id.switch_icon_fix);

		att_header_float = (RelativeLayout) findViewById(R.id.att_header_float);
		att_close_float = (ImageView) findViewById(R.id.att_close_float);
		att_foot_float = (LinearLayout) findViewById(R.id.att_foot_float);
		doc_file_name_float = (TextView) findViewById(R.id.doc_file_name_float);
		switch_icon_float = (ImageView) findViewById(R.id.switch_icon_float);
	}

	private void initData() {
		att_close_fix.setOnClickListener(this);
		att_close_float.setOnClickListener(this);
		switch_icon_fix.setOnClickListener(this);
		switch_icon_float.setOnClickListener(this);

		doc_file_name_fix.setText(mTitle);
		doc_file_name_float.setTag(mTitle);
		if (flag == 0) {
			isFull = true;
			fullShow();
		} else {
			isFull = false;
		}

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				HcDialog.deleteProgressDialog();
				switch (msg.what) {
				case DocGetPdfUri.FILENAME_EMPTY:
					HcUtil.showToast(AttachmentActivity.this, "文件名不能为空");
					break;
				case DocGetPdfUri.DOWNLOAD_OPEN:
					HcDialog.showProgressDialog(AttachmentActivity.this,
							"正在下载...");
					break;
				case DocGetPdfUri.DOWNLOAD_SUCCESS:
					filename = (String) msg.obj;
					file = new File(filename);
					openPdf();
					break;
				case DocGetPdfUri.DOWNLOAD_FAIL:
					HcUtil.showToast(AttachmentActivity.this, "文件名下载失败");
					break;
				case DocGetPdfUri.OPEN_PDF:
					filename = (String) msg.obj;
					file = new File(HcApplication.getPdfDir(), filename
							+ ".pdf");
					openPdf();
					break;
				case DocGetPdfUri.TIMEOUT:
					HcUtil.toastTimeOut(AttachmentActivity.this);
					break;
				case ShowPdfView.ShowButton:
					if(att_header_fix.getVisibility()==View.GONE)
					{
						att_foot_float.setVisibility(View.VISIBLE);
					}
					break;

				case ShowPdfView.HideButton:
					if(att_header_fix.getVisibility()==View.GONE)
					{
						att_foot_float.setVisibility(View.GONE);
					}
					break;
				}
			}

		};

		docGetPdfUri = new DocGetPdfUri(url, mHandler);
		docGetPdfUri.startDownloadPDF();
	}

	@Override
	public void onClick(View arg0) {
		int id = arg0.getId();
		if (id == R.id.att_close_fix || id == R.id.att_close_float) {
			HcAppState.getInstance().removeActivity(this);
			finish();
		} else if (id == R.id.switch_icon_fix) {
			fullShow();
		} else if (id == R.id.switch_icon_float) {
			if (isFull) {
				HcAppState.getInstance().removeActivity(this);
				finish();
			} else {
				noFullShow();
			}
		}
		/*
		switch (arg0.getId()) {
		case R.id.att_close_fix:
		case R.id.att_close_float:
			finish();
			break;
		case R.id.switch_icon_fix:
			fullShow();
			break;

		case R.id.switch_icon_float:
			if (isFull) {
				finish();
			} else {
				noFullShow();
			}
			break;
		}
		*/
	}

	private void noFullShow() {
		att_header_fix.setVisibility(View.VISIBLE);
		att_foot_fix.setVisibility(View.VISIBLE);
		att_header_float.setVisibility(View.GONE);
		att_foot_float.setVisibility(View.GONE);
	}

	private void fullShow() {
		att_header_fix.setVisibility(View.GONE);
		att_foot_fix.setVisibility(View.GONE);
		att_header_float.setVisibility(View.GONE);
		att_foot_float.setVisibility(View.GONE);
	}

	public void openPdf() {
		if (showPdfView == null) {
			showPdfView = new ShowPdfView(AttachmentActivity.this, null,
					filename, mTitle, Intent.ACTION_VIEW, Uri.parse(file
							.getAbsolutePath()), 1.0f, mHandler);
		}
		showPdfView.changePages();

		details_pdf_show_attr.addView(showPdfView.getContentView());
	}

}
