package com.android.hcframe.iterator;

import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-6-20 11:02.
 */
public abstract class TreeInfo {

    private static final String TAG = "TreeInfo";

    public List<TreeInfo> getInfos() {
        throw new UnsupportedOperationException(TAG + "#getInfos!");
    }

    public Iterator<TreeInfo> iterator() {
        // TODO Auto-generated method stub
        return new NullIterator();
    }
}
