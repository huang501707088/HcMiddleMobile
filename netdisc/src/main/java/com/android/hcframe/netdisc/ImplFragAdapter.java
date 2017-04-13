package com.android.hcframe.netdisc;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.frame.download.FileColumn;
import com.android.hcframe.HcLog;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by pc on 2016/6/27.
 */
public class ImplFragAdapter extends BaseAdapter {
    private List<FileColumn> mImplFragItem;
    private Context mContext;
    private LayoutInflater mInflater;
    Intent intent;

    public interface OnClickCallback {
        void onClick(View view, int position);

        void onClickService(View view, int position, FileColumn fileColumn);
    }

    private static OnClickCallback mCallback;

    public static void setOnClickCallback(OnClickCallback callback) {
        mCallback = callback;
    }

    public ImplFragAdapter(Context context, List<FileColumn> list) {
        super();
        mImplFragItem = list;
        mContext = context;
        mInflater = LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return mImplFragItem.size();
    }

    @Override
    public Object getItem(int position) {
        return mImplFragItem.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ImplFragItemHolder implFragItemHolder = null;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.netdisc_impl_frag_list_item,
                    parent, false);
            implFragItemHolder = new ImplFragItemHolder();
            implFragItemHolder.implImg = (ImageView) convertView.findViewById(R.id.netdisc_impl_list_img);
            implFragItemHolder.implFileName = (TextView) convertView.findViewById(R.id.netdisc_impl_list_text);
            implFragItemHolder.implFileM = (TextView) convertView.findViewById(R.id.netdisc_impl_list_one_date);
            implFragItemHolder.implFileDownM = (TextView) convertView.findViewById(R.id.netdisc_impl_list_two_date);
            implFragItemHolder.implFileMS = (TextView) convertView.findViewById(R.id.netdisc_impl_down_speed);
            implFragItemHolder.id_tv_tag = (TextView) convertView.findViewById(R.id.id_tv_tag);
            implFragItemHolder.id_ll_res = (LinearLayout) convertView.findViewById(R.id.id_ll_res);
            implFragItemHolder.mDownloadPercentView = (DownloadPercentView) convertView.findViewById(R.id.downloadPrecentView);
            implFragItemHolder.netdisc_impl_list_right = (LinearLayout) convertView.findViewById(R.id.netdisc_impl_list_right);
            implFragItemHolder.netdisc_impl_delete_img = (ImageView) convertView.findViewById(R.id.netdisc_impl_delete_img);
            convertView.setTag(implFragItemHolder);
        } else {
            implFragItemHolder = (ImplFragItemHolder) convertView.getTag();
        }
        final FileColumn downloadColumn = mImplFragItem.get(position);
        if (downloadColumn.getTag() == null || "".equals(downloadColumn.getTag())) {
            if (downloadColumn.getEdit() == 1) {
                implFragItemHolder.netdisc_impl_list_right.setVisibility(View.GONE);
                implFragItemHolder.netdisc_impl_delete_img.setVisibility(View.VISIBLE);
            } else {
                implFragItemHolder.netdisc_impl_list_right.setVisibility(View.VISIBLE);
                implFragItemHolder.netdisc_impl_delete_img.setVisibility(View.GONE);
            }
            implFragItemHolder.id_ll_res.setVisibility(View.VISIBLE);
            implFragItemHolder.id_tv_tag.setVisibility(View.GONE);
            int speed = downloadColumn.getSpeed();
            String sp = getSpeed(speed);
            implFragItemHolder.implFileMS.setText(sp);
            implFragItemHolder.implFileName.setText(downloadColumn.getName());
            String state = downloadColumn.getState();
            implFragItemHolder.mDownloadPercentView.setStatus(Integer.decode(state));
            long size = Long.decode(downloadColumn.getFileSize());
            int progress = (int) (downloadColumn.getPosition() * 100 / size);
            implFragItemHolder.mDownloadPercentView.setProgress(progress);
            String downSize = getFileSizeB(downloadColumn.getPosition());
            String fileSize = getFileSizeB(size);
            implFragItemHolder.implFileM.setText(downSize);
            implFragItemHolder.implFileDownM.setText(fileSize);
            readExt(downloadColumn.getExt(), implFragItemHolder);
//        implFragItemHolder.mDownloadPercentView.setProgress(downloadColumn.getProgress());
//        implFragItemHolder.mDownloadPercentView.setStatus(Integer.getInteger(downloadColumn.getState()));
            implFragItemHolder.mDownloadPercentView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if ("0".equals(downloadColumn.getState()) || "2".equals(downloadColumn.getState())) {//（0：下载，1，暂停，2等待,3,失败）
                        downloadColumn.setState("1");

                    } else if ("1".equals(downloadColumn.getState()) || "3".equals(downloadColumn.getState())) {//（0：下载，1，暂停，2等待,3,失败）
                        downloadColumn.setState("2");
                    }

                    mCallback.onClickService(view, position, downloadColumn);
                }
            });
            final int mposition = position;
            implFragItemHolder.netdisc_impl_delete_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onClick(view, mposition);
                }
            });
        } else {
            implFragItemHolder.id_tv_tag.setVisibility(View.VISIBLE);
            implFragItemHolder.id_ll_res.setVisibility(View.GONE);
            implFragItemHolder.id_tv_tag.setText(downloadColumn.getName());
        }

        return convertView;
    }

    private String getFileSizeB(long position) {
        String str = "";
        DecimalFormat df = new DecimalFormat(".##");
        if (position != 0) {
            if (position > 1024 * 1024) {
                return str = df.format(((double) position) / (1024 * 1024.00)) + "M";
            } else {
                return str = df.format(((double) position) / 1024.00) + "K";
            }
        }
        return "0k";
    }

    private String getSpeed(int speed) {
        DecimalFormat df = new DecimalFormat(".##");
        if (speed != 0) {
            if (speed > 1024) {
                return df.format(((double) speed) / (1024.00)) + "Mb/s";
            } else {
                return speed + "Kb/s";
            }
        }
        return "0k";
    }

    public class ImplFragItemHolder {
        ImageView implImg, netdisc_impl_delete_img;
        TextView implFileName;
        TextView implFileM;
        TextView implFileDownM;
        TextView implFileMS;
        TextView id_tv_tag;
        LinearLayout id_ll_res, netdisc_impl_list_right;
        DownloadPercentView mDownloadPercentView;
    }

    /**
     * 更新列表项中的进度
     */
    public void updateProgress(String id, String state, long finished, int speed) {
        for (int i = 0; i < mImplFragItem.size(); i++) {
            HcLog.D(id + "");
            HcLog.D(mImplFragItem.get(i).getName());
            if (mImplFragItem.get(i).getMd5() != null && mImplFragItem.get(i).getMd5().length() > 0) {
                if (id.equals(mImplFragItem.get(i).getFileid())) {
                    mImplFragItem.get(i).setPosition(finished);
                    mImplFragItem.get(i).setState(state);
                    mImplFragItem.get(i).setSpeed(speed);
                    HcLog.D(finished + "===============================================");
                }
            } else {
                if (id.equals(mImplFragItem.get(i).getFileid())) {
                    mImplFragItem.get(i).setPosition(finished);
                    mImplFragItem.get(i).setState(state);
                    mImplFragItem.get(i).setSpeed(speed);
                    HcLog.D(finished + "===============================================");
                }
            }

        }
        notifyDataSetChanged();
    }

    /**
     * 更新列表项中的进度
     */
    public void updateProgress(com.android.frame.download.FileColumn fileColumn) {
        for (int i = 0; i < mImplFragItem.size(); i++) {
            HcLog.D(mImplFragItem.get(i).getName());
            if (fileColumn.getFileid().equals(mImplFragItem.get(i).getFileid())) {
                mImplFragItem.get(i).setPosition(fileColumn.getPosition());
                mImplFragItem.get(i).setState(fileColumn.getState());
                mImplFragItem.get(i).setSpeed(fileColumn.getSpeed());
            }
        }
        notifyDataSetChanged();
    }

    private void readExt(String ext, ImplFragItemHolder implFragItemHolder) {
        if (ext == null || "".equals(ext)) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_none_file);
        } else if (ext.equals("txt")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_none_file);
        } else if (ext.equals("doc") || ext.equals("docx")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_word_file);
        } else if (ext.equals("ppt") || ext.equals("pptx")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_ppt_file);
        } else if (ext.equals("xls") || ext.equals("xlsx")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_excel_file);
        } else if (ext.equals("zip") || ext.equals("rar") ||
                ext.equals("tar") || ext.equals("7z")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_zip_file);
        } else if (ext.equals("apk") || ext.equals("ipa")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_none_file);
        } else if (ext.equals("jpg") || ext.equals("jpeg")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_img_file);
        } else if (ext.equals("mp3") || ext.equals("mp4") || ext.equals("amr")) {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_img_file);
        } else {
            implFragItemHolder.implImg.setImageResource(R.drawable.netdisc_none_file);
        }
    }
}
