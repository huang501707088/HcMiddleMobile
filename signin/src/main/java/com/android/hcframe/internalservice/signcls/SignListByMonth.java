package com.android.hcframe.internalservice.signcls;

/**
 * Created by pc on 2016/5/26.
 */
public class SignListByMonth {
    private String signDate;
    private String signStatus;
    private String userId;

    public SignListByMonth() {
    }

    public String getSignDate() {
        return signDate;
    }

    public void setSignDate(String signDate) {
        this.signDate = signDate;
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
