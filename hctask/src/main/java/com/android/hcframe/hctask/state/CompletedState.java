package com.android.hcframe.hctask.state;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-27 09:02.
 */

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.widget.TextView;

import com.android.hcframe.hctask.R;
import com.android.hcframe.hctask.TaskOperator;
import com.android.hcframe.sql.SettingHelper;

/**
 * 已完成的任务
 */
public class CompletedState extends TaskState {

    public CompletedState() {
        mStateIconResId = R.drawable.task_completed_icon;
        mStatus = STATUS_COMPLETED;
        mDividerColor = "#63dea3";
        mSort = 1;
    }

    public CompletedState(Parcel p) {
        super(p);
    }

    public CompletedState(TaskState task) {
        super(task);
        mStateIconResId = R.drawable.task_completed_icon;
        mStatus = STATUS_COMPLETED;
        mDividerColor = "#63dea3";
        mSort = 1;
    }

    @Override
    public void setTextView(Context context, final TaskOperator operator, TextView... views) {
        if (views.length != 2) {
            throw new IndexOutOfBoundsException("CompletedState #setTextView TextView must be 2! views size = "+views.length);
        }
        if (SettingHelper.getUserId(context).equals(getPublisherId())) {
            // 说明是发布者,需要两个按钮（退回整改、结束任务）
            if (views[0].getVisibility() != View.VISIBLE)
                views[0].setVisibility(View.VISIBLE);
            if (views[1].getVisibility() != View.VISIBLE)
                views[1].setVisibility(View.VISIBLE);
            views[0].setText(context.getResources().getString(R.string.task_return_btn));
            views[1].setText(context.getResources().getString(R.string.task_end_btn));
            views[0].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.rectificationTask(CompletedState.this);
                }
            });
            views[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.endTask(CompletedState.this);
                }
            });
        } else {
            // 说明是发布者,只需要一个按钮（提醒检查）
            if (views[0].getVisibility() != View.GONE)
                views[0].setVisibility(View.GONE);
            if (views[1].getVisibility() != View.VISIBLE)
                views[1].setVisibility(View.VISIBLE);
            views[1].setText(context.getResources().getString(R.string.task_remind_btn));
            views[1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    operator.sendRemindMsg(CompletedState.this);
                }
            });
        }
    }

    public static final Creator<CompletedState> CREATOR = new Creator<CompletedState>() {
        @Override
        public CompletedState createFromParcel(Parcel source) {
            return new CompletedState(source);
        }

        @Override
        public CompletedState[] newArray(int size) {
            return new CompletedState[0];
        }
    };
}
