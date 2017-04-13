package com.android.hcframe.netdisc;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.android.frame.download.FileColumn;
import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcApplication;
import com.android.hcframe.netdisc.sql.OperateDatabase;
import com.android.hcframe.view.toast.NoDataView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pc on 2016/6/27.
 */
public class ImplFragment extends Fragment implements View.OnClickListener, ImplFragAdapter.OnClickCallback {
    private View implFragment;
    private LinearLayout mImplEdit;
    private LinearLayout mImplStop;
    private ListView down_lv;
    ImplFragAdapter mImplFragAdapter;
    List<FileColumn> mDownloadColumns;
    List<FileColumn> mUploadColumns;
    List<FileColumn> fileColumnList = new ArrayList<FileColumn>();
    Context mContext;
    Intent intent;
    boolean state_list;
    TextView netdisc_search_file_text;
    ImageView netdisc_search_file_img;
    boolean edit = false;
    TextView netdisc_search_new_text;
    ActivityCallback activityCallback;
    private NoDataView mNoDataView;

    public interface ActivityCallback {
        void activityCallBack(FileColumn fileColumn);
    }

    public void setActivityCallback(ActivityCallback activityCallback) {
        this.activityCallback = activityCallback;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        TransListActivity transListActivity = (TransListActivity) getActivity();
        transListActivity.setProgressCallback(new TransListActivity.ProgressCallback() {
            @Override
            public void serviceCallBack(com.android.frame.download.FileColumn fileColumn) {
                if (fileColumn.getSuccess() == 1) {
                    Iterator<FileColumn> iterator = fileColumnList.iterator();
                    while (iterator.hasNext()) {
                        FileColumn file = iterator.next();
                        if (fileColumn.getFileid().equals(file.getFileid())) {//如果是等待状态则装入正在上传列表中
                            iterator.remove();
                            if (fileColumn.getUpOrDown() == 0) {
                                mUploadColumns.remove(file);
                                OperateDatabase.deleteUploadInfo(fileColumn, mContext);
                            } else {
                                mDownloadColumns.remove(file);
                                OperateDatabase.deleteDownloadInfo(fileColumn, mContext);
                            }
                        }
                    }
                    Iterator<FileColumn> iter = fileColumnList.iterator();
                    while (iter.hasNext()) {
                        FileColumn file = iter.next();
                        if ("2".equals(file.getTag())) {//如果是等待状态则装入正在上传列表中
                            file.setName("正在上传 (" + mUploadColumns.size() + ")");

                        } else if ("1".equals(file.getTag())) {//如果是等待状态则装入正在下载列表中
                            file.setName("正在下载 (" + mDownloadColumns.size() + ")");

                        }
                    }
                    mImplFragAdapter.notifyDataSetChanged();
                } else {
                    mImplFragAdapter.updateProgress(fileColumn);
                }
            }


        });
        implFragment = inflater.inflate(R.layout.netdisc_impl_frag_layout, container, false);
        mImplEdit = (LinearLayout) implFragment.findViewById(R.id.impl_edit);
        mImplStop = (LinearLayout) implFragment.findViewById(R.id.impl_stop);
        down_lv = (ListView) implFragment.findViewById(R.id.down_lv);
        mNoDataView = (NoDataView) implFragment.findViewById(R.id.impl_pager_no_data);
        down_lv.setEmptyView(mNoDataView);
        netdisc_search_file_text = (TextView) implFragment.findViewById(R.id.netdisc_search_file_text);
        netdisc_search_file_img = (ImageView) implFragment.findViewById(R.id.netdisc_search_file_img);
        netdisc_search_new_text = (TextView) implFragment.findViewById(R.id.netdisc_search_new_text);
        ImplFragAdapter.setOnClickCallback(this);
        initData();
        mImplEdit.setOnClickListener(this);
        mImplStop.setOnClickListener(this);
        return implFragment;
    }

    private void initData() {
        fileColumnList.clear();
        mDownloadColumns = OperateDatabase.getDownloadList(mContext);
        if (mDownloadColumns != null && mDownloadColumns.size() > 0) {
            FileColumn fileColumn = new FileColumn();
            fileColumn.setTag("1");
            fileColumn.setName("正在下载 (" + mDownloadColumns.size() + ")");
            fileColumnList.add(fileColumn);
            for (int i = 0; i < mDownloadColumns.size(); i++) {
                if ("0".equals(mDownloadColumns.get(i).getState()) || "2".equals(mDownloadColumns.get(i).getState())) {
                    state_list = true;
                }
                fileColumnList.add(mDownloadColumns.get(i));
            }
        }
        mUploadColumns = OperateDatabase.getuploadList(mContext);
        if (mUploadColumns != null && mUploadColumns.size() > 0) {
            FileColumn fileColumn = new FileColumn();
            fileColumn.setTag("2");
            fileColumn.setName("正在上传 (" + mUploadColumns.size() + ")");
            fileColumnList.add(fileColumn);
            for (int i = 0; i < mUploadColumns.size(); i++) {
                if ("0".equals(mUploadColumns.get(i).getState()) || "2".equals(mUploadColumns.get(i).getState())) {
                    state_list = true;
                }
                fileColumnList.add(mUploadColumns.get(i));
            }
        }
        mImplFragAdapter = new ImplFragAdapter(mContext, fileColumnList);
        down_lv.setAdapter(mImplFragAdapter);
        show();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.impl_edit) {//选择编辑
            if (!edit) {
                edit = true;
                netdisc_search_new_text.setText("取消编辑");
                for (int i = 0; i < fileColumnList.size(); i++) {
                    FileColumn fileColumn = fileColumnList.get(i);
                    fileColumn.setEdit(1);
                }
                mImplFragAdapter.notifyDataSetChanged();
            } else {
                edit = false;
                netdisc_search_new_text.setText("选择编辑");
                for (int i = 0; i < fileColumnList.size(); i++) {
                    FileColumn fileColumn = fileColumnList.get(i);
                    fileColumn.setEdit(2);
                }
                mImplFragAdapter.notifyDataSetChanged();
            }

        } else if (id == R.id.impl_stop) {//全部暂停
            if (fileColumnList != null && fileColumnList.size() > 0) {
                if (state_list) {
                    stop();
                    state_list = false;
                } else {
                    start();
                    state_list = true;
                }
                show();
            }

        }
    }

    public void show() {
        if (state_list) {
            netdisc_search_file_text.setText("全部暂停");
            netdisc_search_file_img.setImageResource(R.drawable.netdisc_impl_stop);
        } else {
            netdisc_search_file_text.setText("全部开始");
            netdisc_search_file_img.setImageResource(R.drawable.netdisc_impl_start);
        }
    }

    public void stop() {
        Iterator<FileColumn> iterator = fileColumnList.iterator();
        while (iterator.hasNext()) {
            FileColumn fileColumn = iterator.next();
            if ("0".equals(fileColumn.getState()) || "2".equals(fileColumn.getState())) {//（0：下载，1，暂停，2等待）
                fileColumn.setState("1");
                if (fileColumn.getUpOrDown() == 0) {//上传
                    OperateDatabase.updateUploadInfo(fileColumn, mContext);
                } else if (fileColumn.getUpOrDown() == 1) {//下载
                    OperateDatabase.updateDownloadInfo(fileColumn, mContext);
                }

                activityCallback.activityCallBack(fileColumn);
            }
        }
        mImplFragAdapter.notifyDataSetChanged();
    }

    public void start() {
        Iterator<FileColumn> iterator = fileColumnList.iterator();
        while (iterator.hasNext()) {
            FileColumn fileColumn = iterator.next();
            if ("1".equals(fileColumn.getState()) || "3".equals(fileColumn.getState())) {
                fileColumn.setState("2");
                activityCallback.activityCallBack(fileColumn);
            }
        }
        mImplFragAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view, int position) {
        FileColumn fileColumn = (FileColumn) mImplFragAdapter.getItem(position);
        if (fileColumn.getUpOrDown() == 0) {//上传
            OperateDatabase.deleteUploadInfo(fileColumn, HcApplication.getContext());
            Iterator<FileColumn> iterator1 = fileColumnList.iterator();
            while (iterator1.hasNext()) {
                FileColumn fileColumn1 = iterator1.next();
                if (fileColumn.getFileid().equals(fileColumn1.getFileid())) {//如果是等待状态则装入正在下载列表中
                    iterator1.remove();
                    mUploadColumns.remove(fileColumn1);
                }
            }
            Iterator<FileColumn> iter1 = fileColumnList.iterator();
            while (iter1.hasNext()) {
                FileColumn fileColumn2 = iter1.next();
                if ("2".equals(fileColumn2.getTag())) {//如果是等待状态则装入正在下载列表中
                    fileColumn2.setName("正在下载 (" + mUploadColumns.size() + ")");

                }
            }
//            File dir = new File(HcDownloadService.UPLOAD_PATH);
//            if (dir.exists()) {
//                File[] files = dir.listFiles();// 读取
//                for (File file : files) {
//                    String fileName = file.getName();
//                    int positions = fileName.lastIndexOf(".");
//                    String ext = fileName.substring(positions + 1, fileName.length());
//                    if (fileName.equals(fileColumn.getName())) {
//                        file.delete();
//                        break;
//                    }
//                }
//            }
        } else if (fileColumn.getUpOrDown() == 1) {//下载
            OperateDatabase.deleteDownloadInfo(fileColumn, HcApplication.getContext());
            Iterator<FileColumn> iterator1 = fileColumnList.iterator();
            while (iterator1.hasNext()) {
                FileColumn fileColumn1 = iterator1.next();
                if (fileColumn.getFileid().equals(fileColumn1.getFileid())) {//如果是等待状态则装入正在下载列表中
                    iterator1.remove();
                    mDownloadColumns.remove(fileColumn1);
                }
            }
            Iterator<FileColumn> iter1 = fileColumnList.iterator();
            while (iter1.hasNext()) {
                FileColumn fileColumn2 = iter1.next();
                if ("1".equals(fileColumn2.getTag())) {//如果是等待状态则装入正在下载列表中
                    fileColumn2.setName("正在下载 (" + mDownloadColumns.size() + ")");

                }
            }
            File dir = new File(HcDownloadService.DWONLOAD_PATH);
            if (dir.exists()) {
                File[] files = dir.listFiles();// 读取
                for (File file : files) {
                    String fileName = file.getName();
                    int positions = fileName.lastIndexOf(".");
                    String ext = fileName.substring(positions + 1, fileName.length());
                    if (fileName.equals(fileColumn.getName())) {
                        file.delete();
                        break;
                    }
                }
            }

        }
        fileColumn.setState("1");
        activityCallback.activityCallBack(fileColumn);
        mImplFragAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClickService(View view, int position, FileColumn fileColumn) {//点击开始暂停按钮回调接口
        if ("1".equals(fileColumn.getState())) {//（0：下载，1，暂停，2等待）
            if (fileColumn.getUpOrDown() == 0) {//上传
                OperateDatabase.updateUploadInfo(fileColumn, mContext);
            } else if (fileColumn.getUpOrDown() == 1) {//下载
                OperateDatabase.updateDownloadInfo(fileColumn, mContext);
            }
        }
        activityCallback.activityCallBack(fileColumn);
    }
}
