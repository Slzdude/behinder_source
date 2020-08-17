package net.rebeyond.behinder.entity;

import java.sql.Timestamp;

public class ShellEntity_bak {
    private Timestamp accessTime;
    private Timestamp addTime;
    private String catagory;
    private String comment;
    private String headers;
    private String id;
    private String ip;
    private String memo;
    private String os;
    private String password;
    private String type;
    private Timestamp updateTime;
    private String url;

    public String getId() {
        return this.id;
    }

    public void setId(String id2) {
        this.id = id2;
    }

    public String getCatagory() {
        return this.catagory;
    }

    public void setCatagory(String catagory2) {
        this.catagory = catagory2;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password2) {
        this.password = password2;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String type2) {
        this.type = type2;
    }

    public String getHeaders() {
        return this.headers;
    }

    public void setHeaders(String headers2) {
        this.headers = headers2;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(String os2) {
        this.os = os2;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment2) {
        this.comment = comment2;
    }

    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo2) {
        this.memo = memo2;
    }

    public Timestamp getAddTime() {
        return this.addTime;
    }

    public void setAddTime(Timestamp addTime2) {
        this.addTime = addTime2;
    }

    public Timestamp getUpdateTime() {
        return this.updateTime;
    }

    public void setUpdateTime(Timestamp updateTime2) {
        this.updateTime = updateTime2;
    }

    public Timestamp getAccessTime() {
        return this.accessTime;
    }

    public void setAccessTime(Timestamp accessTime2) {
        this.accessTime = accessTime2;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip2) {
        this.ip = ip2;
    }
}
