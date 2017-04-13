package com.android.hcframe;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-3-4 14:16.
 */
public class CacheManager {

    private static final String TAG = "CacheManager";

    private static CacheManager MANAGER = new CacheManager();

    private List<TemporaryCache> mCaches = new ArrayList<TemporaryCache>();

    public static CacheManager getInstance() {
        return MANAGER;
    }

    public void addCache(TemporaryCache cache) {
        if (mCaches.contains(cache)) return;
        mCaches.add(cache);
    }

    /**
     *
     * @param exit 是否退出应用
     */
    public void clearCaches(boolean exit) {
        for(TemporaryCache cache : mCaches) {
            cache.clearCache(exit);
        }
        if(exit) {
            mCaches.clear();
        }
    }
}
