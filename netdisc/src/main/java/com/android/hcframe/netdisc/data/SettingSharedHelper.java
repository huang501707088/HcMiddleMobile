package com.android.hcframe.netdisc.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.android.hcframe.HcLog;
import com.android.hcframe.HcUtil;
import com.android.hcframe.netdisc.R;
import com.android.hcframe.netdisc.netdisccls.MySkydriveFoldItem;
import com.android.hcframe.netdisc.netdisccls.MySkydriveInfoItem;
import com.android.hcframe.sql.SettingHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pc on 2016/7/15.
 */
public class SettingSharedHelper {

    private final static String NETDISC = "Netdisc";

    private final static String KEY_CLOUD_LIST = "cloud_list";

    /**
     * 判断是否第一次进入该页面
     */
    // 存储sharedpreferences
    public static void setIsFirst(Context context, boolean first) {
        SharedPreferences mIsFirst = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mIsFirst.edit();
        editor.putBoolean("first", first);
        editor.commit();// 提交修改
    }

    // 获得sharedpreferences的数据
    public static Boolean getIsFirst(Context context) {
        SharedPreferences mIsFirst = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        Boolean isFirst = mIsFirst.getBoolean("first", true);//默认返回true
        return isFirst;
    }

    /**
     * 缓存wifi开关按钮的状态
     */
    // 存储sharedpreferences
    public static void setSharedPreference(Context context, boolean switchFlag) {
        SharedPreferences mSwitchShared = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSwitchShared.edit();
        editor.putBoolean("switchFlag", switchFlag);
        editor.commit();// 提交修改
    }

    /**
     * 获取wifi开关按钮的状态
     *
     * @param context
     * @return
     */
    // 获得sharedpreferences的数据
    public static Boolean getSharePreference(Context context) {
        SharedPreferences mSwitchShared = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        Boolean switchFlag = mSwitchShared.getBoolean("switchFlag", true);//默认返回true
        return switchFlag;
    }

    public static void saveNewFileInfo(Context context, String key, Object data) throws JSONException {
        JSONObject object = new JSONObject((String) data);
        String mUpId = null;
        if (HcUtil.hasValue(object, "up_id")) {
            mUpId = object.getString("up_id");
        }
        JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("new_file_list", jsonBodyArray.toString());
        editor.putString("up_id", mUpId);
        editor.commit();
    }

    public static List<MySkydriveInfoItem> getNewFileInfo(Context context, String key) throws JSONException {
        List<MySkydriveInfoItem> mMySkydriveList = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        String mUpId = sp.getString("up_id", "");
        String mNewFileList = sp.getString("new_file_list", "");
        JSONArray jsonBodyArray = new JSONArray(mNewFileList);
        for (int i = 0; i < jsonBodyArray.length(); i++) {
            MySkydriveInfoItem mMySkydriveItem = new MySkydriveInfoItem();
            JSONObject mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
            String mExt = null;
            if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                String mInfoid = mySkydriveFileObj.getString("infoid");
                mMySkydriveItem.setNetdiscId(mInfoid);
            }
            if (HcUtil.hasValue(mySkydriveFileObj, "infoname") && HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                String mInfoname = mySkydriveFileObj.getString("infoname");
                mExt = mySkydriveFileObj.getString("ext");
                mMySkydriveItem.setNetdiscListText(mInfoname);
            }
            if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                String mCreatetime = mySkydriveFileObj.getString("createtime");
                mMySkydriveItem.setNetdiscListDate(mCreatetime);
            }
            if (HcUtil.hasValue(mySkydriveFileObj, "type")) {
                String mType = mySkydriveFileObj.getString("type");
                mMySkydriveItem.setNetdiscListType(mType);
            }
            if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                String mLvl = mySkydriveFileObj.getString("lvl");
                mMySkydriveItem.setNetdiscDirLvl(mLvl);
            }
            if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                String mFilesize = mySkydriveFileObj.getString("filesize");
                mMySkydriveItem.setNetdiscListFileSize(mFilesize);
            }
            if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                String mFilesize = mySkydriveFileObj.getString("filesize");
                mMySkydriveItem.setNetdiscListFileSize(mFilesize);
            }
            if (!"".equals(mUpId)) {
                mMySkydriveItem.setNetdiscUpdirId(mUpId);
            }

            mMySkydriveList.add(mMySkydriveItem);

        }
        return mMySkydriveList;
    }

    /**
     * 存储网盘的第一页
     *
     * @param context
     * @param data
     */
    public static void saveCloudListInfo(Context context, String data) {
        SharedPreferences sp = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_CLOUD_LIST, data);
        editor.commit();
    }

    /**
     * 获取网盘缓存的第一页
     *
     * @param context
     * @return
     */
    public static List<MySkydriveInfoItem> getCloudListInfo(Context context, MySkydriveInfoItem item) {
        List<MySkydriveInfoItem> mMySkydriveList = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        String jsonData = sp.getString(KEY_CLOUD_LIST, "");
        HcLog.D("SettingSharedHelper #getCloudListInfo jsonData = " + jsonData);
        if (!TextUtils.isEmpty(jsonData)) {
            String mUpId = "";

            try {
                JSONObject object = new JSONObject(jsonData);
                if (HcUtil.hasValue(object, "up_id")) {
                    mUpId = object.getString("up_id");
                    item.setNetdiscId(mUpId);
                }
                JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
                int size = jsonBodyArray.length();
                HcLog.D("SettingSharedHelper #getCloudListInfo size = " + size);
                MySkydriveInfoItem mMySkydriveItem;
                JSONObject mySkydriveFileObj;
                for (int i = 0; i < size; i++) {
                    String mExt = null;
                    mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
                    if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                        mExt = mySkydriveFileObj.getString("ext");
                    }
                    if ("1".equals(mExt)) {
                        mMySkydriveItem = new MySkydriveFoldItem();
                    } else {
                        mMySkydriveItem = new MySkydriveInfoItem();
                    }
                    mMySkydriveItem.setNetdiscListType(mExt);
                    mMySkydriveItem.setNetdiscUpdirId(mUpId);

                    if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                        String mInfoid = mySkydriveFileObj.getString("infoid");
                        mMySkydriveItem.setNetdiscId(mInfoid);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                        String mInfoname = mySkydriveFileObj.getString("infoname");
                        mMySkydriveItem.setNetdiscListText(mInfoname);
                    }

                    if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                        String mCreatetime = mySkydriveFileObj.getString("createtime");
                        mMySkydriveItem.setNetdiscListDate(mCreatetime);
                    }

                    if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                        String mLvl = mySkydriveFileObj.getString("lvl");
                        mMySkydriveItem.setNetdiscDirLvl(mLvl);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                        String mFileSize = mySkydriveFileObj.getString("filesize");
                        mMySkydriveItem.setNetdiscListFileSize(mFileSize);
                    }

                    mMySkydriveList.add(mMySkydriveItem);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                HcLog.D("SettingSharedHelper #getCloudListInfo JSONException e = " + e);
            }
        }

        return mMySkydriveList;
    }

    /**
     * R或N，判断是否是首页，用以获得缓存的数据
     */
    // 存储sharedpreferences
    public static void setTypeRorN(Context context, String type) {
        SharedPreferences mTypeShared = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mTypeShared.edit();
        editor.putString("type", type);
        editor.commit();// 提交修改
    }

    // 获得sharedpreferences的数据
    public static String getTypeRorN(Context context) {
        SharedPreferences mTypeShared = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        String mType = mTypeShared.getString("type", "");//默认返回""
        return mType;
    }

    public static void setMySkydriveList(Context context, String key, ArrayList<MySkydriveInfoItem> mMySkydriveList) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject tmpObj = null;
        int count = mMySkydriveList.size();
        for (int i = 0; i < count; i++) {
            try {
                tmpObj = new JSONObject();
                tmpObj.put("infoid", mMySkydriveList.get(i).getNetdiscId());
                tmpObj.put("infoname", mMySkydriveList.get(i).getNetdiscListText());
                String fileSuffix = mMySkydriveList.get(i).getNetdiscListText().substring(mMySkydriveList.get(i).getNetdiscListText().lastIndexOf(".") + 1);

                if (fileSuffix.matches("(dir|exe)")) {
                    tmpObj.put("ext", "1");
                } else if (fileSuffix.matches("(doc|docx)")) {
                    tmpObj.put("ext", "2");
                } else if (fileSuffix.matches("(xls)")) {
                    tmpObj.put("ext", "3");
                } else if (fileSuffix.matches("(ppt)")) {
                    tmpObj.put("ext", "4");
                } else if (fileSuffix.matches("(jpg|jpeg|png|gif|bmp)")) {
                    tmpObj.put("ext", "5");
                } else if (fileSuffix.matches("(rar|zip|tar|jar|iso)")) {
                    tmpObj.put("ext", "6");
                } else if (fileSuffix.matches("(acm|aif|aifc|aiff|ans|asf|aifc|avi|asp|ram|mov)")) {
                    tmpObj.put("ext", "7");
                } else {
                    tmpObj.put("ext", "0");
                }
                tmpObj.put("lvl", mMySkydriveList.get(i).getNetdiscDirLvl());
                tmpObj.put("createtime", mMySkydriveList.get(i).getNetdiscListDate());
                tmpObj.put("type", mMySkydriveList.get(i).getNetdiscListType());
                tmpObj.put("filesize", mMySkydriveList.get(i).getNetdiscListFileSize());
                jsonArray.put(tmpObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tmpObj = null;
        }
        String dirfiles = jsonArray.toString(); // 将JSONArray转换得到String
        try {
            jsonObject.put("dirfiles", dirfiles);   // 获得JSONObject的String
        } catch (JSONException e) {
            e.printStackTrace();
        }
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("dirfiles", dirfiles);
        editor.putString("up_id", mMySkydriveList.get(0).getNetdiscUpdirId());
        editor.commit();
    }

    public static List<MySkydriveInfoItem> getMySkydriveList(Context context, String key) {
        List<MySkydriveInfoItem> mMySkydriveList = new ArrayList<>();
        SharedPreferences sp = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        String dirList = sp.getString("dirfiles", "");
        if (!TextUtils.isEmpty(dirList)) {
            try {
                JSONArray array = new JSONArray(dirList);
                int size = array.length();
                MySkydriveInfoItem mMySkydriveItem;
                JSONObject mySkydriveFileObj;
                for (int i = 0; i < size; i++) {
                    mMySkydriveItem = new MySkydriveInfoItem();
                    mySkydriveFileObj = array.getJSONObject(i);
                    String mExt = null;
                    if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                        String mInfoid = mySkydriveFileObj.getString("infoid");
                        mMySkydriveItem.setNetdiscId(mInfoid);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                        String mInfoname = mySkydriveFileObj.getString("infoname");
                        mMySkydriveItem.setNetdiscListText(mInfoname);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                        mExt = mySkydriveFileObj.getString("ext");
                        mMySkydriveItem.setNetdiscListType(mExt);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                        String mCreatetime = mySkydriveFileObj.getString("createtime");
                        mMySkydriveItem.setNetdiscListDate(mCreatetime);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "type")) {
                        String mType = mySkydriveFileObj.getString("type");
                        mMySkydriveItem.setNetdiscListType(mType);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                        String mLvl = mySkydriveFileObj.getString("lvl");
                        mMySkydriveItem.setNetdiscDirLvl(mLvl);
                    }
                    if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                        String mFilesize = mySkydriveFileObj.getString("filesize");
                        mMySkydriveItem.setNetdiscListFileSize(mFilesize);
                    }
                    if (!"".equals(sp.getString("up_id", ""))) {
                        mMySkydriveItem.setNetdiscUpdirId(sp.getString("up_id", ""));
                    }

                    mMySkydriveList.add(mMySkydriveItem);
                }
            } catch (JSONException e) {
                HcLog.D("SettingSharedHelper #getMySkydriveList e = " + e + " dirList = " + dirList);
            }
        }

        return mMySkydriveList;
    }

    public static String savemMySkydriveList(ArrayList<MySkydriveInfoItem> mMySkydriveList) {
        JSONArray jsonArray = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        JSONObject tmpObj = null;
        int count = mMySkydriveList.size();
        for (int i = 0; i < count; i++) {
            try {
                tmpObj = new JSONObject();
                tmpObj.put("infoid", mMySkydriveList.get(i).getNetdiscId());
                tmpObj.put("infoname", mMySkydriveList.get(i).getNetdiscListText());
                String fileSuffix = mMySkydriveList.get(i).getNetdiscListText().substring(mMySkydriveList.get(i).getNetdiscListText().lastIndexOf(".") + 1);

                if (fileSuffix.matches("(dir|exe)")) {
                    tmpObj.put("ext", "1");
                } else if (fileSuffix.matches("(doc|docx)")) {
                    tmpObj.put("ext", "2");
                } else if (fileSuffix.matches("(xls)")) {
                    tmpObj.put("ext", "3");
                } else if (fileSuffix.matches("(ppt)")) {
                    tmpObj.put("ext", "4");
                } else if (fileSuffix.matches("(jpg|jpeg|png|gif|bmp)")) {
                    tmpObj.put("ext", "5");
                } else if (fileSuffix.matches("(rar|zip|tar|jar|iso)")) {
                    tmpObj.put("ext", "6");
                } else if (fileSuffix.matches("(acm|aif|aifc|aiff|ans|asf|aifc|avi|asp|ram|mov)")) {
                    tmpObj.put("ext", "7");
                } else {
                    tmpObj.put("ext", "0");
                }
                tmpObj.put("lvl", mMySkydriveList.get(i).getNetdiscDirLvl());
                tmpObj.put("createtime", mMySkydriveList.get(i).getNetdiscListDate());
                tmpObj.put("type", mMySkydriveList.get(i).getNetdiscListType());
                tmpObj.put("filesize", mMySkydriveList.get(i).getNetdiscListFileSize());
                tmpObj.put("up_id", mMySkydriveList.get(i).getNetdiscUpdirId());
                jsonArray.put(tmpObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            tmpObj = null;
        }
        String dirfiles = jsonArray.toString(); // 将JSONArray转换得到String
        return dirfiles;
    }

    public static List<MySkydriveInfoItem> getmMySkydriveList(Context context, String dirfiles) {
        List<MySkydriveInfoItem> mMySkydriveList = new ArrayList<>();
        JSONArray jsonBodyArray = null;
        try {
            jsonBodyArray = new JSONArray(dirfiles);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < jsonBodyArray.length(); i++) {
            MySkydriveInfoItem mMySkydriveItem = new MySkydriveInfoItem();
            try {
                JSONObject mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
                String mExt = null;
                if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                    String mInfoid = mySkydriveFileObj.getString("infoid");
                    mMySkydriveItem.setNetdiscId(mInfoid);
                }
                if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                    String mInfoname = mySkydriveFileObj.getString("infoname");
                    mMySkydriveItem.setNetdiscListText(mInfoname);
                }
                if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                    mExt = mySkydriveFileObj.getString("ext");
                    mMySkydriveItem.setNetdiscListType(mExt);
                }
                if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                    String mCreatetime = mySkydriveFileObj.getString("createtime");
                    mMySkydriveItem.setNetdiscListDate(mCreatetime);
                }
                if (HcUtil.hasValue(mySkydriveFileObj, "type")) {
                    String mType = mySkydriveFileObj.getString("type");
                    mMySkydriveItem.setNetdiscListType(mType);
                }
                if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                    String mLvl = mySkydriveFileObj.getString("lvl");
                    mMySkydriveItem.setNetdiscDirLvl(mLvl);
                }
                if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                    String mFilesize = mySkydriveFileObj.getString("filesize");
                    mMySkydriveItem.setNetdiscListFileSize(mFilesize);
                }

                if (HcUtil.hasValue(mySkydriveFileObj, "up_id")) {
                    String mUpId = mySkydriveFileObj.getString("up_id");
                    mMySkydriveItem.setNetdiscListFileSize(mUpId);
                }

                mMySkydriveList.add(mMySkydriveItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        return mMySkydriveList;
    }

    /**
     * 存储网盘的第一页
     *
     * @param context
     * @param data
     */
    public static void saveUserIdCloudListInfo(Context context, String data) {
        if (TextUtils.isEmpty(data))
            return;
        String datas = getUserIdCloudListInfo(context);
        String mUseId = SettingHelper.getUserId(context);
        if (TextUtils.isEmpty(datas)) {
            datas = mUseId + "&" + data;
        } else {
            StringBuilder builder = new StringBuilder();
            String[] modules = datas.split(";");
            boolean added = false;
            String[] useId = null;
            for (String module : modules) {
                useId = module.split("&");
                if (useId[0].equals(mUseId)) {
                    builder.append(mUseId + "&" + data + ";");
                    added = true;
                } else {
                    // 直接添加
                    builder.append(module + ";");
                }
            }
            if (!added) {
                builder.append(mUseId + "&" + data + ";");
            }
            // 去除最后一个";"
            datas = builder.toString();
            datas = datas.substring(0, datas.length() - 1);
        }
        SharedPreferences sp = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(KEY_CLOUD_LIST, datas);
        editor.commit();
    }

    public static String getUserIdCloudListInfo(Context context) {
        SharedPreferences sp = context.getSharedPreferences(NETDISC, Context.MODE_PRIVATE);
        String useIdJsonData = sp.getString(KEY_CLOUD_LIST, "");
        HcLog.D("SettingSharedHelper #getCloudListInfo jsonData = " + useIdJsonData);
        return useIdJsonData;
    }

    public static List<MySkydriveInfoItem> getCloudListForUserIdInfo(Context context, MySkydriveInfoItem item) {
        List<MySkydriveInfoItem> mMySkydriveList = null;
        String datas = getUserIdCloudListInfo(context);
        String mUseId = SettingHelper.getUserId(context);
        if (!TextUtils.isEmpty(datas)) {
            String[] modules = datas.split(";");
            String[] useId = null;
            for (String module : modules) {
                useId = module.split("&");
                if (mUseId.equals(useId[0])) {
                    //解析json字符串
                    mMySkydriveList = new ArrayList<>();
                    String jsonData = useId[1];
                    if (!TextUtils.isEmpty(jsonData)) {
                        String mUpId = "";
                        try {
                            JSONObject object = new JSONObject(jsonData);
                            if (HcUtil.hasValue(object, "up_id")) {
                                mUpId = object.getString("up_id");
                                item.setNetdiscId(mUpId);
                            }
                            //共享文件数量
                            String groupShareCount = object.optString("groupShareCount");
                            item.setNetdiscListSharedSize(groupShareCount + "个");
                            JSONArray jsonBodyArray = object.getJSONArray("dirfiles");
                            int size = jsonBodyArray.length();
                            HcLog.D("SettingSharedHelper #getCloudListInfo size = " + size);
                            MySkydriveInfoItem mMySkydriveItem;
                            JSONObject mySkydriveFileObj;
                            for (int i = 0; i < size; i++) {
                                String mExt = null;
                                mySkydriveFileObj = jsonBodyArray.getJSONObject(i);
                                if (HcUtil.hasValue(mySkydriveFileObj, "ext")) {
                                    mExt = mySkydriveFileObj.getString("ext");
                                }
                                if ("1".equals(mExt)) {
                                    mMySkydriveItem = new MySkydriveFoldItem();
                                } else {
                                    mMySkydriveItem = new MySkydriveInfoItem();
                                }
                                mMySkydriveItem.setNetdiscListType(mExt);
                                mMySkydriveItem.setNetdiscUpdirId(mUpId);

                                if (HcUtil.hasValue(mySkydriveFileObj, "infoid")) {
                                    String mInfoid = mySkydriveFileObj.getString("infoid");
                                    mMySkydriveItem.setNetdiscId(mInfoid);
                                }
                                if (HcUtil.hasValue(mySkydriveFileObj, "infoname")) {
                                    String mInfoname = mySkydriveFileObj.getString("infoname");
                                    mMySkydriveItem.setNetdiscListText(mInfoname);
                                }


                                if (HcUtil.hasValue(mySkydriveFileObj, "createtime")) {
                                    String mCreatetime = mySkydriveFileObj.getString("createtime");
                                    mMySkydriveItem.setNetdiscListDate(mCreatetime);
                                }

                                if (HcUtil.hasValue(mySkydriveFileObj, "lvl")) {
                                    String mLvl = mySkydriveFileObj.getString("lvl");
                                    mMySkydriveItem.setNetdiscDirLvl(mLvl);
                                }
                                if (HcUtil.hasValue(mySkydriveFileObj, "filesize")) {
                                    String mFileSize = mySkydriveFileObj.getString("filesize");
                                    mMySkydriveItem.setNetdiscListFileSize(mFileSize);
                                }
                                /**
                                 * 判断是否是我的网络列表或共享空间列表
                                 * */
                                if (HcUtil.hasValue(mySkydriveFileObj, "dirType")) {
                                    String mDirType = mySkydriveFileObj.getString("dirType");
                                    mMySkydriveItem.setNetdiscListDirType(mDirType);
                                }
                                mMySkydriveList.add(mMySkydriveItem);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            HcLog.D("SettingSharedHelper #getCloudListInfo JSONException e = " + e);
                        }
                    }

                }
            }
        }
        return mMySkydriveList;
    }
}
