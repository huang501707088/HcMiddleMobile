package com.android.hcframe.netdisc.netdisccls;

/**
 * Created by pc on 2016/6/27.
 */
public class ImplFragItem {
    public int implFileId;
    private String  implImg;//该行删除***
    private String implFileName;//该行删除***
    private String implFileM;
    private String implFileDownM;
    private String implFileMS;
    /**
     * 下载状态:正常,正在下载，暂停，等待，已下载
     * */
    public int implFileState;
    public ImplFragItem() {
        super();
    }

    public int getImplFileId() {
        return implFileId;
    }

    public void setImplFileId(int implFileId) {
        this.implFileId = implFileId;
    }

    public String getImplImg() {
        return implImg;
    }

    public void setImplImg(String implImg) {
        this.implImg = implImg;
    }

    public String getImplFileName() {
        return implFileName;
    }

    public void setImplFileName(String implFileName) {
        this.implFileName = implFileName;
    }

    public String getImplFileM() {
        return implFileM;
    }

    public void setImplFileM(String implFileM) {
        this.implFileM = implFileM;
    }

    public String getImplFileDownM() {
        return implFileDownM;
    }

    public void setImplFileDownM(String implFileDownM) {
        this.implFileDownM = implFileDownM;
    }

    public String getImplFileMS() {
        return implFileMS;
    }

    public void setImplFileMS(String implFileMS) {
        this.implFileMS = implFileMS;
    }

    public int getImplFileState() {
        return implFileState;
    }

    public void setImplFileState(int implFileState) {
        this.implFileState = implFileState;
    }
}
