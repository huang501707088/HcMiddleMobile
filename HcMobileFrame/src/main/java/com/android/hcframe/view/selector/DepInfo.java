package com.android.hcframe.view.selector;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-8 13:50.
 */

import android.os.Parcel;

import com.android.hcframe.HcLog;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门,注意进行远程传递的时候,列表是没有传递的.
 */
public class DepInfo extends ItemInfo {

    private List<ItemInfo> mInfos = new ArrayList<ItemInfo>();

    public DepInfo() {}

    public DepInfo(Parcel p) {
        super(p);
    }

    @Override
    public void addAllChild(List<ItemInfo> childs) {
        mInfos.addAll(childs);
    }

    @Override
    public void addChild(ItemInfo child) {
        if (!mInfos.contains(child))
            mInfos.add(child);
    }

    @Override
    public List<ItemInfo> getChilds() {
        List<ItemInfo> childs = new ArrayList<ItemInfo>();
        for (ItemInfo info : mInfos) {
        	if (info instanceof StaffInfo)
                childs.add(info);
        }
        return childs;
    }

    @Override
    public void addAllChild(List<ItemInfo> childs, boolean clear) {
        if (clear) {
            mInfos.clear();
            addAllChild(childs);
        } else {
            for (ItemInfo item : childs) {
            	addChild(item);
            }
        }
    }

    public static final Creator<DepInfo> CREATOR = new Creator<DepInfo>() {
        @Override
        public DepInfo createFromParcel(Parcel in) {
            return new DepInfo(in);
        }

        @Override
        public DepInfo[] newArray(int size) {
            return new DepInfo[size];
        }
    };

    @Override
    public void setSelected(boolean selected) {
        HcLog.D("DepInfo #setSelected selected = "+selected);
    }

    /**
     * 列表是否全部选中
     * @return
     */
    @Override
    public boolean isSelected() {
        boolean selected = false;
        for (ItemInfo info : mInfos) {
            if (info instanceof StaffInfo) {
                selected = info.isSelected();
                if (!selected) return selected;
            }
        }
        return selected;
    }

    @Override
    public List<ItemInfo> getAllChilds() {
        return mInfos;
    }

}
