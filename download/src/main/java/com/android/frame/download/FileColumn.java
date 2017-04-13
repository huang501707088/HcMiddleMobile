package com.android.frame.download;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class FileColumn implements Serializable {

    /**
     * 文件ID
     */
    private String fileid;
    /**
     * 文件秒传标示
     */
    private String md5;

    /**
     * 文件本地路径
     */
    private String path;
    /**
     * 文件网络路径
     */
    private String url;

    /**
     * 分片总数
     */
    private int all_slice;
    /**
     * 传输的分片数
     */
    private int slice;
    /**
     * 文件名
     */
    private String name;
    /**
     * 文件类型
     */
    private String ext;
    /**
     * 文件是否分片(0:否，1：是)
     */
    private String type;
    /**
     * 文件读取的位置
     */
    private long position;
    /**
     * 上传状态，（0：正在上传，1：暂停，2等待，3失败） 下载（0：下载，1，暂停，2等待,3失败）
     */
    private String state;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 文件最后修改时间
     */
    private String fileTime;


    /**
     * 文件当前级数ID
     */
    private String updirid;
    /**
     * 下载速度
     */
    private int speed;
    /**
     * 上传还是下载（0：上传，1：下载）
     */
    private int upOrDown;
    /**
     * 优先级（1：最低，没有其他传输文件才会传输。2：正常情况下最低。3：优先开始。4：最高优先级）
     */
    private int level;
    /**
     * 传输是否成功（0，失败，1，成功）
     */
    private int success;

    /**
     * 文件来源（0：其他，1：网盘）
     */
    private int source;

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getUpOrDown() {
        return upOrDown;
    }

    public void setUpOrDown(int upOrDown) {
        this.upOrDown = upOrDown;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public String getUpdirid() {
        return updirid;
    }

    public void setUpdirid(String updirid) {
        this.updirid = updirid;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getFileTime() {
        return fileTime;
    }

    public void setFileTime(String fileTime) {
        this.fileTime = fileTime;
    }

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public int getAll_slice() {
        return all_slice;
    }

    public void setAll_slice(int all_slice) {
        this.all_slice = all_slice;
    }

    public int getSlice() {
        return slice;
    }

    public void setSlice(int slice) {
        this.slice = slice;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    private String tag;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    private int edit;

    public int getEdit() {
        return edit;
    }

    public void setEdit(int edit) {
        this.edit = edit;
    }

}