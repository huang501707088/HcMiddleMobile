package com.android.hcframe.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-6-20 11:04.
 */
public class NodeInfo extends TreeInfo {

    private List<TreeInfo> infos = new ArrayList<TreeInfo>();

    @Override
    public Iterator<TreeInfo> iterator() {
        return new CompositeIterator(infos.iterator());
    }

    @Override
    public List<TreeInfo> getInfos() {
        return infos;
    }
}
