/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-27 上午11:53:21
*/
package com.android.hcframe.netdisc.data;

public abstract class AbstractUploadColumn {

    /**
     * 文件唯一标示
     */
    private String filekey;

    /**
     * 文件秒传标示
     */
    private String md5;

    /**
     * 文件路径
     */
    private String path;

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
     * 上传状态，（0：暂停，1：正在下载）
     */
    private int state;
    /**
     * 文件大小
     */
    private String fileSize;
    /**
     * 文件最后修改时间
     */
    private String fileTime;

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

    public int getState() {
        return state;
    }

    public void setState(int state) {
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

    public String getFilekey() {
        return filekey;
    }

    public void setFilekey(String filekey) {
        this.filekey = filekey;
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
}
