package com.android.hcframe.doc.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import com.android.hcframe.HcApplication;
import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.http.HcHttpRequest;
import com.android.hcframe.http.IHttpResponse;
import com.android.hcframe.http.RequestCategory;
import com.android.hcframe.http.ResponseCategory;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

public class DocGetPdfUri implements IHttpResponse {

	private String url;

	private String filename = null;

	private File file;

	private Handler mHandler;

	public static final int FILENAME_EMPTY = 0;

	public static final int OPEN_PDF = 1;

	public static final int DOWNLOAD_OPEN = 2;

	public static final int DOWNLOAD_SUCCESS = 3;

	public static final int DOWNLOAD_FAIL = 4;

	public static final int TIMEOUT = 5;

	public DocGetPdfUri(String url, Handler mHandler) {

		this.url = url;

		this.mHandler = mHandler;
	}

	public void startDownloadPDF() {
		if (TextUtils.isEmpty(url)) {
			// 文件名不能为空
			sendMessage(FILENAME_EMPTY, null);
			return;
		}
		filename = HcUtil.getMD5String(url);
		file = new File(HcApplication.getPdfDir(), filename + ".pdf");
		// 判断本地是否有pdf
		if (file.exists()) {
			// 打开pdf文件
			sendMessage(OPEN_PDF, filename);
		} else {
			// 发送pdf下载命令
			sendMessage(DOWNLOAD_OPEN, null);
			if (HcUtil.CHANDED) {
				url = HcUtil.mappedUrl(url);
			}
			HcHttpRequest.getRequest().sendDownPdfCommand(url, this);
		}
	}

	private void sendMessage(int what, String obj) {
		if (mHandler != null) {
			Message message = Message.obtain(mHandler);
			message.what = what;
			message.obj = obj;
			mHandler.sendMessage(message);
		}
	}

	@Override
	public void notify(Object data, RequestCategory request,
			ResponseCategory response) {
		if (request != null && request == RequestCategory.DOWNLOAD_PDF) {
			
				switch (response) {
				case SUCCESS:
					if (data != null && data instanceof InputStream) {
						InputStream stream = (InputStream) data;
						String filename = createPdf(this.filename, stream);
						if (!HcUtil.isEmpty(filename)) {
							sendMessage(DOWNLOAD_SUCCESS, filename);
						} else {
							sendMessage(DOWNLOAD_FAIL, null);
						}
					}
					break;
				case SESSION_TIMEOUT:
					sendMessage(TIMEOUT, null);
					break;
				case DATA_ERROR:
					break;
				}

			} else {
				sendMessage(DOWNLOAD_FAIL, null);
			}

	}

	private String createPdf(String pdf, InputStream is) {
		if (is != null) {
			OutputStream outputStream = null;
			try {
				String directory = HcApplication.getPdfDir().getAbsolutePath();
				File dir = new File(directory);
				if (!dir.exists())
					dir.mkdirs();
				File file = new File(directory, pdf + ".pdf");
				outputStream = new FileOutputStream(file);
				byte[] b = new byte[1024];
				int len;
				while ((len = is.read(b)) > 0) {
					HcLog.D(" DownloadPDFActivity createPdf len = " + len);
					outputStream.write(b, 0, len);
				}
				return file.getAbsolutePath();
			} catch (Exception e) {
				return null;
			} finally {
				try {
					if (outputStream != null)
						outputStream.close();
					is.close();
					is = null;
				} catch (Exception e2) {
				}
			}
		}

		return null;
	}

	@Override
	public void notifyRequestMd5Url(RequestCategory request, String md5Url) {
		// TODO Auto-generated method stub
		
	}
}
