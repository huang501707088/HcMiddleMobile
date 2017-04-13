package com.android.hcframe.view.selector;

import android.os.Parcel;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-5 16:46.
 */
public class ParentInfo extends ItemInfo {

    private List<ItemInfo> mItems = new ArrayList<ItemInfo>();

    public ParentInfo() {}

    public ParentInfo(Parcel p) {
        super(p);
        int size = p.readInt();
        for (int i = 0; i < size; i++) {

        }

    }

    public static final Creator<ItemInfo> CREATOR = new Creator<ItemInfo>() {
        @Override
        public ParentInfo createFromParcel(Parcel in) {
            return new ParentInfo(in);
        }

        @Override
        public ParentInfo[] newArray(int size) {
            return new ParentInfo[size];
        }
    };
}
