package com.android.hcframe.command;

import android.content.Context;
import android.os.Parcelable;

import java.util.Map;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 17-2-15 14:29.
 */

public interface Command {

    public void execute(Context context);

    public void execute(Context context, Parcelable p);

    public void execute(Context context, Map<String, String> data);
}
