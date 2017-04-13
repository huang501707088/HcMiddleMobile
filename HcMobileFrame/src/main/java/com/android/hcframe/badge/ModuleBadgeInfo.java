package com.android.hcframe.badge;

import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-4-19 14:40.
 */
public class ModuleBadgeInfo extends BadgeInfo {

    private static final String TAG = "ModuleBadgeInfo";

    @Override
    public void onClick(BadgeObserver observer) {
        if (mCount == 0) return;
        int temp = mCount;
        mCount = 0;
        if (observer != null) {
            observer.setCount(mCount);
        }
        if (mParent != null) {
            mParent.removeCount(this, temp);
        }
    }


    @Override
    public void addCount(int count) {
        if (mType == BadgeObserver.FLAG_TEXT) {
            mCount += count;
            BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
            if (observer != null) {
                observer.setCount(mCount);
            }
            // 通知上级应用角标数有更新
            if (mParent != null) {
                mParent.addCount(this, count);
            }
        } else {
            if (mCount == 0) { // 从不可见到可见
                mCount += count;
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                }
            } else {
                mCount += count;
            }

            // 通知上级应用角标数有更新
            if (mParent != null) {
                mParent.addCount(this, count);
            }

        }
    }
}
