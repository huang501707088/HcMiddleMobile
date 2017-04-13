package com.android.hcframe.netdisc.netdisccls;

/**
 * Created by pc on 2016/6/27.
 */
public class ImplDownFragItem {
    public int implDownFileId;
    private String implDownFileDownM;
    private String implFileMS;
    private String implDownFileM;
    private String implDownImg;
    private String implFileName;
    /**
     * 下载状态:正常,正在下载，暂停，等待，已下载
     * */
    public int implFileState;

    public ImplDownFragItem() {
    }

    public int getImplDownFileId() {
        return implDownFileId;
    }

    public void setImplDownFileId(int implDownFileId) {
        this.implDownFileId = implDownFileId;
    }

    public String getImplDownFileDownM() {
        return implDownFileDownM;
    }

    public void setImplDownFileDownM(String implDownFileDownM) {
        this.implDownFileDownM = implDownFileDownM;
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

    public String getImplDownImg() {
        return implDownImg;
    }

    public void setImplDownImg(String implDownImg) {
        this.implDownImg = implDownImg;
    }

    public String getImplFileName() {
        return implFileName;
    }

    public void setImplFileName(String implFileName) {
        this.implFileName = implFileName;
    }

    public String getImplDownFileM() {
        return implDownFileM;
    }

    public void setImplDownFileM(String implDownFileM) {
        this.implDownFileM = implDownFileM;
    }
}
