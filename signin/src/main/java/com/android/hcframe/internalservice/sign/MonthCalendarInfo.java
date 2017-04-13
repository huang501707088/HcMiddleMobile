package com.android.hcframe.internalservice.sign;

import com.android.hcframe.internalservice.signcls.SignListByMonth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zhujiabin on 2017/3/8.
 */

public class MonthCalendarInfo {
    private Map<String, List<SignListByMonth>> mInfos = new HashMap<String, List<SignListByMonth>>();

    public void addInfo(String key, SignListByMonth info) {
        List<SignListByMonth> infos = mInfos.get(key);
        if (infos == null) {
            infos = new ArrayList<SignListByMonth>();
            mInfos.put(key, infos);
        }
        infos.add(info);
    }


    public Map<String, List<SignListByMonth>> getMonthCalendarInfos() {
        return mInfos;
    }

    public boolean isEmpty() {
        return mInfos.isEmpty();
    }

    public void clear() {
        mInfos.clear();
    }
}
