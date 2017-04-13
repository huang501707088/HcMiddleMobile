package com.android.hcframe.netdisc;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.frame.download.HcDownloadService;
import com.android.hcframe.HcUtil;
import com.android.hcframe.netdisc.netdisccls.DownFragItem;
import com.android.hcframe.netdisc.sql.OperateDatabase;
import com.android.hcframe.view.TwoBtnAlterDialog;
import com.android.hcframe.view.toast.NoDataView;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pc on 2016/6/27.
 */
public class DownFragment extends Fragment {
    private View downFragment;
    private ListView mDownLv;
    private List<DownFragItem> mDownFragItem;
    private DownFragAdapter mDownFragAdapter;
    private TextView mNetdiscDownDelete;
    Context mContext;
    private static final int ITEM_TYPE = 1;
    private static final int BUTTON_TYPE = 2;
    private NoDataView mNoDataView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mContext = getActivity();
        downFragment = inflater.inflate(R.layout.netdisc_down_frag_layout, container, false);
        mDownLv = (ListView) downFragment.findViewById(R.id.netdisc_down_lv);
        mNoDataView = (NoDataView) downFragment.findViewById(R.id.down_pager_no_data);
        mDownLv.setEmptyView(mNoDataView);
        mNetdiscDownDelete = (TextView) downFragment.findViewById(R.id.netdisc_down_delete);
        initData();
        TransListActivity transListActivity = (TransListActivity) getActivity();
        transListActivity.setDownlaodCallback(new TransListActivity.DownloadCallback() {
            @Override
            public void notifyDownload() {
                initData();
            }
        });

        mDownLv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapter, View parent, int position,
                                    long arg3) {
                DownFragItem downFragItem = (DownFragItem) adapter.getAdapter().getItem(position);
                //点击PullToRefreshListView触发删除事件
                twoBtnAlterDialog(mContext, "是否删除该文件", position, ITEM_TYPE, downFragItem);

            }
        });
        mNetdiscDownDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDownFragItem != null && mDownFragItem.size() > 0) {
                    twoBtnAlterDialog(mContext, "是否删除所有文件", 0, BUTTON_TYPE, null);
                }
            }
        });
        return downFragment;
    }

    private static TwoBtnAlterDialog alterDialog;

    /**
     * 弹出重新登录dialog
     *
     * @param context activity实例
     * @param msg     提示消息体
     */
    private void twoBtnAlterDialog(final Context context, String msg, final int position, final int type, final DownFragItem downFragItem) {
        if (alterDialog == null) {
            alterDialog = TwoBtnAlterDialog.createDialog(context, msg);
            TwoBtnAlterDialog.btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (type == ITEM_TYPE) {
                        File file = new File(downFragItem.getDownFileM());
                        boolean b = file.delete();
                        if (b) {
                            mDownFragItem.remove(position);
                        }
                        numCallback.notifynum(mDownFragItem.size());
                        mDownFragAdapter.notifyDataSetChanged();

                    } else {
                        Iterator<DownFragItem> iterator = mDownFragItem.iterator();
                        while (iterator.hasNext()) {
                            DownFragItem downFragItem1 = iterator.next();
                            File file = new File(downFragItem1.getDownFileM());
                            boolean b = file.delete();
                            if (b) {
                                iterator.remove();
                            }
                        }
                        numCallback.notifynum(mDownFragItem.size());
                        mDownFragAdapter.notifyDataSetChanged();
                    }
                    if (mDownFragItem == null || mDownFragItem.size() == 0) {
                        mDownLv.setSelected(false);
                        mDownLv.setBackgroundResource(R.color.netdisc_down_delete_bg);
                    } else {
                        mDownLv.setSelected(true);
                        mDownLv.setBackgroundResource(R.color.netdisc_write);
                    }
                    alterDialog.dismiss();
                    alterDialog = null;

                }
            });
            TwoBtnAlterDialog.btn_cancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    alterDialog.dismiss();
                    alterDialog = null;
                }
            });
            alterDialog.show();
        } else {
            alterDialog.dismiss();
            alterDialog = null;
        }

    }

    private void initData() {
        // 为Adapter准备数据
        mDownFragItem = new ArrayList<DownFragItem>();
        File dir = new File(HcDownloadService.DWONLOAD_PATH);
        DownFragItem downFragItem;
        if (dir.exists()) {
            File[] files = dir.listFiles();// 读取
            for (File file : files) {
                downFragItem = new DownFragItem();
                String fileName = file.getName();
                int position = fileName.lastIndexOf(".");
                String ext = fileName.substring(position + 1, fileName.length());
                long lastModified = file.lastModified();//最后修改时间
                String time = HcUtil.getDate("yyyy-MM-dd HH:mm:ss", lastModified);
                downFragItem.setDownFileName(fileName);
                downFragItem.setDownTime(time);
                downFragItem.setDownFileM(file.getAbsolutePath());
                downFragItem.setDownImg(ext);
                mDownFragItem.add(downFragItem);
            }
        }
        List<com.android.frame.download.FileColumn> mDownloading = OperateDatabase.getDownloadList(mContext);
        Iterator<DownFragItem> iterator = mDownFragItem.iterator();
        while (iterator.hasNext()) {
            DownFragItem downFragItem1 = iterator.next();
            for (int i = 0; i < mDownloading.size(); i++) {
                if (downFragItem1.getDownFileName().equals(mDownloading.get(i).getName())) {
                    iterator.remove();
                    break;
                }
            }


        }
        // 实例化自定义的MyAdapter
        mDownFragAdapter = new DownFragAdapter(getActivity(), mDownFragItem);
        // 绑定Adapter
        mDownLv.setAdapter(mDownFragAdapter);
        if (mDownFragItem == null || mDownFragItem.size() == 0) {
            mDownLv.setSelected(false);
            mDownLv.setBackgroundResource(R.color.netdisc_down_delete_bg);
        } else {
            mDownLv.setSelected(true);
            mDownLv.setBackgroundResource(R.color.netdisc_write);
        }
        numCallback.notifynum(mDownFragItem.size());
    }

    NumCallback numCallback;

    public interface NumCallback {
        void notifynum(int size);
    }

    public void setNumCallback(NumCallback numCallback) {
        this.numCallback = numCallback;
    }
}
