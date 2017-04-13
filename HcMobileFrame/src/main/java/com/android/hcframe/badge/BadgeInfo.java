package com.android.hcframe.badge;


import com.android.hcframe.HcLog;

import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-4-19 14:12.
 */
public abstract class BadgeInfo {

    private static final String TAG = "BadgeInfo";

    /**
     * 数据为应用角标
     */
    public static final int TYPE_APP = 1;
    /**
     * 数据为模块角标
     */
    public static final int TYPE_MODULE = 2;

    /**
     * 客户端ID/应用ID
     */
    protected String mAppId;

    /**
     * 应用ID/应用模块的ID
     */
    protected String mModuleId;

    /**
     * 是否显示角标 0：不可见;1：可见,主要用于应用角标且角标类型为圆点型的
     *
     * @deprecated
     */
    private boolean mVisibility;

    /**
     * 角标数
     * 模块的角标数： 不管什么类型,都为模块自身的角标数.
     * 应用的角标数： 类型为圆点型,且有子模块,则数量为0或者1;没有子模块,数量为应用本身
     * 类型为数字型,则为数字型的各模块之和
     */
    protected int mCount;

    /**
     * 角标显示类型 1、数字型
     * 2、圆点型
     */
    protected int mType;

    /**
     * 上级角标
     */
    protected BadgeInfo mParent;

    public BadgeInfo() {
        mType = BadgeObserver.FLAG_NONE;
    }

    /**
     * 设置客户端ID/应用ID
     *
     * @param appId 客户端ID/应用ID
     */
    public final void setAppId(String appId) {
        mAppId = appId;
    }

    /**
     * 设置应用ID/应用模块的ID
     *
     * @param moduleId 应用ID/应用模块的ID
     */
    public final void setModuleId(String moduleId) {
        mModuleId = moduleId;
    }

    /**
     * 设置角标是否可见
     *
     * @param visibility true:可见;false:不可见
     * @deprecated
     */
    public void setVisibility(boolean visibility) {
        mVisibility = visibility;
    }

    /**
     * 初始化类的时候,设置角标数
     * <p>角标数量更新调用{@link #updateCount(int)}</p>
     *
     * @param count 角标数
     * @see #updateCount(int)
     */
    public final void setCount(int count) {
        if (mCount == count) return;
        mCount = count;
    }

    /**
     * 设置角标显示类型 1、数字型 2、圆点型
     * 从数据库中读取数据
     * <p>角标类型更新请调用{@link #updateType(int)}<p/>
     *
     * @param type 角标类型
     * @see #updateType(int)
     */
    public final void setType(int type) {
        if (mType == type) return;
        mType = type;
    }

    /**
     * 添加角标数,使用于模块及没有子模块的应用
     * <p>有子模块的应用的角标数的添加调用{@link #addCount(BadgeInfo, int)}</p>
     *
     * @param count
     * @see #addCount(BadgeInfo, int)
     */
    public void addCount(int count) {
        throw new UnsupportedOperationException(TAG + "#addCount count = " + count);
    }

    /**
     * 获取客户端ID/应用ID
     *
     * @return 返回客户端ID/应用ID
     */
    public String getAppId() {
        return mAppId;
    }

    public String getModuleId() {
        return mModuleId;
    }

    /**
     * @return
     * @deprecated
     */
    public boolean getVisibility() {
        return mVisibility;
    }

    /**
     * 获取角标显示的类型
     *
     * @return 返回角标的显示类型
     */
    public final int getType() {
        return mType;
    }

    public int getCount() {
        return mCount;
    }

    /**
     * 设置上级角标信息
     *
     * @param info
     */
    public void setParentBadge(BadgeInfo info) {
        mParent = info;
    }

    public BadgeInfo getParentBadge() {
        return mParent;
    }

    /**
     * 添加下级角标
     *
     * @param info   下级角标信息
     * @param update 是否需要更新角标数和角标类型
     * @see #mParent
     */
    public void addBadge(BadgeInfo info, boolean update) {
        throw new UnsupportedOperationException(TAG + "#addBadge info = " + info);
    }

    /**
     * 移除下级角标信息
     *
     * @param key
     */
    public void removeBadge(String key) {
        throw new UnsupportedOperationException(TAG + "#removeBadge key = " + key);
    }

    /**
     * 是否有下级角标
     *
     * @return
     */
    public boolean isEmpty() {
        throw new UnsupportedOperationException(TAG + "#isEmpty!");
    }


    public List<BadgeInfo> getBadges() {
        throw new UnsupportedOperationException(TAG + "#getBadges!");
    }

    /**
     * 点击应用或者模块
     *
     * @param observer
     */
    public void onClick(BadgeObserver observer) {
        throw new UnsupportedOperationException(TAG + "#onClick! observer = " + observer);
    }

    /**
     * 更新角标数（覆盖原有的角标数量）,适用于子模块或者没有子模块的应用,
     *
     * @param count
     */
    public void updateCount(int count) {
        HcLog.D(TAG + " #updateCount current count = "+mCount + " update count = "+count);
        if (mCount == count) return;
        int temp = mCount - count;
        mCount = count;

        BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
        if (observer != null) {
            observer.setCount(Math.abs(temp));
        }

        if (mParent != null) {
            if (temp > 0) {
                mParent.removeCount(this, temp);
            } else {
                mParent.addCount(this, Math.abs(temp));
            }
        }


    }

    public enum Operate {
        /**
         * 删除角标数据
         */
        DELETE,
        /**
         * 添加角标数据
         */
        ADD
    }

    /**
     * 更新应用或者模块的角标类型,应用要是有子模块,则不支持更新.
     *
     * @param type
     */
    public void updateType(int type) {
        if (mType == type) return;
        mType = type;
        // 更新界面
        BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
        if (observer != null) {
            observer.setFlag(mType);
        }
        // 通知上级更新类型
        if (mParent != null) {
            mParent.resetType(this);
        }
    }

    /**
     * 当应用或者模块的类型发生变化时,通知上级应用变更角标类型
     *
     * @param info 当前变更的模块或者应用
     */
    public void resetType(BadgeInfo info) {
        throw new UnsupportedOperationException(TAG + "#resetType! info = " + info);
    }

    /**
     * 有子模块的应用添加角标数,角标类型为圆点型的时候,
     * 数量在0与1之间切换
     *
     * @param info  子模块
     * @param count
     */
    public void addCount(BadgeInfo info, int count) {
        throw new UnsupportedOperationException(TAG + "#addCount! info = " + info + " count =" + count);
    }

    /**
     * 有子模块的应用删除角标数,角标类型为圆点型的时候,
     * 数量在0与1之间切换
     *
     * @param info
     * @param count
     */
    public void removeCount(BadgeInfo info, int count) {
        throw new UnsupportedOperationException(TAG + "#addCount! info = " + info + " count =" + count);
    }

    /**
     * 删除角标数
     * <p>删除有子模块的角标数调用{@link #removeCount(BadgeInfo, int)}</p>
     *
     * @param count
     * @see #removeCount(BadgeInfo, int)
     */
    public void removeCount(int count) {
        if (mCount == 0) return;
        int temp = mCount;
        mCount -= count;
        if (mCount < 0) {
            mCount = 0;
            count = temp;
        }
        if (mType == BadgeObserver.FLAG_TEXT) {
            BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
            if (observer != null) {
                observer.setCount(mCount);

            }
            // 通知上级应用变更
            if (mParent != null) {
                mParent.removeCount(this, count);
            }
        } else {
            if (mCount == 0) {
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(0);

                }
                // 这里要不要通知上级应用变更
                if (mParent != null) {
                    mParent.removeCount(this, count);
                }
            }
        }
    }

    public Iterator<BadgeInfo> iterator() {
        // TODO Auto-generated method stub
        return new NullIterator();
    }

    /**
     * 清楚角标数据
     */
    public void clearBadge() {
        throw new UnsupportedOperationException(TAG + "#clearBadge!");
    }
}
