package com.android.hcframe.view.selector;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Iterator;
import java.util.List;

/**
 * @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
 * @URL http://www.zjhcsoft.com
 * @Address 杭州滨江区伟业路1号
 * @Email jinjr@zjhcsoft.com
 * Created by jrjin on 16-8-5 11:52.
 */
public abstract class ItemInfo implements Parcelable {

    /**
     * 列表数据的编号
     */
    private String mItemId;

    /**
     * 列表图标的url
     */
    private String mIconUrl;

    /**
     * 列表显示的值
     */
    private String mItemValue;

    /**
     * 列表图标的资源Id
     */
    private int mIconResId;

    /**
     * 列表是否被选中
     */
    protected boolean mSelected;



    public ItemInfo() {}

    protected ItemInfo(Parcel in) {
        mItemId = in.readString();
        mIconUrl = in.readString();
        mItemValue = in.readString();
        mIconResId = in.readInt();
        mSelected = in.readInt() == 1;
    }

    public static final Creator<ItemInfo> CREATOR = new Creator<ItemInfo>() {
        @Override
        public ItemInfo createFromParcel(Parcel in) {
            return new ItemInfo(in) {};
        }

        @Override
        public ItemInfo[] newArray(int size) {
            return new ItemInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mItemId);
        dest.writeString(mIconUrl);
        dest.writeString(mItemValue);
        dest.writeInt(mIconResId);
        dest.writeInt(mSelected ? 1 : 0);
    }


    /**
     * 获取列表图标的资源Id
     * @return 列表图标的资源Id
     * @see #getIconUrl()
     */
    public int getIconResId() {
//        throw new UnsupportedOperationException(getClass().getName() + "#getIconResId, please use getIconUrl()!");
        return mIconResId;
    }

    /**
     * 设置列表图标的资源Id
     * @param iconResId 图标的资源Id
     * @see #setIconUrl(String)
     */
    public void setIconResId(int iconResId) {
//        throw new UnsupportedOperationException(getClass().getName() + "#setIconResId please use setIconUrl iconResId = "+iconResId);
        mIconResId = iconResId;
    }

    /**
     * 获取图标的url
     * @return 图标的url
     * @see #getIconResId()
     */
    public final String getIconUrl() {
        return mIconUrl;
    }

    /**
     * 设置图标的url
     * @param iconUrl 图标的url
     * @see #setIconResId(int)
     */
    public final void setIconUrl(String iconUrl) {
        mIconUrl = iconUrl;
    }

    /**
     * 获取列表数据的Id
     * @return 列表数据的Id
     */
    public String getItemId() {
        return mItemId;
    }

    /**
     * 设置列表数据的Id
     * @param itemId 列表数据的Id
     */
    public final void setItemId(String itemId) {
        mItemId = itemId;
    }

    /**
     * 获取列表显示的数据
     * @return 列表显示的数据
     */
    public final String getItemValue() {
        return mItemValue;
    }

    /**
     * 设置列表显示的数据
     * @param value 列表显示的数据
     */
    public final void setItemValue(String value) {
        mItemValue = value;
    }

    public boolean isMultipled() {
        throw new UnsupportedOperationException(getClass().getName() + "#isMultipled!!!!!");
    }

    public void setMultipled(boolean multipled) {
        throw new UnsupportedOperationException(getClass().getName() + "#setMultipled!!!!! multipled ="+multipled);
    }

    /**
     * 是否被选中
     * @return
     */
    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public void addChild(ItemInfo child) {
        throw new UnsupportedOperationException(getClass().getName() + "#addChild child = "+child);
    }

    /**
     * 在原来的列表基础上添加,不进行过虑操作,最终的列表中可能有相同的对象.
     * @param childs
     */
    public void addAllChild(List<ItemInfo> childs) {
        throw new UnsupportedOperationException(getClass().getName() + "#addAllChild Child size = "+childs.size());
    }

    /**
     * 只获取{@link StaffInfo}的列表
     * @return
     */
    public List<ItemInfo> getChilds() {
        throw new UnsupportedOperationException(getClass().getName() + "#getChilds !!!!!");
    }

    /**
     * 获取全部的列表
     * @return
     */
    public List<ItemInfo> getAllChilds() {
        throw new UnsupportedOperationException(getClass().getName() + "#getAllChilds !!!!!");
    }

    /**
     * 根据是否清空原列表的数据,进行不同的处理.
     * <p>不清空列表,在原来的列表基础上添加,进行过虑操作,最终的列表中没有相同的对象.</p>
     *
     * @param childs
     * @param clear 是否清空原来的列表
     */
    public void addAllChild(List<ItemInfo> childs, boolean clear) {
        throw new UnsupportedOperationException(getClass().getName() + "#addAllChild Child size = "+childs.size() + " clear = "+clear);
    }

    public Iterator<ItemInfo> iterator() {
        throw new UnsupportedOperationException(getClass().getName() + "#iterator !!!!!");
    }

    public String getUserId() {
        throw new UnsupportedOperationException(getClass().getName() + "#getUserId !!!!!");
    }

    public void setUserId(String userId) {
        throw new UnsupportedOperationException(getClass().getName() + "#setUserId !!!!! userId = "+userId);
    }

    public String getFilePath() {
        throw new UnsupportedOperationException(getClass().getName() + "#getFilePath !!!!!");
    }

    public void setFilePath(String filePath) {
        throw new UnsupportedOperationException(getClass().getName() + "#setFilePath !!!!! userId = "+filePath);
    }
}
