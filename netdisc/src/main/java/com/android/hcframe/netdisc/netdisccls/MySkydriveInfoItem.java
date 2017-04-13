package com.android.hcframe.netdisc.netdisccls;

import android.graphics.Bitmap;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pc on 2016/7/8.
 */
public class MySkydriveInfoItem implements Serializable {
    /**
     * 文件夹编号/文件编号
     */
    private String netdiscId;
    /**
     * 文件夹名称/文件名称
     */
    private String netdiscListText;
    /**
     * 文件夹上级目录/文件上级目录
     */
    private String netdiscUpdirId;
    /**
     * 文件夹层级/文件层级
     */
    private String netdiscDirLvl;

    /**
     * 文件夹日期/文件日期
     */
    private String netdiscListDate;
    /**
     * 文件夹类型/文件类型
     */
    private String netdiscListType;
    /**
     * 文件夹大小/文件大小
     */
    private String netdiscListFileSize;
    /**
     * 共享文件的数量
     */
    private String netdiscListSharedSize;
    /**
     * 判断是否是我的网络列表或共享空间列表
     * */
    private String netdiscListDirType;
    private boolean mChecked = false;

    public MySkydriveInfoItem() {
    }

    public String getNetdiscId() {
        return netdiscId;
    }

    public void setNetdiscId(String netdiscId) {
        this.netdiscId = netdiscId;
    }

    public String getNetdiscListText() {
        return netdiscListText;
    }

    public void setNetdiscListText(String netdiscListText) {
        this.netdiscListText = netdiscListText;
    }

    public String getNetdiscUpdirId() {
        return netdiscUpdirId;
    }

    public void setNetdiscUpdirId(String netdiscUpdirId) {
        this.netdiscUpdirId = netdiscUpdirId;
    }

    public String getNetdiscDirLvl() {
        return netdiscDirLvl;
    }

    public void setNetdiscDirLvl(String netdiscDirLvl) {
        this.netdiscDirLvl = netdiscDirLvl;
    }

    public String getNetdiscListDate() {
        return netdiscListDate;
    }

    public void setNetdiscListDate(String netdiscListDate) {
        this.netdiscListDate = netdiscListDate;
    }

    public String getNetdiscListType() {
        return netdiscListType;
    }

    public void setNetdiscListType(String netdiscListType) {
        this.netdiscListType = netdiscListType;
    }

    public String getNetdiscListFileSize() {
        return netdiscListFileSize;
    }

    public void setNetdiscListFileSize(String netdiscListFileSize) {
        this.netdiscListFileSize = netdiscListFileSize;
    }

    public String getNetdiscListSharedSize() {
        return netdiscListSharedSize;
    }

    public String getNetdiscListDirType() {
        return netdiscListDirType;
    }

    public void setNetdiscListDirType(String netdiscListDirType) {
        this.netdiscListDirType = netdiscListDirType;
    }

    public void setNetdiscListSharedSize(String netdiscListSharedSize) {
        this.netdiscListSharedSize = netdiscListSharedSize;
    }

    public Iterator<MySkydriveInfoItem> iterator() {
        return null;
    }

    public void addItem(MySkydriveInfoItem item) {
        throw new UnsupportedOperationException("MySkydriveInfoItem #addItem item = " + item);
    }

    public List<MySkydriveInfoItem> getItems() {
        throw new UnsupportedOperationException("MySkydriveInfoItem #getItems!!!!!");
    }

    public void addAllItems(List<MySkydriveInfoItem> items) {
        throw new UnsupportedOperationException("MySkydriveInfoItem #addAllItems items = " + items);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
    }

    public boolean getChecked() {
        return mChecked;
    }
}
