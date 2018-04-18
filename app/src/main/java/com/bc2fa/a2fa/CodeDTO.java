
package com.bc2fa.a2fa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class CodeDTO {

    @SerializedName("event")
    @Expose
    private String event;
    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("embeded")
    @Expose
    private Boolean embeded;
    @SerializedName("cert")
    @Expose
    private String cert;
    @SerializedName("service")
    @Expose
    private String service;

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public Boolean getEmbeded() {
        return embeded;
    }

    public void setEmbeded(Boolean embeded) {
        this.embeded = embeded;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

}
