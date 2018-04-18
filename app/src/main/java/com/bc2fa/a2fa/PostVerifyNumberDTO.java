
package com.bc2fa.a2fa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostVerifyNumberDTO {

    @SerializedName("phone_number")
    @Expose
    private String phoneNumber;
    @SerializedName("push_token")
    @Expose
    private String pushToken;
    @SerializedName("code")
    @Expose
    private Integer code;

    PostVerifyNumberDTO(String _phoneNumber, String _pushToken, Integer _code){
        phoneNumber = _phoneNumber;
        pushToken = _pushToken;
        code = _code;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
