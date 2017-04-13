package com.android.hcframe.push;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-9-20 09:38.
 */

public class PushItem {

    /** 应用的ID/模块的ID */
    private String mId;
    /** 应用的名字/模块的名字 */
    private String mName;


    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isPushed() {
        throw new UnsupportedOperationException("PushItem #getPushed!!!!! ");
    }

    public void setPushed(boolean pushed) {
        throw new UnsupportedOperationException("PushItem #setPushed pushed = "+pushed);
    }

    public void addItem(PushItem item) {
        throw new UnsupportedOperationException("PushItem #PushItem!!!!! item = "+item);
    }

    public List<PushItem> getItems() {
        throw new UnsupportedOperationException("PushItem #getItems!!!!! ");
    }
}
