package net.rebeyond.behinder.core;

import java.sql.Timestamp;

public class ShellEntity {
    private Timestamp accesstime;
    private Timestamp addtime;
    private int id;
    private String ip;
    private String memo;
    private String os;
    private String password;
    private String type;
    private Timestamp updatetime;
    private String url;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url2) {
        this.url = url2;
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip2) {
        this.ip = ip2;
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

    public String getOs() {
        return this.os;
    }

    public void setOs(String os2) {
        this.os = os2;
    }

    public String getMemo() {
        return this.memo;
    }

    public void setMemo(String memo2) {
        this.memo = memo2;
    }

    public Timestamp getAddtime() {
        return this.addtime;
    }

    public void setAddtime(Timestamp addtime2) {
        this.addtime = addtime2;
    }

    public Timestamp getUpdatetime() {
        return this.updatetime;
    }

    public void setUpdatetime(Timestamp updatetime2) {
        this.updatetime = updatetime2;
    }

    public Timestamp getAccesstime() {
        return this.accesstime;
    }

    public void setAccesstime(Timestamp accesstime2) {
        this.accesstime = accesstime2;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id2) {
        this.id = id2;
    }
}
