package com.android.hcframe.schedule.data;

import com.android.hcframe.schedule.ScheduleDetailsInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-2-24 15:03.
 */

public class ScheduleColleagueInfo {

    private Map<String, List<ScheduleDetailsInfo>> mInfos = new HashMap<String, List<ScheduleDetailsInfo>>();

    public void addInfo(String key, ScheduleDetailsInfo info) {
        List<ScheduleDetailsInfo> infos = mInfos.get(key);
        if (infos == null) {
            infos = new ArrayList<ScheduleDetailsInfo>();
            mInfos.put(key, infos);
        }
        infos.add(info);
    }

    public List<ScheduleDetailsInfo> getScheduleColleagueInfos(String key) {
        return mInfos.get(key);
    }

    public Map<String, List<ScheduleDetailsInfo>> getScheduleColleagueInfos() {
        return mInfos;
    }

    public boolean isEmpty() {
        return mInfos.isEmpty();
    }

    public void clear() {
        mInfos.clear();
    }
}
