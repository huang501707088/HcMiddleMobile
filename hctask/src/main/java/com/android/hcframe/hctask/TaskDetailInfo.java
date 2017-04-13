package com.android.hcframe.hctask;

import com.android.hcframe.hctask.state.TaskState;

import java.util.ArrayList;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-27 14:08.
 */

/**
 * 任务详情
 */
public class TaskDetailInfo {

    private TaskState mTask;

    private List<DiscussInfo> mDiscusses = new ArrayList<DiscussInfo>();

    public TaskDetailInfo(TaskState task) {
        mTask = task;
    }

    public static class DiscussInfo {
        String mName;
        String mContent;
        String mDate;
        String mUrl;

        public String getmName() {
            return mName;
        }

        public void setmName(String mName) {
            this.mName = mName;
        }

        public String getmContent() {
            return mContent;
        }

        public void setmContent(String mContent) {
            this.mContent = mContent;
        }

        public String getmDate() {
            return mDate;
        }

        public void setmDate(String mDate) {
            this.mDate = mDate;
        }

        public String getmUrl() {
            return mUrl;
        }

        public void setmUrl(String mUrl) {
            this.mUrl = mUrl;
        }
    }

    public TaskState getTask() {
        return mTask;
    }
}
