package com.android.hcframe.hctask.state;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.TextView;

import com.android.hcframe.hctask.TaskOperator;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-13 14:05.
 */
public abstract class TaskState implements Parcelable {

    private static final String TAG = "TaskState";

    /**
     * 任务编号
     */
    private String mId;

    /**
     * 发布日期
     */
    private String mPublishDate;

    /**
     * 截止日期
     */
    private String mEndDate;

    /**
     * 发布者
     */
    private String mPublisher;

    /**
     * 执行者
     */
    private String mExecutor;

    /**
     * 任务描述
     */
    private String mDescription;

    /**
     * 发布者头像url
     */
    private String mPublisherUrl;
    /**
     * 执行者头像url
     */
    private String mExecutorUrl;

    /**
     * 状态显示的图标资源id
     */
    protected int mStateIconResId;

    /**
     * 发布者userId
     */
    private String mPublisherId;

    /**
     * 执行者userId
     */
    private String mExecutorId;

    /**
     * 0:待接收；1：进行中；2：已完成；3：已结束；4：已取消
     */
    protected int mStatus;

    /** 分割线的颜色 */
    protected String mDividerColor;

    /**
     * 任务的排序:1:已完成,2未接收,3进行中,4已结束,5已取消
     */
    protected int mSort;

    public static final int STATUS_RECEIVING = 0;
    public static final int STATUS_PROCESSING = 1;
    public static final int STATUS_COMPLETED = 2;
    public static final int STATUS_END = 3;
    public static final int STATUS_CANCELLED = 4;

    public TaskState() {

    }

    public TaskState(Parcel p) {
        mId = p.readString();
        mPublishDate = p.readString();
        mEndDate = p.readString();
        mPublisher = p.readString();
        mExecutor = p.readString();
        mDescription = p.readString();
        mPublisherUrl = p.readString();
        mExecutorUrl = p.readString();
        mStateIconResId = p.readInt();
        mPublisherId = p.readString();
        mExecutorId = p.readString();
        mStatus = p.readInt();
        mDividerColor = p.readString();
        mSort = p.readInt();
    }

    public TaskState(TaskState task) {
        mId = task.getId();
        mDescription = task.getDescription();
        mEndDate = task.getEndDate();
        mExecutor = task.getExecutor();
        mEndDate = task.getEndDate();
        mExecutorId = task.getExecutorId();
        mExecutorUrl = task.getExecutorUrl();
        mPublishDate = task.getPublishDate();
        mPublisher = task.getPublisher();
        mPublisherId = task.getPublisherId();
        mPublisherUrl = task.getPublisherUrl();
        mSort = task.getSort();
    }

    /**
     * 获取截止日期
     * @return
     */
    public final String getEndDate() {
        return mEndDate;
    }

    /**
     * 设置截止日期
     * @param endDate
     */
    public final void setEndDate(String endDate) {
        mEndDate = endDate;
    }

    /**
     * 获取任务编号
     * @return
     */
    public final String getId() {
        return mId;
    }

    /**
     * 设置任务编号
     * @param id
     */
    public final void setId(String id) {
        mId = id;
    }

    /**
     * 获取发布日期
     * @return
     */
    public final String getPublishDate() {
        return mPublishDate;
    }

    /**
     * 设置发布日期
     * @param publishDate
     */
    public final void setPublishDate(String publishDate) {
        mPublishDate = publishDate;
    }

    /**
     * 获取发布者
     * @return
     */
    public final String getPublisher() {
        return mPublisher;
    }

    /**
     * 设置发布者
     * @param publisher
     */
    public final void setPublisher(String publisher) {
        mPublisher = publisher;
    }

    /**
     * 获取执行者url
     * @return
     */
    public String getExecutorUrl() {
        return mExecutorUrl;
    }

    /**
     * 设置执行者url
     * @param executorUrl
     */
    public void setExecutorUrl(String executorUrl) {
        mExecutorUrl = executorUrl;
    }

    /**
     * 获取发布者头像url
     * @return
     */
    public String getPublisherUrl() {
        return mPublisherUrl;
    }

    /**
     * 设置发布者头像url
     * @param publisherUrl
     */
    public void setPublisherUrl(String publisherUrl) {
        mPublisherUrl = publisherUrl;
    }

    /**
     * 获取执行者
     * @return
     */
    public String getExecutor() {
        return mExecutor;
    }

    /**
     * 设置执行者
     * @param executor
     */
    public void setExecutor(String executor) {
        mExecutor = executor;
    }

    /**
     * 获取任务内容
     * @return
     */
    public String getDescription() {
        return mDescription;
    }

    /**
     * 设置任务内容
     * @param description
     */
    public void setDescription(String description) {
        mDescription = description;
    }

    public int getStateIconResID() {
        return mStateIconResId;
    }

    /**
     * 设置列表项按钮的文字、显示及点击事件
     * @param views
     */
    public void setTextView(Context context, TaskOperator operator, TextView... views) {
        throw new UnsupportedOperationException(TAG +"#setTextView views size = "+views.length);
    }

    public String getPublisherId() {
        return mPublisherId;
    }

    public void setPublisherId(String publisherId) {
        mPublisherId = publisherId;
    }

    public String getExecutorId() {
        return mExecutorId;
    }

    public void setExecutorId(String executorId) {
        mExecutorId = executorId;
    }

    public int getStatus() {
        return mStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeString(mPublishDate);
        dest.writeString(mEndDate);
        dest.writeString(mPublisher);
        dest.writeString(mExecutor);
        dest.writeString(mDescription);
        dest.writeString(mPublisherUrl);
        dest.writeString(mExecutorUrl);
        dest.writeInt(mStateIconResId);
        dest.writeString(mPublisherId);
        dest.writeString(mExecutorId);
        dest.writeInt(mStatus);
        dest.writeString(mDividerColor);
        dest.writeInt(mSort);
    }

    public static final Creator<TaskState> CREATOR = new Creator<TaskState>() {
        @Override
        public TaskState createFromParcel(Parcel source) {
            return new TaskState(source) {

            };
        }

        @Override
        public TaskState[] newArray(int size) {
            return new TaskState[0];
        }
    };

    public String getDividerColor() {
        return mDividerColor;
    }

    /**
     * 把当前任务的状态更改成task的状态
     * @param task 目的状态的task
     */
    public final void updateStatus(TaskState task) {
        mDividerColor = task.mDividerColor;
        mStatus = task.mStatus;
        mStateIconResId = task.mStateIconResId;
        mSort = task.mSort;
    }

    public int getSort() {
        return mSort;
    }
}
