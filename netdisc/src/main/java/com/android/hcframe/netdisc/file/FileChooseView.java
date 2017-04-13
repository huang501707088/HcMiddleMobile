package com.android.hcframe.netdisc.file;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.netdisc.R;
import com.android.hcframe.netdisc.audio.AudioChooseView;
import com.android.hcframe.netdisc.audio.NetdiscAudioInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-1 18:07.
 */
public class FileChooseView extends AudioChooseView {

    private static final String TAG = "FileChooseView";

    private Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    HcUtil.showToast(mContext, "暂无外部存储");
                    mContext.finish();
                    break;
                case 1000:
                    HcLog.D(TAG + " #handleMessage before add size = "+mInfos.size());
                    mInfos.addAll((List<NetdiscAudioInfo>) msg.obj);
                    HcLog.D(TAG + " #handleMessage after add size = " + mInfos.size());
                    if (mAdapter == null) {
                        mAdapter = new FileAdapter(mContext, mInfos);
                        mAbsView.setAdapter(mAdapter);
                    } else {
                        mAdapter.notifyDataSetChanged();
                    }
                    break;

                default:
                    break;
            }
        }
    };

    public FileChooseView(Activity context, ViewGroup group, String data) {
        super(context, group, data, null);
    }


    @Override
    public void initAdapter() {
        if (mAdapter == null) {
            mAdapter = new FileAdapter(mContext, mInfos);
        }
    }

    @Override
    public void scanFiles() {
        if (!Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            mHandler.sendEmptyMessage(100);
            return;
        }

        File root = Environment.getExternalStorageDirectory();// 获得SD卡路径
        final File[] files = root.listFiles();
        readFiles(files);
    }

    private void readFiles(File[] files) {
        if (files == null) return;
        List<NetdiscAudioInfo> infos = null;
        NetdiscAudioInfo info;
        for (File file : files) {
            if (file.isDirectory()) {
                readFiles(file.listFiles());
            } else {
                String name = file.getName();
                if (name.endsWith(".txt")) {
                    if (infos == null) {
                        infos = new ArrayList<NetdiscAudioInfo>();
                    }
                    info = new NetdiscAudioInfo();
                    info.setResId(R.drawable.netdisc_none_file);
                    addFile(infos, info, file);
                } else if (name.endsWith(".doc") || name.endsWith(".docx")) {
                    if (infos == null) {
                        infos = new ArrayList<NetdiscAudioInfo>();
                    }
                    info = new NetdiscAudioInfo();
                    info.setResId(R.drawable.netdisc_word_file);
                    addFile(infos, info, file);
                } else if (name.endsWith(".ppt") || name.endsWith(".pptx")) {
                    if (infos == null) {
                        infos = new ArrayList<NetdiscAudioInfo>();
                    }
                    info = new NetdiscAudioInfo();
                    info.setResId(R.drawable.netdisc_ppt_file);
                    addFile(infos, info, file);
                } else if (name.endsWith(".xls") || name.endsWith(".xlsx")) {
                    if (infos == null) {
                        infos = new ArrayList<NetdiscAudioInfo>();
                    }
                    info = new NetdiscAudioInfo();
                    info.setResId(R.drawable.netdisc_excel_file);
                    addFile(infos, info, file);
                } else if (name.endsWith(".zip") || name.endsWith(".rar") ||
                        name.endsWith(".tar") || name.endsWith(".7z")) {
                    if (infos == null) {
                        infos = new ArrayList<NetdiscAudioInfo>();
                    }
                    info = new NetdiscAudioInfo();
                    info.setResId(R.drawable.netdisc_zip_file);
                    addFile(infos, info, file);
                } else if (name.endsWith(".apk") || name.endsWith(".ipa")|| name.endsWith(".pdf")) {
                    if (infos == null) {
                        infos = new ArrayList<NetdiscAudioInfo>();
                    }
                    info = new NetdiscAudioInfo();
                    info.setResId(R.drawable.netdisc_none_file);
                    addFile(infos, info, file);
                }
            }
        }

        if (infos != null) {
            Message message = new Message();
            message.what = 1000;
            message.obj = infos;
            mHandler.sendMessage(message);
        }
    }

    private void addFile(List<NetdiscAudioInfo> infos, NetdiscAudioInfo info, File file) {
        String name = file.getName(); // 有扩展名的
        String subName = name.substring(0, name.lastIndexOf('.'));
        info.setFileName(subName);
        info.setDisplayName(name);
        info.setFilePath(file.getAbsolutePath());
        info.setDate(file.lastModified());
        info.setSize(file.length() + "");
        HcLog.D(TAG + " #addFile name = " + name + " subName = " + subName);
        infos.add(info);
    }
}
