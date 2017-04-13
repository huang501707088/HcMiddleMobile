package com.android.hcframe.view.selector.file;

import android.content.ContentResolver;
import android.database.Cursor;
import android.provider.MediaStore;

import com.android.hcframe.HcLog;
import com.android.hcframe.R;
import com.android.hcframe.view.selector.AbstractChooseFileActivity;
import com.android.hcframe.view.selector.ItemInfo;

import java.io.File;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-11-28 11:41.
 */

public class VideoChooseActivity extends AbstractChooseFileActivity {

    private static final String TAG = "VideoChooseActivity";

    @Override
    protected String getTopbarTitle() {
        return "视频选择";
    }

    @Override
    protected void scanFiles(String fileName) {
        ContentResolver cr = getContentResolver();
        String[] projections = {MediaStore.Video.Media._ID, //图片 id，从 1 开始自增
                MediaStore.Video.Media.DATA, // 图片绝对路径
                MediaStore.Video.Media.TITLE, //不带扩展名的文件名
                MediaStore.Video.Media.SIZE,
                MediaStore.Video.Media.DATE_ADDED,
                MediaStore.Video.Media.DISPLAY_NAME};
        Cursor c = cr.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projections, null, null, null);
        int count = 0;
        if (c != null && c.getCount() > 0) {
            HcLog.D(TAG + " scanFiles count =" + c.getCount());
            c.moveToFirst();
            ItemInfo info = null;
            File file = null;
            while (!c.isAfterLast()) {
                int fileId = c.getInt(0);
                String data = c.getString(1);
                String name = c.getString(2);
                long size = c.getLong(3);
                long date = c.getLong(4);
                HcLog.D(TAG + "#scanFiles audioId =" + fileId + " data=" + data
                        + " name = " + name + " size = " + size + " date = " + date);

                file = new File(data);
                if (!file.exists()) {
                    c.moveToNext();
                    file = null;
                    continue;
                }
                count ++;
                info = new FileInfo();
                info.setItemId("" + fileId);
                info.setMultipled(mSelected);
                info.setItemValue(c.getString(5)); // 带扩展名的文件名
                /** 注意这里没有增加"file://"  */
                info.setFilePath(data);
                info.setIconUrl("drawable://" + R.drawable.video_file_icon);
                info.setIconResId(R.drawable.video_file_icon);
                addItemInfo(info);
                c.moveToNext();
            }
        }

        if (c != null) {
            c.close();
            c = null;
        }
        HcLog.D(TAG + " #scanFiles infos size = " + count);
    }
}
