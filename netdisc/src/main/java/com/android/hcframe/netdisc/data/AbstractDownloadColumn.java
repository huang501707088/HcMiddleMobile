/*
* @Company 浙 江 鸿 程 计 算 机 系 统 有 限 公 司
* @URL http://www.zjhcsoft.com
* @Address 杭州滨江区伟业路1号
* @Email jinjr@zjhcsoft.com 
* @author jinjr
* @data 2015-8-27 上午11:53:21
*/
package com.android.hcframe.netdisc.data;

public abstract class AbstractDownloadColumn {

    /**
     * 文件ID
     */
    private String fileid;

    /**
     * 文件名
     */
    private String name;

    /**
     * 文件路径
     */
    private String path;

    /**
     * 文件当前级数ID
     */
    private String updirid;
    /**
     * 文件类型
     */
    private String ext;
    /**
     * 文件读取的位置
     */
    private long position;
    /**
     * 上传状态，（0：下载，1，暂停，2等待）
     */
    private String state;
    /**
     * 文件大小
     */
    private String filesize;
    /**
     * 下载速度
     */
    private int speed;

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getFileid() {
        return fileid;
    }

    public void setFileid(String fileid) {
        this.fileid = fileid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getUpdirid() {
        return updirid;
    }

    public void setUpdirid(String updirid) {
        this.updirid = updirid;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public long getPosition() {
        return position;
    }

    public void setPosition(long position) {
        this.position = position;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getFilesize() {
        return filesize;
    }

    public void setFilesize(String filesize) {
        this.filesize = filesize;
    }
}
