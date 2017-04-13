package com.android.hcframe.view.selector.file;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import com.android.hcframe.HcDialog;
import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.R;
import com.android.hcframe.view.selector.AbstractChooseFileActivity;
import com.android.hcframe.view.selector.DepInfo;
import com.android.hcframe.view.selector.ItemInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-28 14:54.
 */

public class FileChooseActivity extends AbstractChooseFileActivity {

    private static final String TAG = "FileChooseActivity";

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HcUtil.showToast(FileChooseActivity.this, "暂无外部存储");
                    finish();
                    break;
                case 101:
                    HcDialog.showProgressDialog(FileChooseActivity.this, "正在搜索文件...");
                    break;
                case 102:
                    HcDialog.deleteProgressDialog();
                    break;
                case 1000:

                    break;

                default:
                    break;
            }
        }
    };

    private boolean mScanAll = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mScanAll = getIntent().getBooleanExtra("scanAll", true);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTopbarTitle() {
        return "文件选择";
    }

    @Override
    protected void scanFiles(String fileName) {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mHandler.sendEmptyMessage(100);
            return;
        }
        if (mScanAll) {
            mHandler.sendEmptyMessage(101);
            File root = Environment.getExternalStorageDirectory();// 获得SD卡路径
            final File[] files = root.listFiles();
            readFiles(files);
            mHandler.sendEmptyMessage(102);
        } else {
            File file;
            if (TextUtils.isEmpty(fileName)) {
                file = Environment.getExternalStorageDirectory();// 获得SD卡路径
            } else {
                file = new File(fileName);
            }

            if (file != null && file.exists() && file.isDirectory()) {
                readFiles(file);
            }
        }

    }

    private void readFiles(File[] files) {
        if (files == null) return;
        List<ItemInfo> infos = null;
        ItemInfo info;
        for (File file : files) {
            if (file.isDirectory()) {
                readFiles(file.listFiles());
            } else {
                String name = file.getName();
                if (name.endsWith(".txt")) {
                    if (infos == null) {
                        infos = new ArrayList<ItemInfo>();
                    }
                    info = new FileInfo();
                    info.setIconUrl("drawable://" + R.drawable.file_default_icon);
                    info.setIconResId(R.drawable.file_default_icon);
                    addFile(infos, info, file);
                } else if (name.endsWith(".doc") || name.endsWith(".docx")) {
                    if (infos == null) {
                        infos = new ArrayList<ItemInfo>();
                    }
                    info = new FileInfo();
                    info.setIconUrl("drawable://" + R.drawable.word_file_icon);
                    info.setIconResId(R.drawable.word_file_icon);
                    addFile(infos, info, file);
                } else if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
                    if (infos == null) {
                        infos = new ArrayList<ItemInfo>();
                    }
                    info = new FileInfo();
                    info.setIconUrl("drawable://" + R.drawable.ppt_file_icon);
                    info.setIconResId(R.drawable.ppt_file_icon);
                    addFile(infos, info, file);
                } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                    if (infos == null) {
                        infos = new ArrayList<ItemInfo>();
                    }
                    info = new FileInfo();
                    info.setIconUrl("drawable://" + R.drawable.excel_file_icon);
                    info.setIconResId(R.drawable.excel_file_icon);
                    addFile(infos, info, file);
                } else if (name.endsWith(".zip") || name.endsWith(".rar") ||
                        name.endsWith(".tar") || name.endsWith(".7z")) {
                    if (infos == null) {
                        infos = new ArrayList<ItemInfo>();
                    }
                    info = new FileInfo();
                    info.setIconUrl("drawable://" + R.drawable.zip_file_icon);
                    info.setIconResId(R.drawable.zip_file_icon);
                    addFile(infos, info, file);
                } else if (name.endsWith(".apk") || name.endsWith(".ipa")) {
                    if (infos == null) {
                        infos = new ArrayList<ItemInfo>();
                    }
                    info = new FileInfo();
                    info.setIconUrl("drawable://" + R.drawable.zip_file_icon);
                    info.setIconResId(R.drawable.zip_file_icon);
                    addFile(infos, info, file);
                }
            }
        }

//        if (infos != null) {
//            Message message = new Message();
//            message.what = 1000;
//            message.obj = infos;
//            mHandler.sendMessage(message);
//        }
        if (infos != null)
            addAllItemInfos(infos);
    }

    private void addFile(List<ItemInfo> infos, ItemInfo info, File file) {
        String name = file.getName(); // 有扩展名的
        String subName = name.substring(0, name.lastIndexOf('.'));
        info.setItemId(file.getAbsolutePath());
        info.setItemValue(name);
        info.setMultipled(mSelected);
        info.setFilePath(file.getAbsolutePath());
        HcLog.D(TAG + " #addFile name = " + name + " subName = " + subName);
        infos.add(info);
    }

    private void readFiles(File directory) {
        File[] files = directory.listFiles();
        ItemInfo info;
        for (File file : files) {
            if (file.isDirectory()) {
                info = new DepInfo();
                info.setIconUrl("drawable://" + R.drawable.file_file_icon);
                info.setIconResId(R.drawable.file_file_icon);
            } else {
                String name = file.getName();
                info = new FileInfo();
                info.setMultipled(mSelected);
                info.setFilePath(file.getAbsolutePath());
                if (name.endsWith(".txt")) {
                    info.setIconUrl("drawable://" + R.drawable.file_default_icon);
                    info.setIconResId(R.drawable.file_default_icon);
                } else if (name.endsWith(".doc") || name.endsWith(".docx")) {
                    info.setIconUrl("drawable://" + R.drawable.word_file_icon);
                    info.setIconResId(R.drawable.word_file_icon);
                } else if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
                    info.setIconUrl("drawable://" + R.drawable.ppt_file_icon);
                    info.setIconResId(R.drawable.ppt_file_icon);
                } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                    info.setIconUrl("drawable://" + R.drawable.excel_file_icon);
                    info.setIconResId(R.drawable.excel_file_icon);
                } else if (name.endsWith(".zip") || name.endsWith(".rar") ||
                        name.endsWith(".tar") || name.endsWith(".7z")) {
                    info.setIconUrl("drawable://" + R.drawable.zip_file_icon);
                    info.setIconResId(R.drawable.zip_file_icon);
                } else if (name.endsWith(".apk") || name.endsWith(".ipa")) {
                    info.setIconUrl("drawable://" + R.drawable.zip_file_icon);
                    info.setIconResId(R.drawable.zip_file_icon);
                } else {
                    info.setIconUrl("drawable://" + R.drawable.file_default_icon);
                    info.setIconResId(R.drawable.file_default_icon);
                }
            }
            info.setItemId(file.getAbsolutePath());
            info.setItemValue(file.getName());
            addItemInfo(info);
        }

    }
}
