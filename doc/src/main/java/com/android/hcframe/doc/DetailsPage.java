package com.android.hcframe.doc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.hcframe.AbstractPage;
import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcObserver;
import com.android.hcframe.HcSubject;
import com.android.hcframe.HcUtil;
import com.android.hcframe.doc.data.DetailsAttAdapter;
import com.android.hcframe.doc.data.DocCacheData;
import com.android.hcframe.doc.data.DocGetPdfUri;
import com.android.hcframe.doc.data.DocInfo;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;
import com.android.hcframe.http.ResponseCodeInfo;
import com.android.hcframe.menu.DownloadPDFActivity;

import java.io.File;
import java.util.Observable;

public class DetailsPage extends AbstractPage implements HcObserver,
		OnItemClickListener {

	private TextView doc_detail_title;

	private TextView doc_detail_publish_time;

	private TextView doc_detail_source;

	private TextView doc_detail_mainbody;

	private TextView doc_detail_attachment;

	private TextView doc_detail_attachment_size;

	private ImageView doc_file_image;

	private ImageView doc_file_arrow;

	private TextView doc_detail_file_title;

	private TextView doc_details_article;

	private DocInfo docInfo;

	private LinearLayout details_pdf_show;

	private LinearLayout doc_file_arrow_lly;

	private DocDetailsActivity mActivity;

	private LocalActivityManager mLocalActivityManager;

	private String data_id;

	private int data_flag;

	private Handler mHandler;

	private DocGetPdfUri docGetPdfUri;

	private ShowPdfView showPdfView;

	private String filename;

	private File file;

	private TextView doc_file_name;

	private ImageView switch_icon;

	private LinearLayout attachment_details_lly;

	private TextView att_num;

	private ListView attachment_list;

	private DetailsAttAdapter attAdapter;

	protected DetailsPage(Activity context, ViewGroup group) {
		super(context, group);
		mActivity = (DocDetailsActivity) context;
	}

	@Override
	public void update(Observable observable, Object data) {

	}

	@Override
	public void onClick(View view) {
		int id = view.getId();
		if (id == R.id.doc_file_arrow_lly) {
			final AlertDialog dialog = new AlertDialog.Builder(mContext)
			.create();
			dialog.setCancelable(true);
			dialog.show();
			dialog.getWindow().setContentView(R.layout.dialog_attachment_lly);
			LinearLayout file_container = (LinearLayout) dialog.getWindow()
					.findViewById(R.id.file_container);
			LinearLayout attachment_container = (LinearLayout) dialog
					.getWindow().findViewById(R.id.attachment_container);
			View dialog_file_item;
			ImageView doc_file_item_image;
			TextView doc_filename_title;
			for (int i = 0; i < docInfo.getDocInfos().size(); i++) {
				dialog_file_item = LayoutInflater.from(mContext).inflate(
						R.layout.dialog_file_item, null);
				doc_file_item_image = (ImageView) dialog_file_item
						.findViewById(R.id.doc_file_item_image);
				doc_filename_title = (TextView) dialog_file_item
						.findViewById(R.id.doc_filename_title);
				doc_filename_title.setText(docInfo.getDocInfos().get(i)
						.getFileName());
				if (i == 0) {
					file_container.addView(dialog_file_item);
				} else {
					attachment_container.addView(dialog_file_item);
				}
				final String url = docInfo.getDocInfos().get(i).getFileUrl();
				final String title = docInfo.getDocInfos().get(i).getFileName();
				dialog_file_item.setOnClickListener(new OnClickListener() {
		
					@Override
					public void onClick(View arg0) {
						startPDFActivity(url, title);
					}
				});
			}
		} else if (id == R.id.switch_icon) {
			fullScreenShow(docInfo.getFileUrl(), docInfo.getFileName(), 0);
		} else if (id == R.id.attachment_details_lly) {
			if (attachment_list.getVisibility() == View.GONE) {
				attachment_list.setVisibility(View.VISIBLE);
			} else {
				attachment_list.setVisibility(View.GONE);
			}
		}
		/*
		switch (view.getId()) {
		case R.id.doc_file_arrow_lly:
			final AlertDialog dialog = new AlertDialog.Builder(mContext)
					.create();
			dialog.setCancelable(true);
			dialog.show();
			dialog.getWindow().setContentView(R.layout.dialog_attachment_lly);
			LinearLayout file_container = (LinearLayout) dialog.getWindow()
					.findViewById(R.id.file_container);
			LinearLayout attachment_container = (LinearLayout) dialog
					.getWindow().findViewById(R.id.attachment_container);
			View dialog_file_item;
			ImageView doc_file_item_image;
			TextView doc_filename_title;
			for (int i = 0; i < docInfo.getDocInfos().size(); i++) {
				dialog_file_item = LayoutInflater.from(mContext).inflate(
						R.layout.dialog_file_item, null);
				doc_file_item_image = (ImageView) dialog_file_item
						.findViewById(R.id.doc_file_item_image);
				doc_filename_title = (TextView) dialog_file_item
						.findViewById(R.id.doc_filename_title);
				doc_filename_title.setText(docInfo.getDocInfos().get(i)
						.getFileName());
				if (i == 0) {
					file_container.addView(dialog_file_item);
				} else {
					attachment_container.addView(dialog_file_item);
				}
				final String url = docInfo.getDocInfos().get(i).getFileUrl();
				final String title = docInfo.getDocInfos().get(i).getFileName();
				dialog_file_item.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						startPDFActivity(url, title);
					}
				});
			}
			break;
		case R.id.switch_icon:
			fullScreenShow(docInfo.getFileUrl(), docInfo.getFileName(), 0);
			break;
		case R.id.attachment_details_lly:
			if (attachment_list.getVisibility() == View.GONE) {
				attachment_list.setVisibility(View.VISIBLE);
			} else {
				attachment_list.setVisibility(View.GONE);
			}
			break;
		}
		*/
	}

	private void fullScreenShow(String url, String title, int flag) {
		Intent intent = new Intent(mContext, AttachmentActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);
		intent.putExtra("flag", flag);
		mContext.startActivity(intent);
	}

	@Override
	public void initialized() {

		data_id = mActivity.getData_id();

		data_flag = mActivity.getData_flag();

		docInfo = DocCacheData.getInstance().getDocDetail(data_id, data_flag);

		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				HcDialog.deleteProgressDialog();
				switch (msg.what) {
				case DocGetPdfUri.FILENAME_EMPTY:
					HcUtil.showToast(mContext, "文件名不能为空");
					break;
				case DocGetPdfUri.DOWNLOAD_OPEN:
					HcDialog.showProgressDialog(mContext, "正在下载...");
					break;
				case DocGetPdfUri.DOWNLOAD_SUCCESS:
					filename = (String) msg.obj;
					file = new File(filename);
					openPdf();
					break;
				case DocGetPdfUri.DOWNLOAD_FAIL:
					HcUtil.showToast(mContext, "文件名下载失败");
					break;
				case DocGetPdfUri.OPEN_PDF:
					filename = (String) msg.obj;
					file = new File(HcApplication.getPdfDir(), filename
							+ ".pdf");
					openPdf();
					break;
				case DocGetPdfUri.TIMEOUT:
					HcUtil.toastTimeOut(mContext);
					break;
				}
			}

		};

		if (docInfo != null) {
			refreshUI();
		}

	}

	public void openPdf() {
		if (showPdfView == null) {
			showPdfView = new ShowPdfView(mContext, null, filename,
					docInfo.getDataTitle(), Intent.ACTION_VIEW, Uri.parse(file
							.getAbsolutePath()),1.0f,null);
		}
		showPdfView.changePages();

		details_pdf_show.addView(showPdfView.getContentView());
	}

	public void refreshUI() {
		doc_detail_title.setText(docInfo.getDataTitle());

		doc_detail_publish_time.setText(HcUtil.getDate(
				HcUtil.FORMAT_POLLUTION_NEW,
				HcUtil.getDate(docInfo.getDate(), HcUtil.FORMAT_POLLUTION_S)
						.getTime()));

		doc_detail_source
				.setText(HcUtil.isEmpty(docInfo.getDataSource()) ? "未知"
						: docInfo.getDataSource());

		doc_detail_mainbody.setText(String.format(
				mContext.getString(R.string.main_body), 1));

		doc_detail_attachment.setText(String.format(mContext
				.getString(R.string.attachment),
				docInfo.getDocInfos().size() - 1));

		doc_detail_attachment_size.setText(docInfo.getFileSizeForUnit() + "");

		doc_detail_file_title.setText(docInfo.getDocInfos().get(0)
				.getFileName());

		doc_file_name.setText(docInfo.getDocInfos().get(0).getFileName());
		// doc_detail_file_title.setText(docInfo.getFileName());

		// doc_file_arrow.setOnClickListener(this);

		doc_file_arrow_lly.setOnClickListener(this);

		details_pdf_show = (LinearLayout) mView
				.findViewById(R.id.details_pdf_show);
		details_pdf_show.setOnTouchListener(new OnTouchListener() {

			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View arg0, MotionEvent arg1) {
				if (arg1.getAction() == MotionEvent.ACTION_DOWN) {
					if (attachment_list.getVisibility() == View.VISIBLE) {
						attachment_list.setVisibility(View.GONE);
					}
				}
				return false;
			}
		});

		switch_icon.setOnClickListener(this);
		attachment_details_lly.setOnClickListener(this);
		attachment_list.setOnItemClickListener(this);
		att_num.setText(String.format(mContext.getString(R.string.attach_num),
				docInfo.getDocInfos().size() - 1));
		if (attAdapter == null) {
			attAdapter = new DetailsAttAdapter(mContext, docInfo.getDocInfos());
			attachment_list.setAdapter(attAdapter);
		}
		attAdapter.notifyDataSetChanged();

		// mLocalActivityManager = new LocalActivityManager(mContext, true);
		//
		// Intent intent = new Intent();
		// intent.putExtra("url", docInfo.getDocInfos().get(0).getFileUrl());
		// intent.putExtra("title", docInfo.getDocInfos().get(0).getFileName());
		// details_pdf_show.addView(activityToView(mContext, intent));

		docGetPdfUri = new DocGetPdfUri(docInfo.getFileUrl(), mHandler);
		docGetPdfUri.startDownloadPDF();
	}

	@Override
	public void setContentView() {
		if (mView == null) {
			mView = mInflater.inflate(R.layout.doc_details_pager, null);

			doc_detail_title = (TextView) mView
					.findViewById(R.id.doc_detail_title);

			doc_detail_publish_time = (TextView) mView
					.findViewById(R.id.doc_detail_publish_time);

			doc_detail_source = (TextView) mView
					.findViewById(R.id.doc_detail_source);

			doc_detail_mainbody = (TextView) mView
					.findViewById(R.id.doc_detail_mainbody);

			doc_detail_attachment = (TextView) mView
					.findViewById(R.id.doc_detail_attachment);

			doc_detail_attachment_size = (TextView) mView
					.findViewById(R.id.doc_detail_attachment_size);

			doc_file_image = (ImageView) mView
					.findViewById(R.id.doc_file_image);

			doc_file_arrow = (ImageView) mView
					.findViewById(R.id.doc_file_arrow);

			doc_detail_file_title = (TextView) mView
					.findViewById(R.id.doc_detail_file_title);

			doc_details_article = (TextView) mView
					.findViewById(R.id.doc_details_article);
			doc_file_arrow_lly = (LinearLayout) mView
					.findViewById(R.id.doc_file_arrow_lly);
			doc_file_name = (TextView) mView.findViewById(R.id.doc_file_name);
			switch_icon = (ImageView) mView.findViewById(R.id.switch_icon);

			attachment_details_lly = (LinearLayout) mView
					.findViewById(R.id.attachment_details_lly);
			att_num = (TextView) mView.findViewById(R.id.att_num);

			attachment_list = (ListView) mView
					.findViewById(R.id.attachment_list);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		DocCacheData.getInstance().addObserver(this);
		if (docInfo == null) {
			DocCacheData.getInstance().getDataDetail(data_id, data_flag);
			HcDialog.showProgressDialog(mContext,
					R.string.dialog_title_get_data);
		}
		if (showPdfView != null) {
			showPdfView.onResume();
		}
		
	}

	@Override
	public void onPause() {
		super.onPause();
		DocCacheData.getInstance().removeObserver(this);
		if (showPdfView != null) {
			showPdfView.onPause();
		}
	}

	public void startPDFActivity(String url, String title) {
		Intent intent = new Intent(mContext, DownloadPDFActivity.class);
		intent.putExtra("url", url);
		intent.putExtra("title", title);
		mContext.startActivity(intent);
	}

	@Override
	public void updateData(HcSubject subject, Object data,
			RequestCategory request, ResponseCategory response) {
		HcLog.D("DetailsPage updateData request = " + request + " response = "
				+ response);
		HcDialog.deleteProgressDialog();
		if (subject != null && subject instanceof DocCacheData) {
			switch (request) {

			case SEARCH_DATA_DETAIL: {

				switch (response) {
				case SUCCESS:
					if (data != null && data instanceof DocInfo) {
						docInfo = (DocInfo) data;
						if (docInfo != null) {
							refreshUI();
						}
					}
					break;
				case SESSION_TIMEOUT:
					HcUtil.toastTimeOut(mContext);
					break;
				case DATA_ERROR:
					HcUtil.toastDataError(mContext);
					break;
				/**
				 * czx
				 * 2016.4.14
				 */
				case REQUEST_FAILED:
					ResponseCodeInfo info = (ResponseCodeInfo) data;
					if (HcHttpRequest.REQUEST_TOKEN_FAILED == info.getCode() ||
							HcHttpRequest.REQUEST_ACCOUT_EXCLUDED == info.getCode()) {
						HcUtil.reLogining(info.getBodyData(), mContext, info.getMsg());
					} else {
						HcUtil.showToast(mContext, info.getMsg());
					}
					break;
				case SYSTEM_ERROR:
					HcUtil.toastSystemError(mContext, data);
					break;
				}
			}
				break;
			}
		}
	}

	@Override
	public void release() {
		super.release();
		if (showPdfView != null) {
			showPdfView.release();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long arg3) {
		if (docInfo.getDocInfos().size() > 1) {
			String urll=docInfo.getDocInfos().get(pos + 1).getFileUrl();
			fullScreenShow(urll, docInfo
					.getDocInfos().get(pos + 1).getFileName(), 1);
		}
	}
}
