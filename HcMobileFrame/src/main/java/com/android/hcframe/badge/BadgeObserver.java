package com.android.hcframe.badge;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-4-20 16:15.
 */
public interface BadgeObserver {

    /** 角标显示数据 */
    public static final int FLAG_TEXT = 1;
    /** 角标显示圆点 */
    public static final int FLAG_NONE = 2;

    /**
     * 设置角标的类型
     * @author jrjin
     * @time 2016-4-20 下午3:36:23
     * @param flag
     */
    public void setFlag(int flag);

    /**
     * 设置角标的数量
     * @author jrjin
     * @time 2016-4-20 下午3:36:23
     * @param count
     */
    public void setCount(int count);
}
