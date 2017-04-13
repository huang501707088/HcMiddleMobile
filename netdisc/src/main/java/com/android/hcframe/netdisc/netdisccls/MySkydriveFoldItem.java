package com.android.hcframe.netdisc.netdisccls;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by pc on 2016/7/8.
 */
public class MySkydriveFoldItem extends MySkydriveInfoItem{

    private static final String TAG = "MySkydriveFoldItem";

    /** 文件夹列表 */
    private List<MySkydriveInfoItem> mInfos = new ArrayList<MySkydriveInfoItem>();

    public MySkydriveFoldItem() {
    }

    public Iterator<MySkydriveInfoItem> iterator() {
        // TODO Auto-generated method stub
        return mInfos.iterator();
    }

    @Override
    public List<MySkydriveInfoItem> getItems() {
        // TODO Auto-generated method stub
        return mInfos;
    }

    @Override
    public void addItem(MySkydriveInfoItem item) {
        if (mInfos.contains(item)) return;
        mInfos.add(item);
    }

    @Override
    public void addAllItems(List<MySkydriveInfoItem> items) {
        mInfos.addAll(items);
    }
}
