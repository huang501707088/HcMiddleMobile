package com.android.hcframe.hctask;

import com.android.hcframe.hctask.state.TaskState;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-18 14:42.
 */
public interface TaskOperator {

    /**
     * 1.发布者提醒执行者接收任务
     * <p>2.执行者提醒发布者检查任务</p>
     *
     */
    void sendRemindMsg(TaskState task);

    /**
     * 发布者变更任务
     * <p>对待接收的任务和进行中的任务可以进行操作</p>
     */
    void changeTask(TaskState task);

    /**
     * 执行者接收任务
     * @param task
     */
    void receiveTask(TaskState task);

    /**
     * 发布者退回整改
     * <p>对已完成的任务可以进行整改操作</p>
     * @param task
     */
    void rectificationTask(TaskState task);

    /**
     * 发布者结束任务
     * <p>对已完成的任务可以进行结束操作</p>
     * @param task
     */
    void endTask(TaskState task);

    /**
     * 执行者提交任务
     * @param task
     */
    void commitTask(TaskState task);
}
