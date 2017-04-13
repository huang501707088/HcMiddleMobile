package com.android.hcframe.hctask.state;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-7-27 09:03.
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
public class EndState extends TaskState {

    public EndState() {
        mStateIconResId = R.drawable.task_end_icon;
        mStatus = STATUS_END;
        mDividerColor = "#7d8e98";
        mSort = 4;
    }

    public EndState(Parcel p) {
        super(p);
    }

    public EndState(TaskState task) {
        super(task);
        mStateIconResId = R.drawable.task_end_icon;
        mStatus = STATUS_END;
        mDividerColor = "#7d8e98";
        mSort = 4;
    }

    @Override
    public void setTextView(Context context, TaskOperator operator, TextView... views) {
        for (TextView view : views) {
            if (view.getVisibility() != View.GONE)
                view.setVisibility(View.GONE);
        }
    }

    public static final Creator<EndState> CREATOR = new Creator<EndState>() {
        @Override
        public EndState createFromParcel(Parcel source) {
            return new EndState(source);
        }

        @Override
        public EndState[] newArray(int size) {
            return new EndState[0];
        }
    };
}
