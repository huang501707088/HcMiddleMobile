package com.android.hcmail;

import java.io.StringReader;

/**
 * Created by zhujiabin on 2017/3/16.
 */

public class HcmailSendbox extends HcmailBox {

    private String mSendFlag;

    public HcmailSendbox() {
        super();
    }

    public String getmSendFlag() {
        return mSendFlag;
    }

    public void setmSendFlag(String mSendFlag) {
        this.mSendFlag = mSendFlag;
    }
}
