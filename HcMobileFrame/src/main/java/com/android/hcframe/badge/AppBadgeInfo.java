package com.android.hcframe.badge;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-4-19 14:39.
 */
public class AppBadgeInfo extends BadgeInfo {

    private static final String TAG = "AppBadgeInfo";

    private List<BadgeInfo> mBadges = new ArrayList<BadgeInfo>();

    @Override
    public void addBadge(BadgeInfo info, boolean update) {
        if (mBadges.contains(info)) return;
        info.setParentBadge(this);
        mBadges.add(info);
        if (!update) return;
        // 更新角标类型和角标数量
        if (mType == BadgeObserver.FLAG_TEXT) {
            // 数字型角标不需要再更新角标类型了
            if (info.getCount() > 0 && info.getType() == BadgeObserver.FLAG_TEXT) {
                mCount += info.getCount();
                // 通知更新上级模块及界面
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                }

                if (mParent != null) {
                    // 通知上级模块重新统计数据
                    mParent.addCount(info, info.getCount());
                }
            }
        } else {
            // 先看类型
            if (info.getType() == BadgeObserver.FLAG_TEXT) {
                mType = BadgeObserver.FLAG_TEXT;
                mCount += info.getCount();
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                    observer.setFlag(mType);
                }

                if (mParent != null) {
                    mParent.resetType(this); // 数量的变更不用通知上级应用,在resetType里面已经处理了
                }

            } else {
                // 角标类型不需要变更
                if (info.getCount() > 0) {
                    if (mCount == 0)  {
                        mCount = 1;
                        BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                        if (observer != null) {
                            observer.setCount(mCount);
                        }

                        if (mParent != null) {
                            mParent.addCount(this, info.getCount());
                        }
                    }
                }
            }

        }

    }


    @Override
    public void updateCount(int count) {
        if (!mBadges.isEmpty()) {
            throw new UnsupportedOperationException(TAG + "#updateCount count = "+count + " badge size = "+mBadges.size());
        }
        super.updateCount(count);
    }

    @Override
    public void onClick(BadgeObserver observer) {
        if (mType == BadgeObserver.FLAG_TEXT) {
            if (mBadges.isEmpty()) {
                int temp = mCount;
                mCount = 0;
                if (observer != null) {
                    observer.setCount(mCount);
                }
                if (mParent != null) {
                    mParent.removeCount(this, temp);
                }
            }
        } else {
            if (mBadges.isEmpty()) {
                if (mCount != 0) {
                    int temp = mCount;
                    mCount = 0;
                    if (observer != null) {
                        observer.setCount(mCount);
                    }
                    if (mParent != null) {
                        mParent.removeCount(this, temp);
                    }
                }
            } else { // 数量在0与1之间切换
                if (mCount != 0) {
                    int temp = mCount;
                    mCount = 0;
                    if (observer != null) {
                        observer.setCount(mCount);
                    }
                    if (mParent != null) {
                        mParent.removeCount(this, temp);
                    }
                }
            }
        }
    }

    @Override
    public void updateType(int type) {
        if (!mBadges.isEmpty())
            throw new UnsupportedOperationException(TAG + "#updateType type = "+type);
        super.updateType(type);
    }

    @Override
    public void resetType(BadgeInfo info) {
        if (mType == BadgeObserver.FLAG_TEXT) {
            if (info.getType() == BadgeObserver.FLAG_TEXT) {
                mCount += info.getCount();
                // 类型不变
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                }
                // 通知上级应用更新角标数量,相当于通知上级角标增加角标数
                if (mParent != null) {
                    mParent.addCount(this, info.getCount());
                }
            } else {
                // 统计角标的类型
                int type = BadgeObserver.FLAG_NONE;
                for (BadgeInfo badgeInfo : mBadges) {
                	if (badgeInfo.getType() == BadgeObserver.FLAG_TEXT) {
                        type = BadgeObserver.FLAG_TEXT;
                        break;
                    }
                }
                if (type == BadgeObserver.FLAG_TEXT) { // 说明类型没有变化
                    // 角标数减少
                    mCount -= info.getCount();
                    // 更新界面
                    BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                    if (observer != null) {
                        observer.setCount(mCount);
                    }
                    // 通知上级应用角标书减少
                    if (mParent != null) {
                        mParent.removeCount(this, info.getCount());
                    }
                } else { // 角标类型变化了, 先个更新上级角标数,在更新角标类型

                    // 角标数减少
                    mCount -= info.getCount(); // 这里应该为0,不然就说明出错了.
                    // 更新界面
                    BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                    if (observer != null) {
                        observer.setCount(mCount);
                    }
                    // 通知上级应用角标数减少了,角标类型变化了
                    // ??????????
                    if (mParent != null) {
                        mParent.removeCount(this, info.getCount());
                    }

                    mType = type;

                    // 更新界面
                    if (observer != null) {
                        observer.setFlag(mType);
                    }
                    // 通知上级应用角标数减少了,角标类型变化了
                    if (mParent != null) {
                        mParent.resetType(this);
                    }
                }
            }

        } else {
            if (info.getType() == BadgeObserver.FLAG_TEXT) {
                mType = BadgeObserver.FLAG_TEXT;
                mCount = info.getCount();
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                    observer.setFlag(mType);
                }

                if (mParent != null) {
                    mParent.resetType(info);
                }
            }
        }
    }

    @Override
    public void addCount(int count) {
        if (mBadges.isEmpty()) {
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
        } else {
            throw new UnsupportedOperationException(TAG + " #addCount count = "+count + " badge size = "+mBadges.size());
        }
    }

    @Override
    public void addCount(BadgeInfo info, int count) {
        if (mType == BadgeObserver.FLAG_TEXT) {
            if (info.getType() == BadgeObserver.FLAG_TEXT) {
                mCount += count;
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                }
                if (mParent != null) {
                    mParent.addCount(this, count);
                }
            }
            // 模块为圆点型角标不需要处理
        } else {
            if (info.getType() == BadgeObserver.FLAG_NONE) {
                if (mCount == 0) {
                    mCount = 1;
                    BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                    if (observer != null) {
                        observer.setCount(mCount);
                    }
                    if (mParent != null) {
                        mParent.addCount(this, count);
                    }
                }
                // mCount != 0 不需要处理,因为是圆点型角标
            } else { // 不肯能出现的
                throw new UnsupportedOperationException(TAG + " it is in addCount  BadgeInfo = "+info + " info type = "+info.getType() + " count = "+count);
            }
        }
    }

    @Override
    public void removeCount(BadgeInfo info, int count) {
        if (mType == BadgeObserver.FLAG_TEXT) {
            if (info.getType() == BadgeObserver.FLAG_TEXT) {
                mCount -= count;
                BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                if (observer != null) {
                    observer.setCount(mCount);
                }
                if (mParent != null) {
                    mParent.removeCount(this, count);
                }
            } // 是圆点型不需要考虑
        } else {
            if (info.getType() == BadgeObserver.FLAG_TEXT) {
                throw new UnsupportedOperationException(TAG + " it is in removeCount  BadgeInfo = "+info + " info type = "+info.getType() + " count = "+count);
            } else {
                if (mCount != 0) {
                    mCount = 0;
                    BadgeObserver observer = BadgeCache.getInstance().getBadgeObserver(mAppId + "_" + mModuleId);
                    if (observer != null) {
                        observer.setCount(mCount);
                    }
                    if (mParent != null) {
                        mParent.removeCount(this, count);
                    }
                }
            }
        }
    }

    @Override
    public void removeCount(int count) {
        if (!mBadges.isEmpty()) {
            throw new UnsupportedOperationException(TAG + "#removeCount count = "+count + " badge size = "+mBadges.size());
        }
        super.removeCount(count);
    }

    @Override
    public List<BadgeInfo> getBadges() {
        return mBadges;
    }

    @Override
    public Iterator<BadgeInfo> iterator() {
        return new CompositeIterator(mBadges.iterator());
    }

    @Override
    public void clearBadge() {
        mBadges.clear();
    }

    @Override
    public boolean isEmpty() {
        return mBadges.isEmpty();
    }
}
