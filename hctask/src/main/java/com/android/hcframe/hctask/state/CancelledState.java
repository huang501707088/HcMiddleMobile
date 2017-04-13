package com.android.hcframe.hctask.state;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-27 09:04.
 */

import android.content.Context;
import android.os.Parcel;
import android.view.View;
import android.widget.TextView;

import com.android.hcframe.hctask.R;
import com.android.hcframe.hctask.TaskOperator;

/**
 * 已结束的任务
 */
public class CancelledState extends TaskState {

    public CancelledState() {
        mStateIconResId = R.drawable.task_cancelled_icon;
        mStatus = STATUS_CANCELLED;
        mDividerColor = "#d9d9d9";
        mSort = 5;
    }

    public CancelledState(Parcel p) {
        super(p);
    }

    public CancelledState(TaskState task) {
        super(task);
        mStateIconResId = R.drawable.task_cancelled_icon;
        mStatus = STATUS_CANCELLED;
        mDividerColor = "#d9d9d9";
        mSort = 5;
    }

    @Override
    public void setTextView(Context context, TaskOperator operator, TextView... views) {
        for (TextView view : views) {
        	if (view.getVisibility() != View.GONE)
                view.setVisibility(View.GONE);
        }
    }

    public static final Creator<CancelledState> CREATOR = new Creator<CancelledState>() {
        @Override
        public CancelledState createFromParcel(Parcel source) {
            return new CancelledState(source);
        }

        @Override
        public CancelledState[] newArray(int size) {
            return new CancelledState[0];
        }
    };
}
