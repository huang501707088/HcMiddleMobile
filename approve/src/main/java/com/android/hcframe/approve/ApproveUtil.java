package com.android.hcframe.approve;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by czx on 2016/12/1.
 */

public class ApproveUtil {
    /**
     * 缓存下载成功文件的ID
     */
    // 存储sharedpreferences
    public static void setFileIDSP(Context context, String FileId) {
        if (FileId != null && !"".equals(FileId)) {
            SharedPreferences mSwitchShared = context.getSharedPreferences("Approve", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = mSwitchShared.edit();
            String fileList = mSwitchShared.getString("fileList", "");
            if (fileList != null && !"".equals(fileList)) {
                String[] strings = fileList.split(",");
                boolean b = false;
                for (int i = 0; i < strings.length; i++) {
                    if (FileId.equals(strings[i])) {
                        b = true;
                    }
                }
                if (!b) {
                    fileList = fileList + "," + FileId;
                }
                editor.putString("fileList", fileList);
            } else {
                editor.putString("fileList", FileId);
            }
            editor.commit();// 提交修改
        }

    }

    /**
     * 获取下载成功文件的ID
     *
     * @param context
     * @return
     */
    public static String getFileIDSP(Context context) {
        SharedPreferences mSwitchShared = context.getSharedPreferences("Approve", Context.MODE_PRIVATE);
        String fileList = mSwitchShared.getString("fileList", "");
        return fileList;
    }
}
