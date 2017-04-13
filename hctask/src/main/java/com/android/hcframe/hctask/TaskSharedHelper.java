package com.android.hcframe.hctask;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.android.hcframe.HcUtil;
import com.android.hcframe.hctask.state.CancelledState;
import com.android.hcframe.hctask.state.EndState;
import com.android.hcframe.hctask.state.TaskState;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pc on 2016/8/17.
 */
public class TaskSharedHelper {
    private final static String TASK = "Task";

    public static void setHistoryList(Context context, List<TaskState> mAllTask) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject tmpObj = null;
        int count = mAllTask.size();
        for (int i = 0; i < count; i++) {
            try {
                tmpObj = new JSONObject();
                tmpObj.put("status",mAllTask.get(i).getStatus());
                tmpObj.put("taskId",mAllTask.get(i).getId());
                tmpObj.put("createUserId",mAllTask.get(i).getPublisherId());
                tmpObj.put("createUserName",mAllTask.get(i).getPublisher());
                tmpObj.put("createUserImg",mAllTask.get(i).getPublisherUrl());
                tmpObj.put("taskContent",mAllTask.get(i).getDescription());
                tmpObj.put("executeUserId",mAllTask.get(i).getExecutorId());
                tmpObj.put("executeUserName",mAllTask.get(i).getExecutor());
                tmpObj.put("executeUserImg",mAllTask.get(i).getExecutorUrl());
                tmpObj.put("deadline",mAllTask.get(i).getEndDate());
                jsonArray.put(tmpObj);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        String task = jsonArray.toString(); // 将JSONArray转换得到String
        SharedPreferences sp = context.getSharedPreferences(TASK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("task", task);
        editor.commit();
    }
    public static List<TaskState> getHistoryList(Context context) {
        List<TaskState> mAllTask = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(TASK, Context.MODE_PRIVATE);
        String task = sp.getString("task", "");
        if (!TextUtils.isEmpty(task)) {
            try {
                JSONArray array = new JSONArray(task);
                int size = array.length();
                for (int i = 0; i < size; i++) {
                    JSONObject jsonEndObj = array.getJSONObject(i);
                    String mStatus = "";
                    TaskState mTaskState = null;
                    String mTaskId = "", mCreateUserId = "", mCreateUserName = "", mCreateUserImg = "", mTaskContent = "", mExecuteUserId = "", mExecuteUserName = "", mExecuteUserImg = "", mDeadline = "";
                    if (HcUtil.hasValue(jsonEndObj, "status")) {
                        mStatus = jsonEndObj.getString("status");
                    }
                    if (mStatus.equals("3")) {
                        mTaskState = new EndState();
                    } else if (mStatus.equals("4")) {
                        mTaskState = new CancelledState();
                    }
                    if (HcUtil.hasValue(jsonEndObj, "taskId")) {
                        mTaskId = jsonEndObj.getString("taskId");
                        mTaskState.setId(mTaskId);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "createUserId")) {
                        mCreateUserId = jsonEndObj.getString("createUserId");
                        mTaskState.setPublisherId(mCreateUserId);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "createUserName")) {
                        mCreateUserName = jsonEndObj.getString("createUserName");
                        mTaskState.setPublisher(mCreateUserName);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "createUserImg")) {
                        mCreateUserImg = jsonEndObj.getString("createUserImg");
                        mTaskState.setPublisherUrl(mCreateUserImg);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "taskContent")) {
                        mTaskContent = jsonEndObj.getString("taskContent");
                        mTaskState.setDescription(mTaskContent);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "executeUserId")) {
                        mExecuteUserId = jsonEndObj.getString("executeUserId");
                        mTaskState.setExecutorId(mExecuteUserId);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "executeUserName")) {
                        mExecuteUserName = jsonEndObj.getString("executeUserName");
                        mTaskState.setExecutor(mExecuteUserName);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "executeUserImg")) {
                        mExecuteUserImg = jsonEndObj.getString("executeUserImg");
                        mTaskState.setExecutorUrl(mExecuteUserImg);
                    }
                    if (HcUtil.hasValue(jsonEndObj, "deadline")) {
                        mDeadline = jsonEndObj.getString("deadline");
                        mTaskState.setEndDate(mDeadline);
                    }

                    mAllTask.add(mTaskState);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return mAllTask;
    }
    public static void cleanHistoryList(Context context) {
        SharedPreferences sp = context.getSharedPreferences(TASK, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("task", "");
        editor.commit();
    }
}
