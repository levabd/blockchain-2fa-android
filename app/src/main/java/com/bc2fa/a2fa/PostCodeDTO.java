
package com.bc2fa.a2fa;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class PostCodeDTO {

    @SerializedName("event")
    @Expose
    private String event;
    @SerializedName("service")
    @Expose
    private String service;
    @SerializedName("phone_number")
    @Expose
    private String phoneNumber;
    @SerializedName("embeded")
    @Expose
    private Boolean embeded;
    @SerializedName("client_timestamp")
    @Expose
    private String clientTimestamp;
    @SerializedName("cert")
    @Expose
    private String cert;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("method")
    @Expose
    private String method;
    @SerializedName("code")
    @Expose
    private Integer code;

    PostCodeDTO(String _phoneNumber, String _event, String _service, Boolean _embeded, String _cert, Integer _code){
        phoneNumber = _phoneNumber;
        event = _event;
        service = _service;
        embeded = _embeded;
        cert = _cert;
        code = _code;
        Long tsLong = System.currentTimeMillis()/1000;
        clientTimestamp = tsLong.toString();
        method = "push";
        status = "VERIFY";
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getEmbeded() {
        return embeded;
    }

    public void setEmbeded(Boolean embeded) {
        this.embeded = embeded;
    }

    public String getClientTimestamp() {
        return clientTimestamp;
    }

    public void setClientTimestamp(String clientTimestamp) {
        this.clientTimestamp = clientTimestamp;
    }

    public String getCert() {
        return cert;
    }

    public void setCert(String cert) {
        this.cert = cert;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

}
